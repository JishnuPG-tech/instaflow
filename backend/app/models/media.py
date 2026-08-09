from enum import Enum
from typing import Optional, List, Dict, Any
from pydantic import BaseModel

class DownloadIntent(str, Enum):
    AUDIO = "AUDIO"
    VIDEO = "VIDEO"
    POST = "POST"
    COMMAND = "COMMAND"

class MediaType(str, Enum):
    IMAGE = "MEDIA_IMAGE"
    VIDEO = "MEDIA_VIDEO"
    CAROUSEL = "MEDIA_CAROUSEL"
    AUDIO_SOURCE = "MEDIA_AUDIO_SOURCE"

class MediaItemClassification(BaseModel):
    index: int
    media_type: MediaType
    url: Optional[str] = None
    thumbnail: Optional[str] = None
    duration: float = 0.0
    vcodec: Optional[str] = None
    acodec: Optional[str] = None
    extra_info: Optional[Dict[str, Any]] = None

class MediaClassificationResult(BaseModel):
    shortcode: str
    primary_type: MediaType
    title: str
    author: str
    thumbnail: Optional[str] = None
    items: List[MediaItemClassification]
    raw_metadata: Optional[Dict[str, Any]] = None
