# FUTURE IMPROVEMENTS — InstaFlow Roadmap

## 1. Pluggable `MediaExtractor` Architecture

Introduce a fallback chain:
1. `YtDlpExtractor`: High-fidelity default.
2. `InstagramGraphQLExtractor`: Native API parser for public Instagram JSON endpoints.
3. `WebviewExtractor`: Fallback DOM scraper for cookie-authenticated content.

## 2. In-App Carousel Media Gallery

- High-resolution carousel swipe preview before starting downloads.
- Selective download checkboxes for downloading specific images/videos from a 10-item carousel post.

## 3. Auto-Caption & Metadata Tagging

- Automatically saves Instagram captions to a `.txt` sidecar file or embeds in media EXIF metadata tags.
