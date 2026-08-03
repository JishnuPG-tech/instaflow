# FEATURE MATRIX

Version: 2.0

Project: InstaFlow

Status:
Living Feature Matrix

Based on:
Seal

License:
GPLv3

---

# 1. Purpose

This document tracks every feature in the project.

It compares:

• Seal

• InstaFlow

• Current Status

• Future Roadmap

It is the primary document used during implementation and release planning.

Every feature must appear here.

---

# Legend

✅ Complete

🔄 In Progress

📋 Planned

❌ Not Supported

🗑 Removed

⭐ InstaFlow Exclusive

---

# 2. Core Infrastructure

| Feature | Seal | InstaFlow | Status |
|----------|------|-----------|--------|
| Compose | ✅ | ✅ | 🔄 |
| MVVM | ✅ | ✅ | 🔄 |
| Repository Pattern | ✅ | ✅ | 🔄 |
| Hilt | ✅ | ✅ | 🔄 |
| Room | ✅ | ✅ | 🔄 |
| WorkManager | ✅ | ✅ | 🔄 |
| Foreground Service | ✅ | ✅ | 🔄 |
| Notifications | ✅ | ✅ | 🔄 |
| MediaStore | ✅ | ✅ | 🔄 |
| SAF | ✅ | ✅ | 🔄 |
| History | ✅ | ✅ | 🔄 |
| Download Queue | ✅ | ✅ | 🔄 |
| Theme System | ✅ | ✅ | 🔄 |

---

# 3. Supported Sites

| Site | Seal | InstaFlow |
|------|------|-----------|
| YouTube | ✅ | ❌ |
| Instagram | ✅ | ✅ |
| TikTok | ✅ | ❌ |
| Facebook | ✅ | ❌ |
| Twitter/X | ✅ | ❌ |
| Reddit | ✅ | ❌ |
| Vimeo | ✅ | ❌ |

Goal

Instagram only.

---

# 4. Instagram Media Types

| Media | Status |
|--------|--------|
| Images | 📋 |
| Videos | 📋 |
| Reels | 📋 |
| Stories | 📋 |
| Highlights | 📋 |
| Profile Pictures | 📋 |
| Carousel Images | 📋 |
| Carousel Videos | 📋 |
| Mixed Carousels | 📋 |
| Captions | 📋 |
| Metadata | 📋 |

---

# 5. Download Options

| Option | Status |
|----------|--------|
| Download Image | 📋 |
| Download Video + Audio | 📋 |
| Download Video Only | 📋 |
| Download Audio Only | 📋 |
| Download Selected | 📋 |
| Download All | 📋 |

---

# 6. Media Preview

| Feature | Status |
|----------|--------|
| Preview Screen | 📋 |
| Image Preview | 📋 |
| Video Preview | 📋 |
| Metadata | 📋 |
| Caption | 📋 |
| Audio Indicator | 📋 |

---

# 7. Carousel

| Feature | Status |
|----------|--------|
| Carousel Detection | 📋 |
| Mixed Media | 📋 |
| Individual Selection | 📋 |
| Select All | 📋 |
| Download Selected | 📋 |
| Download All | 📋 |

---

# 8. Stories

| Feature | Status |
|----------|--------|
| Story Preview | 📋 |
| Story Download | 📋 |
| Story Metadata | 📋 |

---

# 9. Highlights

| Feature | Status |
|----------|--------|
| Browse Highlights | 📋 |
| Download Highlight | 📋 |
| Download Story | 📋 |

---

# 10. Profile Pictures

| Feature | Status |
|----------|--------|
| Highest Resolution | 📋 |
| Preview | 📋 |
| Download | 📋 |

---

# 11. Metadata

| Feature | Status |
|----------|--------|
| Username | 📋 |
| Caption | 📋 |
| Date | 📋 |
| Location | 📋 |
| Audio Title | 📋 |
| Hashtags | 📋 |
| Dimensions | 📋 |
| Duration | 📋 |

---

# 12. Cookie Features

| Feature | Status |
|----------|--------|
| Import Cookies | 🔄 |
| Validate Cookies | 📋 |
| Cookie Required Detection | 📋 |
| Cookie Settings | 📋 |

---

# 13. Queue

| Feature | Status |
|----------|--------|
| Queue | 🔄 |
| Pause | 🔄 |
| Resume | 🔄 |
| Retry | 📋 |
| Cancel | 🔄 |

---

# 14. History

| Feature | Status |
|----------|--------|
| Download History | 🔄 |
| Search | 📋 |
| Filter | 📋 |
| Sort | 📋 |
| Share | 📋 |

---

# 15. Settings

| Feature | Status |
|----------|--------|
| Downloads | 🔄 |
| Storage | 🔄 |
| Appearance | 🔄 |
| Cookies | 📋 |
| About | 🔄 |

---

# 16. Accessibility

| Feature | Status |
|----------|--------|
| TalkBack | 📋 |
| Font Scaling | 📋 |
| Contrast | 📋 |
| Landscape | 📋 |
| Keyboard | 📋 |

---

# 17. Performance

| Feature | Status |
|----------|--------|
| Startup | 📋 |
| Memory | 📋 |
| Queue | 📋 |
| Compose | 📋 |

---

# 18. Security

| Feature | Status |
|----------|--------|
| URL Validation | 📋 |
| Safe Storage | 📋 |
| Cookie Protection | 📋 |
| Filename Validation | 📋 |

---

# 19. Release

| Feature | Status |
|----------|--------|
| APK | 📋 |
| AAB | 📋 |
| GitHub Release | 📋 |
| Changelog | 📋 |

---

# 20. InstaFlow Exclusive Features

These features do not exist in Seal.

⭐ Native Instagram Media Picker

⭐ Carousel Selection

⭐ Mixed Media Support

⭐ Instagram Metadata Viewer

⭐ Caption Export

⭐ Profile Picture Downloader

⭐ Instagram-first Preview Screen

⭐ Download Selected

⭐ Media Type Badges

⭐ Instagram-only Workflow

---

# 21. Removed Features

Removed because they are not relevant to Instagram.

🗑 SponsorBlock

🗑 Subtitle Download UI

🗑 Playlist Workflow

🗑 Generic Supported Sites

🗑 YouTube Terminology

---

# 22. Definition of Complete

A feature is complete only when:

✓ Implemented

✓ Tested

✓ Documented

✓ Regression Tested

✓ Reviewed

✓ No critical bugs

Only then change its status to:

✅ Complete