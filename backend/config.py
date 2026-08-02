from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    app_name: str = "InstaSave Backend API"
    version: str = "1.0.0"
    yt_dlp_version: str = "2025.12.08"
    debug: bool = False
    rate_limit_per_15min: int = 30


settings = Settings()
