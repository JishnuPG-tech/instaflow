import logging
from typing import List
from backend.app.models.media import ContentType, MediaItem, MediaItemType
from backend.app.models.capabilities import MediaCapabilities

logger = logging.getLogger("CapabilityService")

class CapabilityService:
    @classmethod
    def resolve_capabilities(
        cls,
        content_type: ContentType,
        items: List[MediaItem],
        audio_available: bool
    ) -> MediaCapabilities:
        has_images = any(it.type == MediaItemType.IMAGE for it in items)
        has_videos = any(it.type == MediaItemType.VIDEO for it in items) or content_type in [ContentType.REEL, ContentType.VIDEO_POST, ContentType.STORY_VIDEO]
        
        can_download_image = has_images or content_type in [ContentType.IMAGE_POST, ContentType.PROFILE_PHOTO, ContentType.STORY_IMAGE]
        can_download_video = has_videos
        can_extract_audio = audio_available or has_videos
        can_download_audio = can_extract_audio
        
        can_mux_audio_image = can_download_image and can_extract_audio
        can_mux_audio_video = can_download_video and can_extract_audio
        
        item_count = len(items)
        has_multiple = item_count > 1 or content_type == ContentType.CAROUSEL
        
        return MediaCapabilities(
            canDownloadImage=can_download_image,
            canDownloadVideo=can_download_video,
            canDownloadAudio=can_download_audio,
            canExtractAudio=can_extract_audio,
            canMuxAudioWithImage=can_mux_audio_image,
            canMuxAudioWithVideo=can_mux_audio_video,
            canDownloadCarousel=has_multiple,
            canSelectItems=has_multiple,
            hasMultipleItems=has_multiple
        )
