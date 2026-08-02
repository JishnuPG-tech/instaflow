# TESTING_STRATEGY.md — InstaSave

**Purpose:** consolidates the testing requirements scattered across ARCHITECTURE.md, IMPLEMENTATION_PLAN.md's exit tests, and SDLC_BUILD_PLAN.md's Stage 4 into one concrete, checkable test plan. This is what "production ready" actually has to satisfy — not a vague quality bar, but this specific list passing.

**For the AI agent:** a phase is not done if its relevant rows in this document aren't passing with real evidence. This file is a companion to SDLC_BUILD_PLAN.md's Stage 4 (Test), not a replacement for it — use both.

---

## 1. Test Pyramid for This Project

| Layer | What it covers | Tooling |
|---|---|---|
| Unit tests | Use-cases, repositories, URL parsing/validation, error-mapping logic | JUnit5 + MockK (Kotlin), pytest (backend) |
| Integration tests | Backend endpoints against real extraction libraries | pytest + a small set of known-stable public test URLs |
| Instrumented/UI tests | Compose screen behavior, navigation, state transitions | Compose UI testing (`createComposeRule`), Espresso where needed |
| Manual device tests | Real-world behavior no automated test reliably covers | Physical device, checklist below |
| Live extraction tests | Instagram's actual current behavior | Manual `curl`/Postman against the real backend, re-run per phase, not one-and-done |

**Why live extraction tests are their own category**: Instagram's behavior can't be mocked reliably and changes over time — a passing unit test with mocked yt-dlp output tells you the code handles that shape of data correctly, not that Instagram still returns that shape today. Both are required; neither substitutes for the other.

---

## 2. Backend Test Matrix

| Test | Expected Result |
|---|---|
| `POST /api/resolve` — public post URL | 200, valid `MediaInfo`, `type: post` |
| `POST /api/resolve` — public reel URL | 200, valid `MediaInfo`, `type: reel`, formats include at least 720p/1080p |
| `POST /api/resolve` — public carousel URL | 200, `items[]` populated, each item has its own `formats[]` |
| `POST /api/resolve` — non-Instagram URL | 400, `INVALID_URL` |
| `POST /api/resolve` — malformed/non-URL string | 400, `INVALID_URL` (not a 500) |
| `POST /api/resolve` — known login-gated URL | 422, `LOGIN_REQUIRED` — this is a pass, not a failure |
| `POST /api/resolve` — deleted/nonexistent post | 404, `NOT_FOUND` |
| `POST /api/resolve` — same URL requested rapidly (rate-limit trigger) | 429, `RATE_LIMITED`, `Retry-After` header present |
| SSRF probe — `http://localhost/...`, `http://169.254.169.254/...`, `file://...` | Rejected before reaching the extraction layer — see SECURITY_CHECKLIST.md §2 |
| Temp file check | No orphaned files in temp dir 60s after a resolve/download completes |
| Backend restart mid-extraction | No crash; in-flight request either completes or fails cleanly, no zombie process |

## 3. Android Unit Test Matrix

| Component | Test cases |
|---|---|
| URL validator/parser | Accepts `/p/`, `/reel/`, `/reels/`, share-shortlinks; rejects non-Instagram domains, empty strings, malformed URLs |
| `ResolveLinkUseCase` | Maps each `ErrorResponse.code` to the correct domain-level error type; maps successful `MediaInfo` correctly for post/reel/carousel |
| `DownloadMediaUseCase` | Correct MediaStore/SAF path selected based on Settings; correct retry/backoff sequence on simulated failure |
| Room DAOs | Insert/query/delete on `DownloadEntity`; ordering and filtering queries return expected rows |
| Extraction fallback chain | Given a simulated backend failure, on-device yt-dlp path is invoked; given both failing, GraphQL best-effort or the "temporarily broken" state is shown |

## 4. Instrumented/UI Test Matrix

| Screen/flow | Test cases |
|---|---|
| HomeScreen | Paste triggers resolve; clipboard-detect affordance appears only when a valid IG URL is on the clipboard; loading state shows `ContainedLoadingIndicator`, not a blank screen |
| ResolutionPickerSheet | All formats from a resolve response render as selectable options; selecting one enables Download; carousel response shows the grid variant instead |
| DownloadQueueScreen | Aperture Ring reflects real progress percentage; cancel actually stops the download and cleans up; failed items show retry |
| HistoryScreen | Scrolls smoothly with 100+ synthetic entries; search/filter returns correct subset; long-press context menu actions work |
| SettingsScreen | SAF folder picker actually changes where files are written; toggles persist across app restart |
| Share-sheet flow | Simulated `ACTION_SEND` intent with an Instagram URL correctly pre-fills and triggers resolve |
| Error states | Each `ErrorResponse.code` renders the exact copy specified in UI_UX_DESIGN.md §4.8 — not a generic fallback message |
| Dark theme only | No light-theme code path exists; confirm `isSystemInDarkTheme()` is not referenced anywhere in the theme setup |

## 5. Manual Device Test Checklist (run before any release build, every time)

- [ ] Paste a public post URL → resolve → preview sheet appears → download → file playable in gallery
- [ ] Paste a public reel URL → same flow → correct quality options shown
- [ ] Paste a public carousel URL → grid selection → multi-download works, each item lands correctly
- [ ] Share from the actual Instagram app → InstaSave opens/handles it correctly
- [ ] Kill the app mid-download → relaunch → download resumes via aria2c
- [ ] Turn off Wi-Fi mid-download with "Wi-Fi only" enabled → download pauses, doesn't fail permanently
- [ ] Force a backend outage (stop the HF Space) → on-device fallback engages, user sees appropriate messaging, not a crash
- [ ] Try a known login-gated URL → correct `LOGIN_REQUIRED` message, no crash
- [ ] Try an expired story-adjacent link if applicable to current phase → correct expiry message
- [ ] Rotate device during an active download → state survives (WorkManager persistence)
- [ ] Test on at least one Android 8–10 device/emulator (scoped storage transition boundary) and one Android 14+ device
- [ ] TalkBack on: navigate the whole app, confirm every interactive element has a sensible spoken label
- [ ] System font size at 200% → no truncated or overlapping text on Home, Resolution Picker, History
- [ ] "Reduce motion" system setting on → Aperture Ring and Expressive transitions degrade to simple indicators, nothing breaks
- [ ] Fresh install → onboarding/empty states look correct with zero history
- [ ] Signed release build → install on a clean device with no debug tooling attached, confirm no debug logging appears

## 6. Regression Discipline

- Every time a bug is found and fixed, add the specific case that caught it to the relevant matrix above — this file should grow over the project's life, not stay static.
- Before any release build (Phase 6 exit test), run the full Manual Device Test Checklist fresh — not from memory of a previous pass.
- If Instagram's behavior changes and breaks extraction, add the new failure signature to the Backend Test Matrix once a fix ships, so the next regression is caught automatically.
