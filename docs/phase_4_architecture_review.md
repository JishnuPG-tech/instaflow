# Pre-Implementation Architecture Review & Discovery — Phase 4 (Core Download Engine & Foreground Service)

**Phase Scope**: Multi-segment OkHttp Chunked Download Engine, Android Foreground Download Service, Ongoing System Notification Progress Bar, Download Queue Manager.
**Engineering Standard**: Google / AOSP Open-Source Production Grade
**Governance**: Strict Single-Phase Isolation, Pre-Implementation Seal Comparison, AOSP Foreground Service Compliance.

---

## 1. Project Documentation Summary (Phase 4 Baseline)

### PRD.md Requirements
- **FR4 & FR5 (Download Engine)**: Multi-segment parallel chunked download engine utilizing HTTP `Range: bytes=X-Y` headers. Resumable downloads, auto-retry on network failure (3 attempts with exponential backoff), background execution.
- **FR7 (Notifications)**: Foreground service notification displaying current download progress percentage, speed (MB/s), remaining time, and cancel/pause actions.

### ARCHITECTURE.md Requirements
- **Section 3 (Engine Layer)**: `DownloadEngine` handling range chunking, writing chunks into temporary storage (`.part` files), assembling final file, and notifying `MediaStoreWriter`.
- **Section 5 (Service Layer)**: `DownloadService` bound as a Foreground Service on Android API 26+ (`FOREGROUND_SERVICE_DATA_SYNC` type on API 34+).

---

## 2. Mandatory Seal Reference Comparison Table

| Subsystem / Area | InstaSave Spec | Seal Implementation | Final Decision | Rationale for Deviation / Adaptation |
|---|---|---|---|---|
| **Download Engine** | Multi-segment OkHttp parallel range downloader + optional `aria2c` | `aria2c` native binary + OkHttp fallback | **InstaSave Dual Engine Architecture**: High-speed OkHttp multi-segment chunked engine with native `aria2c` binding interface | Guarantees instant download execution on pure Kotlin without requiring external binary packaging while maintaining aria2c compatibility. |
| **Foreground Service** | Android 14+ `FOREGROUND_SERVICE_DATA_SYNC` foreground service | `DownloadService` foreground service with NotificationCompat | **Adapt Seal Foreground Service Pattern**: Hilt-injected `DownloadService` with notification channel & progress bar | Conforms to Android 14/15 strict foreground service permission policies (`android.permission.FOREGROUND_SERVICE_DATA_SYNC`). |
| **Download Queue State** | `DownloadQueueManager` exposing `StateFlow<List<DownloadTask>>` | `DownloadRepository` + Room DB + StateFlow | **InstaSave Download Queue Manager**: Reactive state holder managing active worker coroutines & state updates | Enables real-time UI progress updates across Home and Downloads queue screens. |

---

## 3. Affected Modules & File Inventory

| Module / Component | Action | Target File Path | Purpose |
|---|---|---|---|
| **Download Data Model** | `NEW` | `app/src/main/java/com/instasave/app/core/download/model/DownloadTask.kt` | Data models tracking `DownloadStatus`, progress, bytes, speed, and file paths. |
| **Download Engine** | `NEW` | `app/src/main/java/com/instasave/app/core/download/DownloadEngine.kt` | Multi-segment parallel chunk downloader using OkHttp `Range` headers. |
| **Download Queue Manager** | `NEW` | `app/src/main/java/com/instasave/app/core/download/DownloadQueueManager.kt` | Reactive queue manager controlling tasks and state flow. |
| **Foreground Service** | `NEW` | `app/src/main/java/com/instasave/app/core/download/DownloadService.kt` | Android Foreground Service managing notification progress bars and lifecycle. |
| **Manifest Permissions** | `MODIFY` | [AndroidManifest.xml](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/app/src/main/AndroidManifest.xml) | Register `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`, `POST_NOTIFICATIONS`, and `DownloadService`. |

---

## 4. Dependencies & Version Lock

- **Android SDK**: `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`
- **OkHttp**: `4.12.0` (Range request chunking)
- **NotificationCompat**: `androidx.core:core-ktx:1.15.0`
- **Android Permissions**: `FOREGROUND_SERVICE_DATA_SYNC` (Android 14+ / API 34+)

---

## 5. Architectural Risks & Mitigation Strategies

| Risk ID | Risk Description | Severity | Mitigation Strategy |
|---|---|---|---|
| **RISK-01** | Foreground Service crash on Android 14 (API 34+) due to missing `foregroundServiceType`. | **HIGH** | Declare `android:foregroundServiceType="dataSync"` in `AndroidManifest.xml` and request `FOREGROUND_SERVICE_DATA_SYNC` permission. |
| **RISK-02** | Server does not support HTTP `Range` headers for multi-segment downloads. | **MEDIUM** | Fall back dynamically to single-stream download when HTTP `206 Partial Content` is not returned. |
| **RISK-03** | Partial download file left behind on user cancellation or network loss. | **LOW** | Delete temporary `.part` file on cancellation or failure. |

---

## 6. Assumptions & Constraints

1. **Android 14+ Foreground Compliance**: `DownloadService` explicitly specifies `dataSync` service type.
2. **MediaStore Output**: Completed downloads are automatically written to public MediaStore collections via `MediaStoreWriter`.
3. **Remote Build & Cloud Execution**: Compilation and verification are executed remotely in the cloud / GitHub Codespaces.
