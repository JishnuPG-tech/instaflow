# Pre-Implementation Architecture Review & Discovery — Phase 5 (Room DB History, Search & Settings DataStore)

**Phase Scope**: Room Database Entity & DAO, Download History Repository with Full-Text Search, History Screen UI with Filter Chips, Preferences DataStore for Settings.
**Engineering Standard**: Google / AOSP Open-Source Production Grade
**Governance**: Strict Single-Phase Isolation, Pre-Implementation Seal Comparison, Jetpack Room DB Compliance.

---

## 1. Project Documentation Summary (Phase 5 Baseline)

### PRD.md Requirements
- **FR9 (Download History)**: Persistent history of completed downloads showing thumbnail, title, author, date, file size, format. Full-text search and filtering by media type (Reel, Post, Carousel). Ability to redownload, share, or delete history record.
- **FR10 (Settings & Configuration)**: Dark theme toggle, auto-paste clipboard toggle, max concurrent downloads selector, default download folder indicator.

### ARCHITECTURE.md Requirements
- **Section 6 (Database & Persistence)**: Room DB `instasave_db` with `DownloadEntity` table, indexed on `timestamp` and `author`. Hilt `@Provides` module `DatabaseModule`. Preferences DataStore `user_settings.preferences_pb`.

---

## 2. Mandatory Seal Reference Comparison Table

| Subsystem / Area | InstaSave Spec | Seal Implementation | Final Decision | Rationale for Deviation / Adaptation |
|---|---|---|---|---|
| **Database Architecture** | Room DB (`instasave.db`) + `DownloadDao` | Room DB `seal.db` + `DownloadHistoryDao` | **Adapt Seal Room DB Pattern**: Standard Room DB entity with flow query streams | Battle-tested schema pattern ensuring fast queries and non-blocking Flow streams. |
| **History Search & Filters** | Search query StateFlow + filter chips (All, Video, Photo, Carousel) | Text search + type filters in Seal | **InstaSave History UI**: Material 3 search bar + FilterChips + HistoryCard list | Native Compose UI with smooth filtering state. |
| **Settings Storage** | Jetpack Preferences DataStore (`user_preferences`) | DataStore / SharedPreferences in Seal | **Jetpack DataStore**: Flow-based asynchronous key-value persistence | Modern Android recommendation for robust key-value setting updates. |

---

## 3. Affected Modules & File Inventory

| Module / Component | Action | Target File Path | Purpose |
|---|---|---|---|
| **Room Entity** | `NEW` | `app/src/main/java/com/instasave/app/core/database/entity/DownloadEntity.kt` | Room table definition for downloaded media history. |
| **Room DAO** | `NEW` | `app/src/main/java/com/instasave/app/core/database/dao/DownloadDao.kt` | Data Access Object for Room database queries. |
| **Room Database** | `NEW` | `app/src/main/java/com/instasave/app/core/database/InstaSaveDatabase.kt` | Room Database class. |
| **Hilt DB Module** | `NEW` | `app/src/main/java/com/instasave/app/di/DatabaseModule.kt` | Hilt DI module providing Room DB and DAO instances. |
| **History Repository** | `NEW` | `app/src/main/java/com/instasave/app/core/data/repository/DownloadHistoryRepository.kt` | Repository combining Room queries into reactive state. |
| **History Screen UI** | `NEW` | `app/src/main/java/com/instasave/app/ui/history/HistoryScreen.kt` | Compose screen with search bar, filter chips, & item cards. |
| **Settings Screen UI** | `NEW` | `app/src/main/java/com/instasave/app/ui/settings/SettingsScreen.kt` | Preferences DataStore settings screen. |

---

## 4. Dependencies & Version Lock

- **Android SDK**: `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`
- **Room DB**: `2.6.1` (`androidx.room:room-runtime`, `androidx.room:room-ktx`)
- **DataStore**: `1.1.2` (`androidx.datastore:datastore-preferences`)

---

## 5. Architectural Risks & Mitigation Strategies

| Risk ID | Risk Description | Severity | Mitigation Strategy |
|---|---|---|---|
| **RISK-01** | Room DB schema migration crash on future updates. | **MEDIUM** | Enable `fallbackToDestructiveMigration()` during initial development phases. |
| **RISK-02** | Main thread IO disk block when querying history. | **LOW** | All Room queries return `Flow<List<DownloadEntity>>` running on `Dispatchers.IO`. |

---

## 6. Assumptions & Constraints

1. **Reactive Flow Queries**: UI updates automatically whenever new items are added to Room DB.
2. **Remote Container Execution**: All builds and tests are executed remotely in cloud container `literate-space-zebra-x74gwwwqv54cppp9`.
