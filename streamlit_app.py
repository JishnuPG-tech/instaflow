import os
import sys
import re
import json
import subprocess
import shutil
import urllib.request
import streamlit as st

# Set Streamlit Page Configuration
st.set_page_config(
    page_title="InstaFlow — Instagram Downloader & Login Engine",
    page_icon="⚡",
    layout="centered",
    initial_sidebar_state="expanded"
)

# Custom CSS for Modern Dark Glassmorphism UI
st.markdown("""
<style>
    .main {
        background: linear-gradient(135deg, #0d0e15 0%, #151828 100%);
        color: #f0f2f5;
        font-family: 'Inter', sans-serif;
    }
    .stApp {
        background: #0d0e15;
    }
    .header-card {
        background: linear-gradient(135deg, #833ab4 0%, #fd1d1d 50%, #fcb045 100%);
        padding: 24px;
        border-radius: 20px;
        color: white;
        text-align: center;
        margin-bottom: 25px;
        box-shadow: 0 10px 30px rgba(253, 29, 29, 0.3);
    }
    .header-title {
        font-size: 2.5rem;
        font-weight: 800;
        margin: 0;
        letter-spacing: -1px;
    }
    .header-subtitle {
        font-size: 1.05rem;
        opacity: 0.95;
        margin-top: 6px;
    }
    .stButton>button {
        background: linear-gradient(90deg, #fd1d1d 0%, #833ab4 100%);
        color: white;
        font-weight: 700;
        border: none;
        border-radius: 12px;
        padding: 12px 24px;
        transition: all 0.3s ease;
        width: 100%;
    }
    .stButton>button:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(253, 29, 29, 0.4);
    }
</style>
""", unsafe_allow_html=True)

# ----------------------------------------------------
# 1. INSTAGRAM URL NORMALIZER (Mirrors InstagramUrlNormalizer.kt)
# ----------------------------------------------------
class InstagramUrlNormalizer:
    @staticmethod
    def normalize(raw_url: str) -> str:
        if not raw_url or not raw_url.strip():
            return ""
        trimmed = raw_url.strip()
        if "?" not in trimmed:
            return trimmed
        base, q = trimmed.split("?", 1)
        clean_params = [
            p for p in q.split("&")
            if not p.lower().startswith(("utm_", "igsh", "igshid", "fbclid", "share_id"))
        ]
        return f"{base}?{'&'.join(clean_params)}" if clean_params else base

# ----------------------------------------------------
# 2. INSTAGRAM URL VALIDATOR (Mirrors InstagramUrlValidator.kt)
# ----------------------------------------------------
class InstagramUrlValidator:
    REEL_PATTERN = re.compile(r"https?://(?:www\.)?instagram\.com/(?:reel|reels|tv)/([A-Za-z0-9_-]+)", re.I)
    POST_PATTERN = re.compile(r"https?://(?:www\.)?instagram\.com/p/([A-Za-z0-9_-]+)", re.I)
    STORY_PATTERN = re.compile(r"https?://(?:www\.)?instagram\.com/stories/([A-Za-z0-9._-]+)/(\d+)", re.I)

    @classmethod
    def parse_url(cls, url: str):
        trimmed = url.strip()
        if not trimmed:
            return {"is_valid": False, "type": "UNKNOWN", "raw": url}
        
        m_reel = cls.REEL_PATTERN.search(trimmed)
        if m_reel:
            return {"is_valid": True, "type": "REEL", "shortcode": m_reel.group(1), "raw": trimmed}
        
        m_post = cls.POST_PATTERN.search(trimmed)
        if m_post:
            return {"is_valid": True, "type": "POST", "shortcode": m_post.group(1), "raw": trimmed}
        
        m_story = cls.STORY_PATTERN.search(trimmed)
        if m_story:
            return {"is_valid": True, "type": "STORY", "username": m_story.group(1), "shortcode": m_story.group(2), "raw": trimmed}
        
        return {"is_valid": False, "type": "UNKNOWN", "raw": trimmed}

# ----------------------------------------------------
# 3. SMART COOKIE PARSER & ENGINE (Mirrors DownloadUtil.kt)
# ----------------------------------------------------
OUTPUT_DIR = os.path.join(os.getcwd(), "downloads")
COOKIES_FILE = os.path.join(os.getcwd(), "cookies.txt")
os.makedirs(OUTPUT_DIR, exist_ok=True)

def find_ffmpeg_path():
    ffmpeg_bin = shutil.which("ffmpeg")
    return ffmpeg_bin if ffmpeg_bin else None

def parse_smart_cookies(raw_text: str) -> tuple[str, int]:
    text = raw_text.strip()
    if not text:
        return "", 0
        
    lines = ["# Netscape HTTP Cookie File", "# http://curl.haxx.se/rfc/cookie_spec.html\n"]
    
    if text.startswith("[") and text.endswith("]"):
        try:
            arr = json.loads(text)
            count = 0
            for item in arr:
                name = item.get("name")
                value = item.get("value")
                domain = item.get("domain", ".instagram.com")
                if name and value:
                    lines.append(f"{domain}\tTRUE\t/\tTRUE\t2147483647\t{name}\t{value}")
                    count += 1
            return "\n".join(lines), count
        except Exception:
            pass

    if text.lower().startswith("cookie:"):
        text = text[7:].strip()
        
    pairs = text.split(";")
    count = 0
    for p in pairs:
        if "=" in p:
            parts = p.strip().split("=", 1)
            k, v = parts[0].strip(), parts[1].strip()
            if k and v:
                lines.append(f".instagram.com\tTRUE\t/\tTRUE\t2147483647\t{k}\t{v}")
                count += 1
                
    if count == 0 and len(text) > 10:
        lines.append(f".instagram.com\tTRUE\t/\tTRUE\t2147483647\tsessionid\t{text}")
        count = 1
        
    return "\n".join(lines), count

def get_ig_headers():
    args = [
        "--add-header", "X-IG-App-ID:936619743392459",
        "--add-header", "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "--add-header", "Referer:https://www.instagram.com/",
        "--allow-unplayable-formats",
        "--ignore-no-formats-error"
    ]
    if os.path.exists(COOKIES_FILE) and os.path.getsize(COOKIES_FILE) > 0:
        args.extend(["--cookies", COOKIES_FILE])
    return args

def fetch_metadata(url: str):
    cmd = [
        sys.executable, "-m", "yt_dlp",
        "--dump-single-json",
        "-4"
    ] + get_ig_headers() + [url]
    
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        err = res.stderr
        if "No video formats found" in err or "Login required" in err:
            raise Exception("LOGIN_REQUIRED_PHOTO_POST")
        raise Exception(f"yt-dlp extraction failed: {err[:300]}")
    return json.loads(res.stdout)

def download_media(url: str, format_selector: str = None, playlist_index: int = None, output_prefix: str = "InstaFlow", item_entry: dict = None):
    # Check if item is a Photo Image (.jpg / .webp / .png)
    if item_entry:
        img_url = item_entry.get("thumbnail") or item_entry.get("url")
        if not img_url and item_entry.get("thumbnails"):
            img_url = item_entry["thumbnails"][-1].get("url")
            
        if img_url and not item_entry.get("vcodec") and not item_entry.get("acodec"):
            # Direct High-Res Image Download
            ext = "jpg"
            if ".webp" in img_url.lower(): ext = "webp"
            elif ".png" in img_url.lower(): ext = "png"
            
            filename = f"{output_prefix}_{item_entry.get('id', 'photo')}.{ext}"
            filepath = os.path.join(OUTPUT_DIR, filename)
            
            req = urllib.request.Request(img_url, headers={
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                "Referer": "https://www.instagram.com/"
            })
            with urllib.request.urlopen(req) as resp, open(filepath, "wb") as f:
                f.write(resp.read())
            return filepath

    # Video Download via yt-dlp
    out_tmpl = os.path.join(OUTPUT_DIR, f"{output_prefix}_%(title).100s.%(ext)s")
    cmd = [sys.executable, "-m", "yt_dlp", "-4", "-o", out_tmpl]
    
    ffmpeg_path = find_ffmpeg_path()
    if ffmpeg_path:
        cmd.extend(["--ffmpeg-location", os.path.dirname(ffmpeg_path)])
    
    cmd.extend(["--merge-output-format", "mp4"])
    
    if format_selector and format_selector.strip():
        cmd.extend(["-f", format_selector])
    
    if playlist_index and playlist_index > 0:
        cmd.extend(["--playlist-items", str(playlist_index)])
    else:
        cmd.append("--no-playlist")
    
    cmd.extend(get_ig_headers())
    cmd.append(url)
    
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        raise Exception(f"Download failed: {res.stderr[:300]}")
    
    files = [os.path.join(OUTPUT_DIR, f) for f in os.listdir(OUTPUT_DIR) if not f.endswith(".part") and not f.endswith(".ytdl")]
    files.sort(key=lambda x: os.path.getmtime(x), reverse=True)
    return files[0] if files else None

# ----------------------------------------------------
# 4. STREAMLIT UI & EVENT FLOW
# ----------------------------------------------------
st.markdown("""
<div class="header-card">
    <div class="header-title">⚡ InstaFlow Web App</div>
    <div class="header-subtitle">Instagram Reels, Posts & Carousels Downloader Engine</div>
</div>
""", unsafe_allow_html=True)

with st.sidebar:
    st.header("⚙️ System Status")
    st.write("**Engine**: `yt-dlp` + `FFmpeg`")
    st.write("**Audio Muxing**: Enabled (AAC/MP4)")
    st.write("**Quality**: Up to 1080p Full HD")
    
    ffmpeg_found = find_ffmpeg_path()
    if ffmpeg_found:
        st.success("FFmpeg Detected")
    else:
        st.warning("FFmpeg missing")
        
    st.markdown("---")
    if os.path.exists(COOKIES_FILE) and os.path.getsize(COOKIES_FILE) > 0:
        st.success("🔐 Session Cookies: ACTIVE")
        if st.button("🚪 Clear Session Cookies"):
            os.remove(COOKIES_FILE)
            st.rerun()
    else:
        st.info("🔐 Session Cookies: INACTIVE")

tab_download, tab_login = st.tabs(["📥 Media Downloader", "🔐 Smart Cookie Setup"])

# ----------------------------------------------------
# TAB 1: MEDIA DOWNLOADER
# ----------------------------------------------------
with tab_download:
    input_url = st.text_input("🔗 Enter Instagram URL (Post or Reel):", value="https://www.instagram.com/p/DbqaXCyFO1S/")

    if "analysis_data" not in st.session_state:
        st.session_state["analysis_data"] = None

    if st.button("🚀 Analyze & Download Media"):
        normalized_url = InstagramUrlNormalizer.normalize(input_url)
        parsed = InstagramUrlValidator.parse_url(normalized_url)
        
        if not parsed["is_valid"]:
            st.error("Invalid Instagram URL format.")
        else:
            with st.spinner("Analyzing media metadata with yt-dlp engine..."):
                try:
                    data = fetch_metadata(normalized_url)
                    st.session_state["analysis_data"] = {
                        "url": normalized_url,
                        "parsed": parsed,
                        "data": data,
                        "error": None
                    }
                except Exception as e:
                    err_str = str(e)
                    st.session_state["analysis_data"] = {
                        "url": normalized_url,
                        "parsed": parsed,
                        "data": None,
                        "error": err_str
                    }

    if st.session_state["analysis_data"]:
        item_info = st.session_state["analysis_data"]
        normalized_url = item_info["url"]
        parsed = item_info["parsed"]
        data = item_info["data"]
        err = item_info["error"]
        
        if err == "LOGIN_REQUIRED_PHOTO_POST":
            st.warning("🔒 **Instagram Cookie / Login Required for Photo Posts**")
            st.info("Instagram requires session cookies (`csrftoken`, `sessionid`, `ds_user_id`) to extract Photo Posts.")
            
            st.markdown("#### ⚡ 10-Second Smart Cookie Importer:")
            raw_input = st.text_area("Paste full Cookie Header or raw cookies from browser:", placeholder="Paste e.g.: mid=...; csrftoken=...; ds_user_id=...; sessionid=...", height=100)
            if st.button("🔑 Import Cookies & Retry"):
                netscape_txt, count = parse_smart_cookies(raw_input)
                if count > 0:
                    with open(COOKIES_FILE, "w") as f:
                        f.write(netscape_txt)
                    st.success(f"Successfully imported {count} cookies! Retrying analysis...")
                    st.rerun()
                else:
                    st.error("Could not parse cookies. Please paste your full cookie string.")
        elif err:
            st.error(f"⚠️ {err}")
        elif data:
            st.info(f"**Normalized URL**: `{normalized_url}` | **Type**: `{parsed['type']}`")
            
            entries = data.get("entries")
            if entries and len(entries) > 0:
                st.success(f"📸 Carousel Post Detected! ({len(entries)} items)")
                st.write(f"**Author**: @{data.get('uploader') or data.get('channel') or 'Instagram User'}")
                
                selected_item = st.selectbox("Select Carousel Item to Download:", [f"Item {i+1} of {len(entries)}" for i in range(len(entries))])
                item_idx = int(selected_item.split(" ")[1])
                target_entry = entries[item_idx - 1] if item_idx <= len(entries) else None
                
                # Show Preview Image/Video
                if target_entry:
                    thumb = target_entry.get("thumbnail") or target_entry.get("url")
                    if thumb:
                        st.image(thumb, width=350, caption=f"Carousel Item {item_idx} Preview")
                
                if st.button(f"📥 Download {selected_item}"):
                    with st.spinner(f"Downloading {selected_item}..."):
                        file_path = download_media(normalized_url, playlist_index=item_idx, output_prefix=f"Carousel_Item_{item_idx}", item_entry=target_entry)
                        if file_path:
                            st.success("Download Complete!")
                            filename = os.path.basename(file_path)
                            with open(file_path, "rb") as f:
                                st.download_button("💾 Save File to Device", f, file_name=filename)
                            if filename.endswith(".mp4"):
                                st.video(file_path)
                            else:
                                st.image(file_path)
            else:
                title = data.get("title", "Instagram Media")
                uploader = data.get("uploader") or data.get("channel") or "Instagram User"
                duration = data.get("duration", 0)
                thumbnail = data.get("thumbnail")
                
                st.subheader(f"🎬 {title}")
                st.write(f"**Author**: @{uploader} | **Duration**: {int(duration)}s")
                
                if thumbnail:
                    st.image(thumbnail, width=350)
                
                if st.button("📥 Download Optimal Quality Video (with Audio)"):
                    with st.spinner("Downloading and merging video + audio with FFmpeg..."):
                        file_path = download_media(normalized_url, format_selector="b/bestvideo+bestaudio/best", output_prefix="InstaFlow_Reel")
                        if file_path:
                            st.success("Download Complete! Merged Video + Audio Ready:")
                            filename = os.path.basename(file_path)
                            with open(file_path, "rb") as f:
                                st.download_button("💾 Save Video (.mp4)", f, file_name=filename)
                            st.video(file_path)

# ----------------------------------------------------
# TAB 2: SMART COOKIE SETUP
# ----------------------------------------------------
with tab_login:
    st.header("🔐 Universal Smart Cookie Importer")
    st.write("Importing cookies enables downloading **Photo Posts**, **Carousels**, **Stories**, and **Private Media**.")
    
    st.markdown("""
    ### 📖 How to copy cookies in 10 seconds (Chrome / Edge / Firefox):
    1. Open [Instagram.com](https://www.instagram.com) in your browser and log in.
    2. Press **F12** -> Go to **Network** tab -> Click on any `instagram.com` request.
    3. Under **Request Headers**, right-click **Cookie:** -> Copy value -> Paste below!
    """)
    
    smart_text = st.text_area("Paste Cookie Header / Raw Cookies / JSON Array:", placeholder="Paste e.g. Cookie: mid=...; csrftoken=...; ds_user_id=...; sessionid=...", height=140)
    if st.button("💾 Import Session Cookies"):
        netscape_txt, count = parse_smart_cookies(smart_text)
        if count > 0:
            with open(COOKIES_FILE, "w") as f:
                f.write(netscape_txt)
            st.success(f"Successfully imported {count} cookies to cookies.txt!")
            st.rerun()
        else:
            st.warning("Please paste your raw cookie header or string.")
            
    st.markdown("---")
    st.markdown("### 📄 Option 2: Upload cookies.txt File")
    uploaded_txt = st.file_uploader("Upload Netscape cookies.txt", type=["txt"])
    if uploaded_txt is not None:
        with open(COOKIES_FILE, "wb") as f:
            f.write(uploaded_txt.getbuffer())
        st.success("cookies.txt uploaded successfully!")
        st.rerun()
