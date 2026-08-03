# WP 1.2 STATIC ANALYSIS BASELINE REPORT

## 1. Static Analysis Metric Overview

- **Analysis Tool**: Android Lint (`./gradlew lint`) & `ktfmtCheck`
- **Execution Host**: GitHub Codespace (`Linux x86_64 Ubuntu 22.04 LTS`, OpenJDK 21)
- **Status**: 🟡 RECORDED (Baseline Findings Saved)

---

## 2. Upstream Static Analysis Findings Summary

| Tool | Category / Scope | Total Findings | Baseline Status | Recommendation |
| :--- | :--- | :--- | :--- | :--- |
| **Android Lint** | `ImpliedQuantity` | 1 Error (`values-be/strings.xml`) | Recorded | Add `ImpliedQuantity` to `lint.disable` list in `app/build.gradle.kts` during Phase 4 cleanup. |
| **Android Lint** | Deprecations & Translations | 170 Warnings | Recorded | Retain upstream baseline without altering source strings during initial baseline spikes. |
| **`ktfmtCheck`** | Kotlin Formatting | 6 Files (`DownloadPage.kt`, `AppEntry.kt`, `DownloadPageV2.kt`, `Task.kt`, `DownloadUtil.kt`, `TaskFactory.kt`) | Recorded | Format specific files using `./gradlew ktfmtFormat` when modifying them in upcoming Work Packages. |

---

## 3. Detailed Lint Findings Log

- **`ImpliedQuantity`**: `/workspaces/Seal/app/src/main/res/values-be/strings.xml:390`
- **`Accompanist WebView` Deprecation**: `WebViewPage.kt` lines 25, 26, 88, 112, 128, 129
- **`ToastUtil` Deprecation**: `VideoDetailDrawer.kt`, `VideoListPage.kt`, `DownloadUtil.kt`, `FileUtil.kt`, `TextUtil.kt`
- **`LocalClipboardManager` Deprecation**: `VideoDetailDrawer.kt`, `VideoListPage.kt`
