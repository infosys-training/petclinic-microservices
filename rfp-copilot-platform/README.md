# RFP Copilot Platform

An AI-powered RFP/RFI Response Copilot platform that allows users to upload RFP/RFI documents (PDF, DOCX, XLSX) and automatically generates structured response sections using LLM-powered analysis.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Frontend (Next.js 14)              │
│  ┌──────┐ ┌──────────┐ ┌────────┐ ┌─────────────┐  │
│  │Upload│ │Tab Viewer│ │Editor  │ │Export (DOCX/ │  │
│  │Panel │ │& Nav     │ │(Rich)  │ │PDF)          │  │
│  └──────┘ └──────────┘ └────────┘ └─────────────┘  │
└─────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────┐
│              Backend (Python FastAPI)                 │
│  ┌──────────┐ ┌───────────┐ ┌────────────────────┐  │
│  │Doc Parser│ │RAG Engine │ │Section Generator   │  │
│  │(PDF/DOCX)│ │(LangChain)│ │(Prompt Chains)     │  │
│  └──────────┘ └───────────┘ └────────────────────┘  │
└─────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────┐
│                   Data Layer                         │
│  ┌──────────┐ ┌───────────┐ ┌────────────────────┐  │
│  │pgvector  │ │PostgreSQL │ │File Storage        │  │
│  │(Vectors) │ │(Metadata) │ │(Uploaded Docs)     │  │
│  └──────────┘ └───────────┘ └────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Next.js 14, React, TailwindCSS, Radix UI |
| Backend | Python 3.12, FastAPI, SQLAlchemy, Alembic |
| AI/ML | LangChain, OpenAI GPT-4, text-embedding-3-small |
| Vector Store | pgvector (PostgreSQL extension) |
| Database | PostgreSQL 16 |
| Document Parsing | PyPDF2, python-docx, openpyxl, Tesseract OCR |
| Export | python-docx, ReportLab |
| Containerization | Docker, Docker Compose |

## Features

- **Multi-format document upload**: PDF, DOCX, XLSX, scanned images with OCR
- **Smart extraction**: Auto-parse and chunk documents with embeddings
- **10-tab response generation**: AI generates comprehensive proposal sections
- **RAG-powered**: Retrieval-Augmented Generation over uploaded documents
- **Edit & regenerate**: Users can edit content and regenerate specific sections
- **Confidence scoring**: AI highlights sections with low confidence
- **Citation tracking**: Traceability back to source RFP paragraphs
- **Export**: Download responses as DOCX or PDF
- **Additional context**: Provide extra context for better generation

## Generated Response Tabs

1. **Current System & Challenges** - Architecture assessment, pain points, modernization levers
2. **AI-DLC Solution** - AI-driven development lifecycle approach
3. **Estimation** - Effort estimates with phase/role/module breakdowns
4. **Target Architecture** - Recommended architecture patterns and tech stack
5. **Testing Approach** - Testing strategy across pyramid levels
6. **DevOps Approach** - CI/CD, GitOps, IaC, observability
7. **DB Re-architecture** - Database modernization strategy
8. **DB Migration** - Migration methodology and estimates
9. **User Onboarding** - Rollout strategy and change management
10. **Scope & Risks** - Assumptions, risks, dependencies, constraints

## Quick Start

### Prerequisites

- Docker & Docker Compose
- OpenAI API key

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/infosys-training/rfp-copilot-platform.git
cd rfp-copilot-platform

# Set your OpenAI API key
export OPENAI_API_KEY=sk-your-api-key-here

# Start all services
docker compose up --build

# Run database migrations
docker compose exec backend alembic upgrade head
```

The application will be available at:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8000
- **API Docs**: http://localhost:8000/api/docs

### Local Development

#### Backend

```bash
cd backend

# Create virtual environment
python -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Copy environment file
cp .env.example .env
# Edit .env with your settings

# Run migrations
alembic upgrade head

# Start server
uvicorn app.main:app --reload --port 8000
```

#### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key for GPT-4 and embeddings | Required |
| `OPENAI_MODEL` | LLM model to use | `gpt-4` |
| `DATABASE_URL` | PostgreSQL connection string (async) | `postgresql+asyncpg://rfp_user:rfp_pass@localhost:5432/rfp_copilot` |
| `CORS_ORIGINS` | Allowed CORS origins | `["http://localhost:3000"]` |
| `NEXT_PUBLIC_API_URL` | Backend API URL for frontend | `http://localhost:8000/api` |

## API Endpoints

### Projects
- `POST /api/projects/` - Create project
- `GET /api/projects/` - List projects
- `GET /api/projects/{id}` - Get project
- `PATCH /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Documents
- `POST /api/projects/{id}/documents/upload` - Upload document
- `GET /api/projects/{id}/documents/` - List documents
- `DELETE /api/projects/{id}/documents/{doc_id}` - Delete document

### Sections
- `GET /api/projects/{id}/sections/` - List sections
- `PATCH /api/projects/{id}/sections/{section_id}` - Update section
- `POST /api/projects/{id}/sections/generate` - Generate all sections
- `POST /api/projects/{id}/sections/{tab_key}/regenerate` - Regenerate one section

### Export
- `POST /api/projects/{id}/export/` - Export as DOCX or PDF

## User Workflow

1. **Create Project** - Set up with client name, industry, description
2. **Upload Documents** - Upload RFP/RFI files (PDF, DOCX, XLSX)
3. **Review Extracted Content** - Documents are parsed and chunked automatically
4. **Provide Context** - Add optional context (team size, tech preferences)
5. **Generate Sections** - AI generates all 10 response tabs
6. **Review & Edit** - Edit content, regenerate specific sections
7. **Export** - Download final response as DOCX or PDF

## Project Structure

```
rfp-copilot-platform/
├── backend/
│   ├── app/
│   │   ├── api/routes/          # FastAPI route handlers
│   │   ├── core/                # Config, database setup
│   │   ├── models/              # SQLAlchemy models
│   │   ├── prompts/             # Tab-specific prompt templates
│   │   ├── schemas/             # Pydantic schemas
│   │   └── services/
│   │       ├── ai/              # Embeddings, RAG, generation
│   │       ├── document/        # Parsing (PDF, DOCX, XLSX, OCR)
│   │       └── export/          # DOCX/PDF export
│   ├── alembic/                 # Database migrations
│   ├── requirements.txt
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── app/                 # Next.js pages
│   │   ├── components/
│   │   │   ├── ui/              # Reusable UI components
│   │   │   └── project/         # Project-specific components
│   │   └── lib/                 # API client, utilities
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

## License

MIT
