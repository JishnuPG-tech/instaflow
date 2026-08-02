import ipaddress
import urllib.parse
from fastapi import HTTPException

ALLOWED_HOSTS = {"instagram.com", "www.instagram.com"}


def validate_instagram_url(url_str: str) -> str:
    """
    Validates that the input URL is strictly a public Instagram URL.
    Blocks SSRF vectors including localhost, internal IP ranges, non-HTTP schemes,
    and embedded credentials per SECURITY_CHECKLIST.md §2.
    """
    if not url_str or not isinstance(url_str, str):
        raise HTTPException(status_code=400, detail="INVALID_URL")

    try:
        parsed = urllib.parse.urlparse(url_str.strip())
    except Exception:
        raise HTTPException(status_code=400, detail="INVALID_URL")

    # Reject non-http/https schemes
    if parsed.scheme.lower() not in ("http", "https"):
        raise HTTPException(status_code=400, detail="INVALID_URL")

    # Reject URLs with embedded credentials (e.g. user:pass@host)
    if parsed.username or parsed.password:
        raise HTTPException(status_code=400, detail="INVALID_URL")

    hostname = parsed.hostname
    if not hostname:
        raise HTTPException(status_code=400, detail="INVALID_URL")

    hostname = hostname.lower()

    # Reject localhost / loopback strings explicitly
    if hostname in ("localhost", "0.0.0.0", "127.0.0.1", "::1"):
        raise HTTPException(status_code=400, detail="INVALID_URL")

    # Check for raw IP address to block private/link-local ranges (SSRF)
    try:
        ip = ipaddress.ip_address(hostname)
        if ip.is_private or ip.is_loopback or ip.is_link_local or ip.is_reserved:
            raise HTTPException(status_code=400, detail="INVALID_URL")
    except ValueError:
        # Hostname is a domain name, not a raw IP address
        pass

    # Enforce strict domain allowlist
    if hostname not in ALLOWED_HOSTS:
        raise HTTPException(status_code=400, detail="INVALID_URL")

    # Enforce valid Instagram path patterns
    path = parsed.path.lower()
    valid_paths = (
        "/p/",
        "/reel/",
        "/reels/",
        "/tv/",
        "/share/",
        "/p",
        "/reel",
        "/reels",
        "/tv",
    )
    if not any(path.startswith(prefix) for prefix in valid_paths):
        raise HTTPException(status_code=400, detail="INVALID_URL")

    return url_str
