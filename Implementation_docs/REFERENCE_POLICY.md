# REFERENCE_POLICY.md

Version: 2.0

Project: InstaFlow

Status: Engineering Reference Policy

Primary Upstream Project:
https://github.com/JunkFood02/Seal

License:
GPLv3

---

# 1. Purpose

This document defines how the Seal project must be used as the upstream engineering reference throughout the lifetime of InstaFlow.

The objective is to preserve the mature engineering decisions already solved by Seal while evolving the application into an Instagram-first experience.

Seal is the engineering foundation.

InstaFlow is the Instagram specialization.

---

# 2. Guiding Principle

Always understand before modifying.

Never rewrite a subsystem that already satisfies InstaFlow's requirements.

Reuse proven engineering.

Replace only when Instagram fundamentally requires different behavior.

Every architectural deviation must be intentional.

---

# 3. Upstream Reference

Official Repository

https://github.com/JunkFood02/Seal

Before implementing any feature:

• Study the relevant subsystem.

• Understand its architecture.

• Identify responsibilities.

• Identify extension points.

• Determine whether modification is actually necessary.

Never modify code before understanding why it exists.

---

# 4. Mandatory Research Process

Before beginning any Work Package, complete the following.

Step 1

Locate equivalent implementation inside Seal.

Examples

Download Queue

History

Settings

Navigation

Repository

Foreground Service

MediaStore

Cookie Management

Download Engine

WorkManager

Room

Notifications

Theme

Compose Components

---

Step 2

Read the implementation.

Understand:

Why it exists.

How it works.

Dependencies.

Architecture.

Performance considerations.

Known limitations.

---

Step 3

Determine one of the following.

KEEP

Reuse exactly.

MODIFY

Reuse while adapting.

REPLACE

Only when Instagram requirements demand it.

REMOVE

Only when the feature has no value for Instagram.

---

Step 4

Document the decision.

Every Work Package should include:

Seal Files

Reason for reuse

Reason for modification

Reason for replacement

Expected impact

---

# 5. Component Classification

The following table defines the expected treatment of Seal subsystems.

| Seal Component | InstaFlow Decision |
|----------------|--------------------|
| Download Queue | KEEP |
| WorkManager | KEEP |
| Room Database | KEEP |
| Foreground Service | KEEP |
| Notification System | KEEP |
| MediaStore Integration | KEEP |
| Storage Access Framework | KEEP |
| Download History | KEEP |
| Theme System | KEEP |
| Compose Navigation | KEEP |
| Settings Architecture | KEEP |
| Translation Framework | KEEP |
| Cookie Management | KEEP (Improve UX Only) |
| youtubedl-android | KEEP |
| aria2c Integration | KEEP |
| Playlist Workflow | REPLACE with Carousel |
| Format Picker | REPLACE with Instagram Media Picker |
| Generic Supported Sites | REPLACE with Instagram-only |
| SponsorBlock | REMOVE |
| Subtitle UI | REMOVE |
| YouTube-specific Features | REMOVE |
| Branding | REPLACE |

---

# 6. Engineering Rules

Never replace a subsystem simply because another implementation appears cleaner.

A replacement must satisfy at least one of:

• Simpler

• More maintainable

• Better performance

• Better accessibility

• Better security

• Required for Instagram

Otherwise reuse Seal.

---

# 7. Instagram Adaptation

Seal was designed primarily around videos.

Instagram contains:

Images

Videos

Reels

Stories

Highlights

Profile Pictures

Mixed-media Carousels

The engineering focus is adapting workflows rather than replacing infrastructure.

---

# 8. Mandatory Compatibility Spike

Before modifying any download workflow, verify how Seal behaves with real Instagram URLs.

Test:

Single Image

Single Video

Carousel

Mixed Carousel

Story

Highlight

Profile Picture

Private Post

Cookie Protected Content

Document every limitation before implementing changes.

Never assume behavior.

---

# 9. Preserve Stable Infrastructure

These systems should remain as close to upstream as possible.

Download Queue

Room

WorkManager

Foreground Service

Notification System

History

Theme

Localization

Settings

Storage

MediaStore

SAF

These areas have already been validated by thousands of users.

Avoid unnecessary divergence.

---

# 10. Replace User Experience, Not Infrastructure

The largest architectural changes should occur in the UI and interaction model.

Examples

Seal

↓

Playlist

↓

InstaFlow

Carousel

-------------------------------------

Seal

↓

Format Picker

↓

InstaFlow

Media Picker

-------------------------------------

Seal

↓

Supported Sites

↓

InstaFlow

Instagram

-------------------------------------

Seal

↓

Video-first

↓

InstaFlow

Media-first

---

# 11. Instagram Media Model

Every resolved URL should produce a unified InstagramPost model.

InstagramPost

↓

MediaItem[]

MediaItem

↓

IMAGE

VIDEO

STORY

HIGHLIGHT

PROFILE_PICTURE

Each MediaItem should expose:

Thumbnail

Resolution

Duration

Dimensions

Audio Availability

Metadata

Download Options

The architecture must never assume every post is a video.

---

# 12. Reuse Policy

Prefer:

Reuse

↓

Extension

↓

Replacement

↓

New Implementation

Creating entirely new infrastructure should be the last option.

---

# 13. Performance Policy

If Seal already provides an efficient implementation:

Reuse it.

Do not introduce additional abstraction layers without measurable benefit.

Performance regressions are unacceptable.

---

# 14. Security Policy

Retain Seal's security posture wherever applicable.

Do not weaken:

Cookie handling

Storage

Permissions

Foreground Services

Download execution

File validation

---

# 15. Documentation Policy

Whenever a subsystem differs from Seal:

Document:

Original behavior

New behavior

Reason

Benefits

Migration impact

Future maintenance notes

---

# 16. Synchronization Policy

Regularly review upstream Seal releases.

For each new release:

Review changelog.

Review bug fixes.

Review security fixes.

Review dependency updates.

Determine whether the changes should be merged into InstaFlow.

Do not fall permanently behind upstream.

---

# 17. AI Agent Rules

Before writing code, every AI agent must:

Read:

PROJECT_CONSTITUTION.md

ARCHITECTURE.md

IMPLEMENTATION_PLAN.md

REFERENCE_POLICY.md

Then inspect the equivalent subsystem inside Seal.

Only after understanding both should implementation begin.

---

# 18. Contributor Rules

Every contributor should:

Understand the upstream implementation.

Preserve engineering quality.

Avoid unnecessary rewrites.

Keep changes focused.

Document architectural deviations.

Respect GPLv3 obligations.

---

# 19. Success Criteria

Reference Policy is successful when:

Seal remains recognizable as the engineering foundation.

InstaFlow becomes uniquely optimized for Instagram.

Architecture stays maintainable.

Future upstream improvements remain mergeable.

Technical debt is minimized.

Users experience a native Instagram workflow while benefiting from Seal's proven engineering.

---

# 20. Final Principle

Do not compete with Seal.

Build upon Seal.

Preserve what is excellent.

Replace only what Instagram demands.

The goal is not to erase the upstream project.

The goal is to evolve it into the definitive Instagram-first media manager while respecting the engineering excellence that made Seal successful.