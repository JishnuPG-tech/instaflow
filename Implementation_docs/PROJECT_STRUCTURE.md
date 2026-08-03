# PROJECT STRUCTURE

Version: 2.0

Project: InstaFlow

Status: Production Architecture Map

Based on:
Seal

License:
GPLv3

---

# 1. Purpose

This document describes the entire project structure.

It acts as a map of the codebase.

Every package has exactly one responsibility.

Developers and AI agents should consult this document before creating new files.

Do not create duplicate responsibilities.

---

# 2. High-Level Architecture

InstaFlow

↓

Android App

↓

Compose UI

↓

ViewModels

↓

Repositories

↓

Download Engine

↓

Room

↓

WorkManager

↓

MediaStore

↓

youtubedl-android

↓

Instagram

---

# 3. Project Layout

app/

docs/

gradle/

scripts/

.github/

LICENSE

README.md

---

# 4. Android Package Structure

com.instaflow

├── app
├── core
├── data
├── domain
├── download
├── instagram
├── cookies
├── database
├── history
├── notifications
├── settings
├── workers
├── ui
├── common
└── testing

---

# 5. app/

Purpose

Application bootstrap.

Contains

Application

MainActivity

Dependency Injection

Startup

Navigation host

---

Files

InstaFlowApplication.kt

MainActivity.kt

App.kt

Navigation.kt

---

# 6. core/

Purpose

Reusable infrastructure.

Contains

Logger

Result wrappers

Dispatchers

Extensions

Constants

Utilities

No Instagram-specific logic.

---

# 7. data/

Purpose

Repository implementations.

Contains

Repositories

Remote wrappers

Room coordination

Storage coordination

Download coordination

Every Repository implements an interface from domain/.

---

# 8. domain/

Purpose

Business layer.

Contains

Models

UseCases

Repository interfaces

Business rules

Domain layer must not depend on Android.

---

# 9. instagram/

Purpose

Everything specific to Instagram.

Contains

URL parser

Media parser

Metadata parser

Carousel parser

Story parser

Highlight parser

Profile picture parser

Caption parser

Instagram-specific utilities

---

# 10. download/

Purpose

Download engine.

Contains

DownloadManager

QueueManager

DownloadWorker

MediaResolver

Downloader

Progress tracking

Retry logic

Pause

Resume

Cancellation

This package owns download execution.

---

# 11. cookies/

Purpose

Cookie handling.

Contains

Import

Validation

Storage

Expiration

Secure management

Reuse Seal implementation.

---

# 12. database/

Purpose

Room database.

Contains

Database

DAO

Entities

Migrations

Converters

Never access database directly from UI.

---

# 13. history/

Purpose

Download history.

Contains

HistoryRepository

HistoryViewModel

HistoryScreen

Search

Filter

Sort

---

# 14. notifications/

Purpose

Notification management.

Contains

NotificationManager

Channels

Progress

Completion

Errors

Actions

---

# 15. settings/

Purpose

Application preferences.

Contains

Downloads

Storage

Appearance

Cookies

Advanced

About

Preferences remain centralized.

---

# 16. workers/

Purpose

Background execution.

Contains

DownloadWorker

CleanupWorker

MigrationWorker

Future workers

All long-running work uses WorkManager.

---

# 17. ui/

Purpose

Compose UI.

Structure

ui

↓

theme

↓

navigation

↓

components

↓

screens

↓

dialogs

↓

animations

↓

preview

---

# 18. ui/screens/

Contains

Home

Preview

Carousel

Queue

History

Settings

About

Each screen owns only presentation logic.

---

# 19. ui/components/

Reusable components only.

Examples

PrimaryButton

SecondaryButton

MediaCard

CarouselCard

PreviewCard

HistoryCard

QueueCard

InfoRow

Dialogs

Loading

No business logic.

---

# 20. common/

Purpose

Shared utilities.

Contains

Extensions

Permissions

Formatting

Validation

Date

File

Reusable helpers

---

# 21. testing/

Purpose

Testing infrastructure.

Contains

Fakes

Mocks

Builders

Fixtures

Utilities

Test helpers

---

# 22. Dependency Rules

Allowed

UI

↓

ViewModel

↓

Repository

↓

Download Engine

↓

Storage

↓

Instagram

Not allowed

UI

↓

Database

UI

↓

yt-dlp

UI

↓

Workers

ViewModel

↓

Compose

Database

↓

UI

---

# 23. File Naming

Screens

HomeScreen.kt

PreviewScreen.kt

HistoryScreen.kt

ViewModels

HomeViewModel.kt

Repositories

HistoryRepository.kt

Entities

DownloadEntity.kt

Workers

DownloadWorker.kt

Keep naming predictable.

---

# 24. Package Ownership

Each package has one owner.

Do not place files into unrelated packages.

If a package gains multiple responsibilities, split it.

---

# 25. Creating New Packages

Before creating a package ask:

Does an existing package already own this responsibility?

If yes

Reuse it.

If no

Document the reason.

Avoid package proliferation.

---

# 26. Maximum Responsibilities

A package should solve one problem.

Examples

history/

↓

History

download/

↓

Downloads

instagram/

↓

Instagram parsing

Never mix unrelated responsibilities.

---

# 27. Future Packages

Possible additions

analytics/

(only if privacy-preserving)

favorites/

collections/

search/

backup/

plugins/

Create only when required.

---

# 28. Project Health

A healthy project has:

Small packages.

Clear ownership.

Predictable names.

Minimal coupling.

Maximum cohesion.

---

# 29. Success Criteria

This document is successful when:

A new contributor can understand the codebase within one hour.

An AI agent can identify the correct package before writing code.

No duplicate responsibilities emerge.

Architecture remains clean throughout the project's lifetime.