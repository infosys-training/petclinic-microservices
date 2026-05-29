from datetime import datetime
from uuid import UUID

from pydantic import BaseModel


class ProjectCreate(BaseModel):
    name: str
    description: str | None = None
    client_name: str | None = None
    industry: str | None = None
    submission_deadline: datetime | None = None
    additional_context: str | None = None


class ProjectUpdate(BaseModel):
    name: str | None = None
    description: str | None = None
    client_name: str | None = None
    industry: str | None = None
    submission_deadline: datetime | None = None
    additional_context: str | None = None
    status: str | None = None


class ProjectResponse(BaseModel):
    id: UUID
    name: str
    description: str | None
    client_name: str | None
    industry: str | None
    submission_deadline: datetime | None
    status: str
    additional_context: str | None
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class DocumentResponse(BaseModel):
    id: UUID
    project_id: UUID
    filename: str
    file_type: str
    file_size: int | None
    doc_type: str
    created_at: datetime

    model_config = {"from_attributes": True}


class SectionResponse(BaseModel):
    id: UUID
    project_id: UUID
    tab_key: str
    tab_index: int
    title: str
    content: str | None
    confidence_score: float | None
    citations: str | None
    version: int
    status: str
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class SectionUpdate(BaseModel):
    content: str | None = None
    status: str | None = None


class GenerateRequest(BaseModel):
    tab_keys: list[str] | None = None
    additional_context: str | None = None


class GenerateResponse(BaseModel):
    message: str
    sections: list[SectionResponse]


class ExportRequest(BaseModel):
    format: str = "docx"
    tab_keys: list[str] | None = None


class MetadataExtraction(BaseModel):
    client_name: str | None = None
    industry: str | None = None
    submission_deadline: str | None = None
    evaluation_criteria: list[str] = []
    mandatory_requirements: list[str] = []
    compliance_needs: list[str] = []
    key_technologies: list[str] = []
    project_scope: str | None = None
