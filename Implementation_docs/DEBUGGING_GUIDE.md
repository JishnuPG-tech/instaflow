# DEBUGGING GUIDE

Version: 2.0

Project: InstaFlow

Status: Production Debugging Standard

Based on: Seal (JunkFood02)

License: GPLv3

---

# 1. Purpose

This document defines the debugging methodology for InstaFlow.

Debugging is an engineering process.

It is not trial and error.

It is not guesswork.

Every bug must be investigated, reproduced, understood, fixed, verified, documented, and regression tested.

No bug is considered resolved until objective evidence proves it.

---

# 2. Debugging Philosophy

Always optimize for:

• Root Cause Analysis

• Minimal Correct Fix

• No Regressions

• Repeatability

• Maintainability

Never:

Guess

Disable functionality

Hide crashes

Ignore exceptions

Introduce hacks

Add unnecessary complexity

Replace working architecture

---

# 3. Debugging Lifecycle

Every bug follows exactly this workflow.

```
Bug Report

↓

Reproduce

↓

Collect Evidence

↓

Identify Root Cause

↓

Compare with Seal

↓

Design Fix

↓

Implement

↓

Compile

↓

Unit Tests

↓

Integration Tests

↓

Regression Tests

↓

Manual Verification

↓

Documentation

↓

Close
```

No steps may be skipped.

---

# 4. Bug Classification

Every issue must be classified.

Critical

• App crashes

• Data corruption

• Security issue

• Download impossible

High

• Download failures

• Queue failures

• Cookie failures

• Incorrect media parsing

Medium

• UI bugs

• Performance issues

• Incorrect metadata

Low

• Cosmetic issues

• Minor animations

• Typography

---

# 5. Bug Report Template

Every issue must include:

Issue ID

Summary

Severity

Android Version

Device

App Version

Steps to Reproduce

Expected Result

Actual Result

Screenshots

Screen Recording (if useful)

Logcat

Stack Trace

Relevant URL

Relevant Media Type

Known Workaround

---

# 6. Reproduction Rules

Never attempt a fix before reproducing.

Verify:

Can the issue be reproduced?

How often?

Which devices?

Which Android versions?

Which Instagram media types?

Document exact reproduction steps.

---

# 7. Evidence Collection

Always collect:

Logcat

Crash Stack Trace

StrictMode Violations

WorkManager Logs

Coroutine Exceptions

Download Queue Logs

yt-dlp Output

aria2 Output

Repository State

ViewModel State

Compose State

Navigation State

Never debug without evidence.

---

# 8. Root Cause Analysis

Identify:

Where did it fail?

Why did it fail?

Why was the failure possible?

What assumptions were incorrect?

Could this happen elsewhere?

Avoid symptom-based fixes.

---

# 9. Seal Comparison

Before implementing a fix:

Locate the equivalent implementation in Seal.

Verify:

Does Seal have the same bug?

How does Seal handle this scenario?

Did InstaFlow introduce the regression?

Can the upstream implementation be reused?

Document findings.

---

# 10. Fix Design

The preferred order is:

Reuse upstream solution

↓

Minimal modification

↓

New implementation

↓

Architectural redesign

Never redesign an entire subsystem to fix a small bug.

---

# 11. Implementation Rules

Every fix should:

Solve only one problem.

Avoid unrelated refactoring.

Preserve architecture.

Keep public APIs stable.

Be easy to review.

---

# 12. Compilation

Every fix must compile successfully.

No warnings introduced.

No temporary code.

No commented-out code.

No TODOs.

---

# 13. Unit Testing

Whenever possible, add or update unit tests covering the bug.

Examples:

URL validation

Media parsing

Metadata

Repositories

ViewModels

Download logic

---

# 14. Integration Testing

Verify interactions:

Repository

↓

Download Engine

↓

yt-dlp

↓

Queue

↓

Storage

↓

History

Ensure no side effects.

---

# 15. Regression Testing

Every bug fix must rerun the minimum regression suite.

Verify:

✓ App launches

✓ Image posts

✓ Video posts

✓ Reels

✓ Stories

✓ Highlights

✓ Carousels

✓ Mixed Carousels

✓ Queue

✓ Notifications

✓ History

✓ Settings

---

# 16. Instagram-Specific Debugging

Always identify the media type.

Possible categories:

IMAGE

VIDEO

REEL

CAROUSEL

MIXED CAROUSEL

STORY

HIGHLIGHT

PROFILE PICTURE

Different media types may require different fixes.

Never assume a bug affects every media type.

---

# 17. Download Debugging

Verify:

URL Resolution

Metadata Parsing

Cookie Handling

Media Discovery

Download Queue

Progress Updates

Retry Logic

Pause

Resume

Completion

Storage

Notifications

---

# 18. Performance Debugging

Measure before optimizing.

Collect:

Startup Time

Frame Time

Memory Usage

CPU Usage

Recomposition Count

Queue Performance

Never optimize based on assumptions.

---

# 19. Security Debugging

Verify:

URL Validation

Filename Sanitization

Cookie Storage

Permission Requests

Storage Access

Intent Validation

Sensitive Logging

Security bugs receive the highest priority.

---

# 20. Accessibility Debugging

Verify:

TalkBack

Large Fonts

Contrast

Landscape

Touch Targets

Keyboard Navigation

Accessibility regressions must be fixed before release.

---

# 21. Documentation

Every resolved bug updates:

Issue Tracker

Changelog (if applicable)

Regression Tests

Architecture Notes (if affected)

ADR (if architecture changed)

---

# 22. Definition of Fixed

A bug is fixed only when:

✓ Root cause identified

✓ Correct implementation completed

✓ Compiles successfully

✓ Tests pass

✓ Regression passes

✓ Manual verification complete

✓ Documentation updated

Otherwise:

Status remains OPEN.

---

# 23. Anti-Patterns

Never:

Guess the cause.

Ignore logs.

Suppress exceptions.

Disable functionality.

Replace architecture unnecessarily.

Duplicate code.

Skip regression testing.

Merge unverified fixes.

---

# 24. AI Debugging Workflow

When debugging, every AI agent must:

1. Read this document.

2. Read ARCHITECTURE.md.

3. Read REFERENCE_POLICY.md.

4. Locate the equivalent subsystem in Seal.

5. Reproduce the issue.

6. Collect evidence.

7. Identify root cause.

8. Design the smallest correct fix.

9. Implement.

10. Test.

11. Verify.

12. Document.

The AI must never jump directly to implementation.

---

# 25. Debugging Success

The debugging process is successful when:

The root cause is eliminated.

No regressions are introduced.

Architecture remains clean.

Future contributors understand the fix.

The same issue cannot easily reappear.

Every fix improves the long-term quality of the project.