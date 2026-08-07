# CODEBASE_ANALYSIS.md — InstaFlow, Verified Current State

**Read this before touching any code.** Everything below was confirmed by directly inspecting `github.com/JishnuPG-tech/instaflow` — exact files, exact line numbers, exact data shapes. This is not a plan for a new build; it is a bug list for an existing, partially-working fork of Seal. Do not rebuild from scratch. Do not trust `docs/evidence/*CERTIFICATION*.md` or `PRODUCTION_READINESS_SCORE.md` in this repo — they were verified against the actual code and do not match reality; treat them as unverified claims, not evidence.

---

## 1. What Actually Works Right Now

- `InstagramUrlValidator.kt` (`app/src/main/java/com/junkfood/seal/util/`) correctly parses post/reel/story/highlight/profile URLs via regex, and is genuinely called from `HomePageViewModel.startDownloadVideo()`.
- Single post/reel/video download works end-to-end: fetch → `InstagramMediaPreviewSheet` → tap Download (video) or tap the music-note button (audio-only) → real download via `Downloader.downloadVideoWithInfo()`.
- The carousel bottom sheet UI exists, shows a checkbox grid, and "Download Selected" actually enqueues real download tasks.

## 2. Confirmed Bug: Every Carousel Item Is Mislabeled

**File:** `app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt`, inside `fetchInfoForInstagramSheet()`, in the `is PlaylistResult ->` branch.

**Current (wrong) code:**
```kotlin
mediaType = com.junkfood.seal.database.InstagramMediaType.IMAGE,
isVideo = false,
```
This is hardcoded for every entry, regardless of what it actually is.

**Root cause, confirmed from `VideoInfo.kt`:** `PlaylistEntry` (the real data class returned by `youtubedl-android` for each carousel item) is:
```kotlin
@Serializable
data class PlaylistEntry(
    @SerialName("_type") val type: String? = null,
    val ieKey: String? = null,
    val id: String? = null,
    val url: String? = null,
    val title: String? = null,
    val duration: Double? = .0,
    val uploader: String? = null,
    val channel: String? = null,
    val thumbnails: List<Thumbnail>? = emptyList(),
)
```
There is **no `is_video` field on this class at all.** But `duration` is present — and yt-dlp's own convention is that image entries have no duration (null or 0.0) and video entries have a real nonzero duration. This is the correct, available signal.

**The fix, verified against the real data shape:**
```kotlin
val isVideoEntry = (entry.duration ?: 0.0) > 0.0
mediaType = if (isVideoEntry) InstagramMediaType.VIDEO else InstagramMediaType.IMAGE,
isVideo = isVideoEntry,
```

**Downstream effect once fixed:** `InstagramMediaPreviewSheet.kt` already reads `item.isVideo` to choose the icon (`Icons.Default.Videocam` vs `Icons.Default.Image`, line ~267) — that part of the UI is already correct and needs zero changes. It's been displaying the wrong icon only because the ViewModel was feeding it wrong data.

## 3. Confirmed Dead Code: 17 of 18 New Instagram Files Are Never Called

Verified via `grep` across the whole `app/src/main/java` tree — none of these are referenced anywhere except their own file:

```
InstagramCarouselDetector.kt
InstagramCarouselErrorRecovery.kt
InstagramCarouselFilenameStrategy.kt
InstagramCarouselItemParser.kt          ← contains the CORRECT is_video-based logic already
InstagramCarouselMetadataAggregator.kt
InstagramCarouselNotificationHandler.kt
InstagramCarouselOrchestrator.kt        ← calls the parser above; also unused
InstagramCarouselProgressTracker.kt
InstagramCarouselQueueBuilder.kt
InstagramCarouselRouter.kt
InstagramHighlightHandler.kt
InstagramImagePostHandler.kt
InstagramMediaResolver.kt
InstagramProfilePicHandler.kt
InstagramReelPostHandler.kt
InstagramStoryHandler.kt
InstagramVideoPostHandler.kt
```
Only `InstagramUrlValidator.kt` is wired in.

**Important nuance:** `InstagramCarouselItemParser.parseCarouselItem()` already implements correct video/image detection — but it expects a **raw JSON string per item** with an `is_video` boolean key, which doesn't match `PlaylistEntry`'s actual Kotlin data class (no `is_video` field, no raw JSON available at that point in the code — `youtubedl-android` already deserializes it via kotlinx.serialization before `HomePageViewModel` ever sees it). This is almost certainly *why* it was never wired in: whoever/whatever built it didn't have raw JSON available at the call site, so it silently got bypassed with a two-line hardcoded stand-in instead of adapting the mismatch. **Do not try to force the JSON-string parser to work here — fix `HomePageViewModel.kt` directly using the `duration`-based check in §2, and either delete the 17 dead files or repurpose their good ideas (error recovery, progress tracking, notification handling) as real, wired-in code later, deliberately, not by accident.**

## 4. Confirmed Gap: No Audio-Only Option for Carousel Items

`downloadInstagramSelectedItems()` builds a `Task` per selected item using one shared `DownloadUtil.DownloadPreferences.createFromPreferences()` with no per-item `extractAudio` control. `InstagramMediaPreviewSheet.kt`'s multi-select "Download Selected" button has no audio-only variant — only the single-item flow has the music-note button. If a video within a carousel should be downloadable as audio-only, this path doesn't exist yet.

## 5. Confirmed Gap: Story / Highlight / Profile Have No Real Handling

`InstagramUrlValidator` correctly tags these URL types, but `fetchInfoForInstagramSheet()`'s `when (info)` block only branches on `PlaylistResult` and `VideoInfo` — there is no dedicated path for story expiry messaging, highlight-specific behavior, or profile-picture-specific flow. These URLs currently just fall through to whatever the generic `getPlaylistOrVideoInfo()` call happens to return, with no Instagram-specific handling at all.

## 6. Confirmed: App Identity Was Never Rebranded

`app/build.gradle.kts`:
```kotlin
applicationId = "com.junkfood.seal"
namespace = "com.junkfood.seal"
```
Still literally Seal's package. Not a functional bug, but real — worth fixing alongside everything else so the shipped APK doesn't identify as `com.junkfood.seal`.

## 7. Priority Order for Fixes

1. **§2 (carousel item type)** — highest priority, single-file fix, directly explains your reported symptom
2. **§4 (audio-only for carousel items)** — depends on §2 being fixed first (need correct `isVideo` before audio-only makes sense per item)
3. **§5 (story/highlight/profile handling)** — real feature gap, not a quick fix, scope it as its own piece of work
4. **§3 (dead code cleanup)** — decide keep-and-wire vs. delete once §2 and §4 are stable; don't let this block the functional fixes above
5. **§6 (rebrand)** — mechanical, do last or in parallel, doesn't block functional correctness
