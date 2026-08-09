import os
import logging
from fastapi import APIRouter, HTTPException, BackgroundTasks, Query
from backend.app.models.request import AnalyzeRequest, DownloadRequest
from backend.app.models.response import ErrorCode
from backend.app.models.media import DownloadIntent
from backend.app.services.metadata_service import MetadataService
from backend.app.services.classifier_service import ClassifierService
from backend.app.services.post_service import PostService
from backend.app.services.stream_service import StreamService
from backend.app.services.cleanup_service import CleanupService
from backend.app.utils.temp import TempUtil

router = APIRouter()
logger = logging.getLogger("PostRouter")

@router.post("/post/analyze")
async def analyze_post(req: AnalyzeRequest):
    try:
        logger.info(f"PostRouter: Analyzing post metadata for {req.url}")
        meta = MetadataService.fetch_metadata(req.url)
        result = ClassifierService.classify_url(req.url, meta)
        return {
            "success": True,
            "type": result.contentType.value,
            "author": result.author,
            "title": result.title,
            "thumbnail": result.thumbnail,
            "items": [it.model_dump() for it in result.mediaItems],
            "media_result": result.model_dump(),
            "raw_metadata": meta
        }
    except Exception as e:
        logger.error(f"Post analysis error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.post("/post/download")
async def download_post_post(req: DownloadRequest, background_tasks: BackgroundTasks):
    req.download_type = DownloadIntent.POST
    task_dir = TempUtil.create_task_dir()
    try:
        file_path = PostService.process_post_download(req, task_dir)
        background_tasks.add_task(CleanupService.cleanup_task_dir, task_dir)
        return StreamService.create_file_response(file_path)
    except Exception as e:
        CleanupService.cleanup_task_dir(task_dir)
        logger.error(f"Post download error: {e}")
        raise HTTPException(status_code=500, detail={"success": False, "error_code": ErrorCode.DOWNLOAD_FAILED.value, "message": str(e)})

@router.get("/post/download")
async def download_post_get(
    url: str = Query(..., description="Instagram Post / Carousel URL"),
    item: int = Query(None, description="Item index"),
    background_tasks: BackgroundTasks = BackgroundTasks()
):
    req = DownloadRequest(url=url, item=item, download_type=DownloadIntent.POST)
    return await download_post_post(req, background_tasks)
