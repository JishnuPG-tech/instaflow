import os
import logging
from typing import Optional, Dict, Any
from backend.app.services.download_service import DownloadService
from backend.app.models.request import DownloadRequest

logger = logging.getLogger("AudioService")

class AudioService:
    @classmethod
    def process_audio_download(
        cls,
        req: DownloadRequest,
        task_dir: str
    ) -> str:
        logger.info(f"AudioService: Processing audio extraction for URL {req.url}")
        format_val = req.format or req.quality or "audio"
        
        # Delegates to the core engine with explicit audio_only=True intent
        return DownloadService.download_item(
            url=req.url,
            task_dir=task_dir,
            item_index=req.item or req.index or 0,
            requested_format=format_val,
            audio_only=True,
            mux_audio=False
        )
