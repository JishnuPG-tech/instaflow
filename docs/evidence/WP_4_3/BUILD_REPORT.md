# WP 4.3 — Worker Integration & ViewState Enrichment

- **Work Package ID**: `WP 4.3`
- **Title**: Carousel Download Worker Integration + ViewState Enrichment
- **Build Status**: 🟢 SUCCESSFUL (21s, `assembleDebug` + `testGenericDebugUnitTest`)
- **Files Modified**:
  - [`InstagramCarouselRouter.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/InstagramCarouselRouter.kt)

## Key Discovery

`DownloaderV2Impl` already handles the **full task lifecycle** via a snapshot flow:

```
enqueue(task, state)           → Idle
doYourWork() fires             → task.prepare() → task.fetchInfo()
fetchInfo() completes          → ReadyWithInfo
doYourWork() fires again       → task.download()
download() progress callback   → NotificationUtil.notifyProgress()
download() success             → Completed(filePath)
```

**No new Worker/WorkManager code was needed.** Each carousel Task enqueued in WP 4.2 is automatically driven by the existing `DownloaderV2Impl` machinery, up to `MAX_CONCURRENCY = 3` items simultaneously.

## What Changed

Carousel tasks are now enqueued with a **rich ViewState** instead of the bare URL default:

| Field | Before WP 4.3 | After WP 4.3 |
|---|---|---|
| `title` | `"https://cdn.instagram.com/v/..."` | `"(@username) Item 2 of 5"` |
| `uploader` | `""` | `"username"` |
| `extractorKey` | `""` | `"Instagram"` |
| `thumbnailUrl` | `null` | thumbnail URL from `PlaylistEntry` |
| `duration` | `0` | duration in seconds from `PlaylistEntry` |

This means the **DownloaderV2 task list UI**, **notification title**, and **progress notification text** all show human-readable content from the moment the task is enqueued — before `fetchInfo()` even completes.
