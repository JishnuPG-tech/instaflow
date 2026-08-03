# DOWNLOAD_ENGINE

Version: 2.0

Project: InstaFlow

Status:
Production Download Architecture

Based on:
Seal

License:
GPLv3

---

# 1. Purpose

This document defines the architecture of the InstaFlow download engine.

The download engine is responsible for discovering, resolving, downloading, monitoring, and storing Instagram media.

It is the core subsystem of the application.

Every download must pass through this engine.

Nothing downloads outside this architecture.

---

# 2. Design Goals

The download engine should be:

Reliable

Fast

Recoverable

Observable

Scalable

Maintainable

Offline-first

Background capable

User friendly

Production ready

---

# 3. High-Level Pipeline

User

↓

Paste URL

↓

Validate URL

↓

Resolve Instagram Resource

↓

Extract Metadata

↓

Build Media Model

↓

Display Preview

↓

User Selects Media

↓

Create Download Jobs

↓

Queue Manager

↓

WorkManager

↓

Foreground Service

↓

Download Engine

↓

yt-dlp

↓

aria2

↓

Storage

↓

MediaStore

↓

History

↓

Notification

---

# 4. Download Philosophy

Downloads never begin immediately.

Every download follows:

Resolve

↓

Preview

↓

Selection

↓

Queue

↓

Download

↓

Verify

↓

Store

↓

History

Users always understand what will be downloaded.

---

# 5. Download Types

Supported

Image

Video

Carousel

Mixed Carousel

Story

Highlight

Profile Picture

Caption Export

Metadata Export

Future media should integrate without redesign.

---

# 6. Download Job

Every download becomes a DownloadJob.

DownloadJob contains:

Job ID

Media Item

Priority

State

Progress

Worker

Retries

Destination

Notification ID

Created Time

Completed Time

Error State

---

# 7. Download Queue

Queue responsibilities

Ordering

Scheduling

Priority

Pause

Resume

Retry

Cancel

Persistence

Recovery

Queue survives:

Rotation

App restart

Device reboot

Process death

---

# 8. Queue States

Pending

↓

Queued

↓

Resolving

↓

Ready

↓

Downloading

↓

Paused

↓

Retrying

↓

Completed

↓

Failed

↓

Cancelled

---

# 9. Media Resolution

Every URL resolves into:

InstagramResource

↓

MediaItem[]

↓

Preview

↓

Selection

↓

Download Jobs

Never download unknown media.

---

# 10. Carousel Downloads

Carousel

↓

Media Items

↓

User Selection

↓

Download Jobs

Each selected item becomes an independent job.

Failures never stop successful items.

---

# 11. Mixed Carousel

Support

Image

Video

Image

Video

Image

Each item has:

Own progress

Own state

Own retry

Own history

---

# 12. Download Manager

Responsibilities

Job creation

Queue

Workers

Notifications

History

Storage

Recovery

The DownloadManager is the single entry point.

---

# 13. Queue Manager

Responsible for:

Scheduling

Concurrency

Priority

Cancellation

Recovery

Future batching

---

# 14. Media Resolver

Responsibilities

Validate URL

Extract Metadata

Build MediaItem

Determine Media Type

Expose Download Options

Never download directly.

---

# 15. Metadata Resolver

Extract

Caption

Username

Date

Location

Audio

Resolution

Dimensions

Thumbnail

Duration

Metadata should be available before download.

---

# 16. Worker Architecture

WorkManager owns:

DownloadWorker

RetryWorker

CleanupWorker

Future workers

Each worker has one responsibility.

---

# 17. Foreground Service

Responsibilities

Long downloads

Progress

Notifications

Pause

Resume

Cancellation

Android background restrictions

---

# 18. Notifications

Show

Thumbnail

Progress

Speed

Remaining Time

Pause

Resume

Cancel

Completion

Failure

Never expose sensitive information.

---

# 19. Storage

Store through

MediaStore

SAF

Validate

Filename

Extension

Destination

Duplicates

Never write outside approved locations.

---

# 20. History

Every completed download records:

Media Type

Username

Caption

Thumbnail

Download Date

Size

Location

Status

---

# 21. Retry Logic

Automatic retries only for transient failures.

Examples

Temporary network failure

Timeout

Interrupted download

Do not retry:

Invalid URL

Private content

Permission denied

Corrupt metadata

---

# 22. Error Model

Typed errors

InvalidUrl

PrivateContent

CookieRequired

UnsupportedMedia

DownloadFailed

StorageError

PermissionDenied

UnknownError

UI never receives raw exceptions.

---

# 23. Performance

Support

Large queue

Large carousel

Parallel downloads

Low memory

Fast resume

Efficient cancellation

---

# 24. Security

Validate

URL

Filename

Storage

Permissions

Cookies

Download commands

Never execute arbitrary commands.

---

# 25. Recovery

Recover after

Rotation

App restart

Device reboot

Foreground service restart

Worker restart

Queue state should remain consistent.

---

# 26. Future Features

Batch downloads

Smart queue

Priorities

Download scheduling

Automatic organization

Collections

Cloud backup (if approved)

Architecture should already support them.

---

# 27. Success Criteria

The download engine is successful when:

Downloads are reliable.

Failures recover gracefully.

Users understand download progress.

Queue survives interruptions.

Architecture remains simple.

Every media type follows the same pipeline.

The system scales as new Instagram media types are introduced.