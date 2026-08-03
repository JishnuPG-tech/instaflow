# CHANGELOG — InstaFlow Development History

All notable changes to InstaFlow will be documented in this file.

---

## [0.1.0-alpha] - 2026-08-03

### Added
- **Phase 4 / Gate D (Pipeline Wiring & UI Integration)**: Created `InstagramCarouselRouter.kt` to intercept `PlaylistResult` objects from yt-dlp and automatically route Instagram multi-item carousels to `DownloaderV2Impl` without requiring user interaction in the playlist selection dialog. Enqueued each carousel item task with rich `ViewState` (`(@author) Item N of M`, thumbnail, duration). Verified auto-persistence to Room history database and per-task notification emission.
- **Phase 3 / Gate C (Carousel Specialization)**: Created `InstagramCarouselDetector.kt` and item array extractor functions for carousel item payloads. Certified unit test coverage across parser and routing layers.
- **WP 2.4 (Single Image Posts)**: Created `InstagramImagePostHandler.kt` for extracting and parsing single Instagram image post payloads into `InstagramMediaItem`. Added 100% passing unit test suite in `InstagramImagePostHandlerTest.kt`.
- **WP 2.3 (Media Model Data Classes)**: Created `InstagramMediaModel.kt` with strongly-typed `InstagramMediaType` enum (`IMAGE`, `VIDEO`, `REEL`, `STORY`, `PROFILE_PIC`, `CAROUSEL`) and `InstagramMediaItem` data class. Added 100% passing unit test suite in `InstagramMediaModelTest.kt`.
- **WP 2.2 (Media Resolver)**: Created `InstagramMediaResolver.kt` for building Instagram-optimized `yt-dlp` extraction arguments with custom browser User-Agent headers and optional cookie file injection. Added 100% passing unit test suite in `InstagramMediaResolverTest.kt`.
- **WP 2.1 (Instagram URL Validator)**: Created `InstagramUrlValidator.kt` with pattern matching for Posts (`/p/`), Reels (`/reel/`, `/reels/`, `/tv/`), Stories (`/stories/{user}/{id}`), Story Highlights (`/stories/highlights/{id}`), and Profiles (`/{username}`). Added 100% passing unit test suite in `InstagramUrlValidatorTest.kt`.
- **WP 1.5 (Upstream Baseline Lock)**: Created Git baseline tag `baseline-instaflow-start`. Formally closed Gate A.
- **WP 1.4 (Performance Baseline)**: Documented build speed, ABI APK size splits, and native library binary footprint (~32.4MB). Created `docs/UPSTREAM_SYNC_STATUS.md`.
- **WP 1.3 (Runtime Functional Mapping)**: Mapped 11 core upstream Seal subsystems to InstaFlow target adaptation categories.
- **WP 1.2 (Static Analysis Baseline)**: Recorded 170 static warnings and 1 upstream lint finding in `docs/evidence/WP_1_2/`.
- **WP 1.1 (Upstream Baseline Certification)**: Clean build and 100% unit test suite execution certified on cloud Codespace (`docs/evidence/WP_1_1/`).