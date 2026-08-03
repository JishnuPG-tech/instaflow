# TESTING STRATEGY

Version: 2.0

Project: InstaFlow

Status: Production Testing Standard

Based on: Seal (JunkFood02)

License: GPLv3

---

# 1. Purpose

This document defines the complete testing strategy for InstaFlow.

Testing is not a final phase.

Testing is an engineering activity performed throughout development.

Every feature must be proven to work through objective evidence.

No feature is complete because it compiles.

A feature is complete only after it has passed all required quality gates.

---

# 2. Testing Philosophy

Testing should maximize confidence while minimizing future regressions.

Testing priorities are:

Correctness

↓

Reliability

↓

Regression Prevention

↓

Performance

↓

Accessibility

↓

Security

Every bug discovered should result in a new regression test whenever practical.

---

# 3. Testing Pyramid

                    Manual Verification
                           ▲
                      End-to-End Tests
                           ▲
                  Integration Tests
                           ▲
                   Compose UI Tests
                           ▲
                        Unit Tests

Write many fast tests.

Write fewer slow tests.

Prefer deterministic tests.

Avoid flaky tests.

---

# 4. Mandatory Test Categories

Every Work Package must determine which categories apply.

Required categories include:

- Unit Tests
- Integration Tests
- Compose UI Tests
- Regression Tests
- Manual Verification
- Accessibility Tests
- Performance Tests
- Release Validation

---

# 5. Unit Testing

Unit tests should cover:

Repositories

ViewModels

Use Cases

Validators

Utilities

Mappers

Download Logic

Media Parsing

Metadata Processing

Cookie Utilities

URL Validation

Unit tests must:

- Execute quickly.
- Avoid network access.
- Be deterministic.
- Be isolated.

---

# 6. Integration Testing

Integration tests verify interaction between components.

Examples:

Repository ↔ Download Engine

Repository ↔ Room

Repository ↔ Cookie Manager

ViewModel ↔ Repository

Download Queue ↔ WorkManager

History ↔ Database

Media Resolver ↔ youtubedl-android

---

# 7. Compose UI Testing

Every important screen should have automated UI tests.

Mandatory screens:

Home

Paste URL

Media Preview

Carousel Picker

Download Queue

History

Settings

About

Error Dialogs

Verify:

Rendering

State Changes

Navigation

Selection

Scrolling

Error States

Dark Theme

Large Font Support

---

# 8. Regression Testing

Regression testing is mandatory after every completed Work Package.

Minimum regression checklist:

✓ App launches.

✓ Paste URL works.

✓ Instagram URL accepted.

✓ Image post resolves.

✓ Video post resolves.

✓ Carousel resolves.

✓ Queue works.

✓ History loads.

✓ Settings persist.

✓ Notifications appear.

✓ Downloads complete.

---

# 9. Instagram Test Matrix

Every supported media type must be validated.

## Single Image

Verify:

Preview

Download

Resolution

Metadata

Save Location

---

## Single Video

Verify:

Preview

Video + Audio

Video Only (if supported)

Audio Only (if supported)

Completion

---

## Carousel

Verify:

Image Carousel

Video Carousel

Mixed Carousel

Large Carousel

Single Selection

Multiple Selection

Select All

Clear Selection

Download Selected

Download All

Cancellation

Resume

Retry

---

## Stories

Verify:

Image Story

Video Story

Cookie Required Story

Download

---

## Highlights

Verify:

Highlight List

Story Selection

Download Selected

Download All

---

## Profile Picture

Verify:

Highest Resolution

Preview

Download

---

## Caption

Verify:

Copy

Export

Share

---

## Metadata

Verify:

Username

Caption

Date

Audio

Location

Hashtags

---

# 10. Cookie Testing

Verify:

Cookie Import

Cookie Validation

Expired Cookies

Invalid Cookies

Private Posts

Private Stories

Private Highlights

Secure Storage

No cookie leakage

---

# 11. URL Validation Tests

Accept:

instagram.com

www.instagram.com

m.instagram.com

Reject:

youtube.com

youtu.be

facebook.com

twitter.com

tiktok.com

invalid domains

empty input

malformed URLs

---

# 12. Download Testing

Verify:

Pause

Resume

Cancel

Retry

Concurrent Downloads

Queue Ordering

Failure Recovery

Low Storage

Network Loss

App Restart

Background Execution

Foreground Service

---

# 13. Storage Testing

Verify:

MediaStore

SAF

Existing Files

Duplicate Names

Permissions

Download Location

Large Files

---

# 14. Notification Testing

Verify:

Start

Progress

Pause

Resume

Complete

Failed

Cancel

Notification Actions

---

# 15. Accessibility Testing

Verify:

TalkBack

Large Fonts

Display Scaling

Landscape

Dark Theme

Touch Targets

Contrast

Content Descriptions

Keyboard Navigation

---

# 16. Performance Testing

Measure:

Cold Startup

Warm Startup

Scroll Performance

Media Parsing

Carousel Rendering

Large Carousel

Queue Performance

Memory Usage

CPU Usage

Recomposition Frequency

Avoid:

Main Thread Blocking

Dropped Frames

Memory Leaks

Repeated Parsing

---

# 17. Security Testing

Verify:

URL Validation

Cookie Protection

Storage Access

File Names

Permission Handling

Command Execution

No Sensitive Logs

Safe Downloads

---

# 18. Manual Device Testing

Test on:

Android 8+

Android 10

Android 12

Android 14+

At least one physical Android device before release.

Recommended:

Low-end device

Mid-range device

High-end device

---

# 19. Release Validation

Before every release:

✓ Clean Build

✓ Install APK

✓ Launch

✓ Test Every Instagram Media Type

✓ Verify Queue

✓ Verify Downloads

✓ Verify History

✓ Verify Settings

✓ Verify Notifications

✓ Verify Cookies

✓ Verify Storage

✓ Verify Regression Checklist

Only then approve release.

---

# 20. Bug Policy

Every confirmed bug must include:

Issue ID

Summary

Steps to Reproduce

Expected Result

Actual Result

Screenshots (if applicable)

Logcat

Stack Trace

Root Cause

Files Changed

Tests Added

Regression Verification

Status

---

# 21. Evidence Required

Every completed Work Package must provide:

Build Log

Test Summary

Regression Report

Screenshots (UI)

Performance Notes

Known Limitations

Evidence must be stored alongside the project documentation.

---

# 22. Success Criteria

The testing strategy is successful when:

Every supported Instagram media type functions correctly.

Regression rates remain low.

Critical bugs are detected before release.

Contributors have confidence when modifying the codebase.

The application remains stable across Android versions.

Testing becomes part of development rather than a separate activity.