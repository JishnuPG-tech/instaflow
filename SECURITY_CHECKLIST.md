# SECURITY_CHECKLIST.md — InstaSave

**Purpose:** security requirements are mentioned piecemeal across PRD.md, ARCHITECTURE.md, and AGENT.md — this pulls them into one enforceable checklist. Treat every unchecked box here as a release blocker for Phase 6, not a nice-to-have.

---

## 1. Credential & Session Handling

- [ ] **v1 has no login, no session, no credential storage of any kind.** If any code path in v1 asks for or stores an Instagram username/password/cookie, that's out of scope and should be removed, not merged.
- [ ] No hardcoded API keys, tokens, or secrets anywhere in the repo — use environment variables / secrets management for the backend, never committed `.env` files with real values.
- [ ] (Post-MVP, Phase 7 only) session cookies stored via Jetpack Security `EncryptedSharedPreferences`/DataStore, never plaintext, never logged, never sent anywhere except the app's own backend over HTTPS.
- [ ] (Post-MVP) explicit, easy-to-find "log out / clear session" control — a user must always be able to remove a stored session.

## 2. Backend Input Validation (SSRF prevention)

This is the single most important backend security control — the backend takes a URL from the client and passes it to a subprocess/library that fetches network resources. Without strict validation, that's a direct SSRF vector.

- [ ] Strict allowlist: only `instagram.com` / `www.instagram.com` URL patterns accepted — reject everything else with `INVALID_URL`, including:
  - [ ] `localhost`, `127.0.0.1`, `0.0.0.0`
  - [ ] Private/internal IP ranges (`10.x`, `172.16-31.x`, `192.168.x`, link-local `169.254.x` — this specifically blocks cloud metadata-endpoint SSRF)
  - [ ] Non-http(s) schemes (`file://`, `ftp://`, etc.)
  - [ ] URLs with embedded credentials (`https://user:pass@...`)
- [ ] URL passed to yt-dlp/instaloader/gallery-dl as a **positional argument**, never shell-interpolated into a command string — command injection prevention
- [ ] Zod/Pydantic (whichever the backend uses) validates request shape before any URL touches the extraction layer

## 3. Rate Limiting & Abuse Prevention

- [ ] Per-IP rate limiting on `/api/resolve` (reasonable default: ~30 requests/15 min, tune based on real usage)
- [ ] File-size cap on any download path to prevent resource exhaustion
- [ ] No feature that enables bulk/automated scraping of an entire profile or account — this is a hard product boundary (PRD.md §8), not just a security concern: it's also what gets extraction methods detected and blocked faster for everyone

## 4. Temp File & Resource Hygiene

- [ ] Temp files cleaned up in `finally` blocks and on response-close events, not just the happy path
- [ ] Scheduled cleanup job (e.g., every 15 min) catches anything the per-request cleanup missed
- [ ] No media file is ever retained server-side beyond what's needed to serve a single request — the backend resolves/streams, it does not archive user downloads

## 5. Client-Side Storage & Permissions

- [ ] Scoped storage only — MediaStore or Storage Access Framework, never a raw file path (ARCHITECTURE.md §5)
- [ ] Runtime permissions (media library, notifications) requested only when actually needed, with a clear reason, never pre-emptively on app launch
- [ ] No `MANAGE_EXTERNAL_STORAGE`-style broad permission unless a specific, justified feature requires it

## 6. Privacy

- [ ] No analytics SDK by default (no Firebase Analytics, no Mixpanel, no equivalent) — if ever added, opt-in only and disclosed in Settings
- [ ] Download history stays on-device (Room), never synced to any server
- [ ] Backend logs, if any, never contain the raw Instagram URL or any user-identifying data in plaintext — hash or omit
- [ ] No third-party ad SDKs

## 7. Build & Release Security

- [ ] Signing keystore generated early, backed up securely outside the repo — losing it means the app can never be updated under the same signature again
- [ ] ProGuard/R8 rules reviewed — confirm no sensitive strings/logic are trivially recoverable in the release build
- [ ] No verbose/debug logging in release builds — specifically confirm no URL, file path, or (post-MVP) session data ever hits `Logcat` in a release build
- [ ] Dependencies (yt-dlp, instaloader, gallery-dl, aria2c, all Gradle/pip packages) pinned to specific versions, not floating `latest` — update deliberately, not silently, per IMPLEMENTATION_PLAN.md's ongoing maintenance notes

## 8. Legal Posture Reminder (from PRD.md §8, restated here as a gate)

- [ ] App remains single-user, on-device, no server-side content mirroring/archiving
- [ ] No bulk-profile-download feature exists in any phase
- [ ] Distribution via GitHub Releases/F-Droid, not Play Store, per the same reasoning Seal uses
- [ ] This is guidance, not legal advice — a real legal review is warranted before any monetization or distribution beyond personal/portfolio use

---

## How to Use This File

Run through every box before Phase 6 (Release Hardening) is considered complete. If a box can't be checked, that's a blocker for release, not something to note and ship anyway. Re-run this checklist in full again before any post-MVP phase (7/8) ships, since login/session handling reopens several of these categories.
