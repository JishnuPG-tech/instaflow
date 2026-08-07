# Entity Reference — Database Models

## 1. `DownloadedVideoInfo` (`DownloadedVideoInfo` Table)
- **Primary Key**: `id: Int = 0` (AutoGenerate)
- **Fields**:
  - `videoTitle: String` — Extracted video title.
  - `videoAuthor: String` — Channel or creator name.
  - `videoUrl: String` — Original media URL.
  - `thumbnailUrl: String` — Cover art preview URL.
  - `videoPath: String` — Storage Access Framework URI string or absolute file path.
  - `instagramUsername: String?` — Instagram user handle.
  - `captionText: String?` — Media caption.

## 2. `AccountProfile` (`AccountProfile` Table)
- **Primary Key**: `id: Int` (AutoGenerate)
- **Fields**:
  - `url: String` — Profile URL or site name.
  - `content: String` — Full Netscape format session string.

## 3. `CommandTemplate` (`CommandTemplate` Table)
- **Primary Key**: `id: Int`
- **Fields**:
  - `name: String` — Name of custom preset.
  - `template: String` — CLI option string.
