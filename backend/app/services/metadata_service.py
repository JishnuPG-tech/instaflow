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
        
        logger.info(f"Executing Photo Fallback Extraction for {url}")
        
        shortcode = "photo"
        if "/p/" in url:
            shortcode = url.split("/p/")[1].split("/")[0]
        elif "/reel/" in url:
            shortcode = url.split("/reel/")[1].split("/")[0]
            
        session = requests.Session()
        session.headers.update({
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",
            "Referer": "https://www.instagram.com/",
            "X-IG-App-ID": "936619743392459",
        })
        
        if os.path.exists(settings.COOKIES_FILE) and os.path.getsize(settings.COOKIES_FILE) > 0:
            try:
                with open(settings.COOKIES_FILE, "r") as f:
                    for line in f:
                        if not line.startswith("#") and line.strip():
                            parts = line.strip().split("\t")
                            if len(parts) >= 7:
                                session.cookies.set(parts[5], parts[6], domain=parts[0])
            except Exception as ce:
                logger.warning(f"Error loading cookies in photo fallback: {ce}")
                
        # Attempt 1: Embed captioned page
        embed_url = f"https://www.instagram.com/p/{shortcode}/embed/captioned/"
        resp = session.get(embed_url, timeout=6)
        
        images = re.findall(r'class="EmbeddedMediaImage"[^>]*src="([^"]+)"', resp.text) or re.findall(r'property="og:image" content="([^"]+)"', resp.text) or re.findall(r'"display_url":"([^"]+)"', resp.text)
        
        # Attempt 2: Direct post page if embed page had no images
        if not images:
            logger.info("Embed page yielded no images, attempting direct post page...")
            web_url = f"https://www.instagram.com/p/{shortcode}/"
            resp_web = session.get(web_url, timeout=6)
            images = re.findall(r'property="og:image" content="([^"]+)"', resp_web.text) or re.findall(r'"display_url":"([^"]+)"', resp_web.text)
            if not resp.text or len(resp.text) < 1000:
                resp = resp_web
            
        clean_images = []
        for img in images:
            clean = img.replace("\\/", "/").replace("\\u0026", "&").replace("&amp;", "&")
            if clean not in clean_images:
                clean_images.append(clean)
                
        username_match = re.search(r'class="UsernameText"[^>]*>(.*?)</div>', resp.text, re.DOTALL)
        username = "Instagram User"
        if username_match:
            raw_user = re.sub(r'<[^>]+>', '', username_match.group(1)).strip()
            username = raw_user.split()[0] if raw_user else "Instagram User"
            
        caption_match = re.search(r'<div class="Caption"[^>]*>(.*?)</div>', resp.text, re.DOTALL)
        caption = f"Post by {username}"
        if caption_match:
            cap_text = html.unescape(re.sub(r'<[^>]+>', '', caption_match.group(1)).strip())
            if cap_text:
                caption = cap_text
                
        if not clean_images:
            raise RuntimeError("No photo images found in post")
            
        primary_url = clean_images[0]
        entries = []
        for idx, img_url in enumerate(clean_images):
            entries.append({
                "id": f"{shortcode}_{idx+1}",
                "title": f"Photo {idx+1} by {username}",
                "url": img_url,
                "thumbnail": img_url,
                "vcodec": "none",
                "acodec": "none",
                "duration": 0
            })
            
        meta: Dict[str, Any] = {
            "id": shortcode,
            "title": caption,
            "uploader": username,
            "channel": username,
            "vcodec": "none",
            "acodec": "none",
            "duration": 0,
            "thumbnail": primary_url,
            "url": primary_url,
            "ext": "jpg",
            "is_photo": True,
            "formats": [{"url": u, "ext": "jpg", "format_id": f"photo_{idx+1}", "vcodec": "none", "acodec": "none"} for idx, u in enumerate(clean_images)]
        }
        if len(entries) > 1:
            meta["entries"] = entries
        return meta

    @classmethod
    def fetch_metadata(cls, url: str) -> Dict[str, Any]:
        norm_url = normalize_instagram_url(url)
        logger.info(f"Fetching in-memory yt-dlp metadata for {norm_url}")
        
        has_cookies = os.path.exists(settings.COOKIES_FILE) and os.path.getsize(settings.COOKIES_FILE) > 0
        
        err_messages = []
        
        # Step 1: Cookie Mode FIRST if cookies available (Fastest & most reliable on server IPs)
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
