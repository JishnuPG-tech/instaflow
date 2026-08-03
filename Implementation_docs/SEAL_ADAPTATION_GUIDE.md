# SEAL ADAPTATION GUIDE

Version: 2.0

Project: InstaFlow

Upstream:
https://github.com/JunkFood02/Seal

License:
GPLv3

Status:
Living Engineering Document

---

# 1. Purpose

This document explains how every major subsystem of Seal should be treated when evolving it into InstaFlow.

It prevents unnecessary rewrites.

It preserves years of mature engineering.

It ensures every contributor follows the same migration strategy.

---

# 2. Engineering Philosophy

Seal is the engineering foundation.

InstaFlow is the Instagram specialization.

Do not redesign stable infrastructure.

Redesign only Instagram-specific workflows.

Always preserve mature engineering.

---

# 3. Adaptation Rules

Every subsystem belongs to one category.

KEEP

MODIFY

REPLACE

REMOVE

Every implementation must document which category it belongs to.

---

# 4. KEEP

These systems should remain almost identical to Seal.

Reason:

Already mature.

Already tested.

Already production quality.

Subsystems

✓ Download Queue

✓ WorkManager

✓ Foreground Service

✓ Room

✓ MediaStore

✓ Storage Access Framework

✓ Notification System

✓ Theme System

✓ Settings Architecture

✓ Localization

✓ Translation Framework

✓ History

✓ Cookie Manager

✓ youtubedl-android

✓ aria2

Only improve when a measurable benefit exists.

---

# 5. MODIFY

These systems require Instagram-specific changes.

Download Engine

Reason

Instagram media behaves differently.

Changes

Improve metadata extraction.

Support mixed media.

Improve media detection.

---

Cookie UX

Keep implementation.

Improve discoverability.

Simplify importing.

Improve validation feedback.

---

Settings

Remove irrelevant options.

Keep architecture.

---

History

Add Instagram metadata.

Media thumbnails.

Media type badges.

Username.

Caption preview.

---

About Screen

Replace branding.

Maintain GPL attribution.

---

# 6. REPLACE

These systems should be redesigned.

Playlist

↓

Carousel

--------------------------------

Format Picker

↓

Instagram Media Picker

--------------------------------

Supported Sites

↓

Instagram Only

--------------------------------

Video-first UI

↓

Media-first UI

--------------------------------

Generic Download Actions

↓

Instagram Download Actions

---

# 7. REMOVE

Remove only features with no Instagram value.

Examples

SponsorBlock

Subtitle UI

Playlist terminology

Site selection

YouTube specific preferences

Generic extractor UI

Do not remove infrastructure.

Only remove UX.

---

# 8. New Systems

Instagram Media Model

Instagram Preview Screen

Carousel Picker

Media Selection

Profile Picture Downloader

Caption Export

Metadata Viewer

These are new InstaFlow systems.

---

# 9. Download Engine

Keep

Queue

Retry

Pause

Resume

Notifications

Storage

Replace

Media parsing

Selection logic

Metadata model

---

# 10. Compose UI

Keep

Navigation

Architecture

State handling

Component hierarchy

Replace

Screens

Cards

Terminology

Media presentation

---

# 11. ViewModels

Keep architecture.

Modify business logic.

Never move business logic into Compose.

---

# 12. Repository Layer

Reuse.

Extend.

Avoid duplication.

Repositories remain the single source of truth.

---

# 13. WorkManager

No architectural changes.

Reuse.

Only extend worker responsibilities where required.

---

# 14. Room

Reuse.

Possible additions

Media metadata

Favorites

Collections

Future tables should use Room migrations.

---

# 15. Notifications

Reuse.

Add Instagram-specific icons and wording.

Support

Download Selected

Download All

Carousel progress

---

# 16. History

Enhance.

Display

Thumbnail

Username

Media type

Caption preview

Download date

---

# 17. Settings

Keep architecture.

Simplify options.

Hide advanced features unless needed.

---

# 18. Cookie Management

Never rewrite.

Reuse upstream implementation.

Improve

Import UX

Validation

Documentation

---

# 19. youtubedl-android

Treat as infrastructure.

Never modify without strong justification.

Prefer updating upstream.

---

# 20. aria2

Reuse.

Do not expose complexity to users.

---

# 21. Build System

Reuse Gradle structure.

Reuse version catalog.

Reuse CI.

Only modify when project requirements demand it.

---

# 22. File Structure

Preserve package organization whenever practical.

Avoid moving files unnecessarily.

Prefer extension over relocation.

---

# 23. Naming

Replace

Seal

↓

InstaFlow

Replace

Playlist

↓

Carousel

Replace

Video Format

↓

Media Item

Replace

Download Formats

↓

Download Options

Keep terminology consistent.

---

# 24. Testing

Every modified subsystem requires

Unit Tests

Integration Tests

Regression Tests

Every kept subsystem requires regression verification after adjacent changes.

---

# 25. Migration Checklist

For every subsystem complete:

Locate in Seal

↓

Understand

↓

Classify

↓

Document

↓

Modify

↓

Compile

↓

Test

↓

Review

↓

Merge

---

# 26. Future Sync

Monitor upstream Seal releases.

Review

Security fixes

Dependency updates

Bug fixes

Performance improvements

Merge when beneficial.

Keep divergence as small as practical.

---

# 27. Success Criteria

The adaptation is successful when:

Users recognize a polished Instagram application.

Developers recognize Seal's mature engineering.

The architecture remains clean.

Future upstream merges remain feasible.

Technical debt is minimized.

The project evolves without losing its engineering foundation.