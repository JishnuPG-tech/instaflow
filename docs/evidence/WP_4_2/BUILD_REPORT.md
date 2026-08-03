# WP 4.2 BUILD & TEST REPORT

- **Work Package ID**: `WP 4.2`
- **Title**: HomePageViewModel Carousel Wiring
- **Build Status**: 🟢 SUCCESSFUL (8s, `assembleDebug` + all unit tests)
- **Files Modified**:
  - [`InstagramCarouselRouter.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/InstagramCarouselRouter.kt) — Refactored: primary entry point now `routeFromPlaylist(PlaylistResult)` matching actual yt-dlp carousel output format
  - [`HomePageViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt) — `parsePlaylistInfo()` now routes Instagram `PlaylistResult` objects to the carousel pipeline before showing the playlist selection dialog

## Key Architectural Decision

Instagram carousels now bypass the playlist selection dialog. When a user pastes an Instagram carousel URL and has "Playlist" mode enabled:

1. `getPlaylistOrVideoInfo()` returns a `PlaylistResult` with `extractorKey = "Instagram"` and N entries
2. `InstagramCarouselRouter.routeFromPlaylist()` detects the Instagram extractor key
3. One `Task` per carousel entry is enqueued into `DownloaderV2`
4. A toast confirms: "Downloading N items from @username"
5. Non-Instagram playlists (YouTube, SoundCloud) are unaffected — they still show the selection dialog

## Regression Guard

- All upstream Seal download paths (single video, YouTube playlist, custom command) are unmodified
- The `PlaylistResult.is` branch only activates the carousel router; all fallbacks return `RoutingResult.SingleItem` which routes to `showPlaylistPage()` as before

## Outstanding (Gate C close criteria)

On-device verification still pending — see [`WP_4_1/INTEGRATION_CHECKLIST.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/evidence/WP_4_1/INTEGRATION_CHECKLIST.md)
