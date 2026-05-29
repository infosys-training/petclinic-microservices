import logging
import os
import uuid

import aiofiles
from fastapi import APIRouter, Depends, File, HTTPException, UploadFile
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.database import get_db
from app.models.models import Document, DocumentChunk, Project
from app.schemas.schemas import DocumentResponse, MetadataExtraction
from app.services.document.parser import chunk_text, parse_document

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/projects/{project_id}/documents", tags=["documents"])


@router.post("/upload", response_model=DocumentResponse, status_code=201)
async def upload_document(
    project_id: uuid.UUID,
    file: UploadFile = File(...),
    doc_type: str = "rfp",
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(select(Project).where(Project.id == project_id))
    project = result.scalar_one_or_none()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    allowed_types = {".pdf", ".docx", ".xlsx", ".xls", ".txt", ".png", ".jpg", ".jpeg", ".tiff", ".tif"}
    file_ext = os.path.splitext(file.filename or "")[1].lower()
    if file_ext not in allowed_types:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported file type: {file_ext}. Allowed: {', '.join(allowed_types)}",
        )

    upload_dir = os.path.join(settings.UPLOAD_DIR, str(project_id))
    os.makedirs(upload_dir, exist_ok=True)

    file_id = uuid.uuid4()
    file_path = os.path.join(upload_dir, f"{file_id}{file_ext}")

    content = await file.read()
    file_size = len(content)

    if file_size > settings.MAX_FILE_SIZE_MB * 1024 * 1024:
        raise HTTPException(
            status_code=400,
            detail=f"File too large. Max size: {settings.MAX_FILE_SIZE_MB}MB",
        )

    async with aiofiles.open(file_path, "wb") as f:
        await f.write(content)

    try:
        extracted_text = parse_document(file_path)
    except Exception as e:
        logger.error(f"Failed to parse document: {e}")
        extracted_text = ""

    doc = Document(
        id=file_id,
        project_id=project_id,
        filename=file.filename or "unknown",
        file_type=file_ext,
        file_path=file_path,
        file_size=file_size,
        doc_type=doc_type,
        extracted_text=extracted_text,
    )
    db.add(doc)
    await db.flush()

    if extracted_text:
        chunks = chunk_text(
            extracted_text,
            chunk_size=settings.CHUNK_SIZE,
            chunk_overlap=settings.CHUNK_OVERLAP,
        )
        for chunk_data in chunks:
            chunk = DocumentChunk(
                id=uuid.uuid4(),
                document_id=doc.id,
                chunk_index=chunk_data["index"],
                content=chunk_data["content"],
                page_number=chunk_data["page_number"],
            )
            db.add(chunk)

        try:
            from app.services.ai.embeddings import create_embeddings

            chunk_texts = [c["content"] for c in chunks]
            embeddings = await create_embeddings(chunk_texts)

            chunk_result = await db.execute(
                select(DocumentChunk)
                .where(DocumentChunk.document_id == doc.id)
                .order_by(DocumentChunk.chunk_index)
            )
            db_chunks = chunk_result.scalars().all()
            for db_chunk, embedding in zip(db_chunks, embeddings):
                db_chunk.embedding = embedding
        except Exception as e:
            logger.warning(f"Embedding creation failed (will work without RAG): {e}")

    await db.flush()
    await db.refresh(doc)
    return doc


@router.get("/", response_model=list[DocumentResponse])
async def list_documents(
    project_id: uuid.UUID, db: AsyncSession = Depends(get_db)
):
    result = await db.execute(
        select(Document).where(Document.project_id == project_id).order_by(Document.created_at)
    )
    return result.scalars().all()


@router.delete("/{document_id}", status_code=204)
async def delete_document(
    project_id: uuid.UUID,
    document_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Document).where(
            Document.id == document_id, Document.project_id == project_id
        )
    )
    doc = result.scalar_one_or_none()
    if not doc:
        raise HTTPException(status_code=404, detail="Document not found")

    if os.path.exists(doc.file_path):
        os.remove(doc.file_path)

    await db.delete(doc)
