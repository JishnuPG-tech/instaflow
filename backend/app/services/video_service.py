import os
import logging
from typing import Optional, Dict, Any
from backend.app.services.download_service import DownloadService
from backend.app.models.request import DownloadRequest

logger = logging.getLogger("VideoService")

class VideoService:
    @classmethod
    def process_video_download(
        cls,
        req: DownloadRequest,
        task_dir: str
    ) -> str:
        logger.info(f"VideoService: Processing video download for URL {req.url}")
        format_val = req.format or req.quality or "best"
        
        # Delegates to the core engine with explicit video intent
        return DownloadService.download_item(
            url=req.url,
            task_dir=task_dir,
            item_index=req.item or req.index or 0,
            requested_format=format_val,
            audio_only=False,
            mux_audio=False
        )
