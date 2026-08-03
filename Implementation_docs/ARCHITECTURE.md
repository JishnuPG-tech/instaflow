# ARCHITECTURE

Version: 2.0

Project: InstaFlow

Status: Production Architecture

Based on: Seal (JunkFood02)

License: GPLv3

---

# 1. Purpose

This document defines the technical architecture of InstaFlow.

Its purpose is to ensure every contributor implements features using the same architectural principles.

Architecture is intentionally conservative.

The project inherits mature engineering from Seal and extends it only where Instagram requires different behavior.

---

# 2. Architecture Philosophy

InstaFlow is NOT built from scratch.

InstaFlow is a production-grade specialization of Seal.

Seal has already solved:

- Download Queue
- WorkManager
- Foreground Services
- Room Database
- MediaStore
- SAF
- Notifications
- Compose Architecture
- youtubedl-android
- aria2c
- Settings
- Download History

These systems remain unless Instagram requirements demand changes.

Never rewrite proven infrastructure unnecessarily.

---

# 3. High-Level Architecture

                 User
                   │
                   ▼
            Jetpack Compose UI
                   │
                   ▼
              ViewModels (MVVM)
                   │
                   ▼
             Repository Layer
                   │
                   ▼
           Download Manager
                   │
      ┌────────────┴────────────┐
      ▼                         ▼
 Cookie Manager         Metadata Resolver
      │                         │
      └────────────┬────────────┘
                   ▼
          youtubedl-android
                   │
                   ▼
                 yt-dlp
                   │
                   ▼
              Instagram

All media resolution and downloading occur locally.

There is no application backend.

---

# 4. Design Principles

The architecture must satisfy:

- Single Responsibility Principle
- Separation of Concerns
- Dependency Inversion
- Unidirectional Data Flow
- Immutable UI State
- Repository Pattern
- MVVM
- Lifecycle Awareness
- Testability
- Extensibility

---

# 5. Layer Responsibilities

## Presentation Layer

Responsibilities

- Compose UI
- Navigation
- User interaction
- Animations
- Rendering state

Must never:

- Call yt-dlp
- Access Room directly
- Perform business logic
- Access storage directly

---

## ViewModel Layer

Responsibilities

- UI State
- Business Events
- Coroutine Scope
- User Actions
- Error Mapping

Must never:

- Access Android Views
- Know Compose implementation
- Parse downloads directly

---

## Repository Layer

Responsibilities

- Single source of truth
- Download requests
- Cookie handling
- Metadata
- History
- Storage coordination

Repositories hide implementation details from ViewModels.

---

## Download Engine

Responsibilities

- Queue
- Scheduling
- WorkManager
- Foreground Service
- Retry
- Pause
- Resume

The UI must never interact with yt-dlp directly.

---

## Extraction Layer

Responsibilities

- youtubedl-android
- yt-dlp invocation
- Metadata extraction
- Media discovery
- Format discovery

This layer should remain as close as possible to Seal.

---

# 6. Module Organization

Suggested package structure

com.instaflow

app

core

download

repository

database

settings

history

media

instagram

cookies

notifications

work

ui

theme

navigation

common

testing

Each package should have one clear responsibility.

---

# 7. Instagram Media Model

Everything revolves around a single model.

InstagramPost

contains

MediaItem[]

Each MediaItem contains

- Type
- Thumbnail
- Width
- Height
- Duration
- Resolution
- File Size
- Audio Available
- Download Options

Media Types

- IMAGE
- VIDEO
- CAROUSEL_ITEM
- STORY
- HIGHLIGHT
- PROFILE_PICTURE

Never assume every item is a video.

---

# 8. Instagram Workflow

User

↓

Paste URL

↓

Validate URL

↓

Resolve Media

↓

Preview Screen

↓

Media Picker

↓

Download Queue

↓

Storage

This workflow replaces Seal's video-oriented download flow.

---

# 9. Carousel Architecture

A carousel is treated as a collection of independent media items.

Features

- Select one
- Select many
- Select all
- Clear selection
- Download selected
- Download all

Every item retains its own metadata.

Mixed image/video carousels are fully supported.

---

# 10. Cookie Management

Reuse Seal's implementation.

Responsibilities

- Import cookies
- Validate cookies
- Store securely
- Refresh when required

Only improve user experience.

Do not redesign cookie handling.

---

# 11. Download Options

Image

- Download

Video

- Video + Audio
- Video Only (if supported)
- Audio Only (if supported)

Carousel

- Download Selected
- Download All

The UI must display only valid actions.

---

# 12. Storage

Use:

- MediaStore
- Storage Access Framework

Downloads should integrate naturally with Android storage.

Avoid legacy storage APIs.

---

# 13. Database

Reuse Room.

Store:

- Download history
- Favorites (future)
- Queue state
- User preferences
- Metadata cache (optional)

Database schema changes require an ADR.

---

# 14. Background Processing

Reuse WorkManager.

Responsibilities

- Queue execution
- Retry
- Constraints
- Notifications
- Persistence

Long-running downloads must use a Foreground Service.

---

# 15. Notifications

Reuse Seal's implementation.

Support

- Progress
- Pause
- Resume
- Cancel
- Complete
- Failed

Notification behavior should be consistent across all download types.

---

# 16. Security

Validate:

- Instagram URLs
- File names
- Storage paths
- Cookie imports

Never log sensitive information.

Never expose cookies.

Never execute arbitrary commands.

---

# 17. Accessibility

Every screen must support

- TalkBack
- Font scaling
- High contrast
- Screen rotation
- Keyboard navigation

Accessibility is part of the architecture, not an afterthought.

---

# 18. Performance

Target

- Fast startup
- Smooth scrolling
- Efficient recomposition
- Low memory usage
- Stable download queue

Avoid

- Main thread blocking
- Duplicate parsing
- Redundant recompositions

---

# 19. Architecture Invariants

The following rules must never be broken.

- UI never accesses yt-dlp directly.
- UI never accesses Room directly.
- ViewModels never know Compose implementation details.
- Repository is the only data gateway.
- Downloads always go through Download Manager.
- Every feature must be independently testable.
- Business logic never lives inside Composables.
- No cyclic dependencies.
- Generated or upstream code is not modified unless necessary.

---

# 20. Seal Adaptation Matrix

| Seal Component | InstaFlow Action |
|---------------|------------------|
| Download Queue | Keep |
| WorkManager | Keep |
| Room | Keep |
| Foreground Service | Keep |
| Notifications | Keep |
| Settings | Keep |
| History | Keep |
| youtubedl-android | Keep |
| aria2c | Keep |
| Playlist | Replace with Carousel |
| Generic Sites | Replace with Instagram Only |
| Format Picker | Replace with Media Picker |
| Branding | Replace |
| SponsorBlock | Remove |
| Subtitle UI | Remove or Hide |

---

# 21. Future Evolution

Architecture should support future additions without major rewrites.

Examples

- Batch downloads
- Smart folders
- Metadata export
- Favorites
- Download rules
- Plugin system
- Cloud sync (if ever approved)

Future features must not compromise the current architecture.

---

# 22. Architecture Decision Records

Any change affecting:

- Modules
- Data flow
- Storage
- Download engine
- Database
- Navigation
- Dependencies

requires a new ADR before implementation.

---

# 23. Summary

InstaFlow preserves Seal's proven engineering while redesigning the user experience around Instagram's native media model.

The architecture favors stability, maintainability, and extensibility over unnecessary reinvention.

Every contributor should first understand Seal, then understand InstaFlow's goals, and only then implement changes.