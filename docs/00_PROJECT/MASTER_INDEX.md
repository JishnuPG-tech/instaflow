# JunkFood02/Seal Engineering Wiki — Master Index

Welcome to the comprehensive technical documentation and reverse-engineering suite for **JunkFood02/Seal**, an open-source Android video/audio downloader powered by `yt-dlp`, `ffmpeg`, and `aria2c`.

---

## Documentation Structure

This engineering wiki is divided into 21 domain-specific folders:

### [00. Project Overview](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/00_PROJECT/PROJECT_OVERVIEW.md)
- [`PROJECT_OVERVIEW.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/00_PROJECT/PROJECT_OVERVIEW.md) — High-level product summary, technical identity, and core mission.
- [`GLOSSARY.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/00_PROJECT/GLOSSARY.md) — Domain-specific technical glossary.

### [01. Architecture](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/01_ARCHITECTURE/ARCHITECTURE.md)
- [`ARCHITECTURE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/01_ARCHITECTURE/ARCHITECTURE.md) — High-level system architecture, single-activity design, and MVVM + Repository pattern.
- [`MODULE_MAP.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/01_ARCHITECTURE/MODULE_MAP.md) — Gradle multi-module breakdown (`:app`, `:color`).
- [`PACKAGE_MAP.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/01_ARCHITECTURE/PACKAGE_MAP.md) — Package hierarchy and class location mapping.
- [`DEPENDENCY_GRAPH.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/01_ARCHITECTURE/DEPENDENCY_GRAPH.md) — Koin DI graph and module dependencies.
- [`DATA_FLOW.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/01_ARCHITECTURE/DATA_FLOW.md) — Reactive data flows using Kotlin Flow & StateFlow.

### [02. UI System](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/02_UI/DESIGN_SYSTEM.md)
- [`DESIGN_SYSTEM.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/02_UI/DESIGN_SYSTEM.md) — Jetpack Compose & Material Design 3 design system.
- [`THEME_SYSTEM.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/02_UI/THEME_SYSTEM.md) — Dynamic color engine (`:color` module) & dark mode implementation.
- [`NAVIGATION_GRAPH.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/02_UI/NAVIGATION_GRAPH.md) — Navigation Graph and destination routes.
- [`COMPONENT_LIBRARY.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/02_UI/COMPONENT_LIBRARY.md) — Custom composable component reference.
- [`SCREEN_REFERENCE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/02_UI/SCREEN_REFERENCE.md) — Screen-by-screen breakdown.

### [03. Download Engine](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/03_DOWNLOAD/DOWNLOAD_ENGINE.md)
- [`DOWNLOAD_ENGINE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/03_DOWNLOAD/DOWNLOAD_ENGINE.md) — Native execution engine (`youtubedl-android`, `yt-dlp`, `aria2c`, `ffmpeg`).
- [`DOWNLOAD_PIPELINE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/03_DOWNLOAD/DOWNLOAD_PIPELINE.md) — Task execution lifecycle, format selection, and progress callbacks.
- [`TASK_MANAGEMENT.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/03_DOWNLOAD/TASK_MANAGEMENT.md) — `DownloaderV2`, task factories, and concurrency.

### [04. Database Layer](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/04_DATABASE/ROOM_DATABASE.md)
- [`ROOM_DATABASE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/04_DATABASE/ROOM_DATABASE.md) — Room DB configuration, migrations, and schema export.
- [`DAO_REFERENCE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/04_DATABASE/DAO_REFERENCE.md) — `VideoInfoDao` specification.
- [`ENTITY_REFERENCE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/04_DATABASE/ENTITY_REFERENCE.md) — `DownloadedVideoInfo`, `CommandTemplate`, `CookieProfile`, `OptionShortcut`.

### [05. Workers & Background](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/05_WORKERS/WORKMANAGER_REFERENCE.md)
- [`WORKMANAGER_REFERENCE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/05_WORKERS/WORKMANAGER_REFERENCE.md) — Foreground services (`DownloadService`), WorkManager jobs, and background lifecycle.

### [06. Notifications](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/06_NOTIFICATIONS/NOTIFICATION_SYSTEM.md)
- [`NOTIFICATION_SYSTEM.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/06_NOTIFICATIONS/NOTIFICATION_SYSTEM.md) — Notification channels, progress updates, actions (`NotificationActionReceiver`).

### [07. Settings & Storage](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/07_SETTINGS/SETTINGS_ARCHITECTURE.md)
- [`SETTINGS_ARCHITECTURE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/07_SETTINGS/SETTINGS_ARCHITECTURE.md) — MMKV key-value storage model.
- [`PREFERENCE_UTILITY.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/07_SETTINGS/PREFERENCE_UTILITY.md) — `PreferenceUtil.kt` API reference.
- [`COOKIE_SYSTEM.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/07_SETTINGS/COOKIE_SYSTEM.md) — Cookie profile management, import/export, Netscape format parsing.

### [08. Security Architecture](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/08_SECURITY/SECURITY.md)
- [`SECURITY.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/08_SECURITY/SECURITY.md) — Storage Access Framework (SAF), network security, credential non-persistence.

### [09. Testing Strategy](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/09_TESTING/TESTING.md)
- [`TESTING.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/09_TESTING/TESTING.md) — Unit tests, Compose UI tests, static linting (`ktfmt`).

### [10. Build System](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/10_BUILD_SYSTEM/BUILD_SYSTEM.md)
- [`BUILD_SYSTEM.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/10_BUILD_SYSTEM/BUILD_SYSTEM.md) — Gradle configuration, product flavors (`generic`, `githubPreview`, `fdroid`), ProGuard.
- [`CI_CD.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/10_BUILD_SYSTEM/CI_CD.md) — GitHub Actions workflows for automated builds and releases.

### [11. Dependency Catalog](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/11_DEPENDENCIES/DEPENDENCY_REFERENCE.md)
- [`DEPENDENCY_REFERENCE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/11_DEPENDENCIES/DEPENDENCY_REFERENCE.md) — Gradle version catalog (`libs.versions.toml`) breakdown.

### [12. Indices & Matrices](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/12_REFERENCE/CLASS_INDEX.md)
- [`CLASS_INDEX.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/12_REFERENCE/CLASS_INDEX.md) — Index of all Kotlin classes, interfaces, objects, and enums.
- [`FILE_INDEX.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/12_REFERENCE/FILE_INDEX.md) — Complete file map of source code across repository.

### [13. API Contracts](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/13_API/PUBLIC_INTERNAL_APIS.md)
- [`PUBLIC_INTERNAL_APIS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/13_API/PUBLIC_INTERNAL_APIS.md) — Internal contracts, intent deep-links, share-sheet receivers.

### [14. Utilities](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/14_UTILITIES/UTILITY_REFERENCE.md)
- [`UTILITY_REFERENCE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/14_UTILITIES/UTILITY_REFERENCE.md) — `DownloadUtil`, `FileUtil`, `NotificationUtil`, `UpdateUtil`, `TextUtil`.

### [15. Localization](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/15_TRANSLATIONS/LOCALIZATION.md)
- [`LOCALIZATION.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/15_TRANSLATIONS/LOCALIZATION.md) — Transifex / Weblate workflows, locale config generation.

### [16. Performance](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/16_PERFORMANCE/PERFORMANCE.md)
- [`PERFORMANCE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/16_PERFORMANCE/PERFORMANCE.md) — Memory profile, native binary execution overhead, Room caching.

### [17. Release Process](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/17_RELEASE/RELEASE_PROCESS.md)
- [`RELEASE_PROCESS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/17_RELEASE/RELEASE_PROCESS.md) — Versioning scheme (`versionCode` ABI mapping), signing configs, APK output names.

### [18. AI Integration Notes](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/18_AI/AI_INTEGRATION_NOTES.md)
- [`AI_INTEGRATION_NOTES.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/18_AI/AI_INTEGRATION_NOTES.md) — Principles for AI-assisted analysis and code context generation.

### [19. Architecture Decision Records (ADRs)](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/19_ADR/ADR_001_KOIN_DI.md)
- [`ADR_001_KOIN_DI.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/19_ADR/ADR_001_KOIN_DI.md) — Decision to use Koin for Lightweight DI.
- [`ADR_002_MMKV_PREFERENCES.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/19_ADR/ADR_002_MMKV_PREFERENCES.md) — Decision to use MMKV over Jetpack DataStore/SharedPreferences.
- [`ADR_003_YOUTUBEDL_ANDROID.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/19_ADR/ADR_003_YOUTUBEDL_ANDROID.md) — Decision to wrap native `yt-dlp` via JNI/C++ executables.
- [`ADR_004_JETPACK_COMPOSE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/19_ADR/ADR_004_JETPACK_COMPOSE.md) — Decision to build 100% declarative UI in Jetpack Compose.

### [20. Appendix & Mermaid Diagrams](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/20_APPENDIX/MERMAID_DIAGRAMS.md)
- [`MERMAID_DIAGRAMS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/20_APPENDIX/MERMAID_DIAGRAMS.md) — Complete visual catalog of sequence, flowchart, and architecture diagrams.
- [`CLASS_DIAGRAMS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/20_APPENDIX/CLASS_DIAGRAMS.md) — UML Class diagrams for core components.
