# SECURITY_CHECKLIST.md

Version: 2.0

Project: InstaFlow

Status: Production Security Standard

Based on: Seal (JunkFood02)

License: GPLv3

---

# 1. Purpose

This document defines the security requirements for InstaFlow.

Security is a core engineering responsibility.

Every feature must be evaluated for security impact before implementation.

Security reviews occur continuously throughout development.

No feature is complete until it satisfies this checklist.

---

# 2. Security Philosophy

The application must follow these principles:

• Least Privilege

• Defense in Depth

• Secure by Default

• Fail Securely

• Minimize Attack Surface

• Privacy First

• Never Trust User Input

• Explicit Validation

• Secure Local Storage

• No Hidden Behavior

---

# 3. Threat Model

Protect against:

✓ Malicious URLs

✓ Path Traversal

✓ Command Injection

✓ Cookie Leakage

✓ Sensitive File Exposure

✓ Download Directory Abuse

✓ Invalid File Names

✓ Permission Misuse

✓ Storage Corruption

✓ Unsafe Intent Handling

✓ Clipboard Abuse

✓ Notification Data Leakage

✓ Malicious Metadata

---

# 4. URL Validation

Only accept:

instagram.com

www.instagram.com

m.instagram.com

Reject:

youtube.com

facebook.com

twitter.com

tiktok.com

localhost

127.0.0.1

Private IPs

Unknown domains

Malformed URLs

Whitespace-only input

Extremely long URLs

Non-HTTPS URLs unless explicitly supported.

---

# 5. Cookie Security

Reuse Seal's cookie management.

Additional requirements:

Store cookies securely.

Never log cookies.

Never expose cookies in crash reports.

Never export cookies.

Validate cookie file before import.

Reject malformed cookie files.

Allow users to remove imported cookies.

Never transmit cookies to any external service.

---

# 6. Storage Security

All downloaded files must use:

MediaStore

Storage Access Framework

Never:

Write to arbitrary file paths.

Overwrite system files.

Escape selected directories.

Create hidden files.

Follow symbolic links outside user-selected locations.

Sanitize all generated filenames.

---

# 7. File Name Validation

Reject filenames containing:

../

..\

Null bytes

Reserved Windows names

Illegal Android filename characters

Excessively long names

Normalize Unicode filenames before saving.

---

# 8. Download Engine Security

Only invoke yt-dlp through the approved wrapper.

Never execute arbitrary shell commands.

Never concatenate user input into command strings.

Use structured argument lists.

Validate every generated command before execution.

---

# 9. Metadata Security

Metadata should never be trusted.

Validate:

Caption length

Username length

Audio title

Location

Hashtags

Unicode characters

Malformed JSON

Unexpected null values

Display metadata safely.

Never render HTML.

Never execute embedded content.

---

# 10. Permission Model

Request only required permissions.

Avoid unnecessary permissions.

Permissions should be requested:

Only when required.

Only immediately before use.

Never request broad storage permissions if SAF or MediaStore is sufficient.

---

# 11. Intent Security

Validate every incoming Intent.

Reject:

Missing data

Malformed data

Unsupported schemes

Unexpected MIME types

Do not expose internal Activities unnecessarily.

Use explicit intents where possible.

---

# 12. Clipboard Security

Only access the clipboard when initiated by the user.

Never continuously monitor clipboard contents.

Provide a user setting for clipboard detection if implemented in the future.

---

# 13. Notification Security

Notifications must never expose:

Cookie information

Private URLs

Sensitive metadata

Authentication details

Notification actions must validate state before execution.

---

# 14. Logging Policy

Never log:

Cookies

Authentication tokens

Private URLs

User file paths

Personally identifiable information

Allowed logs:

Lifecycle events

Download state

Queue status

Errors (sanitized)

Performance metrics

---

# 15. Error Handling

Display user-friendly messages.

Log detailed errors only in debug builds.

Release builds should never expose stack traces.

Avoid revealing implementation details.

---

# 16. Dependency Security

Dependencies must:

Come from trusted sources.

Be actively maintained.

Have compatible licenses.

Be updated regularly.

Review new dependencies before adoption.

Avoid unnecessary libraries.

---

# 17. yt-dlp Security

Use official releases only.

Verify downloaded updates where possible.

Do not execute unknown binaries.

Keep yt-dlp isolated behind the download engine.

---

# 18. aria2 Security

Reuse Seal's integration.

Validate download arguments.

Prevent arbitrary command injection.

Never expose internal configuration files.

---

# 19. Data Privacy

InstaFlow does not require a backend.

Do not collect:

User accounts

Analytics

Telemetry

Crash reporting without explicit consent

Advertising identifiers

Tracking identifiers

The application should function entirely on-device.

---

# 20. Secure Defaults

By default:

No cookies imported.

No background clipboard monitoring.

No telemetry.

No analytics.

No automatic uploads.

No external synchronization.

---

# 21. Release Security Checklist

Before every release verify:

✓ URL validation

✓ Cookie handling

✓ Storage access

✓ Download paths

✓ Permission requests

✓ Notification privacy

✓ Logging policy

✓ Dependency updates

✓ License compliance

✓ No known critical vulnerabilities

---

# 22. Security Review Questions

For every feature ask:

Does it introduce new permissions?

Does it increase attack surface?

Can user input reach yt-dlp?

Can it expose sensitive information?

Does it require an ADR?

Can it be abused?

How can it fail?

Can failure be handled safely?

---

# 23. Incident Response

If a security issue is discovered:

Stop release.

Reproduce.

Determine impact.

Implement minimal correct fix.

Add regression tests.

Document the issue.

Verify the fix.

Only then continue development.

---

# 24. Definition of Secure

A feature is secure when:

✓ Inputs are validated.

✓ Outputs are sanitized.

✓ Permissions are minimal.

✓ Storage is safe.

✓ No sensitive data leaks.

✓ Errors are handled safely.

✓ Logging is sanitized.

✓ Regression tests pass.

---

# 25. Long-Term Security

Security is an ongoing responsibility.

Review dependencies regularly.

Review permissions regularly.

Review cookie handling regularly.

Review storage handling regularly.

Review download engine updates regularly.

Every major release should include a dedicated security review.