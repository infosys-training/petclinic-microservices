import logging

from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings

logger = logging.getLogger(__name__)


async def create_embeddings(texts: list[str]) -> list[list[float]]:
    from langchain_openai import OpenAIEmbeddings

    embeddings_model = OpenAIEmbeddings(
        model=settings.OPENAI_EMBEDDING_MODEL,
        openai_api_key=settings.OPENAI_API_KEY,
    )
    return await embeddings_model.aembed_documents(texts)


async def similarity_search(
    db: AsyncSession, query: str, project_id: str, top_k: int = 10
) -> list[dict]:
    from langchain_openai import OpenAIEmbeddings

    embeddings_model = OpenAIEmbeddings(
        model=settings.OPENAI_EMBEDDING_MODEL,
        openai_api_key=settings.OPENAI_API_KEY,
    )
    query_embedding = await embeddings_model.aembed_query(query)

    sql = text("""
        SELECT dc.id, dc.content, dc.page_number, d.filename,
               1 - (dc.embedding <=> :embedding::vector) as similarity
        FROM document_chunks dc
        JOIN documents d ON dc.document_id = d.id
        WHERE d.project_id = :project_id
          AND dc.embedding IS NOT NULL
        ORDER BY dc.embedding <=> :embedding::vector
        LIMIT :top_k
    """)

    result = await db.execute(
        sql,
        {
            "embedding": str(query_embedding),
            "project_id": project_id,
            "top_k": top_k,
        },
    )
    rows = result.fetchall()
    return [
        {
            "id": str(row[0]),
            "content": row[1],
            "page_number": row[2],
            "filename": row[3],
            "similarity": float(row[4]),
        }
        for row in rows
    ]
