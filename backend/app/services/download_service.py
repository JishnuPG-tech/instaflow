import os
import sys
import subprocess
import urllib.request
import logging
from typing import Optional, Dict, Any
from backend.app.services.metadata_service import MetadataService
from backend.app.services.ffmpeg_service import FFmpegService
from backend.app.services.validation_service import ValidationService
from backend.app.utils.media import normalize_instagram_url
from backend.app.utils.filename import sanitize_filename
from backend.app.models.response import ErrorCode

logger = logging.getLogger("DownloadService")

class DownloadService:
    @classmethod
    def download_item(
        cls,
        url: str,
        task_dir: str,
        item_index: Optional[int] = None,
        item_entry: Optional[Dict[str, Any]] = None,
        requested_format: Optional[str] = None,
        audio_only: bool = False
    ) -> str:
        norm_url = normalize_instagram_url(url)
        
        # Check Strategy 1: Direct Photo CDN Download
        if item_entry and not audio_only:
            img_url = item_entry.get("thumbnail") or item_entry.get("url")
            if not img_url and item_entry.get("thumbnails"):
                img_url = item_entry["thumbnails"][-1].get("url")
                
            vcodec = item_entry.get("vcodec")
            acodec = item_entry.get("acodec")
            duration = float(item_entry.get("duration") or 0.0)
            
            if img_url and (not vcodec or vcodec == "none") and (not acodec or acodec == "none") and duration == 0.0:
                logger.info("Executing Photo CDN download")
                ext = "jpg"
                if ".webp" in img_url.lower(): ext = "webp"
                elif ".png" in img_url.lower(): ext = "png"
                
                fname = f"InstaFlow_{item_entry.get('id', 'photo')}.{ext}"
                target_path = os.path.join(task_dir, fname)
                
                req = urllib.request.Request(img_url, headers={
                    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                    "Referer": "https://www.instagram.com/"
                })
                with urllib.request.urlopen(req) as resp, open(target_path, "wb") as f:
                    f.write(resp.read())
                    
                ValidationService.validate_file(target_path, is_video=False)
                return target_path

        # Strategy 2: Audio Only vs Full Video Download
        out_tmpl = os.path.join(task_dir, "InstaFlow_%(title).100s.%(ext)s")
        cmd = [sys.executable, "-m", "yt_dlp", "-4", "-o", out_tmpl]
        
        ffmpeg_bin = FFmpegService.get_ffmpeg_binary()
        if ffmpeg_bin:
            cmd.extend(["--ffmpeg-location", ffmpeg_bin])

        is_audio = audio_only or (requested_format and ("audio" in requested_format.lower() or "m4a" in requested_format.lower()))
        
        if is_audio:
            logger.info("Executing Audio-Only Extraction")
            cmd.extend(["-x", "--audio-format", "m4a", "-f", "bestaudio/best"])
        else:
            logger.info("Executing Full Video Download with Audio Muxing")
            cmd.extend(["--merge-output-format", "mp4"])
            if requested_format and requested_format not in ["best", "bestvideo+bestaudio/best", ""]:
                # Custom requested resolution (e.g. 1080p, 720p)
                cmd.extend(["-f", f"{requested_format}+bestaudio/b[ext=mp4]/best[ext=mp4]/best"])
            else:
                cmd.extend(["-f", "b[ext=mp4]/best[ext=mp4]/bestvideo[vcodec^=avc1]+bestaudio/b/best"])
        
        if item_index and item_index > 0:
            cmd.extend(["--playlist-items", str(item_index)])
        else:
            cmd.append("--no-playlist")
            
        cmd.extend(MetadataService.get_ig_headers())
        cmd.append(norm_url)
        
        logger.info(f"Executing yt-dlp command: {' '.join(cmd)}")
        res = subprocess.run(cmd, capture_output=True, text=True)
        
        if res.returncode != 0:
            logger.warning(f"Explicit format failed: {res.stderr[:200]}. Retrying progressive fallback...")
            cmd_fallback = [sys.executable, "-m", "yt_dlp", "-4", "-o", out_tmpl]
            if ffmpeg_bin:
                cmd_fallback.extend(["--ffmpeg-location", ffmpeg_bin])
            if item_index and item_index > 0:
                cmd_fallback.extend(["--playlist-items", str(item_index)])
            else:
                cmd_fallback.append("--no-playlist")
            
            if is_audio:
                cmd_fallback.extend(["-x", "--audio-format", "m4a", "-f", "bestaudio/best"])
            else:
                cmd_fallback.extend(["-f", "b/best"])
                
            cmd_fallback.extend(MetadataService.get_ig_headers())
            cmd_fallback.append(norm_url)
            res = subprocess.run(cmd_fallback, capture_output=True, text=True)
            if res.returncode != 0:
                raise RuntimeError(f"{ErrorCode.DOWNLOAD_FAILED.value}: {res.stderr[:200]}")

        # Scan task_dir for valid completed output files
        import re
        all_entries = [f for f in os.listdir(task_dir) if not f.startswith(".")]
        
        clean_files = []
        for f in all_entries:
            lname = f.lower()
            if lname.endswith((".part", ".ytdl", ".tmp", ".temp", ".nomedia", ".json")):
                continue
            if "fdash" in lname or re.search(r'\.f\d+\.', lname):
                continue
            clean_files.append(os.path.join(task_dir, f))

        if is_audio:
            # For audio requests, select .m4a / .mp3 / .aac audio file
            audio_files = [f for f in clean_files if f.lower().endswith((".m4a", ".mp3", ".aac", ".flac", ".ogg"))]
            if audio_files:
                audio_files.sort(key=lambda x: os.path.getmtime(x), reverse=True)
                target_file = audio_files[0]
            elif clean_files:
                clean_files.sort(key=lambda x: os.path.getmtime(x), reverse=True)
                target_file = clean_files[0]
            else:
                raise RuntimeError(f"{ErrorCode.DOWNLOAD_FAILED.value}: No downloaded audio file produced.")
        else:
            # For video requests, strictly select .mp4 video file with audio
            video_files = [f for f in clean_files if f.lower().endswith((".mp4", ".mkv", ".webm"))]
            if video_files:
                video_files.sort(key=lambda x: os.path.getmtime(x), reverse=True)
                target_file = video_files[0]
            elif clean_files:
                clean_files.sort(key=lambda x: os.path.getmtime(x), reverse=True)
                target_file = clean_files[0]
            else:
                raw_files = [os.path.join(task_dir, f) for f in all_entries if not f.endswith((".part", ".ytdl", ".tmp"))]
                raw_mp4s = [f for f in raw_files if f.lower().endswith((".mp4", ".mkv", ".webm"))]
                if raw_mp4s:
                    raw_mp4s.sort(key=lambda x: os.path.getmtime(x), reverse=True)
                    target_file = raw_mp4s[0]
                elif raw_files:
                    raw_files.sort(key=lambda x: os.path.getmtime(x), reverse=True)
                    target_file = raw_files[0]
                else:
                    raise RuntimeError(f"{ErrorCode.DOWNLOAD_FAILED.value}: No downloaded media file produced.")

        is_video_res = target_file.lower().endswith((".mp4", ".mkv", ".webm"))
        ValidationService.validate_file(target_file, is_video=is_video_res)
        return target_file
