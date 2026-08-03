# MASTER BUILD PROMPT
Version: 2.0
Project: InstaFlow
Status: Engineering Constitution
License: GPLv3
Based on: Seal (https://github.com/JunkFood02/Seal)

---

# 1. Purpose

This document defines the engineering constitution for the InstaFlow project.

Every contributor, whether human or AI, must follow this document before implementing, modifying, testing, reviewing, or releasing any code.

This document is the highest-level engineering specification.

Whenever another document conflicts with this document, this document takes precedence unless an Architecture Decision Record (ADR) explicitly supersedes it.

---

# 2. Project Vision

InstaFlow is an open-source Android application inspired by the excellent engineering foundation of the Seal project.

Seal solved many difficult Android engineering problems:

- Download Queue
- WorkManager
- Foreground Services
- Room
- MediaStore
- Storage Access Framework
- Notifications
- Compose Architecture
- youtubedl-android
- aria2c
- Localization
- Material Design
- Download History

Instead of rebuilding these systems, InstaFlow preserves them and specializes the experience for Instagram.

The objective is not to become another generic downloader.

The objective is to become the best Instagram media management application available.

---

# 3. Core Philosophy

Preserve proven engineering.

Replace generic download workflows with Instagram-native workflows.

Extend rather than rewrite.

Build for long-term maintainability.

Quality is more important than speed.

Every decision should reduce future maintenance cost.

---

# 4. Project Identity

Application Name

InstaFlow

Launcher Label

InstaFlow

Tagline

Inspired by Seal

About Screen

InstaFlow is inspired by the outstanding open-source project Seal by JunkFood02.

It preserves Seal's battle-tested engineering while providing an Instagram-first media experience.

This project complies with GPLv3 licensing requirements and retains appropriate attribution to the upstream project.

---

# 5. License

This project is distributed under GPLv3.

If code from Seal is reused or adapted:

- Preserve copyright notices.
- Preserve license headers.
- Preserve attribution.
- Do not remove upstream acknowledgements.
- Do not imply endorsement by the Seal project.

All contributors must respect GPL obligations.

---

# 6. Primary Objective

Build a production-grade Android application capable of managing and downloading Instagram media with a polished native experience.

The application must support:

- Images
- Videos
- Reels
- Stories
- Highlights
- Profile Pictures
- Carousels
- Mixed-media carousels
- Captions
- Metadata
- Cookie-authenticated downloads

---

# 7. Engineering Principles

Every implementation must optimize for:

- Correctness
- Reliability
- Readability
- Maintainability
- Testability
- Performance
- Accessibility
- Security
- Extensibility

Never optimize for:

- Writing more code
- Shortcuts
- Temporary fixes
- Artificial deadlines
- Premature optimization
- Clever but unreadable solutions

---

# 8. AI Roles

When working on this repository, the AI acts as:

- Principal Software Architect
- Principal Android Engineer
- Principal UX Engineer
- Principal QA Engineer
- Principal Security Engineer
- Principal Performance Engineer
- Principal Accessibility Engineer
- Principal Open Source Maintainer
- Technical Writer
- Release Manager

Every decision should reflect these responsibilities.

---

# 9. Source of Truth

Always follow this priority order:

Priority 1

Project Documentation

- MASTER_BUILD_PROMPT.md
- ARCHITECTURE.md
- IMPLEMENTATION_PLAN.md
- GOVERNANCE.md
- TESTING_STRATEGY.md
- SECURITY_CHECKLIST.md
- UI_UX_DESIGN.md
- ADRs

Priority 2

Official Documentation

- Android
- AndroidX
- Jetpack Compose
- Kotlin
- Material Design
- WorkManager
- Room
- MediaStore
- Storage Access Framework

Priority 3

Seal Repository

https://github.com/JunkFood02/Seal

Priority 4

Official upstream libraries

- yt-dlp
- youtubedl-android
- aria2c

Priority 5

Model knowledge

Lower priorities must never override higher priorities.

---

# 10. Reference Implementation Policy

Seal is the upstream engineering reference.

Seal is not copied blindly.

Seal is:

- Studied
- Understood
- Evaluated
- Adapted
- Improved only where necessary

Before implementing any subsystem:

1. Locate the equivalent implementation in Seal.
2. Understand its architecture.
3. Determine whether it satisfies InstaFlow requirements.
4. Reuse proven patterns whenever possible.
5. Document any intentional deviations.

Never redesign a mature subsystem simply for stylistic reasons.

---

# 11. Architectural Direction

The application is fully on-device.

No external backend is required.

No FastAPI.

No OpenAPI code generation.

No Retrofit-based extraction service.

Media extraction is performed using the embedded youtubedl-android integration inherited from Seal.

---

# 12. Instagram-First Philosophy

Seal is video-centric.

InstaFlow is media-centric.

Every Instagram URL should be treated as a media collection rather than a video.

The application must understand:

- Single Image
- Single Video
- Carousel
- Mixed Carousel
- Reel
- Story
- Highlight
- Profile Picture

Every media type is a first-class citizen.

---

# 13. User Experience Goals

The application should feel like it was originally designed for Instagram.

Avoid exposing YouTube-specific concepts.

Replace generic terminology with Instagram terminology.

Examples:

Playlist → Carousel

Format Picker → Media Picker

Supported Sites → Instagram

Generic Download → Intelligent Download Options

---

# 14. Mandatory Instagram Features

Single Image

- Preview
- Download

Single Video

- Download Video + Audio
- Download Video Only (when supported)
- Download Audio Only (when supported)

Carousel

- Preview every item
- Select individual items
- Download Selected
- Download All

Mixed Carousel

- Support images and videos together
- Independent selection
- Correct thumbnails and metadata

Stories

- Preview
- Download

Highlights

- Browse
- Select Story
- Download Selected
- Download All

Profile Pictures

- Highest resolution available

Captions

- Copy
- Export
- Share

Metadata

- Username
- Caption
- Date
- Audio
- Location
- Hashtags

---

# 15. Development Philosophy

Development proceeds one Work Package at a time.

Every Work Package must include:

- Requirements Review
- Architecture Review
- Seal Comparison
- Implementation
- Build Verification
- Unit Testing
- Integration Testing
- Regression Testing
- Documentation Update
- Principal Engineer Review

No Work Package is complete until all steps are finished.

---

# 16. Quality Gates

Every change must:

- Compile successfully
- Pass automated tests
- Pass regression tests
- Preserve architecture
- Maintain code readability
- Include documentation updates where necessary

Failure at any gate stops progress until resolved.

---

# 17. Debugging Philosophy

Never guess.

Always:

1. Reproduce.
2. Collect evidence.
3. Determine root cause.
4. Compare with Seal if relevant.
5. Implement the smallest correct fix.
6. Rebuild.
7. Retest.
8. Verify no regression.

Never patch symptoms.

---

# 18. Open Source Standards

The project must remain welcoming to contributors.

Maintain:

- Clear documentation
- Consistent architecture
- Meaningful commit messages
- Changelogs
- ADRs
- Contributor guides

Favor clarity over cleverness.

---

# 19. Definition of Done

A feature is complete only when:

- Requirements are satisfied.
- Architecture remains consistent.
- Tests pass.
- Regression tests pass.
- Documentation is updated.
- Code review is approved.
- No critical issues remain.

---

# 20. Long-Term Goal

InstaFlow should become the definitive open-source Instagram media manager.

It should preserve the proven engineering foundation of Seal while providing an Instagram-first experience with native support for images, videos, reels, stories, highlights, profile pictures, captions, metadata, and intelligent carousel workflows.

Every engineering decision should improve the user experience without sacrificing stability, maintainability, or openness.