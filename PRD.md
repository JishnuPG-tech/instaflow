# PRD — InstaSave (Instagram Media Downloader for Android)

**Author:** Jishnu | **Status:** Draft v1.1 | **Date:** August 2026
**Model:** Local-first, open-source-style Android APK — architecturally similar to Seal (yt-dlp GUI), but purpose-built for Instagram. **Validated reference point:** Seal Plus (a Seal fork) already ships Instagram downloading via yt-dlp on Kotlin 2.3/Compose/SDK 37 with aria2c downloads — this proves the model, not just inspires it. See ARCHITECTURE.md for the full current tech stack.

---

## 1. Problem Statement

Instagram gives no first-party way to save a story, reel, post, or carousel to a device. Dozens of web tools already do this (SnapInsta, SaveInsta, InstaDP-style sites), but they are:
- ad-infested, slow, and often inject malicious redirects
- rate-limited or dead within weeks because they scrape without any resilience layer
- web-only — no share-sheet integration, no download queue, no offline history

You want a **native Android APK** — install once, share a link from Instagram → app downloads it in the resolution/quality you pick — with the polish and reliability of Seal, but scoped to Instagram instead of yt-dlp's 1800 generic sites.

## 2. Goals

| Goal | Success metric |
|---|---|
| One-tap download from share sheet | Share → InstaSave → file saved, ≤3 taps |
| Support all Instagram media types | Posts (image/video/carousel), Reels, Stories, IGTV/Video, Highlights |
| Resolution/quality choice | User picks from available renditions before download, like Seal's format picker |
| Resilient to IG's anti-scraping changes | Extraction layer swappable without an app store update (remote config / pluggable backend) |
| Private, local-first | No user data leaves the device except the request to Instagram/backend itself |
| Production-grade Android app | Material 3, Kotlin, Compose, handles Android 10+ scoped storage correctly |

### Non-goals (v1)
- Bulk-ripping an entire profile's history (this crosses from "personal download tool" into scraping-at-scale territory and is the #1 reason such tools get DMCA'd / cut off — explicitly out of scope)
- Downloading content behind a private account you don't have access to
- iOS app (Android only, per your stated scope)
- Built-in video editing/re-upload features

## 3. Target User

You, primarily — and secondarily, the same audience as Seal: technically comfortable Android users in Kerala/India who want a clean, ad-free, single-purpose tool. This is a **portfolio-grade production app**, not just a personal script, so treat store-readiness (Play Store policy, or F-Droid/GitHub-release distribution like Seal does) as a real constraint.

## 4. Core User Flows

### Flow A — Share Sheet (primary, like Seal)
1. User is in Instagram app, taps Share → Share to → **InstaSave**
2. App resolves the link in the background, shows a bottom sheet: thumbnail, caption snippet, available qualities (e.g., 1080p/720p/480p for video, original res for images), audio-only option for Reels
3. User taps a quality → download starts with a persistent notification (progress %, speed, cancel)
4. Saved to `Pictures/InstaSave/` or `Movies/InstaSave/` (scoped storage via MediaStore API)

### Flow B — In-app paste
1. Open app → paste/auto-detect clipboard Instagram link → same resolution picker → download

### Flow C — Batch (carousel post)
1. Carousel post link → app shows all N slides as a grid with checkboxes → "Download selected" or "Download all"

### Flow D — Story / Highlight
1. Story link (or username, if you add "latest stories from user X") → list of active story items with expiry-aware handling (stories expire in 24h — app should warn if a story link may already be dead)

## 5. Functional Requirements

| ID | Requirement |
|---|---|
| FR1 | Parse and validate Instagram URLs: `/p/`, `/reel/`, `/reels/`, `/stories/`, `/tv/`, share-shortlink (`instagram.com/share/...`) |
| FR2 | Extract available media renditions (resolution, bitrate, container) for a given link |
| FR3 | Download selected rendition with pause/resume, retry-with-backoff on failure |
| FR4 | Persist download history (room DB) with thumbnail, source URL, date, file path |
| FR5 | Handle login-gated content gracefully: prompt user to import a session cookie via in-app WebView login (see Architecture §3) |
| FR6 | Respect Android 10+ scoped storage — write only via MediaStore, no raw `/sdcard` paths |
| FR7 | Configurable extraction backend (self-hosted API vs on-device) — swappable at runtime via remote config, since IG breaks scrapers every few weeks |
| FR8 | Settings: download location, video quality default, Wi-Fi-only toggle, concurrent download limit |
| FR9 | In-app update checker (GitHub Releases API, like Seal) since this won't be on Play Store initially |

## 6. Non-Functional Requirements

- **Resilience**: extraction failures must degrade gracefully with a clear "Instagram changed something, try updating the app / re-login" message rather than a raw stack trace — because this *will* break periodically. Budget for it structurally, not as an afterthought.
- **Privacy**: no analytics/telemetry by default (or fully opt-in + disclosed, matching FOSS norms). No ads.
- **Performance**: cold start < 1.5s, download start latency < 3s for typical link resolution.
- **Offline-safe**: queued downloads resume after connectivity returns.
- **Legal posture**: personal-use, on-device tool the user runs against their own logged-in session — same posture as Seal/yt-dlp/gallery-dl for Instagram. No server-side mass scraping component. See §8.

## 7. Feature Comparison Target (parity with Seal, mapped to Instagram)

| Seal (YouTube/yt-dlp) | InstaSave (Instagram) |
|---|---|
| Paste any yt-dlp-supported URL | Paste/share any Instagram post/reel/story/IGTV URL |
| Format/quality picker | Resolution picker per rendition IG actually serves |
| Playlist → download all | Carousel post → download all slides |
| Cookies-from-browser import | In-app WebView login → extract & store session cookie securely |
| Custom yt-dlp command templates (power user) | Optional "advanced mode" exposing raw extraction backend choice |
| Embedded aria2c, resumable downloads | OkHttp/DownloadManager with range-request resume |
| Material You, single-activity Compose | Same |
| GitHub-releases self-update, F-Droid-style distribution | Same |

## 8. Legal & Platform-Policy Notes (read before building)

This is worth being explicit about, because it shapes real architecture decisions, not because it's a lecture:

- Instagram's Terms of Use prohibit scraping/automated data collection; a personal download tool for content you can already see is the same legal gray zone Seal/yt-dlp/gallery-dl/4K Stogram have operated in for years — courts and platforms have mostly gone after **mass commercial scraping**, not single-user personal-download tools. Keep the app single-user, on-device, no server-side content mirroring, and you stay in the same lane as those projects.
- Google Play's policy on apps that download from third-party platforms is inconsistent and many such apps get rejected/pulled — this is exactly why Seal ships via **GitHub Releases + F-Droid**, not Play Store. Plan the same distribution model for v1.
- Don't build the "download entire profile" / "bulk story archiver" feature — that's the line between "personal save tool" and "scraper," and it's also the feature most likely to trip Instagram's abuse detection and get the extraction method blocked faster for everyone using it.
- This section is information, not legal advice — you're not a lawyer and neither am I; if you ever monetize or distribute this beyond personal/portfolio use, worth a real legal read before doing so.

## 9. Milestones (high-level — detailed in IMPLEMENTATION_PLAN.md)

**Scope note (v2):** v1 targets public content only — no login/session. Stories are almost always login-gated even on public accounts, so they move to a post-MVP milestone alongside login. The API contract between app and backend is defined via an OpenAPI spec with a generated Kotlin client, not hand-written — see ARCHITECTURE.md §2a for why.

1. **M1** — OpenAPI contract drafted + client codegen wired up; core extraction engine working from CLI/test harness (posts + reels + carousels, public content only)
2. **M2** — Minimal Android app: paste link → resolve via generated client → download single video/image
3. **M3** — Share-sheet intent, resolution picker, carousel support
4. **M4** — Download manager, history, resilience (fallback chain, resumable downloads)
5. **M5** — Polish: Material 3 UI (per UI_UX_DESIGN.md), settings, self-update
6. **M6** — Hardening: retry/backoff, release build, signing, GitHub Releases pipeline — **v1 ships here**
7. **M7 (post-MVP, gated on real usage data)** — Login/session via WebView cookie import, then stories support
