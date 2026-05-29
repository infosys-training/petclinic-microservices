import logging
import re

from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.prompts.tab_prompts import SYSTEM_PROMPT_TEMPLATE, TAB_PROMPTS
from app.services.ai.embeddings import similarity_search

logger = logging.getLogger(__name__)


def _extract_confidence_score(text: str) -> float | None:
    match = re.search(r"CONFIDENCE_SCORE:\s*(\d+(?:\.\d+)?)", text)
    if match:
        return min(float(match.group(1)), 100.0)
    return None


def _extract_citations(text: str) -> str:
    refs = re.findall(r"\[RFP-REF:[^\]]+\]", text)
    return "; ".join(refs) if refs else ""


async def generate_section(
    db: AsyncSession,
    project_id: str,
    tab_key: str,
    extracted_text: str,
    industry: str = "Not specified",
    scale: str = "Not specified",
    additional_context: str = "",
) -> dict:
    from langchain_openai import ChatOpenAI

    tab_config = TAB_PROMPTS.get(tab_key)
    if not tab_config:
        raise ValueError(f"Unknown tab key: {tab_key}")

    rag_context = ""
    try:
        search_query = f"{tab_config['section_domain']} requirements specifications"
        results = await similarity_search(db, search_query, project_id, top_k=8)
        if results:
            rag_parts = []
            for r in results:
                source = f"[Source: {r['filename']}"
                if r["page_number"]:
                    source += f", Page {r['page_number']}"
                source += f", Relevance: {r['similarity']:.2f}]"
                rag_parts.append(f"{source}\n{r['content']}")
            rag_context = "\n\n---\n\n".join(rag_parts)
    except Exception as e:
        logger.warning(f"RAG search failed for {tab_key}: {e}")
        rag_context = "No additional context available from vector store."

    prompt = SYSTEM_PROMPT_TEMPLATE.format(
        section_domain=tab_config["section_domain"],
        extracted_rfp_content=extracted_text[:8000] if extracted_text else "No RFP content available.",
        rag_retrieved_context=rag_context or "No additional context available.",
        section_name=tab_key.replace("_", " ").title(),
        industry=industry,
        scale=scale,
        additional_context=additional_context,
        section_specific_format=tab_config["section_specific_format"],
    )

    llm = ChatOpenAI(
        model=settings.OPENAI_MODEL,
        openai_api_key=settings.OPENAI_API_KEY,
        temperature=0.3,
        max_tokens=4000,
    )

    response = await llm.ainvoke(prompt)
    content = response.content

    confidence = _extract_confidence_score(content)
    citations = _extract_citations(content)

    clean_content = re.sub(r"\nCONFIDENCE_SCORE:\s*\d+(?:\.\d+)?", "", content).strip()

    return {
        "content": clean_content,
        "confidence_score": confidence,
        "citations": citations,
    }
