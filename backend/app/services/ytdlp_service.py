import os
import logging
from typing import Dict, Any, Optional
import yt_dlp
from backend.app.core.config import settings
from backend.app.services.ffmpeg_service import FFmpegService

logger = logging.getLogger("YtDlpService")

class YtDlpService:
    @classmethod
    def get_base_opts(cls, out_tmpl: str) -> Dict[str, Any]:
        ffmpeg_bin = FFmpegService.get_ffmpeg_binary()
        opts: Dict[str, Any] = {
            "outtmpl": out_tmpl,
            "quiet": True,
            "no_warnings": True,
            "cachedir": False,
            "force_ipv4": True,
            "socket_timeout": 8,
            "retries": 5,
            "fragment_retries": 5,
            "http_headers": {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                "Referer": "https://www.instagram.com/",
                "X-IG-App-ID": "936619743392459",
            }
        }
        if ffmpeg_bin:
            opts["ffmpeg_location"] = ffmpeg_bin
            
        if os.path.exists(settings.COOKIES_FILE) and os.path.getsize(settings.COOKIES_FILE) > 0:
            opts["cookiefile"] = settings.COOKIES_FILE
            
        return opts

    @classmethod
    def download_url(cls, url: str, opts: Dict[str, Any]) -> bool:
        try:
            with yt_dlp.YoutubeDL(opts) as ydl:
                ydl.download([url])
            return True
        except Exception as err:
            logger.warning(f"yt-dlp execution error: {err}")
            # Anonymous fallback retry if cookies failed
            if "cookiefile" in opts:
                anon_opts = dict(opts)
                del anon_opts["cookiefile"]
                try:
                    logger.info("Retrying yt-dlp in Anonymous Mode (no cookies)...")
                    with yt_dlp.YoutubeDL(anon_opts) as ydl_anon:
                        ydl_anon.download([url])
                    return True
                except Exception as anon_err:
                    logger.warning(f"Anonymous mode retry error: {anon_err}")
            return False
