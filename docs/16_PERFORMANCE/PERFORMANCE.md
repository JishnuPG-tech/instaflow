# Performance Optimization & Resource Overhead

## 1. Native Binary Process Overhead

- **Python Runtime Overhead**: Spawning native `yt-dlp` via `youtubedl-android` creates a child process consuming ~30-70 MB RAM per active extraction.
- **Process Isolation**: Process lifecycle is isolated from main UI thread, ensuring zero frame drops or ANRs on Compose rendering.

## 2. Image Caching & Memory Management

- **Coil Disk Caching**: Caches remote video thumbnails locally to prevent duplicate network requests.
- **Room DB Paging**: History queries use Kotlin Flow to lazily load records into LazyColumn composables.
