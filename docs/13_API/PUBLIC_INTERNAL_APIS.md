# Public & Internal API Specifications

## 1. External Intent Entry Points (`QuickDownloadActivity.kt`)

[`QuickDownloadActivity.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/QuickDownloadActivity.kt) acts as a translucent share-target dialog:

```xml
<activity
    android:name=".QuickDownloadActivity"
    android:exported="true"
    android:theme="@style/Theme.Seal.Transparent">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

- Intercepts shared links from YouTube, Instagram, Twitter/X, TikTok, etc.
- Extracts `Intent.EXTRA_TEXT` string, parses valid HTTP/HTTPS URLs via `TextUtil`, and launches the quick download action sheet without forcing user into the main app UI.

---

## 2. Downloader Engine Interfaces

```kotlin
interface DownloaderV2 {
    val taskListFlow: StateFlow<List<Task>>
    fun enqueueTask(task: Task)
    fun cancelTask(taskId: String)
    fun pauseTask(taskId: String)
    fun resumeTask(taskId: String)
}
```
