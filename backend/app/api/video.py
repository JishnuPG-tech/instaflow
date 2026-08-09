import os
import logging
from fastapi import APIRouter, HTTPException, BackgroundTasks, Query
from fastapi.responses import StreamingResponse
from backend.app.models.request import AnalyzeRequest, DownloadRequest
from backend.app.models.response import ErrorCode
from backend.app.models.media import DownloadIntent
from backend.app.services.metadata_service import MetadataService
from backend.app.services.video_service import VideoService
from backend.app.services.stream_service import StreamService
from backend.app.services.cleanup_service import CleanupService
from backend.app.utils.temp import TempUtil

router = APIRouter()
logger = logging.getLogger("VideoRouter")

@router.post("/video/analyze")
async def analyze_video(req: AnalyzeRequest):
    try:
        logger.info(f"VideoRouter: Analyzing video metadata for {req.url}")
        meta = MetadataService.fetch_metadata(req.url)
        return {"success": True, "type": "video", "data": meta}
    except Exception as e:
        logger.error(f"Video analysis error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.post("/video/download")
async def download_video_post(req: DownloadRequest, background_tasks: BackgroundTasks):
    req.download_type = DownloadIntent.VIDEO
    task_dir = TempUtil.create_task_dir()
    try:
        file_path = VideoService.process_video_download(req, task_dir)
        background_tasks.add_task(CleanupService.cleanup_task_dir, task_dir)
        return StreamService.create_file_response(file_path)
    except Exception as e:
        CleanupService.cleanup_task_dir(task_dir)
        logger.error(f"Video download error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.get("/video/download")
async def download_video_get(
    url: str = Query(..., description="Instagram Video URL"),
    quality: str = Query(None, description="Quality preference"),
    background_tasks: BackgroundTasks = BackgroundTasks()
):
    req = DownloadRequest(url=url, quality=quality, download_type=DownloadIntent.VIDEO)
    return await download_video_post(req, background_tasks)
