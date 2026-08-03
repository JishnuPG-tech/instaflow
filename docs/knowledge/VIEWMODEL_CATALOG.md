# VIEWMODEL CATALOG — State Management

## ViewModel: `HomePageViewModel`
- **Location**: [`app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/HomePageViewModel.kt)
- **Purpose**: Manages home page state, clipboard url monitoring, and dialog triggers.
- **Called by**: `DownloadPage` via Koin `koinViewModel()`
- **Depends on**: `PreferenceUtil`
- **Thread**: Main / StateFlow
- **Decision**: KEEP
- **Reason**: Clean reactive state handling.
- **Future modifications**: Support auto-detection of Instagram post URLs.

---

## ViewModel: `VideoListViewModel`
- **Location**: [`app/src/main/java/com/junkfood/seal/ui/page/videolist/VideoListViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/videolist/VideoListViewModel.kt)
- **Purpose**: Manages download history queries, search filtering, and deletion.
- **Called by**: `VideoListPage`
- **Depends on**: `VideoInfoDao`
- **Thread**: `Dispatchers.IO` / Room Flow
- **Decision**: KEEP & MODIFY
- **Reason**: Stable Room reactivity.
- **Future modifications**: Add filter tags for Carousel, Reel, Story, and Profile Picture downloads.
