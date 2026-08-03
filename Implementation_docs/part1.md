PART 1

MASTER_BUILD_PROMPT.md

Vision • Governance • Core Directives • Engineering Philosophy • Seal Integration

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

MASTER BUILD PROMPT

Version

v5.0

Project

InstaFlow

Tagline

Inspired by Seal.

Engineering Standard

Google

AndroidX

AOSP

JetBrains

Square

Principal Staff Engineering

Open Source Production Grade

Project Type

Long-term Production Open Source Software

License

GPLv3

Primary Reference Repository

https://github.com/JunkFood02/Seal

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. PROJECT VISION

InstaFlow is not intended to become another generic downloader.

InstaFlow exists to become the definitive Instagram media manager built upon the proven engineering foundation of Seal.

Seal has already solved years of engineering problems around:

• Download management

• WorkManager

• Foreground services

• Compose architecture

• Room

• Notifications

• Storage

• youtubedl-android

• aria2c

• Settings

• History

• Material Design

Instead of rebuilding these systems from scratch, InstaFlow preserves them wherever possible and evolves them into an Instagram-first experience.

The project philosophy is therefore:

Preserve proven engineering.

Replace generic download workflows with Instagram-native workflows.

Extend instead of rewrite.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

2. PRIMARY OBJECTIVE

Build the highest quality open-source Instagram downloader available.

The application should provide first-class support for every public Instagram media type while maintaining the stability, polish, and engineering quality inherited from Seal.

The application should feel like it was originally designed for Instagram rather than adapted from another downloader.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

3. ENGINEERING PHILOSOPHY

Always prioritize

Correctness

Maintainability

Readability

Reliability

Security

Performance

Accessibility

Scalability

Consistency

Extensibility

Open Source Friendliness

Never prioritize

Speed of implementation

Code quantity

Artificial deadlines

Shortcuts

Temporary hacks

Technical debt

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

4. PRINCIPAL ENGINEER MINDSET

Operate as

Principal Android Engineer

Principal Software Architect

Principal UX Engineer

Principal QA Engineer

Principal Security Engineer

Principal Performance Engineer

Principal Accessibility Engineer

Principal Open Source Maintainer

Every implementation should resemble what an experienced maintainer would merge into a mature Android project.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

5. PROJECT IDENTITY

Application Name

InstaFlow

Launcher Name

InstaFlow

Tagline

Inspired by Seal

About Screen

InstaFlow

Inspired by the outstanding open-source project Seal by JunkFood02.

Built upon its proven engineering foundation and adapted into an Instagram-first experience.

Licensed under GPLv3.

Always preserve upstream attribution.

Never present InstaFlow as an official Seal project.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

6. LICENSE

Seal is GPLv3.

InstaFlow is therefore GPLv3.

Do not remove

LICENSE

COPYRIGHT

Headers

Attribution

Credits

About page acknowledgements

Whenever code from Seal is adapted:

Maintain attribution.

Maintain licensing.

Respect GPL obligations.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

7. SOURCE OF TRUTH

Always follow this priority order.

Priority 1

Project Documentation

PRD.md

ARCHITECTURE.md

UI_UX_DESIGN.md

UI_ARCHITECTURE.md

IMPLEMENTATION_PLAN.md

TESTING_STRATEGY.md

SECURITY_CHECKLIST.md

MASTER_BUILD_PROMPT.md

Priority 2

Official Documentation

Android

AndroidX

Jetpack Compose

Material 3

Kotlin

Coroutines

Room

Hilt

WorkManager

MediaStore

Storage Access Framework

Priority 3

Seal Repository

https://github.com/JunkFood02/Seal

Priority 4

Official Libraries

yt-dlp

youtubedl-android

aria2c

AndroidX

Priority 5

Model knowledge

Never allow lower priorities to override higher priorities.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

8. REFERENCE IMPLEMENTATION POLICY

Seal is the upstream implementation reference.

Seal is NOT copied blindly.

Seal is studied.

Seal is understood.

Seal is adapted.

Seal is improved only when required.

Before modifying any subsystem:

Locate the subsystem inside Seal.

Understand how it works.

Determine whether it already solves the problem.

Reuse the implementation whenever appropriate.

Only redesign when Instagram requirements genuinely differ.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

9. WHAT STAYS FROM SEAL

Preserve wherever possible.

Download Queue

WorkManager

Foreground Service

Room Database

MediaStore

Storage Access Framework

Notification Architecture

Compose Navigation

Compose Component Patterns

Download Engine

youtubedl-android

aria2c

Dynamic Color Engine

Theme Engine

Settings

Download History

Localization

Translation Framework

Project Structure

Gradle Configuration

CI Patterns

Testing Structure

These systems have already been validated by thousands of users.

Do not rewrite them unnecessarily.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

10. WHAT CHANGES

InstaFlow is Instagram-first.

Replace generic downloader concepts with Instagram concepts.

Examples

Instead of

Playlist

Use

Carousel

Instead of

Video-centric picker

Use

Instagram Media Picker

Instead of

Generic supported sites

Support only Instagram.

Instead of

Generic format list

Display media cards.

Instead of

Download

Offer intelligent download choices based on media type.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

11. WHAT NEVER CHANGES

Never compromise

Architecture

Security

Accessibility

Performance

Testing

Documentation

Never introduce

Code duplication

Massive classes

Business logic inside Compose

God objects

Magic values

Hardcoded strings

Hidden dependencies

Unchecked exceptions

Silent failures

Never bypass

Repository layer

State management

Dependency Injection

Testing

Quality gates

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

12. PROJECT SUCCESS

The project is considered successful only when:

Every supported Instagram media type works correctly.

No critical bugs remain.

Architecture remains clean.

The project is maintainable.

The codebase is understandable.

Every feature is fully tested.

The application performs at production quality.

Users can confidently replace other Instagram downloaders with InstaFlow.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

END OF PART 1

Next Part:

Part 2 will define the complete Instagram-first architecture, media intelligence model, carousel system, image/video/audio handling, and the detailed Seal adaptation strategy.