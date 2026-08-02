# Pre-Implementation Architecture Review & Discovery — Phase 3 (Format Selection, Carousel Grid & Scoped Storage)

**Phase Scope**: Format Selection Bottom Sheet, Carousel Item Grid Picker, Android 10+ Scoped Storage Manager (`MediaStore`), Download Destination Routing.
**Engineering Standard**: Google / AOSP Open-Source Production Grade
**Governance**: Strict Single-Phase Isolation, Pre-Implementation Seal Comparison, MediaStore Compliance.

---

## 1. Project Documentation Summary (Phase 3 Baseline)

### PRD.md Requirements
- **Flow A Step 2-3 (Format Picker)**: Bottom sheet displaying thumbnail, caption snippet, available qualities (1080p, 720p, 480p for video; original res for images; audio-only for Reels). Tapping quality starts download.
- **Flow C (Carousel Post)**: Grid with checkboxes showing all N slides -> "Download Selected" or "Download All".
- **FR3 & FR6 (Scoped Storage)**: Save files to `Pictures/InstaSave/` (images) or `Movies/InstaSave/` (videos) using Android 10+ (API 29+) MediaStore API without requiring raw `/sdcard` paths or legacy write permissions.

### UI_UX_DESIGN.md Requirements
- **Format Card Item**: SurfaceVariantDark background (`#1E1E1E`), radio/selection highlight in Instagram Coral (`#E1306C`), resolution label, file size badge, container extension tag (`.mp4`, `.jpg`).
- **Carousel Grid**: 2-column or 3-column responsive grid, thumbnail preview, type icon badge (photo vs video), item index pill, multi-select checkboxes.

### ARCHITECTURE.md Requirements
- **Section 4 (Storage Layer)**: `MediaStoreWriter` utilizing `ContentResolver` and `MediaStore.Images.Media` / `MediaStore.Video.Media` URI insertion with `IS_PENDING` flag protocol for atomic writes.

---

## 2. Mandatory Seal Reference Comparison Table

| Subsystem / Area | InstaSave Spec | Seal Implementation | Final Decision | Rationale for Deviation / Adaptation |
|---|---|---|---|---|
| **Format Selection UI** | Material 3 Expressive Bottom Sheet (`ModalBottomSheet`) | Seal Format Dialog / Bottom Sheet | **InstaSave Format Picker Sheet**: Bottom sheet with rendition list & carousel tab support | Matches Seal's format picker UX while styling with Instagram coral tokens. |
| **Carousel Grid Picker** | Grid layout with slide checkboxes ("Select All" / "Download Selected") | Playlist format picker in Seal | **InstaSave Carousel Grid**: Multi-select grid per `PRD Flow C` | Purpose-built for Instagram carousel posts mixing images and videos. |
| **Scoped Storage Writer** | Android 10+ `MediaStore` API (`Pictures/InstaSave/`, `Movies/InstaSave/`) | Seal SAF (Storage Access Framework) + MediaStore fallback | **InstaSave MediaStore `IS_PENDING` Protocol**: Direct `ContentResolver` insertion into MediaStore collections | Ensures zero storage permission prompts on Android 10-15 while preventing half-written file corruption. |

---

## 3. Affected Modules & File Inventory

| Module / Component | Action | Target File Path | Purpose |
|---|---|---|---|
| **Format Picker Sheet** | `NEW` | `app/src/main/java/com/instasave/app/ui/picker/FormatPickerBottomSheet.kt` | Render format renditions list and single-tap selection. |
| **Carousel Item Grid** | `NEW` | `app/src/main/java/com/instasave/app/ui/picker/CarouselItemGrid.kt` | Render carousel slides in responsive grid with selection state. |
| **Storage Manager** | `NEW` | `app/src/main/java/com/instasave/app/core/storage/MediaStoreWriter.kt` | Atomic file writing to MediaStore `Pictures/InstaSave/` and `Movies/InstaSave/`. |
| **Home Contract Update** | `MODIFY` | [HomeContract.kt](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/app/src/main/java/com/instasave/app/ui/home/HomeContract.kt) | Add picker state (`selectedFormat`, `selectedCarouselIndices`). |
| **Home Screen Update** | `MODIFY` | [HomeScreen.kt](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/app/src/main/java/com/instasave/app/ui/home/HomeScreen.kt) | Embed `FormatPickerBottomSheet` into `HomeScreen`. |

---

## 4. Dependencies & Version Lock

- **Android SDK**: `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`
- **MediaStore API**: `android.provider.MediaStore` (`VOLUME_EXTERNAL_PRIMARY`)
- **Jetpack Compose Material 3**: `ModalBottomSheet`, `LazyVerticalGrid`

---

## 5. Architectural Risks & Mitigation Strategies

| Risk ID | Risk Description | Severity | Mitigation Strategy |
|---|---|---|---|
| **RISK-01** | Half-written files appearing in Media Gallery during download. | **HIGH** | Set `MediaStore.MediaColumns.IS_PENDING = 1` during writing, set to `0` upon completion. |
| **RISK-02** | Duplicate file names causing MediaStore overwrite errors. | **MEDIUM** | Append unique shortcode/timestamp hash to filename before insertion (e.g. `InstaSave_C123456789_1080p.mp4`). |
| **RISK-03** | Large carousel posts causing memory overhead in Compose grid. | **LOW** | Use `LazyVerticalGrid` with keying by slide index. |

---

## 6. Assumptions & Constraints

1. **Scoped Storage Only**: Files are written exclusively via `ContentResolver` to public MediaStore collections (no raw file paths).
2. **Zero Local CPU Build Overhead**: Build compilation and verification are handled remotely via GitHub Actions Cloud CI.
