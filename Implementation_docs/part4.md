PART 4

Testing • Debugging • Security • Performance • CI/CD • Release Engineering • Production Readiness

54. TESTING PHILOSOPHY

No code is considered complete simply because it compiles.

Every feature must be verified through multiple layers of testing before it is accepted.

Testing exists to prove correctness, prevent regressions, and maintain long-term confidence in the codebase.

Testing is a first-class engineering activity.

55. TEST PYRAMID

Every feature should follow this hierarchy.

                  Manual Verification
                         ▲
                    End-to-End Tests
                         ▲
                 Integration Tests
                         ▲
                    UI Compose Tests
                         ▲
                     Unit Tests

Favor many fast tests over a few slow tests.

56. REQUIRED TEST TYPES

Every Work Package must include, where applicable:

Unit Tests

ViewModels
Repositories
Utilities
Mappers
Validators

Integration Tests

Repository ↔ yt-dlp
Repository ↔ Room
WorkManager
Download Queue

Compose UI Tests

Navigation
Dialogs
Carousel Picker
Media Preview
Settings
Queue
History

Regression Tests

Existing download workflows
Existing settings
Existing queue behavior
Existing notifications

Performance Tests

Startup
Scrolling
Download Queue
Media Parsing

Accessibility Tests

Screen Reader
Contrast
Font Scaling
Keyboard Navigation

Manual Device Verification

Android 8+
Android 10
Android 12
Android 14+
Tablets (optional)
57. INSTAGRAM TEST MATRIX

Every supported Instagram media type must be tested.

Mandatory cases:

✓ Public Image Post

✓ Public Video Post

✓ Public Reel

✓ Public Carousel

✓ Mixed Carousel

✓ Story

✓ Highlight

✓ Profile Picture

✓ Caption

✓ Metadata

✓ Cookie Protected Content

✓ Invalid URL

✓ Deleted Content

✓ Private Content

✓ Geo-restricted Content

✓ Unsupported URL

58. CAROUSEL TESTS

Verify:

Single Image Carousel

Image + Video Carousel

Large Carousel

Select One

Select Multiple

Select All

Clear Selection

Download Selected

Download All

Cancel Download

Resume Download

Delete Download

Every workflow must be tested independently.

59. AUDIO TESTS

Verify:

Video + Audio

Video Only

Audio Only

Unavailable Audio

Corrupted Audio

Missing Audio

If an option is unsupported, the UI must hide it rather than fail.

60. DEBUGGING MODE

When a bug is discovered:

Stop feature development immediately.

Switch into Debugging Mode.

Follow this sequence:

Reproduce the issue.
Collect evidence.
Analyze the root cause.
Compare with Seal.
Implement the smallest correct fix.
Rebuild.
Retest.
Run regression tests.
Verify.
Close the issue.

Never skip a step.

61. BUG REPORT FORMAT

Every bug report must include:

Issue ID

Summary

Steps to Reproduce

Expected Result

Actual Result

Logcat

Stack Trace

Relevant Screens

Affected Files

Root Cause

Fix

Tests Executed

Regression Results

Status

62. LOG COLLECTION

Always collect:

Android

Logcat
StrictMode
ANR
Crash Stack
Coroutine Exceptions
WorkManager Logs
Download Logs

Download Engine

yt-dlp Output
aria2c Output
Download Queue Events

Application

ViewModel State
Repository State
Navigation Events

Never debug without evidence.

63. SECURITY REQUIREMENTS

Maintain:

Least Privilege

Secure Storage

Safe File Handling

Input Validation

Cookie Protection

Safe URI Parsing

No Sensitive Logging

Validate:

Instagram URLs

File Names

Download Paths

Storage Access

Permissions

64. PERFORMANCE BUDGETS

Target:

Fast cold start

Responsive scrolling

Smooth Compose animations

Efficient download queue

Minimal memory usage

Minimal unnecessary recompositions

Avoid:

Blocking Main Thread

Excessive allocations

Repeated parsing

Redundant recompositions

Duplicate downloads

65. ACCESSIBILITY

Every screen should support:

TalkBack

Large Fonts

High Contrast

Keyboard Navigation

Touch Target Size

Readable Typography

Meaningful Content Descriptions

Accessibility is mandatory.

66. CI/CD EXPECTATIONS

Every pull request should automatically run:

Gradle Build

Unit Tests

Compose Tests

Lint

Detekt (if enabled)

Ktlint

Dependency Audit

License Check

Regression Tests

Release Build Validation

Every CI run must finish successfully before merge.

67. RELEASE CHECKLIST

Before any release:

✓ All Work Packages complete

✓ No critical bugs

✓ No known crashes

✓ CI green

✓ Tests passing

✓ Documentation updated

✓ Changelog updated

✓ Version incremented

✓ License verified

✓ Attribution verified

✓ APK generated

✓ AAB generated

✓ Manual smoke test completed

Only then create a release.

68. OPEN SOURCE STANDARDS

Maintain:

Readable documentation

Contributor guidelines

Issue templates

Pull request templates

Changelog

Code of Conduct

Architecture Decision Records (ADRs)

Session handoff reports

Welcome external contributors.

69. FUTURE ROADMAP

Design the architecture to support future features without major rewrites.

Potential future enhancements:

Batch URL imports
Clipboard monitoring
Scheduled downloads
Download rules
Smart folders
Metadata export
Gallery organization
Media search
Favorites
Download analytics
Plugin architecture (if ever needed)

Do not implement these now.

Simply ensure the architecture can accommodate them.

70. FINAL ENGINEERING PRINCIPLES

Every decision should satisfy these questions:

Is it simpler?

Is it safer?

Is it easier to maintain?

Is it easier to test?

Is it easier to extend?

Does it preserve Seal's proven engineering where appropriate?

Does it improve the Instagram experience?

If the answer to any of these is "No," reconsider the implementation.

71. FINAL OBJECTIVE

The goal is not to create another downloader.

The goal is to create the definitive open-source Instagram media manager.

InstaFlow should:

Preserve the proven engineering foundation of Seal.
Deliver a polished, Instagram-first experience.
Support images, videos, reels, stories, highlights, profile pictures, captions, metadata, and mixed-media carousels.
Provide intelligent download workflows such as "Download Selected," "Download All," "Video + Audio," "Video Only," and "Audio Only" where technically supported.
Be stable, secure, maintainable, accessible, and welcoming to contributors.
Stand on its own identity while openly acknowledging and respecting the upstream Seal project and its GPLv3 license.
END OF MASTER_BUILD_PROMPT.md

This completes the core engineering handbook. One enhancement I would make before using it with an autonomous coding agent is to add appendices rather than expanding the main document. Useful appendices include:

Appendix A: Complete phase-by-phase implementation roadmap.
Appendix B: Instagram test URL matrix and expected behaviors.
Appendix C: UI/UX specifications and screenshots.
Appendix D: Feature parity matrix comparing Seal and InstaFlow.
Appendix E: Coding conventions and naming standards.
Appendix F: Known limitations and technical decisions (ADR index).

Keeping those as separate reference documents will make the master prompt easier for AI agents to consume while preserving all the project detail.