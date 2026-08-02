# IMPLEMENTATION_PLAN.md — InstaSave

**v3 — updated with current (Aug 2026) library versions, aria2c/SAF adoption from the validated Seal Plus reference, and Material 3 Expressive UI work folded into the polish phase.** See ARCHITECTURE.md and UI_UX_DESIGN.md for the full reasoning; this file is the literal, ordered task list an AI agent executes.

**How to use this file if you are the AI agent building this:** Work exactly one phase at a time, in order. Do not start a phase until the previous phase's Exit Test passes with actual evidence (build output, screenshot, live request/response) — not your own claim that it should work. Follow SDLC_BUILD_PLAN.md's six-stage cycle for each phase below.

---

## v1 — Public Content Only

## Phase 0 — Environment & Repo Setup (0.5–1 day)
- [ ] Android Studio (latest stable), **Kotlin 2.3.x**, **compileSdk/targetSdk 37**, minSdk 26
- [ ] New repo `insta-save-android`, Gradle Kotlin DSL, version catalog (`libs.versions.toml`) — pin Compose BOM, Hilt, Navigation 3, Kotlinx Serialization, Room to their current stable releases at build time
- [ ] Second repo (or `/backend` folder) `insta-save-backend` — FastAPI, same skeleton pattern as SmartWatt
- [ ] Hugging Face Space created for backend (Docker SDK)
- [ ] Draft `backend/openapi.yaml` skeleton: `MediaInfo`, `MediaFormat`, `CarouselItem`, `ErrorResponse` (per ARCHITECTURE.md §2a)
- [ ] Wire up `openapi-generator-cli` (or Gradle plugin) pointed at the spec, confirm it generates Kotlin classes even before real endpoints exist
- [ ] `ktlint` + `io.nlopez.compose.rules` configured; confirm it runs and passes on the empty scaffold
- **Exit test**: empty Compose app builds and installs on a device/emulator; empty FastAPI `/health` responds on HF Spaces; client codegen produces Kotlin classes with zero errors; `ktlint` passes clean.

## Phase 1 — Extraction Engine, Public Content Only (2–3 days)
- [ ] Backend: `instaloader`/`gallery-dl` extraction for public posts, reels, carousels → matches `MediaInfo` schema
- [ ] Backend: explicit `LOGIN_REQUIRED` typed error path (not a generic 500) when IG returns a login wall
- [ ] Regenerate Android client after any spec change; confirm it still compiles
- [ ] On-device fallback: `youtubedl-android` wired up standalone, pinned to current stable yt-dlp (`2025.12.08` or newer — **check for a newer stable release before pinning**), tested cookie-free against the same public URLs
- **Exit test**: `curl` your backend with 3 public URL types (post, reel, carousel) returns correct renditions + working CDN URLs; a known login-gated URL returns `LOGIN_REQUIRED`, not a crash/timeout.

## Phase 2 — Minimal Android App: Paste → Resolve → Download (3–4 days)
- [ ] HomeScreen: paste field + clipboard-detect, styled per UI_UX_DESIGN.md §4.1
- [ ] `ResolveLinkUseCase` via the **generated** Retrofit client
- [ ] Single-quality ("best") download via WorkManager, fetching directly from the CDN URL the backend returned
- [ ] **Use aria2c for the file transfer**, not a hand-rolled OkHttp download — wrap it the same way `youtubedl-android` wraps yt-dlp (process-exec/JNI, see ARCHITECTURE.md §5)
- [ ] Write via MediaStore (default path) per ARCHITECTURE.md §5
- [ ] Basic progress notification; `LOGIN_REQUIRED` surfaced with the exact copy from UI_UX_DESIGN.md §4.8, not a generic toast
- **Exit test**: paste a public reel URL, tap download, file appears in `Movies/InstaSave/`, playable; pasting a login-gated URL shows the specific message, not a crash.

## Phase 3 — Share-Sheet + Resolution Picker (2–3 days)
- [ ] `ACTION_SEND` intent-filter for Instagram share
- [ ] `ResolutionPickerSheet` using **M3 Expressive connected Button Group** for quality selection (UI_UX_DESIGN.md §4.2/§5) — not a hand-rolled row of buttons
- [ ] Carousel grid multi-select (§4.3)
- **Exit test**: Share from Instagram → InstaSave, quality sheet appears using the actual Expressive button-group component, correct file downloads.

## Phase 4 — Download Manager, History, Resilience (3–4 days)
- [ ] `DownloadQueueScreen` with the **Aperture Ring** custom component (UI_UX_DESIGN.md §4.4/§5) — this is the one hand-built component in the whole UI; everything else in this phase uses stock Expressive components
- [ ] `HistoryScreen`, Room-backed, thumbnails, re-open/delete
- [ ] aria2c-backed resumable downloads (native to the library — don't reimplement range-request logic)
- [ ] Exponential backoff + rate-limit-aware retry
- [ ] Fallback chain wired: Backend → on-device yt-dlp → GraphQL best-effort
- [ ] **Track `LOGIN_REQUIRED` frequency** (even a simple local counter) — this is the evidence for whether Phase 7/8 (post-MVP) get built at all
- **Exit test**: kill app mid-download, relaunch, resumes via aria2c; force backend failure, confirm on-device fallback engages.

## Phase 5 — UI Polish, Settings, Material 3 Expressive Pass (2–3 days)
- [ ] Full theme per UI_UX_DESIGN.md: true-black background, token colors, **Expressive shape scale and type scale** (not classic M3 defaults), `LoadingIndicator`/`ContainedLoadingIndicator` for all short-wait states
- [ ] SettingsScreen: download location (MediaStore default + **SAF folder picker** per ARCHITECTURE.md §5), default quality, Wi-Fi-only, concurrency limit, optional **custom yt-dlp template** advanced setting (adopted from Seal, low priority)
- [ ] Empty/error states with exact copy from UI_UX_DESIGN.md §4.8
- [ ] App icon, splash screen
- [ ] Motion pass: confirm every screen transition and state change actually animates (per UI_UX_DESIGN.md §3's explicit rule — a static screen at this point is a bug, not a finished screen)
- **Exit test**: side-by-side screenshot comparison against UI_UX_DESIGN.md wireframes for every screen; SAF folder picker actually changes the save location; no raw stack traces visible anywhere.

## Phase 6 — Release Hardening & Distribution (2–3 days)
- [ ] ProGuard/R8 rules, release signing config
- [ ] In-app update checker via GitHub Releases API (Seal's exact mechanism)
- [ ] `arm64-v8a` + `universal` APK variants
- [ ] README, CONTRIBUTING, LICENSE (GPLv3, if open-sourcing, matching the Seal-family convention)
- [ ] Optional: F-Droid metadata
- **Exit test**: signed release APK installs clean on a fresh device, self-update check works, no debug logging in release build.

**v1 ships here.**

---

## Post-MVP (gated on Phase 4's `LOGIN_REQUIRED` tracking data)

## Phase 7 — Login / Session Management (2–3 days)
Full spec in ARCHITECTURE.md §4. Note the "Cookie Fix" reality: Seal Plus shipped an actual cookie-handling fix as recently as July 2026 — this is an actively-maintained concern in this app category, budget ongoing maintenance time if this phase is built, not a one-time task.
- [ ] `LoginWebViewScreen`, cookie extraction, encrypted storage
- [ ] "Log out" flow; update `openapi.yaml` with session endpoints, regenerate client
- **Exit test**: log in, then successfully resolve a reel that previously returned `LOGIN_REQUIRED`.

## Phase 8 — Stories & Expiry Handling (1–2 days)
Depends on Phase 7 (stories are almost always login-gated even on public accounts).
- [ ] Story parsing + extraction (requires Phase 7 session)
- [ ] Expiry detection, clear error message if 24h window passed
- **Exit test**: save an active story; expired-story link shows a clear error, not a crash.

---

## Ongoing (post-launch)
- Monitor yt-dlp/instaloader/gallery-dl upstream for Instagram breakage — subscribe to GitHub issues/releases
- Check for newer yt-dlp stable releases regularly; Instagram-specific fixes land often
- Keep `openapi.yaml` as the first thing updated on any backend response shape change; regenerate the Android client immediately
- Track backend uptime on Hugging Face Spaces
- Watch the `LOGIN_REQUIRED` counter — real signal for Phase 7/8

## Suggested Total Timeline
~2.5–3.5 weeks solo, part-time, for v1 (Phases 0–6). Phase 1 remains the highest-risk, do-it-first phase; Phase 5's Expressive UI pass is more substantial than a typical "polish" phase because motion/component correctness is being verified, not just colors.
