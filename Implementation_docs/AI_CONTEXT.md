# AI_CONTEXT

Version: 2.0

Project: InstaFlow

Status: AI Engineering Context

Based on:
Seal by JunkFood02

License:
GPLv3

---

# 1. Purpose

This document is the first document every AI coding agent must read before performing any task.

It provides a concise but complete understanding of:

• Project vision

• Current architecture

• Engineering philosophy

• Current development phase

• Reference implementation

• Coding standards

• Development workflow

Reading this document should eliminate the need for the AI to infer project direction.

---

# 2. Project Summary

Project Name

InstaFlow

Tagline

Inspired by Seal

Project Type

Native Android Application

Platform

Android

Technology

Kotlin

Jetpack Compose

Material Design 3

Material 3 Expressive

Room

WorkManager

Hilt

Coroutines

Flow

MediaStore

Storage Access Framework

youtubedl-android

aria2

Architecture

Single Activity

Compose

MVVM

Repository Pattern

Offline First

On-device Processing

---

# 3. Project Vision

InstaFlow is not a generic downloader.

InstaFlow is an Instagram-first media manager built upon the mature engineering foundation of Seal.

Seal already solved the difficult Android infrastructure problems.

InstaFlow specializes the experience for Instagram.

The goal is to preserve proven engineering while redesigning the user experience around Instagram media.

---

# 4. Upstream Reference

Primary Repository

https://github.com/JunkFood02/Seal

Before modifying any subsystem:

Study Seal.

Understand the subsystem.

Reuse proven implementation.

Only modify when Instagram requires different behavior.

---

# 5. Current Architecture

Compose UI

↓

ViewModel

↓

Repository

↓

Download Manager

↓

WorkManager

↓

youtubedl-android

↓

yt-dlp

↓

Instagram

No backend.

No FastAPI.

No OpenAPI.

Everything executes on-device.

---

# 6. Engineering Philosophy

Always prefer:

Reuse

↓

Extension

↓

Replacement

↓

New Implementation

Avoid rewriting mature infrastructure.

---

# 7. Supported Media

The application must support:

Single Image

Single Video

Carousel

Mixed Carousel

Story

Highlight

Profile Picture

Caption

Metadata

Cookie Protected Content

---

# 8. Download Options

Images

Download

Videos

Video + Audio

Video Only

Audio Only (if supported)

Carousel

Download Selected

Download All

The UI should never display unsupported actions.

---

# 9. Seal Component Policy

KEEP

Download Queue

Room

WorkManager

Foreground Service

Notifications

History

Storage

MediaStore

SAF

Theme

Localization

Settings

Cookie Manager

youtubedl-android

aria2

REPLACE

Playlist

↓

Carousel

Format Picker

↓

Instagram Media Picker

Supported Sites

↓

Instagram Only

REMOVE

SponsorBlock

Subtitle UI

YouTube-specific features

---

# 10. Development Workflow

Every task follows:

Requirements

↓

Architecture Review

↓

Seal Review

↓

Implementation

↓

Compilation

↓

Unit Tests

↓

Integration Tests

↓

Regression Tests

↓

Documentation

↓

Review

↓

Commit

---

# 11. AI Rules

Before writing code:

Read:

PROJECT_CONSTITUTION.md

ARCHITECTURE.md

IMPLEMENTATION_PLAN.md

REFERENCE_POLICY.md

Then inspect the equivalent subsystem inside Seal.

Never write code without understanding the existing implementation.

---

# 12. Coding Rules

Never:

Duplicate code.

Move business logic into Compose.

Access Room from UI.

Call yt-dlp directly from the UI.

Create large ViewModels.

Create God classes.

Introduce cyclic dependencies.

Prefer:

Small files.

Small functions.

Reusable components.

Clear naming.

Immutable UI state.

Structured concurrency.

---

# 13. Current Development Phase

Current Phase

[Update this section as development progresses.]

Current Work Package

[Update this section.]

Current Branch

[Update this section.]

Current Milestone

[Update this section.]

Open Issues

[Update this section.]

Known Risks

[Update this section.]

---

# 14. Required Verification

Before marking work complete verify:

✓ Builds

✓ Tests

✓ Regression

✓ Documentation

✓ No architecture drift

✓ No security regressions

✓ No accessibility regressions

---

# 15. Common Commands

Android

./gradlew assembleDebug

./gradlew testDebugUnitTest

./gradlew connectedDebugAndroidTest

./gradlew lint

Formatting

./gradlew ktlintCheck

./gradlew detekt

Build

./gradlew bundleRelease

Release

./gradlew assembleRelease

---

# 16. Documents

Always consult:

PROJECT_CONSTITUTION.md

ARCHITECTURE.md

IMPLEMENTATION_PLAN.md

GOVERNANCE.md

TESTING_STRATEGY.md

SECURITY_CHECKLIST.md

REFERENCE_POLICY.md

DEBUGGING_GUIDE.md

UI_UX_DESIGN.md

ADRs

---

# 17. Success Criteria

Every engineering decision should answer:

Does this preserve Seal's proven engineering?

Does this improve the Instagram experience?

Does this reduce future maintenance?

Does this keep the architecture clean?

If any answer is "No", reconsider the implementation.

---

# 18. Long-Term Goal

InstaFlow should become the definitive open-source Instagram media manager.

Every feature should feel intentionally designed for Instagram while preserving the engineering quality that made Seal successful.