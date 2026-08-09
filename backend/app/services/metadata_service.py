import os
import logging
from typing import Dict, Any, List
import yt_dlp
from backend.app.core.config import settings
from backend.app.utils.media import normalize_instagram_url
from backend.app.models.response import ErrorCode

logger = logging.getLogger("MetadataService")

class MetadataService:
    @staticmethod
    def get_ig_headers() -> List[str]:
        args = [
            "--add-header", "X-IG-App-ID:936619743392459",
            "--add-header", "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "--add-header", "Referer:https://www.instagram.com/"
        ]
        if os.path.exists(settings.COOKIES_FILE) and os.path.getsize(settings.COOKIES_FILE) > 0:
            args.extend(["--cookies", settings.COOKIES_FILE])
        return args

    @staticmethod
    def get_ydl_opts(extract_flat: bool = False) -> Dict[str, Any]:
        opts: Dict[str, Any] = {
            "quiet": True,
            "no_warnings": True,
            "skip_download": True,
            "extract_flat": extract_flat,
            "cachedir": False,
            "force_ipv4": True,
            "socket_timeout": 8,
            "retries": 3,
            "fragment_retries": 3,
            "http_headers": {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                "Referer": "https://www.instagram.com/",
                "X-IG-App-ID": "936619743392459",
            }
        }
        if os.path.exists(settings.COOKIES_FILE) and os.path.getsize(settings.COOKIES_FILE) > 0:
            opts["cookiefile"] = settings.COOKIES_FILE
        return opts

    @staticmethod
    def extract_photo_fallback(url: str) -> Dict[str, Any]:
        import re
        import html
        import requests
        
        shortcode = "media"
        if "/p/" in url:
            shortcode = url.split("/p/")[1].split("/")[0]
        elif "/reel/" in url:
            shortcode = url.split("/reel/")[1].split("/")[0]
            
        logger.info(f"Executing Direct Embed Extraction for shortcode {shortcode}")
        
        session = requests.Session()
        session.headers.update({
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",
            "Referer": "https://www.instagram.com/",
        })
        
        clean_video_url = None
        clean_images = []
        username = "Instagram User"
        caption = f"Instagram Media ({shortcode})"

        embed_url = f"https://www.instagram.com/p/{shortcode}/embed/captioned/"
        try:
            resp = session.get(embed_url, timeout=6.0)
            if resp.status_code == 200:
                resp_text = resp.text
                
                # Check for video_url in embed JSON/JS
                video_matches = re.findall(r'"video_url"\s*:\s*"([^"]+)"', resp_text) or re.findall(r'video_url\\":\\"(.*?)\\"', resp_text)
                if video_matches:
                    clean_video_url = html.unescape(video_matches[0].replace("\\/", "/").replace("\\u0026", "&").replace("\\\\/", "/"))

                images = re.findall(r'src="([^"]+fbcdn\.net[^"]+)"', resp_text) or re.findall(r'class="EmbeddedMediaImage"[^>]*src="([^"]+)"', resp_text) or re.findall(r'property="og:image" content="([^"]+)"', resp_text)
                for img in images:
                    clean = html.unescape(img.replace("\\/", "/").replace("\\u0026", "&"))
                    if clean not in clean_images:
                        clean_images.append(clean)
                        
                username_match = re.search(r'class="UsernameText"[^>]*>(.*?)</div>', resp_text, re.DOTALL)
                if username_match:
                    raw_user = re.sub(r'<[^>]+>', '', username_match.group(1)).strip()
                    if raw_user:
                        username = raw_user.split()[0]
                        
                caption_match = re.search(r'<div class="Caption"[^>]*>(.*?)</div>', resp_text, re.DOTALL)
                if caption_match:
                    cap_text = html.unescape(re.sub(r'<[^>]+>', '', caption_match.group(1)).strip())
                    if cap_text:
                        caption = cap_text
        except Exception as embed_err:
            logger.warning(f"Embed page extraction error: {embed_err}")

        primary_thumb = clean_images[0] if clean_images else f"https://www.instagram.com/p/{shortcode}/media/?size=l"
        
        if clean_video_url:
            return {
                "id": shortcode,
                "title": caption,
                "uploader": username,
                "channel": username,
                "vcodec": "h264",
                "acodec": "aac",
                "duration": 15,
                "thumbnail": primary_thumb,
                "url": clean_video_url,
                "ext": "mp4",
                "is_photo": False,
                "is_video": True,
                "formats": [{"url": clean_video_url, "ext": "mp4", "format_id": "video_hd", "vcodec": "h264", "acodec": "aac"}]
            }
        else:
            return {
                "id": shortcode,
                "title": caption,
                "uploader": username,
                "channel": username,
                "vcodec": "none",
                "acodec": "none",
                "duration": 0,
                "thumbnail": primary_thumb,
                "url": primary_thumb,
                "ext": "jpg",
                "is_photo": True,
                "is_video": False,
                "formats": [{"url": primary_thumb, "ext": "jpg", "format_id": "photo_1", "vcodec": "none", "acodec": "none"}]
            }

    @classmethod
    def fetch_metadata(cls, url: str) -> Dict[str, Any]:
        norm_url = normalize_instagram_url(url)
        logger.info(f"Fetching in-memory metadata for {norm_url}")
        
        # Step 0: For /p/ Photo/Carousel URLs, execute Photo Fallback FIRST (300ms instant extraction)
        if "/p/" in norm_url:
            try:
                logger.info("Executing Primary Photo Embed Extraction for /p/ URL...")
                return cls.extract_photo_fallback(norm_url)
            except Exception as photo_err:
                logger.warning(f"Primary Photo Embed Extraction failed: {photo_err}. Proceeding to yt-dlp...")

        has_cookies = os.path.exists(settings.COOKIES_FILE) and os.path.getsize(settings.COOKIES_FILE) > 0
        
        err_messages = []
        
        # Step 1: Cookie Mode FIRST if cookies available
        if has_cookies:
            cookie_opts = cls.get_ydl_opts()
            try:
                with yt_dlp.YoutubeDL(cookie_opts) as ydl_cookie:
                    info_cookie = ydl_cookie.extract_info(norm_url, download=False)
                    if info_cookie:
                        logger.info("Cookie Mode metadata extraction succeeded!")
                        return ydl_cookie.sanitize_info(info_cookie)
            except Exception as cookie_err:
                err_str = str(cookie_err)
                err_messages.append(err_str)
                logger.warning(f"Cookie Mode metadata extraction failed: {err_str[:150]}. Trying Anonymous Mode fallback...")

        # Step 2: Anonymous Mode fallback (Or primary if no cookies file)
        anon_opts: Dict[str, Any] = {
            "quiet": True,
            "no_warnings": True,
            "skip_download": True,
            "cachedir": False,
            "force_ipv4": True,
            "socket_timeout": 8,
            "retries": 3,
            "fragment_retries": 3,
            "http_headers": {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                "Referer": "https://www.instagram.com/",
                "X-IG-App-ID": "936619743392459",
            }
        }
        
        try:
            with yt_dlp.YoutubeDL(anon_opts) as ydl_anon:
                info_anon = ydl_anon.extract_info(norm_url, download=False)
                if info_anon:
                    logger.info("Anonymous Mode metadata extraction succeeded!")
                    return ydl_anon.sanitize_info(info_anon)
        except Exception as anon_err:
            err_str = str(anon_err)
            err_messages.append(err_str)
            logger.error(f"Anonymous Mode metadata extraction failed: {err_str[:150]}")

        # Step 3: Photo & Carousel Embed Fallback for non-video posts
        combined_err = " ".join(err_messages).lower()
        if "no video" in combined_err or "404" in combined_err or "not found" in combined_err or "/p/" in url:
            try:
                logger.info("Triggering Photo Embed Fallback for non-video post...")
                return cls.extract_photo_fallback(norm_url)
            except Exception as photo_err:
                logger.error(f"Photo embed fallback failed: {photo_err}")

        # Fallback error categorization
        err_str = str(anon_err) if 'anon_err' in locals() else (err_messages[0] if err_messages else "Metadata extraction failed")
        if "Login required" in err_str:
            raise ValueError(ErrorCode.LOGIN_REQUIRED.value)
        elif "Private account" in err_str or "private" in err_str.lower():
            raise ValueError(ErrorCode.PRIVATE_POST.value)
        elif "429" in err_str or "Too Many Requests" in err_str:
            raise ValueError(ErrorCode.RATE_LIMITED.value)
        elif "404" in err_str or "Not Found" in err_str:
            raise ValueError(ErrorCode.NOT_FOUND.value)
        else:
            raise RuntimeError(f"{ErrorCode.DOWNLOAD_FAILED.value}: {err_str[:200]}")
