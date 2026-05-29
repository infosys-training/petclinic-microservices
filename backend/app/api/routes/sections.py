import logging
import uuid

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.models.models import Document, Project, Section
from app.prompts.tab_prompts import TAB_DEFINITIONS
from app.schemas.schemas import (
    GenerateRequest,
    GenerateResponse,
    SectionResponse,
    SectionUpdate,
)
from app.services.ai.generator import generate_section

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/projects/{project_id}/sections", tags=["sections"])


@router.get("/", response_model=list[SectionResponse])
async def list_sections(
    project_id: uuid.UUID, db: AsyncSession = Depends(get_db)
):
    result = await db.execute(
        select(Section)
        .where(Section.project_id == project_id)
        .order_by(Section.tab_index)
    )
    return result.scalars().all()


@router.get("/{section_id}", response_model=SectionResponse)
async def get_section(
    project_id: uuid.UUID,
    section_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Section).where(
            Section.id == section_id, Section.project_id == project_id
        )
    )
    section = result.scalar_one_or_none()
    if not section:
        raise HTTPException(status_code=404, detail="Section not found")
    return section


@router.patch("/{section_id}", response_model=SectionResponse)
async def update_section(
    project_id: uuid.UUID,
    section_id: uuid.UUID,
    data: SectionUpdate,
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Section).where(
            Section.id == section_id, Section.project_id == project_id
        )
    )
    section = result.scalar_one_or_none()
    if not section:
        raise HTTPException(status_code=404, detail="Section not found")

    update_data = data.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(section, key, value)

    if "content" in update_data:
        section.status = "edited"
        section.version += 1

    await db.flush()
    await db.refresh(section)
    return section


@router.post("/generate", response_model=GenerateResponse)
async def generate_sections(
    project_id: uuid.UUID,
    data: GenerateRequest,
    db: AsyncSession = Depends(get_db),
):
    project_result = await db.execute(select(Project).where(Project.id == project_id))
    project = project_result.scalar_one_or_none()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    docs_result = await db.execute(
        select(Document).where(Document.project_id == project_id)
    )
    documents = docs_result.scalars().all()
    extracted_text = "\n\n---\n\n".join(
        doc.extracted_text for doc in documents if doc.extracted_text
    )

    if not extracted_text:
        raise HTTPException(
            status_code=400,
            detail="No documents with extracted text found. Please upload documents first.",
        )

    tab_keys = data.tab_keys
    if not tab_keys:
        tab_keys = [td["key"] for td in TAB_DEFINITIONS]

    sections_result = await db.execute(
        select(Section).where(
            Section.project_id == project_id, Section.tab_key.in_(tab_keys)
        )
    )
    sections = {s.tab_key: s for s in sections_result.scalars().all()}

    generated = []
    for tab_key in tab_keys:
        section = sections.get(tab_key)
        if not section:
            continue

        section.status = "generating"
        await db.flush()

        try:
            result = await generate_section(
                db=db,
                project_id=str(project_id),
                tab_key=tab_key,
                extracted_text=extracted_text,
                industry=project.industry or "Not specified",
                scale="Enterprise",
                additional_context=data.additional_context or project.additional_context or "",
            )

            section.content = result["content"]
            section.confidence_score = result["confidence_score"]
            section.citations = result["citations"]
            section.status = "generated"
            section.version += 1

        except Exception as e:
            logger.error(f"Failed to generate section {tab_key}: {e}")
            section.status = "pending"
            section.content = f"Generation failed: {str(e)}"

        await db.flush()
        await db.refresh(section)
        generated.append(section)

    return GenerateResponse(
        message=f"Generated {len(generated)} sections successfully",
        sections=generated,
    )


@router.post("/{tab_key}/regenerate", response_model=SectionResponse)
async def regenerate_section(
    project_id: uuid.UUID,
    tab_key: str,
    additional_context: str = "",
    db: AsyncSession = Depends(get_db),
):
    project_result = await db.execute(select(Project).where(Project.id == project_id))
    project = project_result.scalar_one_or_none()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    section_result = await db.execute(
        select(Section).where(
            Section.project_id == project_id, Section.tab_key == tab_key
        )
    )
    section = section_result.scalar_one_or_none()
    if not section:
        raise HTTPException(status_code=404, detail="Section not found")

    docs_result = await db.execute(
        select(Document).where(Document.project_id == project_id)
    )
    documents = docs_result.scalars().all()
    extracted_text = "\n\n---\n\n".join(
        doc.extracted_text for doc in documents if doc.extracted_text
    )

    section.status = "generating"
    await db.flush()

    try:
        result = await generate_section(
            db=db,
            project_id=str(project_id),
            tab_key=tab_key,
            extracted_text=extracted_text,
            industry=project.industry or "Not specified",
            scale="Enterprise",
            additional_context=additional_context or project.additional_context or "",
        )

        section.content = result["content"]
        section.confidence_score = result["confidence_score"]
        section.citations = result["citations"]
        section.status = "generated"
        section.version += 1

    except Exception as e:
        logger.error(f"Failed to regenerate section {tab_key}: {e}")
        section.status = "pending"

    await db.flush()
    await db.refresh(section)
    return section
