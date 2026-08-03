# MEDIA_MODEL

Version: 2.0

Project: InstaFlow

Status: Core Domain Model

---

# 1. Purpose

This document defines every media entity inside InstaFlow.

Every screen, repository, ViewModel, database table and download workflow should use these models.

The media model is the heart of InstaFlow.

Every feature should build upon these entities instead of creating new ones.

---

# 2. Design Philosophy

Seal is video-centric.

Instagram is media-centric.

Never assume a URL represents a video.

Instead:

Instagram URL

↓

Instagram Resource

↓

Media Collection

↓

Media Items

---

# 3. Root Entity

Every supported URL resolves into one object.

InstagramResource

It represents one Instagram link.

Examples

Single Image

Single Video

Carousel

Story

Highlight

Profile Picture

Future Media Types

---

InstagramResource

contains

Resource Metadata

+

Media Items

---

# 4. InstagramResource

Fields

Resource ID

Original URL

Resolved URL

Owner

Caption

Location

Upload Date

Like Count (optional)

Comment Count (optional)

Audio

Media Items

Cookies Required

Private Content

Media Count

Media Type

---

# 5. MediaItem

Every downloadable object becomes one MediaItem.

Fields

ID

Type

Thumbnail

Width

Height

Resolution

Duration

File Size

Download URL

Mime Type

Audio Available

Selected

Downloaded

Download State

---

# 6. Media Types

Supported values

IMAGE

VIDEO

REEL

STORY_IMAGE

STORY_VIDEO

HIGHLIGHT_IMAGE

HIGHLIGHT_VIDEO

PROFILE_PICTURE

CAROUSEL_IMAGE

CAROUSEL_VIDEO

Future additions should extend this enum rather than replacing it.

---

# 7. Carousel Model

Carousel

↓

MediaItem[]

Each MediaItem behaves independently.

Images and videos may coexist.

Selection state belongs to the MediaItem.

Never to the Carousel itself.

---

# 8. Download Options

Image

Download

Video

Video + Audio

Video Only

Audio Only

Carousel

Download Selected

Download All

Story

Download

Highlight

Download Selected

Download All

Only expose valid actions.

---

# 9. Metadata

Every MediaItem should expose:

Username

Caption

Date

Location

Audio Title

Hashtags

Resolution

Dimensions

Duration

Media Type

---

# 10. Download State

Every MediaItem has a lifecycle.

Pending

↓

Queued

↓

Resolving

↓

Downloading

↓

Paused

↓

Completed

↓

Failed

↓

Cancelled

The UI should observe this state.

---

# 11. Error Model

Every failure maps into a typed error.

Examples

Invalid URL

Unsupported Media

Private Content

Cookie Required

Network Error

Storage Error

Permission Error

Download Failed

Parsing Failed

Unknown Error

Never expose raw exceptions directly to the UI.

---

# 12. Selection Model

Every MediaItem has

Selected

Not Selected

Selection should survive:

Rotation

Configuration changes

Background execution

Queue creation

---

# 13. Future Extensions

Architecture should support

Live Videos

Instagram Collections

Shared Albums

Pinned Posts

Future media without redesign.

---

# 14. Final Principle

Everything inside InstaFlow revolves around MediaItem.

If a new feature cannot naturally use MediaItem, reconsider the design before introducing another entity.