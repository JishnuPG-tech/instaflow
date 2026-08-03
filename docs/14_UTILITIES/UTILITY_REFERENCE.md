# Utility Reference — Helper Objects

## 1. `DownloadUtil` ([`DownloadUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/DownloadUtil.kt))
- Assembles CLI request option arrays (`--format`, `--output`, `--cookies`, `--embed-metadata`).
- Parses format JSON metadata strings emitted by `yt-dlp -j`.

## 2. `FileUtil` ([`FileUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/FileUtil.kt))
- Resolves Storage Access Framework (SAF) document file URIs.
- Creates directory structures, writes cookie text files, and computes storage availability.

## 3. `UpdateUtil` ([`UpdateUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/UpdateUtil.kt))
- Connects to GitHub API (`api.github.com/repos/JunkFood02/Seal/releases/latest`) via OkHttp.
- Compares semantic version codes and prompts updates.
