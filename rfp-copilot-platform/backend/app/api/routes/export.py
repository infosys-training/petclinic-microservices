import uuid

from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import Response
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.models.models import Project, Section
from app.schemas.schemas import ExportRequest
from app.services.export.exporter import export_to_docx, export_to_pdf

router = APIRouter(prefix="/projects/{project_id}/export", tags=["export"])


@router.post("/")
async def export_project(
    project_id: uuid.UUID,
    data: ExportRequest,
    db: AsyncSession = Depends(get_db),
):
    project_result = await db.execute(select(Project).where(Project.id == project_id))
    project = project_result.scalar_one_or_none()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    query = select(Section).where(Section.project_id == project_id)
    if data.tab_keys:
        query = query.where(Section.tab_key.in_(data.tab_keys))
    query = query.order_by(Section.tab_index)

    result = await db.execute(query)
    sections = result.scalars().all()

    section_dicts = [
        {
            "tab_index": s.tab_index,
            "title": s.title,
            "content": s.content or "Content not yet generated.",
            "confidence_score": s.confidence_score,
        }
        for s in sections
    ]

    if data.format == "pdf":
        file_bytes = export_to_pdf(section_dicts, project.name)
        return Response(
            content=file_bytes,
            media_type="application/pdf",
            headers={
                "Content-Disposition": f'attachment; filename="{project.name}_RFP_Response.pdf"'
            },
        )
    else:
        file_bytes = export_to_docx(section_dicts, project.name)
        return Response(
            content=file_bytes,
            media_type="application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            headers={
                "Content-Disposition": f'attachment; filename="{project.name}_RFP_Response.docx"'
            },
        )
