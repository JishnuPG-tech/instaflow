import os
import logging
from typing import Optional
from fastapi import APIRouter, HTTPException, BackgroundTasks, Query
from backend.app.models.request import AnalyzeRequest, DownloadRequest
from backend.app.models.response import ErrorCode
from backend.app.models.media import DownloadIntent
from backend.app.services.metadata_service import MetadataService
from backend.app.services.classifier_service import ClassifierService
from backend.app.services.download_service import DownloadService
from backend.app.services.video_service import VideoService
from backend.app.services.audio_service import AudioService
from backend.app.services.post_service import PostService
from backend.app.services.stream_service import StreamService
from backend.app.services.cleanup_service import CleanupService
from backend.app.utils.temp import TempUtil

router = APIRouter()
logger = logging.getLogger("GatewayRouter")

def _to_dict(obj):
    if hasattr(obj, "dict"):
        return obj.dict()
    if hasattr(obj, "model_dump"):
        return obj.model_dump()
    return obj

@router.post("/analyze")
async def analyze_gateway(req: AnalyzeRequest):
    try:
        logger.info(f"GatewayRouter: Analyzing URL {req.url} (intent: {req.download_type})")
        meta = MetadataService.fetch_metadata(req.url)
        result = ClassifierService.classify_url(req.url, meta)
        return {
            "success": True,
            "type": result.contentType.value if hasattr(result.contentType, "value") else str(result.contentType),
            "author": result.author,
            "title": result.title,
            "thumbnail": result.thumbnail,
            "items": [_to_dict(it) for it in result.mediaItems],
            "media_result": _to_dict(result),
            "raw_metadata": meta
        }
    except Exception as e:
        logger.error(f"Gateway analyze error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.post("/download")
async def download_gateway_post(req: DownloadRequest, background_tasks: BackgroundTasks):
    task_dir = TempUtil.create_task_dir()
    try:
        intent = req.download_type
        logger.info(f"GatewayRouter: Dispatching download request for {req.url} (intent: {intent})")
        
        if intent == DownloadIntent.AUDIO or req.audio_only:
            file_path = AudioService.process_audio_download(req, task_dir)
        elif intent == DownloadIntent.VIDEO:
            file_path = VideoService.process_video_download(req, task_dir)
        elif intent == DownloadIntent.POST:
            file_path = PostService.process_post_download(req, task_dir)
        else:
            format_val = req.format or req.quality
            file_path = DownloadService.download_item(
                url=req.url,
                task_dir=task_dir,
                item_index=req.item or req.index or 0,
                requested_format=format_val,
                audio_only=bool(req.audio_only),
                mux_audio=bool(req.mux_audio or req.merge_photo_audio)
            )

        background_tasks.add_task(CleanupService.cleanup_task_dir, task_dir)
        return StreamService.create_file_response(file_path)
    except Exception as e:
        CleanupService.cleanup_task_dir(task_dir)
        logger.error(f"Gateway download error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.get("/download")
async def download_gateway_get(
    url: str = Query(..., description="Instagram URL"),
    item: Optional[int] = Query(None, description="Item index"),
    quality: Optional[str] = Query(None, description="Quality preference"),
    format: Optional[str] = Query(None, description="Format preference"),
    audio_only: Optional[bool] = Query(False, description="Audio only flag"),
    merge_photo_audio: Optional[bool] = Query(False, description="Merge photo audio flag"),
    download_type: Optional[DownloadIntent] = Query(None, description="Explicit download intent"),
    background_tasks: BackgroundTasks = BackgroundTasks()
):
    req = DownloadRequest(
        url=url,
        item=item,
        quality=quality or format,
        format=format or quality,
        audio_only=audio_only,
        mux_audio=merge_photo_audio,
        download_type=download_type
    )
    return await download_gateway_post(req, background_tasks)
