# UI_UX_DESIGN.md

Version: 2.0

Project: InstaFlow

Status: Production UI/UX Specification

Based on:
Seal by JunkFood02

Design Language:
Material Design 3
Material 3 Expressive
Instagram-first UX

License:
GPLv3

---

# 1. Purpose

This document defines the complete visual and interaction design of InstaFlow.

It is the single source of truth for:

• UI

• UX

• Navigation

• Components

• Motion

• Typography

• Color

• Accessibility

• Interaction

Every screen must follow this specification.

---

# 2. Design Philosophy

The interface should feel:

Simple

Fast

Minimal

Modern

Native

Professional

Confident

Never feel:

Busy

Complicated

Outdated

Generic

Cluttered

The user should always understand what to do next.

---

# 3. Core Design Principles

Instagram First

↓

Media First

↓

Minimal Touches

↓

Maximum Clarity

↓

Fast Downloads

↓

Beautiful Motion

↓

Accessibility

---

# 4. Design Language

Foundation:

Material Design 3

Material 3 Expressive

Compose

Single Activity

Pure Compose Navigation

---

# 5. Visual Identity

Application Name

InstaFlow

Tagline

Inspired by Seal

Visual Identity

Modern

Dark

Professional

Content-focused

---

# 6. Color System

Primary Background

#000000

Secondary Background

#0D0D0D

Surface

#161616

Card

#1D1D1D

Primary Accent

#3DE8FF

Success

#2ECC71

Warning

#FFC107

Error

#FF5252

Divider

#2A2A2A

No light theme.

The application is true-black only.

---

# 7. Typography

Primary Font

Inter

Display Font

Space Grotesk

Monospace

JetBrains Mono

Typography hierarchy

Display

Headline

Title

Body

Label

Caption

Use dynamic font scaling.

---

# 8. Shape System

Material 3 Expressive

Rounded corners

Large cards

Soft containers

Modern FAB

Large touch targets

Avoid sharp rectangles.

---

# 9. Elevation

Minimal.

Use contrast rather than heavy shadows.

Focus attention using:

Spacing

Color

Motion

Typography

Not excessive elevation.

---

# 10. Navigation

Bottom Navigation

Home

Queue

History

Settings

About

Single Activity

Compose Navigation

Animated transitions.

---

# 11. Home Screen

Components

Paste URL

Paste Button

Clipboard Suggestion

Recent Downloads

Quick Actions

Primary CTA

Download

The Home screen should never feel empty.

---

# 12. Media Preview Screen

After resolving a URL, show a dedicated preview before downloading.

Display:

Large Preview

Username

Caption

Upload Date

Media Type

Resolution

Audio Availability

Metadata

Download Actions

Never start downloading immediately after pasting a URL.

---

# 13. Instagram Media Picker

Replace Seal's format picker.

Every media item appears as a card.

Each card shows:

Thumbnail

Image or Video badge

Resolution

Dimensions

Duration

Estimated Size

Selection Checkbox

The user can:

Select one

Select many

Select all

Clear selection

Download selected

Download all

---

# 14. Carousel Experience

Mixed-media carousels are first-class.

Display:

Horizontal cards

Swipe

Selection state

Download status

Videos should display:

Duration

Resolution

Audio

Images display:

Resolution

Dimensions

---

# 15. Download Actions

Single Image

Download

Single Video

Video + Audio

Video Only

Audio Only (if supported)

Carousel

Download Selected

Download All

Never show unavailable actions.

---

# 16. Download Queue

Show:

Thumbnail

Progress

Speed

Remaining Time

Pause

Resume

Cancel

Retry

Status

Support multiple simultaneous downloads.

---

# 17. History

Display:

Thumbnail

Username

Media Type

Date

Size

Downloaded Location

Actions

Open

Share

Delete

Re-download

Search

Sort

Filter

---

# 18. Settings

Organize into sections.

Downloads

Storage

Appearance

Cookies

Advanced

About

Hide advanced settings unless necessary.

---

# 19. About

Display:

App Version

Seal Attribution

GPLv3 Notice

GitHub Repository

Contributors

Licenses

Open Source Credits

---

# 20. Empty States

Every screen must define an empty state.

Example

History

"No downloads yet."

Queue

"No active downloads."

Search

"No matching items."

Always provide a suggested action.

---

# 21. Error States

Every error should include:

Clear title

Human-readable explanation

Recovery suggestion

Retry action

Never expose stack traces.

---

# 22. Loading States

Use:

Skeleton screens

Progress indicators

Shimmer

Avoid blank screens.

---

# 23. Motion

Animations should be:

Short

Natural

Meaningful

Use motion to explain state changes.

Avoid decorative animations.

---

# 24. Haptics

Use subtle haptic feedback for:

Download started

Selection

Completed

Errors

Long press

Never overuse haptics.

---

# 25. Accessibility

Support:

TalkBack

Large Fonts

High Contrast

Landscape

Keyboard Navigation

Minimum 48dp touch targets.

Meaningful content descriptions.

---

# 26. Responsive Layout

Support:

Phones

Foldables

Tablets

Landscape

Portrait

Use adaptive layouts.

---

# 27. Performance

UI should remain smooth with:

1000+ history items

Large download queue

Large carousel

Rapid scrolling

Avoid unnecessary recompositions.

---

# 28. Component Library

Create reusable components.

Examples

Primary Button

Secondary Button

Media Card

Carousel Card

Preview Card

History Card

Queue Item

Section Header

Info Row

Confirmation Dialog

Error Dialog

Loading Skeleton

Do not duplicate UI code.

---

# 29. Design Tokens

Never hardcode:

Colors

Spacing

Radius

Typography

Animation duration

Icons

Everything should come from the design system.

---

# 30. Iconography

Use Material Symbols.

Keep icon style consistent.

Avoid mixing icon sets.

---

# 31. User Experience Goals

The user journey should require as few decisions as possible.

Paste

↓

Preview

↓

Select

↓

Download

↓

Done

No unnecessary dialogs.

No confusing terminology.

---

# 32. Design Review Checklist

Before approving UI:

✓ Instagram-first

✓ Consistent

✓ Accessible

✓ Responsive

✓ Beautiful

✓ Minimal

✓ Fast

✓ Reusable

✓ Maintainable

---

# 33. Future Design

Architecture should allow future additions:

Batch Downloads

Favorites

Collections

Smart Folders

Metadata Export

Search

Without redesigning the interface.

---

# 34. Final Principle

InstaFlow should feel like an application originally designed for Instagram.

Users should immediately understand how to download any supported media.

The interface should be elegant, efficient, accessible, and consistent while preserving the proven engineering quality inherited from Seal.