from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
from config import settings
from security import validate_instagram_url
from extractor import extract_media_info

app = FastAPI(
    title=settings.app_name,
    version=settings.version,
    description="Source of truth backend for InstaSave Android application.",
)


class ResolveRequest(BaseModel):
    url: str


class MediaFormat(BaseModel):
    formatId: str
    label: str
    ext: str
    vcodec: Optional[str] = None
    acodec: Optional[str] = None
    height: Optional[int] = None
    width: Optional[int] = None
    tbr: Optional[float] = None
    filesizeBytes: Optional[int] = None


class CarouselItem(BaseModel):
    index: int
    type: str  # photo | video
    thumbnailUrl: Optional[str] = None
    formats: List[MediaFormat]


class MediaInfo(BaseModel):
    id: str
    type: str  # post | reel | carousel
    author: Optional[str] = None
    authorDisplayName: Optional[str] = None
    authorAvatarUrl: Optional[str] = None
    thumbnailUrl: Optional[str] = None
    caption: Optional[str] = None
    uploadedAt: Optional[str] = None
    durationSeconds: Optional[float] = None
    formats: Optional[List[MediaFormat]] = None
    items: Optional[List[CarouselItem]] = None


class ErrorDetail(BaseModel):
    code: str
    message: str


class ErrorResponse(BaseModel):
    error: ErrorDetail


@app.get("/health")
def get_health():
    return {"status": "ok", "ytDlpVersion": settings.yt_dlp_version}


@app.post("/api/resolve", response_model=MediaInfo)
async def resolve_media(body: ResolveRequest):
    # Enforce strict SSRF & URL validation
    validate_instagram_url(body.url)

    # Perform metadata extraction via yt-dlp
    extracted_data = await extract_media_info(body.url)
    return MediaInfo(**extracted_data)
