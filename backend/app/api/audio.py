import os
import logging
from fastapi import APIRouter, HTTPException, BackgroundTasks, Query
from backend.app.models.request import AnalyzeRequest, DownloadRequest
from backend.app.models.response import ErrorCode
from backend.app.models.media import DownloadIntent
from backend.app.services.metadata_service import MetadataService
from backend.app.services.audio_service import AudioService
from backend.app.services.stream_service import StreamService
from backend.app.services.cleanup_service import CleanupService
from backend.app.utils.temp import TempUtil

router = APIRouter()
logger = logging.getLogger("AudioRouter")

@router.post("/audio/analyze")
async def analyze_audio(req: AnalyzeRequest):
    try:
        logger.info(f"AudioRouter: Analyzing audio metadata for {req.url}")
        meta = MetadataService.fetch_metadata(req.url)
        return {"success": True, "type": "audio", "data": meta}
    except Exception as e:
        logger.error(f"Audio analysis error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.post("/audio/download")
async def download_audio_post(req: DownloadRequest, background_tasks: BackgroundTasks):
    req.download_type = DownloadIntent.AUDIO
    req.audio_only = True
    task_dir = TempUtil.create_task_dir()
    try:
        file_path = AudioService.process_audio_download(req, task_dir)
        background_tasks.add_task(CleanupService.cleanup_task_dir, task_dir)
        return StreamService.create_file_response(file_path)
    except Exception as e:
        CleanupService.cleanup_task_dir(task_dir)
        logger.error(f"Audio download error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.get("/audio/download")
async def download_audio_get(
    url: str = Query(..., description="Instagram URL to extract audio from"),
    format: str = Query(None, description="Audio format (m4a, mp3)"),
    background_tasks: BackgroundTasks = BackgroundTasks()
):
    req = DownloadRequest(url=url, format=format, audio_only=True, download_type=DownloadIntent.AUDIO)
    return await download_audio_post(req, background_tasks)
