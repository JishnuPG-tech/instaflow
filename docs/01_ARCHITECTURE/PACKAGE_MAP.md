# Package Map — JunkFood02/Seal

## Package Hierarchy (`com.junkfood.seal`)

| Package Path | Core Contents & Primary Responsibility |
| :--- | :--- |
| `com.junkfood.seal` | Application entry points (`App.kt`, `MainActivity.kt`, `QuickDownloadActivity.kt`, `CrashReportActivity.kt`, `DownloadService.kt`, `Downloader.kt`, `NotificationActionReceiver.kt`) |
| `com.junkfood.seal.download` | Task queue architecture (`DownloaderV2.kt`, `Task.kt`, `TaskFactory.kt`) |
| `com.junkfood.seal.database` | Room database definition (`AppDatabase.kt`) and DAO interfaces (`VideoInfoDao.kt`) |
| `com.junkfood.seal.database.objects` | Entity models (`DownloadedVideoInfo.kt`, `CookieProfile.kt`, `CommandTemplate.kt`, `OptionShortcut.kt`) |
| `com.junkfood.seal.ui` | Main UI package housing navigation, components, and pages |
| `com.junkfood.seal.ui.page.download` | Main home download page (`DownloadPage.kt`, `HomePageViewModel.kt`, `DownloadSettingsDialog.kt`) |
| `com.junkfood.seal.ui.page.downloadv2` | V2 interactive task action sheet & cards (`DownloadPageV2.kt`, `ActionSheet.kt`, `VideoCardV2.kt`) |
| `com.junkfood.seal.ui.page.settings` | Preference and settings screens (`SettingsPage.kt`, `CookiesViewModel.kt`, etc.) |
| `com.junkfood.seal.ui.page.videolist` | History screen (`VideoListViewModel.kt`, video item management) |
| `com.junkfood.seal.ui.component` | Reusable Compose UI elements (dialogs, sliders, preference tiles, top bars) |
| `com.junkfood.seal.ui.theme` | Material 3 Theme setup, typography, and color palette wiring |
| `com.junkfood.seal.util` | Core utilities (`DownloadUtil.kt`, `PreferenceUtil.kt`, `FileUtil.kt`, `NotificationUtil.kt`, `UpdateUtil.kt`) |
