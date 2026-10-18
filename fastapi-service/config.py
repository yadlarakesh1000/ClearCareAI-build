"""Centralized configuration and logging for the FastAPI service."""
import logging
from pydantic import Field,SecretStr
from pydantic_settings import BaseSettings,SettingsConfigDict

class Settings(BaseSettings):
  """Load and validate the service's environment-based configuration"""
  gemini_api_key: SecretStr = SecretStr("YOUR_SENTINEL_FROM_SECTION_1_3")
  internal_api_secret: SecretStr = SecretStr("")
  omnidim_webhook_secret: SecretStr = SecretStr("")
  # Keep this configurable because Google can retire model names.
  gemini_model: str = "gemini-2.0-flash"
  spring_boot_url: str = "http://localhost:8080"
  gemini_timeout_seconds: float = Field(default=30.0, gt=0)
  spring_timeout_seconds: float = Field(default=10.0, gt=0)
  webhook_timestamp_tolerance_seconds: int = Field(default=300, gt=0)
  model_config = SettingsConfigDict(
    env_file=".env",
     extra="ignore",
  )
settings = Settings()
logging.basicConfig(
  
  level=logging.INFO,
  format="%(asctime)s %(levelname)s %(name)s %(message)s",
  )
logger = logging.getLogger("fastapi-service")