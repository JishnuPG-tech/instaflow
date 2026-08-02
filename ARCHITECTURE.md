# ARCHITECTURE.md — InstaSave

**v3 — rewritten with current (Aug 2026) tooling and a validated reference point.** A Seal fork called **Seal Plus** already ships Instagram downloading via yt-dlp on-device (alongside YouTube/TikTok/Facebook/Twitter), running Kotlin 2.3, Android SDK 37, Jetpack Compose, yt-dlp `2025.12.08`, with aria2c multi-threaded downloads and a gradient dark theme. That's not a hypothetical — it's proof this exact approach ships and works. This document is written to get InstaSave to that same bar, scoped specifically to Instagram rather than 1000+ sites.

**This file is written for an AI coding agent to build from.** Every decision below is final unless explicitly marked "open question" — do not re-litigate settled decisions mid-build; implement them as specified. Where a library version is pinned, use that exact version unless it's been superseded by a newer stable release, in which case use the newer stable release and note the change.

---

## 1. High-Level Shape

```
┌─────────────────────────────────────────────────────────────┐
│                Android App (Kotlin 2.3, Compose)              │
│  Single Activity · MVVM · Hilt DI · Jetpack Navigation 3       │
│                                                                │
│  UI Layer (Compose, Material 3 Expressive)                    │
│   ├─ HomeScreen (paste/share intent)                          │
│   ├─ ResolutionPickerSheet                                    │
│   ├─ DownloadQueueScreen                                      │
│   ├─ HistoryScreen                                             │
│   ├─ LoginWebViewScreen (POST-MVP — see §4)                   │
│   └─ SettingsScreen                                            │
│                                                                │
│  Domain Layer                                                  │
│   ├─ ResolveLinkUseCase                                        │
│   ├─ FetchRenditionsUseCase                                    │
│   ├─ DownloadMediaUseCase                                      │
│   └─ ManageSessionUseCase (POST-MVP)                          │
│                                                                │
│  Data Layer                                                    │
│   ├─ ExtractionRepository  ──┐                                 │
│   ├─ DownloadRepository      │                                 │
│   ├─ SessionRepository       │  (POST-MVP, Jetpack Security)   │
│   └─ Room DB (history)       │                                 │
└───────────────────────────────┼───────────────────────────────┘
                                 │
                 ┌───────────────┴────────────────┐
                 │      Extraction Backend         │
                 │  (pluggable — see §3)           │
                 │  A) Embedded yt-dlp, on-device   │
                 │     (youtubedl-android + aria2c) │
                 │  B) Self-hosted micro-API         │
                 │     (Python: instaloader /        │
                 │     gallery-dl)                   │
                 └───────────────┬────────────────┘
                                 │
                          Instagram CDN
```

## 2. Tech Stack (pinned versions, current as of Aug 2026)

| Layer | Choice | Version / Notes |
|---|---|---|
| Language | Kotlin | **2.3.x** — matches what Seal Plus currently ships |
| compileSdk / targetSdk | Android SDK | **37** (Android 16) — this is the SDK that shipped Material 3 Expressive natively |
| minSdk | Android 8.0 | API 26 — keep broad compatibility; Storage Access Framework and scoped storage both have to be branch-handled below this vs. above |
| UI | Jetpack Compose + **Material 3 Expressive** | Opt-in via `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` where new components are used (see UI_UX_DESIGN.md) |
| Navigation | **Jetpack Navigation 3** (not the older Navigation-Compose 2.x patterns) | Type-safe destinations via Kotlin serialization |
| Architecture | MVVM + Clean-ish layering (UI/Domain/Data) | Same as before — appropriately scoped, not over-engineered |
| DI | Hilt | Standard; matches Seal Plus's own stack choice |
| Serialization | Kotlinx Serialization | For nav args and any local JSON, not Gson/Moshi |
| Async | Kotlin Coroutines + Flow | Structured concurrency for download progress streams |
| Local DB | Room | Download history, queue persistence |
| Networking | OkHttp + Retrofit, **generated from an OpenAPI spec** (§2a) | Unchanged from v2 — still the right call |
| Multi-threaded/resumable downloads | **aria2c**, bundled the same way Seal/Seal Plus bundle it | This is a concrete upgrade over plain OkHttp range-requests — aria2c is what the proven reference app actually uses for fast, resumable, multi-connection downloads. Wrap via a thin JNI/process-exec layer, same pattern `youtubedl-android` uses for yt-dlp itself. |
| Storage | **Storage Access Framework (SAF)** + `MediaStore` | Seal uses SAF specifically so users can pick *any* folder, not just the fixed `Pictures/InstaSave` MediaStore collection. Support both: MediaStore for the simple default path, SAF as a Settings-configurable custom folder — this matches Seal's actual behavior and is a real usability upgrade over MediaStore alone. |
| Secure storage | Jetpack Security `EncryptedSharedPreferences` / DataStore | POST-MVP — only needed once §4 (login) is built |
| Background work | `WorkManager` + `Foreground Service` | Unchanged — still correct |
| Media extraction | See §3 | |
| Build | Gradle Kotlin DSL (version catalogs, `libs.versions.toml`) | Modern standard |
| Lint/format | **ktlint** (`io.nlopez.compose.rules` for Compose-specific rules) + Android Lint | Matches the linting stack real Compose-catalog projects use in 2026 |
| Distribution | GitHub Releases + F-Droid, in-app update checker via GitHub API | Same as Seal; this is a validated distribution model, not a guess |

## 2a. API Contract — OpenAPI Spec + Generated Client

Unchanged from v2, restated because it's still the single most important structural decision for avoiding "half the features don't work":

1. `backend/openapi.yaml` is the single source of truth for `MediaInfo`, `MediaFormat`, `CarouselItem`, `ErrorResponse`.
2. FastAPI + Pydantic enforces the spec server-side (near-free with FastAPI's own OpenAPI generation).
3. Android: `openapi-generator-cli generate -i openapi.yaml -g kotlin -library jvm-retrofit2` produces typed Retrofit interfaces into `core/network/generated/`. **Never hand-edit generated files.**
4. CI regenerates the client on every spec change and fails the build on mismatch — client/server drift becomes a build failure, not a runtime bug.

## 3. The Core Decision: Extraction Strategy

Still true: Instagram's extractor breaks more often than YouTube's, and most reel/story requests hit `"rate-limit reached or login required"` without a session. **New information that changes the confidence level here**: Seal Plus proves the on-device, cookie-free, yt-dlp-based approach genuinely works in production for Instagram's public content today (alongside TikTok/Facebook/Twitter) — this isn't a risky bet, it's a validated pattern.

**v1 scope, unchanged from v2: public content only, no login/session.** See §4 for why and when to revisit.

### Option A — On-device, embedded (Seal Plus's actual proven model)
- Bundle `youtubedl-android` (wraps yt-dlp binary + Python runtime) directly in the APK, pinned to a recent yt-dlp release (`2025.12.08` or newer stable at build time).
- Cookie-free for v1, public URLs only.
- Pair with **aria2c** for the actual file transfer once yt-dlp resolves the direct media URL — this is what makes Seal-family apps feel fast, not just yt-dlp alone.
- **Pros**: 100% on-device, no server to maintain, directly matches the app this whole project is modeled on, works fully offline-of-backend.
- **Cons**: yt-dlp's Instagram extractor breaks periodically when IG changes its page structure; mitigate with `YoutubeDL.updateYoutubeDL()` on a schedule (Seal's own auto-update mechanism for exactly this reason).

### Option B — Self-hosted micro-API (still recommended primary, Option A as offline fallback)
- FastAPI + `instaloader`/`gallery-dl`, deployed to Hugging Face Spaces, same pattern as SmartWatt/MyHermes.
- Cookie-free, public content only for v1. Returns metadata + direct CDN URLs — **never proxies the actual file bytes** (bandwidth-cost reasoning unchanged from v2).
- **Pros**: fastest to patch when Instagram changes something, matches your working style.
- **Cons**: needs monitoring; still hits `LOGIN_REQUIRED` on gated content same as Option A.

### Option C — Pure client-side GraphQL/embed scraping
- Last-resort fallback only, same as v2. Not reliable enough to be primary.

### Recommended Runtime Strategy (unchanged logic, now higher-confidence)
```
resolveLink(url):
  1. Try Option B (FastAPI backend) — fastest to patch, primary path
  2. On backend failure/timeout → Option A (embedded yt-dlp, on-device, cookie-free)
  3. On both failure → Option C best-effort, else show "temporarily broken" state
```

### Power-user feature (adopt from Seal, low cost, high value): custom yt-dlp command templates
Seal lets advanced users define, save, and apply their own yt-dlp command-line templates for edge cases the built-in UI doesn't cover. This is worth including as an optional **Advanced Mode** setting in InstaSave (post-Phase-5, low priority) — it costs little (a text field + saved presets list) and gives power users an escape hatch when the built-in extraction strategy doesn't cover something. Not required for v1; flag as a nice-to-have in Settings.

## 4. Session / Login Handling (POST-MVP — unchanged trigger condition from v2)

Deliberately out of v1. Build only once real `LOGIN_REQUIRED` frequency (tracked from Phase 4 onward, per IMPLEMENTATION_PLAN.md) justifies the added security surface of storing a session.

- In-app `WebView` loads `instagram.com/accounts/login`; same mechanism yt-dlp's `--cookies-from-browser` and Seal's own cookie-import feature rely on. (Seal Plus's July 2026 changelog specifically shipped a "Cookie Fix" — this is a live, actively-maintained concern in this exact app category, not a one-time solved problem; budget for ongoing maintenance if you build this phase.)
- Extract cookies via `CookieManager`, store encrypted (DataStore + Jetpack Security), never store raw password.
- "Log out / clear session" setting required.

## 5. Storage & Downloads

- **Default path**: `MediaStore.Video`/`MediaStore.Images` targeting `Pictures/InstaSave`/`Movies/InstaSave` — zero-configuration default.
- **Configurable path (adopted from Seal)**: Storage Access Framework (SAF) folder picker in Settings, for users who want downloads in a specific custom folder (e.g., a synced cloud folder, a specific gallery album). This is a real, proven Seal feature, not scope creep — implement it as a Settings option, default still MediaStore.
- `WorkManager` for queued/retryable downloads + `Foreground Service` for live progress notification.
- **aria2c** for the actual transfer (see §3, Option A) — multi-connection, resumable by design, rather than hand-rolling HTTP range-request resume logic from scratch.

## 6. Data Model (Room)

```kotlin
DownloadEntity(
  id, sourceUrl, mediaType[IMAGE|VIDEO|CAROUSEL_ITEM],
  chosenQuality, filePath, thumbnailUrl,
  status[QUEUED|DOWNLOADING|DONE|FAILED],
  createdAt, completedAt, errorMessage
)
SessionEntity(   -- POST-MVP, see §4 — not created in v1
  igUsernameHint, cookieBlobEncrypted, capturedAt, isValid
)
```

## 7. Module Structure

```
app/
 ├─ core/
 │   ├─ network/
 │   │   └─ generated/     (Kotlin client generated from backend/openapi.yaml — never hand-edit, see §2a)
 │   ├─ extraction/        (strategy interfaces: BackendExtractor, YtDlpExtractor, GraphQLExtractor)
 │   ├─ storage/           (MediaStore + SAF helpers)
 │   ├─ download/          (aria2c wrapper, mirrors youtubedl-android's process-exec pattern)
 │   └─ security/          (POST-MVP — cookie encryption, added alongside §4)
 ├─ data/
 │   ├─ db/ (Room)
 │   └─ repository/
 ├─ domain/
 │   └─ usecase/
 ├─ ui/
 │   ├─ home/ resolver/ history/ settings/
 │   │   └─ login/         (POST-MVP screen, see §4 — not built in v1)
 │   └─ theme/ (Material 3 Expressive tokens — see UI_UX_DESIGN.md)
 └─ work/ (WorkManager workers, foreground download service)

backend/ (separate repo, mirrors SmartWatt's FastAPI structure)
 ├─ openapi.yaml          (source of truth for the API contract — see §2a)
 ├─ main.py (FastAPI)
 ├─ extractors/ (instaloader_client.py, gallery_dl_wrapper.py)
 ├─ session_store.py      (POST-MVP — encrypted IG session, added alongside §4)
 └─ Dockerfile (Hugging Face Spaces deploy, same pattern as MyHermes)
```

## 8. Failure-Mode Design

| Failure | Handling |
|---|---|
| IG changes page structure, extractor breaks | Backend-first strategy = patch Python backend in minutes; on-device yt-dlp fallback auto-updates its binary on a schedule (Seal's exact mitigation for the exact same problem) |
| Content requires login (v1 has no session) | Distinct `LOGIN_REQUIRED` error code from the OpenAPI `ErrorResponse` — surfaced as *"This content needs a logged-in session — not supported yet"* |
| Story already expired (24h) | Explicit "this story has expired" message, not a generic error |
| Rate-limited by IG | Exponential backoff + per-session request throttling |
| Large carousel/video timeout | aria2c's own multi-connection resumability handles this natively |
| App killed mid-download | WorkManager persists queue, resumes on next launch/connectivity |
| Client/server contract drift | Prevented structurally by §2a's generated-client CI check |

## 9. AI Agent Build Notes (read before writing code)

- **Do not substitute a different navigation library** for Jetpack Navigation 3, a different DI framework for Hilt, or a different serialization library for Kotlinx Serialization "because it's simpler" — these are fixed decisions, not suggestions, and substituting them will make this doc and IMPLEMENTATION_PLAN.md's phase instructions inconsistent with the actual code.
- **Do not implement HTTP range-request resume logic by hand** when aria2c is specified — that's solved, tested code in a library already used by the app this project models itself on; hand-rolling it is wasted, riskier effort.
- **Do not build the SessionEntity, security module, or login UI in v1** — these are explicitly gated behind Phase 4/7 trigger conditions in IMPLEMENTATION_PLAN.md. If a task seems to require login-gated content in v1, that's a signal to surface the `LOGIN_REQUIRED` error path correctly, not to build session handling early.
- **Always check the pinned yt-dlp version** against the latest stable release before building Phase 1 — Instagram-specific fixes land frequently; use whatever is current stable at build time, not a stale pin from this document.
