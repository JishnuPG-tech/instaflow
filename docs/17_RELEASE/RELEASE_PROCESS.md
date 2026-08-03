# Release Process & APK Versioning Scheme

## 1. Version Code Mapping Rule ([`app/build.gradle.kts`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/build.gradle.kts))

Version codes combine base version code with target ABI architecture codes:

```kotlin
val abiCodes = mapOf("armeabi-v7a" to 1, "arm64-v8a" to 2, "x86" to 3, "x86_64" to 4)
output.versionCode.set(baseAbiCode + (output.versionCode.get() ?: 0))
```

This ensures Google Play and custom package managers install the appropriate native binary split for the user's CPU hardware architecture.

---

## 2. Output Naming Scheme

`Seal-${defaultConfig.versionName}-${flavor}-${abi}.apk` (e.g. `Seal-1.12.0-generic-arm64-v8a.apk`).
