# WP 4.1 BUILD & TEST REPORT

- **Work Package ID**: `WP 4.1`
- **Title**: Carousel Pipeline Wiring — Router + Detector Extension
- **Build Status**: 🟢 SUCCESSFUL (8s)
- **Test Status**: 🟢 PASS (9/9)
- **Files Added/Modified**:
  - [`InstagramCarouselRouter.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/InstagramCarouselRouter.kt) — NEW
  - [`InstagramCarouselDetector.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/InstagramCarouselDetector.kt) — MODIFIED (added `extractItemJsonList()`)
  - [`InstagramCarouselRouterTest.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/test/java/com/junkfood/seal/util/InstagramCarouselRouterTest.kt) — NEW

## Tests (pure-function surface)
1. `testExtractShortcodeFromPostUrl` — PASSED
2. `testExtractShortcodeFromReelUrl` — PASSED
3. `testExtractShortcodeFromTvUrl` — PASSED
4. `testExtractShortcodeFromNonInstagramUrl` — PASSED
5. `testExtractShortcodeWithTrailingQueryParams` — PASSED
6. `testExtractItemJsonListTwoItems` — PASSED
7. `testExtractItemJsonListThreeItems` — PASSED
8. `testExtractItemJsonListReturnsEmptyForNonCarousel` — PASSED
9. `testExtractItemJsonListReturnsEmptyWhenEntriesEmpty` — PASSED

## Known Limitation
`InstagramCarouselRouter.route()` has no caller in production code yet.
`Downloader.kt` must be modified in the next wiring step to call `route()` after `fetchVideoInfo` returns.
On-device verification: see [`INTEGRATION_CHECKLIST.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/evidence/WP_4_1/INTEGRATION_CHECKLIST.md)
