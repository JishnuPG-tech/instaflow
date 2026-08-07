# UI_UX_BLUEPRINT.md — InstaFlow, All Pages & Flows (as they exist in the real repo)

Grounded directly in the actual code: `HomePageViewModel.kt`, `InstagramMediaPreviewSheet.kt`,
`InstagramUrlValidator.kt`. This draws every real flow the app has today, plus the ones from
`CODEBASE_ANALYSIS.md` that need to be added (Story/Highlight/Profile). Fixes from
`FIX_PROMPTS.md` are marked inline where they change what's drawn.

```
LEGEND
--   boundary / edge of a screen or sheet        -->  navigates to / triggers
|    vertical divider                             ↕   modal opens over current screen
[x]  tappable button/icon                         (!) known bug, see CODEBASE_ANALYSIS.md
```

---

## 1. ENTRY POINT — Home / URL Input

```
-----------------------------------------------
|  InstaFlow                                    |
|                                                 |
|  --------------------------------------------  |
|  | Paste an Instagram link                  |  |
|  --------------------------------------------  |
|                                                 |
|              [ Fetch ] ------------------------|--> InstagramUrlValidator.parseUrl(url)
-----------------------------------------------          |
                                                           v
                                          -----------------------------
                                          | isValid == true ?          |
                                          -----------------------------
                                           yes |                | no
                                               v                v
                                  fetchInfoForInstagramSheet()   falls through to
                                               |                 generic Seal flow
                                               v                 (any yt-dlp site,
                                    DownloadUtil.getPlaylistOrVideoInfo(url)     unrelated to Instagram)
                                               |
                        -------------------------------------------------
                        |                      |                        |
                  is VideoInfo          is PlaylistResult          (!) STORY / HIGHLIGHT /
                  (single post/reel)     (carousel)                    PROFILE types detected
                        |                      |                    by validator but NOT
                        v                      v                    branched here yet --
                  --> SECTION 2          --> SECTION 3               see SECTION 5 (to build)
```

---

## 2. SINGLE VIDEO / REEL DOWNLOAD (works today)

```
-----------------------------------------------
|  ↕  InstagramMediaPreviewSheet                |
|                                                 |
|  [Movie icon]  Reel by @username                |  <- Icons.Default.Movie
|                                                 |     (isCarousel == false)
|  "caption text..."                              |
|                                                 |
|  --------------------------------------------  |
|  |          [ Download ]                     |--> downloadInstagramSingle(audioOnly=false)
|  --------------------------------------------  |         |
|  --------------------------------------------  |         v
|  |  [MusicNote]  Download as audio only      |--> downloadInstagramSingle(audioOnly=true)
|  --------------------------------------------  |         |
-----------------------------------------------          v
                                          Downloader.downloadVideoWithInfo(
                                            info = targetVideoInfo,
                                            preferences = prefs.copy(extractAudio = audioOnly)
                                          )
                                                  |
                                                  v
                                          real file on device --
                                          THIS PATH ALREADY WORKS END TO END
```

---

## 3. SINGLE IMAGE POST DOWNLOAD (works today, same path as §2)

```
-----------------------------------------------
|  ↕  InstagramMediaPreviewSheet                |
|                                                 |
|  [Movie icon]  Post by @username                |  <- (!) always shows Movie icon here,
|                                                 |     header icon logic uses isCarousel only,
|  "caption text..."                              |     doesn't distinguish single-photo vs
|                                                 |     single-video posts at the header level
|  --------------------------------------------  |     -- minor, cosmetic, not in FIX_PROMPTS
|  |          [ Download ]                     |--> same path as SECTION 2
|  --------------------------------------------  |
|  --------------------------------------------  |
|  |  [MusicNote]  Download as audio only      |  <- (!) shown even for a static image post,
|  --------------------------------------------  |     where "audio only" is meaningless --
-----------------------------------------------      worth hiding this button when the
                                                       resolved post has no audio track at all
```

---

## 4. CAROUSEL DOWNLOAD — MULTIPLE ITEMS, MIXED PHOTO/VIDEO

### 4a. Before Prompt 1 fix (current, broken)
```
-----------------------------------------------
|  [PhotoLibrary icon]  Carousel by @username     |  <- isCarousel == true
|                                                 |
|  ------  ------  ------                        |
|  |img1| |img2| |img3|   <- ALL show Image icon, even actual videos
|  ------  ------  ------                            because HomePageViewModel.kt hardcodes:
|  [ ]    [ ]    [x]                                  mediaType = IMAGE, isVideo = false   (!)
|                                                 |
|  --------------------------------------------  |
|  |    [Download] Download Selected (1)       |  |
|  --------------------------------------------  |
-----------------------------------------------
```

### 4b. After Prompt 1 fix (correct)
```
-----------------------------------------------
|  [PhotoLibrary icon]  Carousel by @username     |
|                                                 |
|  ------  --------  ------                      |
|  |img1| |Videocam| |img3|  <- item 2 now correctly shows
|  ------  --------  ------      Videocam icon, derived from:
|  [ ]      [x]       [ ]        isVideoEntry = (entry.duration ?: 0.0) > 0.0
|                                                 |
|  --------------------------------------------  |
|  |    [Download] Download Selected (1)       |  |
|  --------------------------------------------  |
-----------------------------------------------
```

### 4c. After Prompt 2 fix (audio-only added for carousel video items)
```
-----------------------------------------------
|  [PhotoLibrary icon]  Carousel by @username     |
|                                                 |
|  ------  --------  ------                      |
|  |img1| |Videocam| |img3|                       |
|  ------  --------  ------                      |
|  [ ]      [x]       [ ]                        |
|                                                 |
|  ( ) Download as video   (x) Download as audio  |  <- new toggle, only meaningful/enabled
|      only (applies to selected video items)      |     when at least one selected item
|                                                 |     has isVideo == true
|  --------------------------------------------  |
|  |    [Download] Download Selected (1)       |--> downloadInstagramSelectedItems(
|  --------------------------------------------  |      selectedItems, audioOnly = true
-----------------------------------------------       )
                                                        |
                                                        v
                                             per item: if isVideo && audioOnly
                                               -> extract audio only
                                             else
                                               -> download as-is (image, or full video)
```

---

## 5. STORY / HIGHLIGHT / PROFILE PICTURE (to be built — Prompt 3)

```
-----------------------------------------------
|  URL parsed as STORY / HIGHLIGHT / PROFILE      |
|  (InstagramUrlValidator already detects this,   |
|   but fetchInfoForInstagramSheet() has no        |
|   branch for it yet)                             |
-----------------------------------------------
              |
              v
   -------------------------------------------
   | Verify real getPlaylistOrVideoInfo()      |
   | response shape for each type FIRST --     |
   | don't assume, test against real URLs       |
   -------------------------------------------
              |
    ----------------------------------------------------
    |                    |                              |
  STORY               HIGHLIGHT                      PROFILE
    |                    |                              |
    v                    v                              v
-----------------   -----------------              -----------------
| still active?  |   | treat as a      |              | single image,  |
| -- yes: same    |   | playlist/       |              | no video/audio |
|    sheet as §2   |   | carousel of     |              | options shown  |
| -- no: show      |   | story items --  |              | (nothing to    |
|    "This story   |   | verify against  |              | extract)       |
|    is no longer  |   | a real          |              -----------------
|    available --  |   | highlight URL   |
|    stories expire|   -----------------
|    after 24      |
|    hours"        |
-----------------
```

---

## 6. QUEUE / HISTORY / SETTINGS (inherited from Seal, unchanged)

```
-----------------------------------------------
|  Download queue                                |
|  ------------------------------------------    |
|  | [progress]  reel_shortcode.mp4           |  |  <- standard Seal download-task UI,
|  ------------------------------------------    |     no Instagram-specific changes needed
|                                                 |     here -- carries whatever mediaType/
|  History                                       |     isVideo was set correctly upstream
|  ------  ------  ------                        |     (once Prompt 1 is fixed, history
|  |img1| |vid2| |img3|                          |     thumbnails/icons will also be correct,
|  ------  ------  ------                        |     since they read the same fields)
|                                                 |
|  Settings                                      |
|  (unchanged Seal settings screens --            |
|   see earlier UI_UX_DESIGN.md for the full       |
|   list/toggle pattern reference if rebuilding    |
|   any of these from scratch)                     |
-----------------------------------------------
```

---

## 7. End-to-End Flow Map (all paths, one diagram)

```
Paste/Share URL
      |
      v
InstagramUrlValidator.parseUrl()
      |
   -----------------------------------------------------
   |          |            |            |               |
  POST       REEL        STORY       HIGHLIGHT        PROFILE
   |          |            |            |               |
   v          v            v            v               v
getPlaylistOrVideoInfo() for all types --------------------
   |
   -------------------------------
   |                             |
 VideoInfo                 PlaylistResult
 (single item)              (carousel, multi-item)
   |                             |
   v                             v
SECTION 2/3                 SECTION 4
(works today)          (4a broken --> 4b fixed --> 4c audio-only added)
   |                             |
   -------------------------------
                |
                v
        real file on device
     (MediaStore, per Seal's
      existing storage layer --
      unchanged, not part of
      the Instagram-specific bugs)
```
