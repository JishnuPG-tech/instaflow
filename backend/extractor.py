import asyncio
import logging
from typing import Dict, Any, List
from fastapi import HTTPException
import yt_dlp

logger = logging.getLogger("instasave.extractor")


def _normalize_format(fmt: Dict[str, Any], idx: int = 0) -> Dict[str, Any]:
    height = fmt.get("height")
    width = fmt.get("width")
    ext = fmt.get("ext", "mp4")
    format_id = fmt.get("format_id") or f"{height}p" if height else f"fmt_{idx}"
    label = (
        f"{height}p HD"
        if height and height >= 720
        else (f"{height}p SD" if height else "Standard Quality")
    )

    return {
        "formatId": str(format_id),
        "label": label,
        "ext": ext,
        "vcodec": fmt.get("vcodec") if fmt.get("vcodec") != "none" else None,
        "acodec": fmt.get("acodec") if fmt.get("acodec") != "none" else None,
        "height": height,
        "width": width,
        "tbr": float(fmt["tbr"]) if fmt.get("tbr") else None,
        "filesizeBytes": fmt.get("filesize") or fmt.get("filesize_approx"),
    }


def _extract_sync(url: str) -> Dict[str, Any]:
    ydl_opts = {
        "quiet": True,
        "no_warnings": True,
        "extract_flat": False,
        "skip_download": True,
        "allowed_extractors": ["instagram"],
        "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        try:
            info = ydl.extract_info(url, download=False)
            if not info:
                raise HTTPException(
                    status_code=422,
                    detail={
                        "error": {
                            "code": "MEDIA_NOT_FOUND",
                            "message": "Instagram media could not be found or extracted.",
                        }
                    },
                )
            return info
        except yt_dlp.utils.DownloadError as e:
            err_msg = str(e)
            if "private" in err_msg.lower() or "login" in err_msg.lower():
                raise HTTPException(
                    status_code=403,
                    detail={
                        "error": {
                            "code": "PRIVATE_ACCOUNT",
                            "message": "This post belongs to a private account or requires Instagram login.",
                        }
                    },
                )
            elif "not found" in err_msg.lower() or "404" in err_msg:
                raise HTTPException(
                    status_code=404,
                    detail={
                        "error": {
                            "code": "MEDIA_NOT_FOUND",
                            "message": "The requested Instagram URL was not found or has been deleted.",
                        }
                    },
                )
            else:
                logger.error(f"yt-dlp extraction error: {err_msg}")
                raise HTTPException(
                    status_code=502,
                    detail={
                        "error": {
                            "code": "INSTAGRAM_UNREACHABLE",
                            "message": f"Instagram extraction failed: {err_msg}",
                        }
                    },
                )


async def extract_media_info(url: str) -> Dict[str, Any]:
    # Run blocking yt-dlp in thread pool to preserve async performance
    info = await asyncio.to_thread(_extract_sync, url)

    media_id = info.get("id") or info.get("display_id") or "unknown_id"
    entries = info.get("entries")

    # Check if carousel
    if entries and len(entries) > 0:
        items: List[Dict[str, Any]] = []
        for idx, entry in enumerate(entries):
            e_type = (
                "video"
                if entry.get("vcodec") != "none" or entry.get("ext") in ["mp4", "webm"]
                else "photo"
            )
            e_formats = entry.get("formats", [])
            norm_formats = (
                [_normalize_format(f, i) for i, f in enumerate(e_formats)]
                if e_formats
                else [
                    {
                        "formatId": "original",
                        "label": "Original",
                        "ext": entry.get("ext", "jpg"),
                        "height": entry.get("height"),
                        "width": entry.get("width"),
                        "filesizeBytes": entry.get("filesize"),
                    }
                ]
            )
            items.append(
                {
                    "index": idx,
                    "type": e_type,
                    "thumbnailUrl": entry.get("thumbnail"),
                    "formats": norm_formats,
                }
            )

        return {
            "id": media_id,
            "type": "carousel",
            "author": info.get("uploader") or info.get("uploader_id"),
            "authorDisplayName": info.get("uploader"),
            "authorAvatarUrl": None,
            "thumbnailUrl": info.get("thumbnail"),
            "caption": info.get("description") or info.get("title"),
            "uploadedAt": str(info.get("timestamp")) if info.get("timestamp") else None,
            "durationSeconds": float(info["duration"])
            if info.get("duration")
            else None,
            "formats": None,
            "items": items,
        }
    else:
        # Single Post / Reel / Video
        raw_formats = info.get("formats", [])
        is_video = (
            info.get("vcodec") != "none"
            or info.get("ext") in ["mp4", "webm"]
            or len(raw_formats) > 0
        )
        media_type = (
            "reel"
            if "/reel/" in url or "/reels/" in url
            else ("video" if is_video else "post")
        )

        norm_formats = (
            [_normalize_format(f, i) for i, f in enumerate(raw_formats)]
            if raw_formats
            else [
                {
                    "formatId": "original",
                    "label": "Original Quality",
                    "ext": info.get("ext", "jpg" if not is_video else "mp4"),
                    "height": info.get("height"),
                    "width": info.get("width"),
                    "filesizeBytes": info.get("filesize"),
                }
            ]
        )

        return {
            "id": media_id,
            "type": media_type,
            "author": info.get("uploader") or info.get("uploader_id"),
            "authorDisplayName": info.get("uploader"),
            "authorAvatarUrl": None,
            "thumbnailUrl": info.get("thumbnail"),
            "caption": info.get("description") or info.get("title"),
            "uploadedAt": str(info.get("timestamp")) if info.get("timestamp") else None,
            "durationSeconds": float(info["duration"])
            if info.get("duration")
            else None,
            "formats": norm_formats,
            "items": None,
        }
