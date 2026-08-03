# Preference Utility Reference — `PreferenceUtil.kt`

## 1. Overview

[`PreferenceUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/PreferenceUtil.kt) provides single-point static extension methods for reading and mutating MMKV preferences.

---

## 2. Core API Methods

```kotlin
object PreferenceUtil {
    fun updateString(key: String, value: String)
    fun getString(key: String, default: String): String
    fun updateBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, default: Boolean): Boolean
    
    // Convenience Accessors
    fun getDownloadDirectoryUri(): String
    fun isAria2Enabled(): Boolean
    fun getAria2ConcurrentConnections(): Int
    fun isPureBlackEnabled(): Boolean
    fun getCustomUserAgent(): String
}
```
