# ARCHITECTURE OPPORTUNITIES — Strategic Refactoring

## 1. Domain Module Isolation

Decouple generic Android UI components from core media extraction engine:

```
insta-flow-android/
├── core-engine/        # Pure Kotlin MediaExtractor, Task queue, CLI Builder
├── core-database/      # Room DB, DAOs, Entities
├── core-ui/            # Design System, Material 3 Expressive theme, Icons
└── app/                # Android Application, ViewModels, Compose Navigation
```

## 2. Reactive Task Pipeline with Kotlin Flow

Replace raw callbacks in `youtubedl-android` with cold Kotlin `Flow<DownloadProgress>` streams:

```kotlin
interface MediaExtractor {
    fun extractInfo(url: String): Flow<MediaInfoState>
    fun downloadMedia(task: DownloadTask): Flow<DownloadProgressState>
}
```

This guarantees reactive progress updates directly to Compose UI without manual notification broadcasting or polling logic.
