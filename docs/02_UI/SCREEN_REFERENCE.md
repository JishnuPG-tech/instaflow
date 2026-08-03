# Screen Reference — Composable Screen Analysis

## 1. Download Screen (`DownloadPage.kt` / `DownloadPageV2.kt`)
- **Primary Purpose**: Acts as the main application home screen.
- **Key Features**:
  - URL text field with auto-paste from clipboard.
  - Media info fetcher trigger.
  - Interactive format selection (Video / Audio / Custom Command).
  - Real-time download progress list.
- **ViewModel**: [`HomePageViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt) & [`DownloadDialogViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/downloadv2/configure/DownloadDialogViewModel.kt).

## 2. Download History Screen (`VideoListPage.kt`)
- **Primary Purpose**: Browse, search, filter, and open previously downloaded videos/audios.
- **Key Features**:
  - Room DB Flow collection.
  - Search bar query filtering.
  - Thumbnail display via Coil.
  - Open file with external player / Share file / Delete item & file.
- **ViewModel**: [`VideoListViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/videolist/VideoListViewModel.kt).

## 3. Cookie Management Screen (`CookiesViewModel.kt` / `NetworkPreferencePage.kt`)
- **Primary Purpose**: Import Netscape format `cookies.txt` or create manually.
- **Key Features**:
  - File picker for `.txt` cookie files.
  - Active domain table preview.
  - Database persistence into `CookieProfile`.
- **ViewModel**: [`CookiesViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/settings/network/CookiesViewModel.kt).
