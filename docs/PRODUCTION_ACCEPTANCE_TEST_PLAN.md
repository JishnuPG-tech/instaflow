# PRODUCTION ACCEPTANCE TEST PLAN — InstaFlow E2E Reality Check

- **Project**: InstaFlow (Specialization of JunkFood02/Seal)
- **Target Version**: `0.1.0-alpha`
- **Document Status**: 🟡 ACTIVE — MANUAL E2E ACCEPTANCE GATE
- **Target Environment**: Physical Android Devices & Android Emulators (API 26 to API 35)

---

## Executive Overview

This document defines the **Production Acceptance Checklist** required before certifying Gate F. Successful automated unit test execution and Gradle compilation are necessary baselines, but **Gate F Certification requires empirical verification on running Android devices.**

---

## Section 1: Single Media Download Flow

### 1.1 Single Image Posts
- [ ] **[SIM-01]** Paste public Instagram single image post URL (`https://www.instagram.com/p/...`).
- [ ] **[SIM-02]** Verify resolution of high-resolution JPEG/PNG payload.
- [ ] **[SIM-03]** Verify downloaded file opens cleanly in Android Gallery / Photos app.
- [ ] **[SIM-04]** Verify correct filename formatting (`instagram_username_shortcode.jpg`).

### 1.2 Single Video Posts & Reels
- [ ] **[SVR-01]** Paste public Instagram video post / Reel URL (`https://www.instagram.com/reel/...`).
- [ ] **[SVR-02]** Download default format (Video + Audio combined). Verify audio plays in sync with video.
- [ ] **[SVR-03]** Test "Video Only" format option if technically available for item.
- [ ] **[SVR-04]** Test "Audio Only" format option (MP3/M4A extraction) if technically available.
- [ ] **[SVR-05]** Verify video metadata (duration, resolution, aspect ratio) displays in preview UI prior to download.

### 1.3 Instagram Stories & Highlights
- [ ] **[STO-01]** Paste public Instagram Story URL (`https://www.instagram.com/stories/{user}/{id}/`).
- [ ] **[STO-02]** Download active Story (video or image). Verify file integrity.
- [ ] **[STO-03]** Paste expired Story URL. Verify app displays a graceful error message without crashing.
- [ ] **[HGT-01]** Paste public Highlight URL (`https://www.instagram.com/stories/highlights/{id}/`).
- [ ] **[HGT-02]** Download multi-item Highlight sequence. Verify all items process in sequence.

### 1.4 Profile Pictures
- [ ] **[PFP-01]** Paste Instagram profile URL (`https://www.instagram.com/{username}/`).
- [ ] **[PFP-02]** Verify highest available resolution avatar HD image URL is resolved and saved.

---

## Section 2: Carousel Multi-Item Flow

### 2.1 Carousel Detection & Item Extraction
- [ ] **[CAR-01] Image Carousel**: Paste URL for 5+ image carousel. Verify all items detected.
- [ ] **[CAR-02] Video Carousel**: Paste URL for multi-video carousel. Verify all items detected.
- [ ] **[CAR-03] Mixed Carousel**: Paste URL for mixed image/video carousel. Verify correct media type tags (`IMAGE` vs `VIDEO`).

### 2.2 Carousel UI & User Selection
- [ ] **[CUI-01]** Verify thumbnail carousel grid preview displays all extracted items in original post order.
- [ ] **[CUI-02] Item Selection**: Tap item #2 and #4. Verify selection checkboxes toggle cleanly.
- [ ] **[CUI-03] "Download Selected"**: Click "Download Selected". Verify ONLY items #2 and #4 are enqueued into `DownloaderV2`.
- [ ] **[CUI-04] "Download All"**: Click "Download All". Verify all N items are enqueued in sequential queue.
- [ ] **[CUI-05] Item Naming**: Verify downloaded files are named indexed sequentially: `shortcode_01.jpg`, `shortcode_02.mp4`, etc.

---

## Section 3: Authentication & Cookie Testing

- [ ] **[COK-01] Public Post**: Verify download succeeds without Instagram cookies logged in.
- [ ] **[COK-02] Login-Required / Private Account**: Paste URL requiring login. Verify graceful prompt to import/log-in with Instagram cookies.
- [ ] **[COK-03] Cookie File Import**: Import `cookies.txt` via Settings -> Network -> Cookies. Verify private post download succeeds.
- [ ] **[COK-04] Expired Cookies**: Test with expired session cookies. Verify clear user error alert ("Instagram session expired").
- [ ] **[COK-05] Cookie Logout / Clear**: Clear cookies in Settings. Verify app returns to unauthenticated mode safely.

---

## Section 4: Stress, Resilience & Process Lifecycle Testing

- [ ] **[STR-01] Heavy Queue**: Enqueue 50+ carousel items simultaneously. Verify app remains responsive without OOM / ANR.
- [ ] **[STR-02] Screen Rotation**: Rotate screen (Portrait <-> Landscape) during active download. Verify download progress state is retained.
- [ ] **[STR-03] App Backgrounding**: Send app to background during download. Verify foreground service keeps download active.
- [ ] **[STR-04] Process Death Recovery**: Force kill app via Android Settings while tasks are running. Reopen app. Verify tasks restore state.
- [ ] **[STR-05] Storage Permission Denial**: Revoke storage permission in OS settings. Attempt download. Verify graceful permission request dialog.
- [ ] **[STR-06] Poor Network / Disconnection**: Toggle Airplane Mode during active download. Verify task enters Retry / Error state gracefully without app crash.

---

## Section 5: Android Version Matrix & Device Compatibility

| OS Version | API Level | Hardware / Emulator Target | Status | Tested By / Date |
|---|---|---|---|---|
| Android 8.0 | API 26 | Emulator / Legacy Device | ⏳ Pending | |
| Android 10.0 | API 29 | Emulator / Mid-Tier Device | ⏳ Pending | |
| Android 12.0 | API 31 | Physical Device (Pixel/Samsung) | ⏳ Pending | |
| Android 14 / 15 | API 34/35 | Modern Physical / Emulator Target | ⏳ Pending | |

---

## Section 6: UI/UX & System Integration

- [ ] **[UIX-01] Dark / Light / Dynamic Theme**: Verify contrast and legibility across Light, Dark, and Material You / AMOLED black themes.
- [ ] **[UIX-02] Font Scaling**: Enable maximum font scaling in Android Accessibility Settings. Verify no text clipping in UI cards.
- [ ] **[UIX-03] Long Captions & Usernames**: Test with 200+ character post caption and long username. Verify truncation with ellipsis (`...`).
- [ ] **[UIX-04] System Notifications**: Verify download progress notifications display thumbnail, progress bar, cancel action, and tap-to-open intent upon completion.

---

## Acceptance Sign-Off Criteria for Gate F

Gate F will be certified **ONLY when 100% of applicable checkboxes in this document have been manually verified and signed off on real Android hardware / emulators.**
