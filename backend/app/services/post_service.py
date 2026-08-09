import os
import logging
from typing import Optional, Dict, Any
from backend.app.services.metadata_service import MetadataService
from backend.app.services.classifier_service import ClassifierService
from backend.app.services.download_service import DownloadService
from backend.app.models.media import MediaType
from backend.app.models.request import DownloadRequest

logger = logging.getLogger("PostService")

class PostService:
    @classmethod
    def process_post_download(
        cls,
        req: DownloadRequest,
        task_dir: str
    ) -> str:
        logger.info(f"PostService: Processing post content for URL {req.url}")
        
        # Step 1: Fetch and classify metadata
        raw_meta = MetadataService.fetch_metadata(req.url)
        classified = ClassifierService.classify(raw_meta)
        
        item_idx = req.item or req.index or 1
        items = classified.items
        
        target_item: Optional[Dict[str, Any]] = None
        if items:
            # Map index (1-based)
            sel = next((it for it in items if it.index == item_idx), items[0])
            target_item = sel.extra_info or {
                "id": classified.shortcode,
                "url": sel.url,
                "thumbnail": sel.thumbnail,
                "is_video": (sel.media_type == MediaType.VIDEO),
                "duration": sel.duration
            }
            
        logger.info(f"PostService: Item {item_idx} classified as {classified.primary_type}")

        # Step 2: Route item based on classification
        return DownloadService.download_item(
            url=req.url,
            task_dir=task_dir,
            item_index=item_idx,
            item_entry=target_item,
            requested_format=req.format or req.quality,
            audio_only=req.audio_only,
            mux_audio=req.mux_audio or req.merge_photo_audio
        )
