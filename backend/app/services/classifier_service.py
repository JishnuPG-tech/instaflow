import logging
from typing import Dict, Any, List
from backend.app.models.media import (
    ContentType,
    MediaItemType,
    MediaItem,
    MediaResult
)
from backend.app.services.capability_service import CapabilityService

logger = logging.getLogger("ClassifierService")

class ClassifierService:
    @classmethod
    def classify_url(cls, url: str, metadata: Dict[str, Any]) -> MediaResult:
        media_id = str(metadata.get("id") or "media")
        title = metadata.get("title") or "Instagram Post"
        description = metadata.get("description") or metadata.get("title")
        author = metadata.get("uploader") or metadata.get("channel") or "Instagram User"
        author_username = metadata.get("uploader_id") or metadata.get("uploader")
        author_avatar = metadata.get("uploader_avatar")
        thumb = metadata.get("thumbnail") or metadata.get("url")
        duration = float(metadata.get("duration") or 0.0)
        
        entries = metadata.get("entries") or metadata.get("items") or []
        
        # Scenario 1: Multi-item Carousel
        if len(entries) > 1:
            media_items: List[MediaItem] = []
            audio_found = False
            for idx, entry in enumerate(entries, start=1):
                vcodec = entry.get("vcodec")
                is_vid = (vcodec and vcodec != "none") or float(entry.get("duration") or 0.0) > 0.0 or entry.get("ext") == "mp4"
                if is_vid or entry.get("acodec") != "none":
                    audio_found = True
                    
                item_type = MediaItemType.VIDEO if is_vid else MediaItemType.IMAGE
                media_items.append(MediaItem(
                    itemId=str(entry.get("id") or f"{media_id}_{idx}"),
                    index=idx,
                    type=item_type,
                    thumbnail=entry.get("thumbnail") or entry.get("url"),
                    width=entry.get("width"),
                    height=entry.get("height"),
                    duration=float(entry.get("duration") or 0.0),
                    hasVideo=is_vid,
                    hasAudio=bool(entry.get("acodec") and entry.get("acodec") != "none"),
                    imageUrl=entry.get("url") if not is_vid else None
                ))
                
            content_type = ContentType.CAROUSEL
            capabilities = CapabilityService.resolve_capabilities(content_type, media_items, audio_found)
            return MediaResult(
                contentType=content_type,
                sourceUrl=url,
                canonicalUrl=metadata.get("webpage_url") or url,
                id=media_id,
                title=title,
                description=description,
                author=author,
                authorUsername=author_username,
                authorAvatar=author_avatar,
                thumbnail=thumb,
                duration=duration,
                itemCount=len(media_items),
                mediaItems=media_items,
                audioAvailable=audio_found,
                capabilities=capabilities,
                metadata=metadata
            )

        # Scenario 2: Single Media (Reel, Video Post, Image Post, Story, Profile)
        vcodec = metadata.get("vcodec")
        is_video = metadata.get("is_video", False) or (vcodec and vcodec != "none") or duration > 0.0 or metadata.get("ext") == "mp4"
        acodec = metadata.get("acodec")
        has_audio = bool(acodec and acodec != "none") or is_video
        
        # Determine exact ContentType from URL structure & metadata
        if "/reel/" in url or "/reels/" in url:
            content_type = ContentType.REEL
        elif "/stories/" in url:
            content_type = ContentType.STORY_VIDEO if is_video else ContentType.STORY_IMAGE
        elif "/p/" in url:
            content_type = ContentType.VIDEO_POST if is_video else ContentType.IMAGE_POST
        elif is_video:
            content_type = ContentType.VIDEO_POST
        else:
            content_type = ContentType.IMAGE_POST

        single_item = MediaItem(
            itemId=media_id,
            index=1,
            type=MediaItemType.VIDEO if is_video else MediaItemType.IMAGE,
            thumbnail=thumb,
            width=metadata.get("width"),
            height=metadata.get("height"),
            duration=duration,
            hasVideo=is_video,
            hasAudio=has_audio,
            imageUrl=metadata.get("url") if not is_video else None
        )
        
        capabilities = CapabilityService.resolve_capabilities(content_type, [single_item], has_audio)
        
        return MediaResult(
            contentType=content_type,
            sourceUrl=url,
            canonicalUrl=metadata.get("webpage_url") or url,
            id=media_id,
            title=title,
            description=description,
            author=author,
            authorUsername=author_username,
            authorAvatar=author_avatar,
            thumbnail=thumb,
            duration=duration,
            itemCount=1,
            mediaItems=[single_item],
            audioAvailable=has_audio,
            capabilities=capabilities,
            metadata=metadata
        )
