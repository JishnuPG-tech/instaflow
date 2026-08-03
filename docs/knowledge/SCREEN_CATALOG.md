# SCREEN CATALOG — Composable UI Screens

## Screen: `DownloadPage` / `DownloadPageV2`
- **Location**: [`app/src/main/java/com/junkfood/seal/ui/page/download/DownloadPage.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/DownloadPage.kt)
- **Purpose**: Home screen with URL input bar, quick paste, and active task progress list.
- **Called by**: `AppEntry` NavHost
- **Depends on**: `HomePageViewModel`, `DownloadDialogViewModel`
- **Thread**: Main / Compose Recomposition
- **Decision**: REUSE WITH MODIFICATION
- **Reason**: Clean layout foundation.
- **Future modifications**: Adapt URL input field for Instagram share links and add Instagram Media Action Sheet.

---

## Screen: `DownloadSettingsDialog`
- **Location**: [`app/src/main/java/com/junkfood/seal/ui/page/download/DownloadSettingsDialog.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/DownloadSettingsDialog.kt)
- **Purpose**: Generic format and resolution selection modal.
- **Called by**: `DownloadPage`
- **Depends on**: `DownloadDialogViewModel`
- **Thread**: Main
- **Decision**: REPLACE
- **Reason**: Contains YouTube resolution ladders and video codec selections irrelevant for Instagram.
- **Future modifications**: Replace with Instagram Carousel Picker & Quality Action Sheet.
