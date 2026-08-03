# WP 1.1 BUILD REPORT — Upstream Baseline Compilation

## 1. Environment Configuration

- **Host OS**: `Linux 6.8.0-1052-azure` (Ubuntu 22.04 LTS x86_64)
- **JDK Version**: OpenJDK `21.0.11`
- **Gradle Version**: `8.10.2`
- **Android Gradle Plugin**: `8.7.2`
- **Kotlin Version**: `2.0.20`

---

## 2. APK Artifact Output Inventory (`app/build/outputs/apk/`)

| Product Flavor | Build Type | Target ABI | Output File Name |
| :--- | :--- | :--- | :--- |
| `generic` | `debug` | `arm64-v8a` | `Seal-1.12.0-generic-arm64-v8a-debug.apk` |
| `generic` | `debug` | `armeabi-v7a` | `Seal-1.12.0-generic-armeabi-v7a-debug.apk` |
| `generic` | `debug` | `x86_64` | `Seal-1.12.0-generic-x86_64-debug.apk` |
| `generic` | `debug` | `universal` | `Seal-1.12.0-generic-universal-debug.apk` |
| `generic` | `release` | `universal` | `Seal-1.12.0-generic-universal-release-unsigned.apk` |
| `githubPreview` | `debug` | `universal` | `Seal-1.12.0-githubPreview-universal-debug.apk` |
| `fdroid` | `debug` | `universal` | `Seal-1.12.0-(F-Droid)-fdroid-universal-debug.apk` |
