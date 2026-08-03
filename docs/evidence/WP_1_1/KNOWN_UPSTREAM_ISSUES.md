# WP 1.1 KNOWN UPSTREAM ISSUES & WARNINGS

## 1. Upstream `ktfmtCheckMain` Formatting Discrepancies

- **Issue**: Upstream repository contains 6 source files that fail `ktfmtCheckMain` strict formatting checks out of the box:
  - `src/main/java/com/junkfood/seal/ui/page/download/DownloadPage.kt`
  - `src/main/java/com/junkfood/seal/ui/page/AppEntry.kt`
  - `src/main/java/com/junkfood/seal/ui/page/downloadv2/DownloadPageV2.kt`
  - `src/main/java/com/junkfood/seal/download/Task.kt`
  - `src/main/java/com/junkfood/seal/util/DownloadUtil.kt`
  - `src/main/java/com/junkfood/seal/download/TaskFactory.kt`
- **Policy**: Do not fix upstream formatting files until modifying those specific files during upcoming work packages.

---

## 2. Accompanist WebView Deprecation Warnings

- **Issue**: Compiler warnings logged for `AccompanistWebViewClient` and `rememberWebViewState` in `WebViewPage.kt`.
- **Policy**: Maintain upstream implementation; migrate to native AndroidX Webkit in Phase 3/4.
