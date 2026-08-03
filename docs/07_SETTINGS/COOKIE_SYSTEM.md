# Cookie System Architecture — Netscape Format & DB Backup

## 1. Subsystem Purpose

Enables media extraction for age-gated YouTube videos, private posts, or subscriber-only streams by injecting Netscape formatted cookies into `yt-dlp` requests.

---

## 2. Cookie Lifecycle & Workflow

```mermaid
sequenceDiagram
    participant User as User
    participant UI as CookiesViewModel
    participant DB as Room DB (CookieProfile)
    participant File as FileUtil (cookies.txt)
    participant Engine as Downloader (yt-dlp)

    User->>UI: Import cookies.txt file
    UI->>UI: Parse & Validate Netscape Format
    UI->>DB: Insert CookieProfile Entity
    DB->>File: Write active cookies to app private storage
    Engine->>File: Read --cookies /data/user/0/com.junkfood.seal/files/cookies.txt
    Engine->>User: Media Download Success
```

---

## 3. Storage Security Rules

- Cookies are stored in Room database (`CookieProfile` table) and exported strictly to app-private internal storage (`/data/data/com.junkfood.seal/files/cookies.txt`).
- Never committed to git or exposed to external apps.
