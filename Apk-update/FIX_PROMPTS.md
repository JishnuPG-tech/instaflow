# FIX_PROMPTS.md — Copy-Paste Prompts to Make InstaFlow Fully Functional

Use these **one at a time**, in order, in Claude Code (or your agent of choice) pointed at the `instaflow` repo. Each prompt is scoped to one fix, and each includes its own verification step — don't let the agent move to the next prompt until the current one's verification passes with real evidence (build output, logs, or a real Instagram URL test), not just a claim that it should work.

Read `CODEBASE_ANALYSIS.md` first — every prompt below assumes that context.

---

## Prompt 1 — Fix the carousel item type bug (highest priority)

```
Read CODEBASE_ANALYSIS.md section 2 for full context. In
app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt,
inside fetchInfoForInstagramSheet(), the `is PlaylistResult ->` branch currently
hardcodes every carousel item as:
    mediaType = com.junkfood.seal.database.InstagramMediaType.IMAGE,
    isVideo = false,

Fix this to derive the real type from each entry's `duration` field, since
PlaylistEntry (in VideoInfo.kt) has no is_video field — duration is the only
available signal, and yt-dlp's convention is: image entries have no duration
(null or 0.0), video entries have a real nonzero duration.

Change it to:
    val isVideoEntry = (entry.duration ?: 0.0) > 0.0
    mediaType = if (isVideoEntry) InstagramMediaType.VIDEO else InstagramMediaType.IMAGE,
    isVideo = isVideoEntry,

Do not touch InstagramMediaPreviewSheet.kt — it already reads item.isVideo
correctly and needs no changes; it's only been showing the wrong icon because
the ViewModel was feeding it wrong data.

After the change:
1. Build the project and confirm it compiles clean.
2. Find a real Instagram carousel post URL that mixes at least one photo and
   one video slide. Paste it into the app, open the carousel sheet, and show
   me a screenshot confirming the video item now shows the video icon
   (Icons.Default.Videocam) and the photo item still shows the image icon.
3. Download the video item from the carousel and confirm the resulting file
   is a playable video, not corrupted or mis-typed.

Do not mark this done until step 2 and 3 are demonstrated with real evidence,
not just "the code looks correct."
```

---

## Prompt 2 — Add per-item audio-only option for carousel selections

```
Read CODEBASE_ANALYSIS.md section 4. Prompt 1 must be verified working first —
this depends on isVideo being correct per item.

In HomePageViewModel.kt's downloadInstagramSelectedItems(), add a per-item
audio-only option:
1. Add an optional audioOnly: Boolean parameter (or a per-item override if the
   selection UI supports it — check InstagramMediaPreviewSheet.kt's current
   selection state shape first and tell me which is simpler before making the
   change).
2. In InstagramMediaPreviewSheet.kt, add an audio-only affordance for the
   multi-select "Download Selected" flow — this can be a single toggle that
   applies to all selected video items at once (simplest), or a per-item
   option if the existing selection state already supports per-item metadata
   easily. Only build the per-item version if it's not meaningfully harder;
   otherwise the single shared toggle is fine for a first version.
3. Only apply audioOnly to items where isVideo is true — audio extraction
   from an image item should either be disabled/hidden or a no-op, not an
   error.

After the change:
1. Build and confirm it compiles clean.
2. Using the same mixed carousel URL from Prompt 1, select a video item and
   the new audio-only option, download it, and confirm the resulting file is
   audio-only (no video track), not a full video file.
3. Confirm selecting audio-only alongside an image-only item in the same
   batch doesn't crash or silently produce a broken file for the image item.

Show me real output for both checks before considering this done.
```

---

## Prompt 3 — Story, Highlight, and Profile Picture handling

```
Read CODEBASE_ANALYSIS.md section 5. InstagramUrlValidator already correctly
tags STORY, HIGHLIGHT, and PROFILE url types, but fetchInfoForInstagramSheet()
in HomePageViewModel.kt only branches on PlaylistResult and VideoInfo — there
is no dedicated handling for these three cases.

Before writing any new code:
1. Test what DownloadUtil.getPlaylistOrVideoInfo() actually returns today for
   a real story URL, a real highlight URL, and a real public profile URL.
   Show me the raw result type and fields for each — don't assume, verify.
2. Based on what you find, add explicit `is StoryInfo ->` / relevant branches
   (or reuse VideoInfo/PlaylistResult if that's genuinely what comes back,
   but add story-specific handling on top):
   - Story: if the story has expired, surface a clear message
     ("This story is no longer available — stories expire after 24 hours"),
     not a generic error or crash.
   - Highlight: treat as a playlist/carousel of story items if that's what
     yt-dlp returns; verify against a real highlight URL.
   - Profile picture: single-image flow, no video/audio-only options shown
     since there's nothing to extract audio from.
3. Existing dead files InstagramStoryHandler.kt, InstagramHighlightHandler.kt,
   and InstagramProfilePicHandler.kt may contain a starting point — read them
   first, but don't assume they're correct or wire them in verbatim; verify
   their logic against what you actually observe from real URLs in step 1,
   the same way Prompt 1 found the carousel logic was based on a data shape
   that didn't match reality.

Test each of the three cases against a real URL and show me the actual
resulting behavior (screenshot or logged output) before calling this done.
```

---

## Prompt 4 — Dead code cleanup decision

```
Read CODEBASE_ANALYSIS.md section 3. Prompts 1-3 are complete and verified.

For each of these 17 files, decide and act — don't leave them as silent dead
code either way:
  InstagramCarouselDetector.kt, InstagramCarouselErrorRecovery.kt,
  InstagramCarouselFilenameStrategy.kt, InstagramCarouselItemParser.kt,
  InstagramCarouselMetadataAggregator.kt, InstagramCarouselNotificationHandler.kt,
  InstagramCarouselOrchestrator.kt, InstagramCarouselProgressTracker.kt,
  InstagramCarouselQueueBuilder.kt, InstagramCarouselRouter.kt,
  InstagramImagePostHandler.kt, InstagramMediaResolver.kt,
  InstagramReelPostHandler.kt, InstagramVideoPostHandler.kt
  (InstagramStoryHandler.kt, InstagramHighlightHandler.kt,
  InstagramProfilePicHandler.kt were already addressed in Prompt 3)

For each file: either (a) actually wire it into the real download flow if its
logic is genuinely useful and correct (e.g., InstagramCarouselErrorRecovery's
retry logic, or InstagramCarouselNotificationHandler's per-item notifications,
could be real improvements over what's currently inline in
HomePageViewModel.kt), or (b) delete it if it duplicates something already
fixed inline or doesn't match the real data shapes in this codebase (the way
InstagramCarouselItemParser's JSON-string approach didn't match PlaylistEntry).

Give me a one-line decision + reason for each file before making changes, so
I can confirm before you touch 17 files at once.
```

---

## Prompt 5 — Rebrand the app identity

```
Read CODEBASE_ANALYSIS.md section 6. This is mechanical, lowest risk, do last.

Change app/build.gradle.kts:
    applicationId = "com.junkfood.seal"  →  applicationId = "<your real package,
                                              e.g. com.jishnupg.instaflow>"
    namespace = "com.junkfood.seal"      →  namespace = "<same new package>"

This requires moving the actual Kotlin package directory structure to match
(app/src/main/java/com/junkfood/seal/* → app/src/main/java/<new path>/*) and
updating every `package com.junkfood.seal...` declaration and import
accordingly — Android Studio's "Refactor > Rename Package" does this safely
in one operation; don't do it with find-and-replace across 180+ files by hand.

Also update: app name string resource, app icon/mipmap assets, and confirm
GPLv3 LICENSE file and attribution to the original Seal project are kept
intact (required by the license, not optional — see ARCHITECTURE.md's
license note).

After the change: clean build, install on a device, confirm the app installs
under the new package name and doesn't conflict with an existing Seal
install on the same device, and confirm the app still launches and the
Prompt 1-3 fixes still work (full regression check, not just a compile check).
```
