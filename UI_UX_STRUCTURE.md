# UI_UX_STRUCTURE.md — InstaSave Complete App Drawing

A single rough structural drawing of the entire app — navigation map, every screen's layout, component
composition, and data flow — all in plain text so it can be read top-to-bottom like a hand-drawn sketch.
Companion to UI_UX_DESIGN.md (tokens/visual spec), UI_ARCHITECTURE.md (code structure), and
UI_BLUEPRINTS.html (dimensioned visual sheets). This file is the fastest way to see the *whole shape*
of the app at once.

```
LEGEND
──  wall / boundary            →  navigates to / triggers
┊   optional / conditional      ↕  modal opens over current screen
▢   tappable element            ⇄  bidirectional data flow
●   state held in ViewModel     ✕  destructive action
```

---

## 1. FULL APP MAP — screens, modals, and how they connect

```
                              ┌─────────────────────┐
                              │   APP LAUNCH         │
                              └──────────┬───────────┘
                                         │
                         ┌───────────────┴───────────────┐
                         │   Share intent from Instagram? │
                         └───────┬─────────────────┬──────┘
                              yes│                  │no
                                 ▼                  ▼
                    ┌─────────────────────────────────────┐
                    │                                       │
     ┌──────────────┤          BOTTOM NAV (4 tabs)          │
     │              │                                       │
     │              └───────────────────────────────────────┘
     │
     │   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌──────────┐
     ├──▶│  HOME   │   │  QUEUE  │   │ HISTORY │   │ SETTINGS │
     │   └────┬────┘   └────┬────┘   └────┬────┘   └────┬─────┘
     │        │             │             │             │
     │        │ url resolved│             │ tap item     │ tap "save
     │        ▼             │             ▼             │ location"
     │  ┌───────────────┐   │       ┌──────────────┐     ▼
     │  │ RESOLUTION     │↕ │       │ FULL PREVIEW  │┊ ┌─────────────┐
     │  │ PICKER (modal) │  │       │ (post-MVP)    │  │ SAF FOLDER  │
     │  └───────┬────────┘   │       └──────────────┘  │ PICKER (OS) │
     │          │            │                          └─────────────┘
     │          │ is carousel?
     │          ▼
     │  ┌───────────────┐
     │  │ CAROUSEL GRID  │↕  (swaps in for Resolution Picker's
     │  │ (modal variant)│    quality selector when items[] present)
     │  └───────┬────────┘
     │          │ tap Download
     │          ▼
     │  ┌───────────────────────────┐
     └─▶│  enqueue → WorkManager →   │───▶ appears in QUEUE tab
        │  aria2c transfer           │───▶ notification (system tray)
        └───────────┬───────────────┘
                     │ on success
                     ▼
        ┌───────────────────────────┐
        │ MediaStore/SAF write  +    │───▶ appears in HISTORY tab
        │ Room INSERT                │───▶ visible in device Gallery
        └───────────────────────────┘

  ┊ POST-MVP, not built in v1 ┊
  ┊                                                              ┊
  ┊   SETTINGS ──▶ LOGIN WEBVIEW (modal) ──▶ session stored ──▶  ┊
  ┊               (Phase 7)                  unlocks Stories     ┊
  ┊                                          (Phase 8)           ┊
  ┊                                                              ┊
```

---

## 2. SCREEN 01 — HOME (rough layout)

```
┌───────────────────────────────────────────┐
│ InstaSave                             ⚙ ▢ │  ← top bar
├───────────────────────────────────────────┤
│                                             │
│  ┌───────────────────────────────────┐    │
│  │ Paste an Instagram link             │▢  │  ← hero input, cyan
│  │ [_______________________________]   │    │    focus glow
│  │                        [ Paste ]▢ → │    │
│  └───────────────────────────────────┘    │
│                                             │
│  ┊ Paste from clipboard? ▢ ┊ (conditional) │
│                                             │
│  ── Recent ───────────────────────────     │
│  ┌───────────────────────────────────┐    │
│  │ ▢ [img] Reel · 1080p · Done         │───┼──▶ tap → HISTORY detail
│  └───────────────────────────────────┘    │
│  ┌───────────────────────────────────┐    │
│  │ ▢ [img] Post · 2 slides · Done      │    │
│  └───────────────────────────────────┘    │
│  ┌───────────────────────────────────┐    │
│  │ ▢ [img] Story · Expired             │    │
│  └───────────────────────────────────┘    │
│                                             │
├───────────────────────────────────────────┤
│   ●Home     Queue      History   Settings  │  ← bottom nav, Home active
└───────────────────────────────────────────┘

state (●HomeUiState):
  urlInput ─┐
            ├──▶ FetchClicked ──▶ resolveLinkUseCase() ──▶ success? ──▶ ↕ RESOLUTION PICKER
  clipboard ┘                                            └▶ failure ──▶ inline error banner
```

---

## 3. SCREEN 02 — RESOLUTION PICKER (modal, ↕ over Home)

```
        (scrim, dims Home behind)
┌───────────────────────────────────────────┐
│                    ▬▬                      │  ← drag handle
│  [thumb] Reel by @username                 │
│  "caption text truncated to two lines..."  │
│                                             │
│  Choose quality                            │
│  ┌───────┐┌───────┐┌───────┐              │
│  │ 1080p ││ 720p  ││ 480p  │  ← connected  │
│  │ 24 MB ││ 12 MB ││ 6 MB  │    button grp │
│  └───────┘└───────┘└───────┘              │
│  ○ Audio only (.m4a)                       │
│                                             │
│  ┌─────────────────────────────────────┐  │
│  │         Download            ▢       │──┼──▶ enqueue → §1 download flow
│  └─────────────────────────────────────┘  │
└───────────────────────────────────────────┘

  is carousel? ──yes──▶ swap content region for SCREEN 03 below
              └──no───▶ stays as drawn above
```

---

## 4. SCREEN 03 — CAROUSEL GRID (modal variant, replaces quality selector above)

```
┌───────────────────────────────────────────┐
│  Carousel · 6 items          [Select all]▢│
│  ┌─────┐  ┌─────┐  ┌─────┐                │
│  │ ✓1  │  │  2  │  │ ✓3  │                │  ← 3-col grid,
│  └─────┘  └─────┘  └─────┘                │    tap toggles ✓
│  ┌─────┐  ┌─────┐  ┌─────┐                │
│  │  4  │  │ ✓5  │  │  6  │                │
│  └─────┘  └─────┘  └─────┘                │
│  ┌─────────────────────────────────────┐  │
│  │     Download 3 selected      ▢      │──┼──▶ enqueue×3 → §1 download flow
│  └─────────────────────────────────────┘  │
└───────────────────────────────────────────┘
```

---

## 5. SCREEN 04 — DOWNLOAD QUEUE

```
┌───────────────────────────────────────────┐
│ Queue                                      │
├───────────────────────────────────────────┤
│  ┌──┐  reel_DApNLu.mp4                     │
│  │◐ │  1080p · 14.2 / 24.6 MB              │  ← Aperture Ring, closing
│  └──┘  ⏸ pause ▢    ✕ cancel ▢             │    proportionally
│                                             │
│  ┌──┐  post_carousel_2of6.jpg              │
│  │○ │  Queued                              │  ← ring fully open = waiting
│  └──┘                                      │
│                                             │
│  ┌──┐  story_expired.mp4                   │
│  │! │  Failed — story expired              │  ← state.error ring
│  └──┘  ↻ retry ▢                            │
├───────────────────────────────────────────┤
│    Home     ●Queue      History   Settings │
└───────────────────────────────────────────┘

  active item ⇄ WorkManager progress Flow ⇄ persistent system notification (mirrors top item)
```

---

## 6. SCREEN 05 — HISTORY

```
┌───────────────────────────────────────────┐
│ History                          🔍 ▢      │
├───────────────────────────────────────────┤
│ ┌─────┐ ┌─────┐ ┌─────┐                   │
│ │ img │ │ img │ │ img │  ← 3-col grid,     │
│ └─────┘ └─────┘ └─────┘    long-press ▢    │
│ ┌─────┐ ┌─────┐ ┌─────┐    for context menu│
│ │ img │ │ img │ │ img │      (share/delete)│
│ └─────┘ └─────┘ └─────┘                   │
├───────────────────────────────────────────┤
│    Home     Queue      ●History  Settings  │
└───────────────────────────────────────────┘

  tap tile ──▶ full preview (post-MVP) / re-open file
  long-press ──▶ ┬─ Share ▢
                 ├─ Re-download ▢
                 └─ Delete ▢ ✕
```

---

## 7. SCREEN 06 — SETTINGS

```
┌───────────────────────────────────────────┐
│ Settings                                   │
├───────────────────────────────────────────┤
│ Downloads                                  │
│  Save location        Movies/InstaSave ▢ → │──▶ OS SAF folder picker
│  Default quality                 1080p ▢   │
│  Wi-Fi only                         ○●▢    │
│  Max concurrent downloads              2▢  │
│                                             │
│ Advanced                                   │
│  Custom yt-dlp templates              ▢ →  │──▶ (optional, power-user)
│                                             │
│ About                                      │
│  Version 1.0.0        [Check for updates]▢ │──▶ GitHub Releases API
├───────────────────────────────────────────┤
│    Home     Queue      History   ●Settings │
└───────────────────────────────────────────┘
```

---

## 8. COMPONENT COMPOSITION TREE (what's shared vs. screen-local)

```
InstaSaveTheme
 └─ InstaSaveNavGraph
     ├─ HomeScreen ────────────────┬─▶ InstaSaveTopBar          (shared)
     │                             ├─▶ UrlInputField            (local)
     │                             └─▶ DownloadHistoryCard × N  (shared)───┐
     │                                                                      │
     ├─ [modal] ResolutionPickerSheet ─┬─▶ QualitySelector       (shared)   │
     │                                  └─▶ CarouselGridContent  (local)    │
     │                                                                      │
     ├─ DownloadQueueScreen ───────────┬─▶ ApertureRing × N      (shared)   │
     │                                  └─▶ ErrorState/EmptyState (shared)  │
     │                                                                      │
     ├─ HistoryScreen ──────────────────▶ DownloadHistoryCard × N ─────────┘
     │                                    (same component instance as Home's "Recent")
     │
     └─ SettingsScreen ──────────────────▶ (screen-local sections, no shared components)
```

---

## 9. DATA / STATE FLOW (applies identically to every screen — see UI_ARCHITECTURE.md §1-2)

```
 ▢ user taps something
        │
        ▼
 onEvent(UiEvent) ──────────▶ ViewModel
                                  │
                                  ▼
                            UseCase (domain)
                                  │
                    ┌─────────────┴─────────────┐
                    ▼                             ▼
           generated Retrofit client      Room DAO (local)
           (from openapi.yaml)                    │
                    │                              │
                    ▼                              │
           Instagram CDN / backend                 │
                    │                              │
                    └──────────────┬───────────────┘
                                   ▼
                         ViewModel updates ●UiState (StateFlow)
                                   │
                                   ▼
                    Composable recomposes (collectAsStateWithLifecycle)
```

---

## 10. END-TO-END USER JOURNEY (the whole app, one path, start to finish)

```
Instagram app
     │ Share ▢
     ▼
Android share sheet ──▶ InstaSave
     │
     ▼
HOME (url pre-filled) ──▶ auto-resolve ──▶ ↕ RESOLUTION PICKER (or ↕ CAROUSEL GRID)
     │                                              │
     │                                      tap Download ▢
     │                                              ▼
     │                                    QUEUE (Aperture Ring animates)
     │                                              │
     │                                    ┌─────────┴─────────┐
     │                                    ▼                   ▼
     │                          notification: "Saved"   MediaStore/SAF file
     │                                    │                   │
     │                                    ▼                   ▼
     │                              HISTORY entry        device Gallery
     ▼
back to HOME — "Recent" list now shows the new download
```
