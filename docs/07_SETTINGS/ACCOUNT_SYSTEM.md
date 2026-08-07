# Account System Architecture — Netscape Format & DB Backup

## 1. Subsystem Purpose

Enables media extraction for age-gated YouTube videos, private posts, or subscriber-only streams by injecting Netscape formatted session accounts into `yt-dlp` requests.

---

## 2. Account Lifecycle & Workflow

```mermaid
sequenceDiagram
    participant User as User
    participant UI as AccountsViewModel
    participant DB as Room DB (AccountProfile)
    participant File as FileUtil (cookies.txt)
    participant Engine as Downloader (yt-dlp)

    User->>UI: Import session file
    UI->>UI: Parse & Validate Netscape Format
    UI->>DB: Insert AccountProfile Entity
    DB->>File: Write active session to app private storage
    Engine->>File: Read --cookies /data/user/0/com.junkfood.seal/files/cookies.txt
    Engine->>User: Media Download Success
```

---

## 3. Storage Security Rules

- Account sessions are stored in Room database (`AccountProfile` table) and exported strictly to app-private internal storage (`/data/data/com.instaflow.app/files/cookies.txt`).
- Never committed to git or exposed to external apps.
