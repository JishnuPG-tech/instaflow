# Pre-Implementation Architecture Review & Discovery — Phase 1 (Extraction Engine, Public Content Only)

**Phase Scope**: Public Media Extraction Engine (`FastAPI /api/resolve` + `yt-dlp`), SSRF Security Isolation, On-Device Extraction Fallback Scaffold, and Extraction Test Suite.
**Engineering Standard**: Google / AOSP Open-Source Production Grade
**Governance**: Strict Single-Phase Isolation, Pre-Implementation Seal Comparison, Dependency Lock, Risk Assessment.

---

## 1. Project Documentation Summary (Phase 1 Baseline)

### PRD.md Requirements
- **FR1**: Parse and validate Instagram URLs (`/p/`, `/reel/`, `/reels/`, `/stories/`, `/tv/`, `instagram.com/share/...`).
- **FR2**: Extract available media renditions (resolution, bitrate, container) for a given link.
- **FR7**: Configurable extraction engine (self-hosted API primary + on-device fallback).
- **NFR (Resilience & Privacy)**: Graceful error degradation, no user tracking, single-user on-device posture.

### ARCHITECTURE.md Requirements
- **Section 3**: Hybrid extraction strategy — FastAPI `/api/resolve` primary backend service with `yt-dlp` python library for public content extraction.
- **Contract-Driven DTOs**: OpenAPI `API_SPEC.yaml` defines `MediaInfo`, `MediaFormat`, `CarouselItem`, `ResolveRequest`, `ErrorResponse`.
- **SSRF Hardening**: Strict URL domain allowlist (`instagram.com`, `instagr.am`), loopback IP blocking, private IP range blocking, credential stripping.

### SECURITY_CHECKLIST.md Requirements
- **SEC-01**: Validate input URL scheme (`https://` only).
- **SEC-02**: Domain verification via canonical hostname matching (`instagram.com`, `instagr.am`, `www.instagram.com`).
- **SEC-03**: Reject private IP ranges (`10.x.x.x`, `172.16.x.x-172.31.x.x`, `192.168.x.x`, `127.0.0.1`, `169.254.x.x`).
- **SEC-04**: Strip embedded credentials (`https://user:pass@domain`).

---

## 2. Mandatory Seal Reference Comparison Table

| Subsystem / Area | InstaSave Spec | Seal Implementation | Final Decision | Rationale for Deviation / Adaptation |
|---|---|---|---|---|
| **Extraction Engine** | FastAPI Backend (`yt-dlp` python package) + On-Device Fallback | Local `youtubedl-android` wrapper running directly in Kotlin Android process | **Hybrid Architecture**: Primary FastAPI backend + Android `yt-dlp` fallback wrapper scaffold | Keeps app resilient to IG layout breaks via backend remote updates, while preserving Seal's offline capabilities. |
| **Media Resolution Models** | OpenAPI `MediaInfo`, `MediaFormat`, `CarouselItem` DTOs | `Format`, `VideoInfo`, `FormatGroup` in Seal Kotlin model layer | **Adapt to OpenAPI DTOs**: Map `yt-dlp` extraction dictionary to `API_SPEC.yaml` schemas | Enforces strict API contract between Android client and backend service. |
| **URL Parsing & Validation** | Regex + Hostname allowlist + SSRF filter (`security.py`) | Simple string pattern matching in `Intent` handler | **InstaSave Strict SSRF Validation**: Regex match for `/p/`, `/reel/`, `/reels/`, `/stories/`, `/tv/` + IP block | Prevents SSRF vulnerabilities, malicious redirects, and local network port scanning. |
| **Carousel Handling** | Grid selection with multi-item `items: List[CarouselItem]` | Multiple format selection in single list | **InstaSave Carousel Item Grid**: Explicit item index, type (`photo`/`video`), format renditions | Matches Instagram UX structure where carousels mix photos and videos. |
| **Error Handling & Normalization** | Structured `ErrorResponse` DTO (`INSTAGRAM_UNREACHABLE`, `PRIVATE_ACCOUNT`, `INVALID_URL`) | Raw exception toast / error dialog | **Structured Error Codes**: Map `yt-dlp` exceptions to `API_SPEC.yaml` error codes | Enables Android client to display contextual recovery UI (e.g. prompt cookie login). |

---

## 3. Affected Modules & File Inventory

| Module / Component | Action | Target File Path | Purpose |
|---|---|---|---|
| **Backend Engine** | `MODIFY` | [main.py](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/backend/main.py) | Integrate `yt-dlp` extraction engine into `POST /api/resolve`. |
| **Backend Security** | `MODIFY` | [security.py](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/backend/security.py) | Comprehensive URL validation, shortcode extraction, and SSRF filter. |
| **Backend Extractor Helper** | `NEW` | `backend/extractor.py` | Encapsulate `yt-dlp` extraction, format parsing, and dictionary mapping. |
| **Backend PyTest Suite** | `MODIFY` | [test_health.py](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/backend/tests/test_health.py) | Expand test suite to include URL parsing, public Reel/Post resolution, and SSRF edge cases. |
| **Android Fallback Scaffold** | `NEW` | `app/src/main/java/com/instasave/app/core/extractor/OnDeviceExtractor.kt` | Interface contract for local fallback extraction on Android. |

---

## 4. Dependencies & Version Lock

- **Python Runtime**: `python 3.11+`
- **yt-dlp Python Library**: `yt-dlp>=2025.01.26` (Latest stable release with Instagram patch updates)
- **FastAPI**: `fastapi>=0.115.0`
- **Pydantic**: `pydantic>=2.10.0`
- **PyTest**: `pytest>=8.3.0`
- **HTTPX**: `httpx>=0.28.0`

---

## 5. Architectural Risks & Mitigation Strategies

| Risk ID | Risk Description | Severity | Mitigation Strategy |
|---|---|---|---|
| **RISK-01** | Instagram changes JSON API structure or blocks `yt-dlp` default user-agent. | **HIGH** | Configure custom browser User-Agents, request headers, and extract data via fallback `yt-dlp` extractors. |
| **RISK-02** | `yt-dlp` blocking thread during metadata extraction under heavy request load. | **MEDIUM** | Wrap `yt-dlp.YoutubeDL().extract_info` in `asyncio.to_thread` executor to prevent blocking FastAPI async event loop. |
| **RISK-03** | Malicious shortcode or URL redirect leading to SSRF or internal network access. | **HIGH** | Enforce hostname domain whitelist (`instagram.com`, `instagr.am`), disallow IP literals, strip credentials, and block HTTP redirect following to external hosts. |
| **RISK-04** | Large carousel posts containing 20+ high-res renditions overwhelming response payload size. | **LOW** | Filter and sort renditions by resolution/bitrate before returning normalized DTO response. |

---

## 6. Assumptions & Constraints

1. **Public Content Focus**: Phase 1 handles strictly public Posts, Reels, and Carousels that do not require Instagram session cookies (`sessionid`).
2. **No Rate Limit Overuse**: Extraction is single-request per URL triggered by explicit user action (no automated batch scraping).
3. **OpenAPI Schema Compliance**: Response JSON payloads must strictly validate against `API_SPEC.yaml`.
