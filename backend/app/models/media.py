from enum import Enum
from typing import Optional, List, Dict, Any
from pydantic import BaseModel
from backend.app.models.capabilities import MediaCapabilities

class DownloadIntent(str, Enum):
    AUDIO = "AUDIO"
    VIDEO = "VIDEO"
    POST = "POST"
    COMMAND = "COMMAND"

class ContentType(str, Enum):
    REEL = "REEL"
    VIDEO_POST = "VIDEO_POST"
    IMAGE_POST = "IMAGE_POST"
    CAROUSEL = "CAROUSEL"
    STORY_IMAGE = "STORY_IMAGE"
    STORY_VIDEO = "STORY_VIDEO"
    PROFILE_PHOTO = "PROFILE_PHOTO"
    OTHER = "OTHER"

class MediaItemType(str, Enum):
    IMAGE = "IMAGE"
    VIDEO = "VIDEO"

class MediaItem(BaseModel):
    itemId: str
    index: int
    type: MediaItemType
    thumbnail: Optional[str] = None
    width: Optional[int] = None
    height: Optional[int] = None
    duration: float = 0.0
    hasVideo: bool = False
    hasAudio: bool = False
    imageUrl: Optional[str] = None
    videoFormats: List[Dict[str, Any]] = []
    audioFormats: List[Dict[str, Any]] = []

class MediaResult(BaseModel):
    contentType: ContentType
    sourceUrl: str
    canonicalUrl: Optional[str] = None
    id: str
    title: str
    description: Optional[str] = None
    author: str
    authorUsername: Optional[str] = None
    authorAvatar: Optional[str] = None
    thumbnail: Optional[str] = None
    duration: float = 0.0
    itemCount: int = 1
    mediaItems: List[MediaItem] = []
    audioAvailable: bool = False
    capabilities: MediaCapabilities
    metadata: Optional[Dict[str, Any]] = None
