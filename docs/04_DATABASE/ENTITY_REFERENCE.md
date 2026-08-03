# Entity Reference — Database Models

## 1. `DownloadedVideoInfo` (`video_info` Table)
- **Primary Key**: `id: Int = 0` (AutoGenerate)
- **Fields**:
  - `videoTitle: String` — Extracted video title.
  - `videoAuthor: String` — Channel or creator name.
  - `videoUrl: String` — Original media URL.
  - `thumbnailUrl: String` — Cover art preview URL.
  - `videoDuration: Int` — Duration in seconds.
  - `fileSizeBytes: Long` — Downloaded file size on disk.
  - `downloadPath: String` — Storage Access Framework URI string or absolute file path.
  - `downloadTimestamp: Long` — System time millis of completion.

## 2. `CookieProfile` (`cookie_profile` Table)
- **Primary Key**: `id: Int` (AutoGenerate)
- **Fields**:
  - `name: String` — Profile alias (e.g. "Instagram Cookies").
  - `content: String` — Full Netscape format cookie string.
  - `dateAdded: Long` — Timestamp of creation.

## 3. `CommandTemplate` (`command_template` Table)
- **Primary Key**: `id: Int`
- **Fields**:
  - `templateName: String` — Name of custom preset.
  - `commandString: String` — CLI option string (e.g. `--recode-video mp4`).
