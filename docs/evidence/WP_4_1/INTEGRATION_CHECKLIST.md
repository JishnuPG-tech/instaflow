# WP 4.1 — Carousel Pipeline Wiring: Integration Verification Checklist

**Work Package**: WP 4.1
**Title**: Carousel Pipeline Wiring into DownloadUtil / DownloaderV2
**Date Written**: 2026-08-03
**Status**: 🟡 UNIT TESTS PASSING — ON-DEVICE VERIFICATION PENDING

---

> [!IMPORTANT]
> This checklist must be completed on a real Android device or emulator (API 26+) before Gate C can be honestly closed. Unit tests alone do not satisfy these criteria.

---

## What Was Built in WP 4.1

| File | Purpose |
|---|---|
| [`InstagramCarouselRouter.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/InstagramCarouselRouter.kt) | Single entry point: routes a yt-dlp JSON payload to single-item or carousel pipeline |
| [`InstagramCarouselDetector.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/InstagramCarouselDetector.kt) | Extended with `extractItemJsonList()` for parsing carousel item JSON array |
| [`InstagramCarouselRouterTest.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/test/java/com/junkfood/seal/util/InstagramCarouselRouterTest.kt) | 9 unit tests covering shortcode extraction and item list extraction |

## What Still Needs Wiring (next WPs)

| Item | Location to Modify | Notes |
|---|---|---|
| Call `InstagramCarouselRouter.route()` from `Downloader` | `Downloader.kt` — after `fetchVideoInfo` returns | After yt-dlp JSON is available |
| Persist carousel items to Room database | `DatabaseUtil.kt` + `DownloadedVideoInfo` | One row per item |
| Wire `InstagramCarouselProgressTracker` | Worker / `DownloaderV2Impl` progress callback | Per-task progress update |
| Wire `InstagramCarouselNotificationHandler` | `NotificationUtil.kt` / `DownloaderV2Impl` | Start / progress / success content |

---

## On-Device Verification Steps

### Step 1: Single-item routing still works (regression)
- [ ] Open InstaFlow
- [ ] Paste a single Instagram image URL
- [ ] Confirm: `InstagramCarouselRouter.route()` returns `RoutingResult.SingleItem`
- [ ] Confirm: Download proceeds via normal Seal pipeline
- [ ] Confirm: File saved successfully
- **Evidence required**: Screenshot of completed download + file in storage

### Step 2: Carousel routing detects all items
- [ ] Paste a carousel Instagram URL (2–5 items)
- [ ] Confirm: `InstagramCarouselDetector.isCarousel()` returns `true`
- [ ] Confirm: `extractItemJsonList()` returns the correct item count
- [ ] Confirm: `InstagramCarouselOrchestrator.orchestrate()` produces N `InstagramMediaItem` objects
- **Evidence required**: Logcat output showing item count + shortcode

### Step 3: Carousel tasks enqueued to DownloaderV2
- [ ] Confirm: One `Task` per carousel item appears in `DownloaderV2.getTaskStateMap()`
- [ ] Confirm: Each task has the correct direct download URL (not the carousel parent URL)
- **Evidence required**: Logcat output showing N task enqueue calls

### Step 4: Files downloaded with correct names
- [ ] Confirm: Files saved with position-indexed names (e.g. `AbCdEfGhIjK_1_of_5.jpg`, `AbCdEfGhIjK_2_of_5.mp4`)
- **Evidence required**: Screenshot of files in device storage / file manager

### Step 5: Notifications appear correctly
- [ ] Confirm: Notification shows "Downloading carousel from @username"
- [ ] Confirm: Progress notification updates per-item ("Item 2 of 5 downloaded")
- [ ] Confirm: Final notification shows "All 5 items saved"
- **Evidence required**: Screenshots of notification shade during and after download

### Step 6: History entries created
- [ ] Confirm: Each carousel item appears as a separate entry in the History screen
- [ ] Confirm: Each entry shows the correct thumbnail, filename, and author
- **Evidence required**: Screenshot of History screen

### Step 7: Downloaded media opens successfully
- [ ] Tap a downloaded item in History
- [ ] Confirm: Image opens in device image viewer
- [ ] Tap a downloaded video item in History
- [ ] Confirm: Video plays in device video player
- **Evidence required**: Screenshot of opened media

### Step 8: Mixed carousel (images + videos)
- [ ] Use a carousel URL with at least one image AND one video
- [ ] Confirm: Image items saved as `.jpg`, video items saved as `.mp4`
- [ ] Confirm: All items play/display correctly
- **Evidence required**: Screenshot of both file types in storage

### Step 9: Partial failure and retry
- [ ] Simulate a network failure during a carousel download (airplane mode mid-download)
- [ ] Confirm: Completed items are not re-downloaded
- [ ] Confirm: Failed items can be retried individually
- [ ] Confirm: `InstagramCarouselErrorRecovery` classification appears in error notification
- **Evidence required**: Screenshot of partial failure notification + retry behavior

---

## Gate C Close Criteria

Gate C will be formally closed when ALL of the following are true:

- [ ] Steps 1–9 above completed and evidence screenshots collected
- [ ] No regression in existing Seal download functionality
- [ ] `assembleRelease` build succeeds cleanly
- [ ] `ktlint` and `detekt` pass with no new errors

---

## Known Limitations at WP 4.1

1. **`Downloader.kt` not yet modified** — `InstagramCarouselRouter.route()` is not yet called from any production code path. The router exists but has no caller. The next wiring WP must add the call site.
2. **UI has no carousel awareness** — The preview screen, download options sheet, and history screen are unmodified from upstream Seal. Carousel UI is Phase 4 WP 4.3–4.4.
3. **`route()` is not unit-tested end-to-end** — The method requires `DownloaderV2` (Compose/Koin/Android). It is integration-test-only.
