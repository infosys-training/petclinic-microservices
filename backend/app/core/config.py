from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    APP_NAME: str = "RFP Copilot Platform"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = False

    # Database
    DATABASE_URL: str = "postgresql+asyncpg://rfp_user:rfp_pass@localhost:5432/rfp_copilot"
    DATABASE_URL_SYNC: str = "postgresql://rfp_user:rfp_pass@localhost:5432/rfp_copilot"

    # OpenAI
    OPENAI_API_KEY: str = ""
    OPENAI_MODEL: str = "gpt-4"
    OPENAI_EMBEDDING_MODEL: str = "text-embedding-3-small"

    # File storage
    UPLOAD_DIR: str = "uploads"
    MAX_FILE_SIZE_MB: int = 50

    # CORS
    CORS_ORIGINS: list[str] = ["http://localhost:3000", "http://localhost:3001"]

    # JWT
    SECRET_KEY: str = "change-me-in-production-use-a-real-secret-key"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60

    # Vector store
    EMBEDDING_DIMENSION: int = 1536
    CHUNK_SIZE: int = 1000
    CHUNK_OVERLAP: int = 200

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
