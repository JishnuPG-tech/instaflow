# GOVERNANCE

Version: 2.0

Project: InstaFlow

Status: Engineering Governance

License: GPLv3

---

# 1. Purpose

This document defines the engineering governance for InstaFlow.

Governance ensures that every contribution follows the same engineering standards regardless of whether it is created by:

- Human contributors
- AI coding agents
- Future maintainers

The objective is consistency, quality, maintainability, and long-term sustainability.

---

# 2. Engineering Principles

Every engineering decision should optimize for:

- Correctness
- Maintainability
- Reliability
- Simplicity
- Testability
- Performance
- Accessibility
- Security
- Documentation
- Contributor friendliness

Never optimize solely for:

- Speed
- Quantity of code
- Artificial deadlines
- Clever implementations
- Premature optimization

---

# 3. Source of Truth

Engineering decisions follow this order:

1. PROJECT_CONSTITUTION.md (or MASTER_BUILD_PROMPT.md)
2. ARCHITECTURE.md
3. IMPLEMENTATION_PLAN.md
4. ADRs
5. TESTING_STRATEGY.md
6. SECURITY_CHECKLIST.md
7. Official Android documentation
8. Upstream Seal repository

---

# 4. Engineering Roles

Every completed Work Package should be reviewed from these perspectives.

Principal Android Engineer

Focus

- Android architecture
- Compose
- Lifecycle
- Build quality

---

Principal Software Architect

Focus

- Layer boundaries
- Dependencies
- Maintainability

---

Principal QA Engineer

Focus

- Testing
- Regression
- Edge cases

---

Principal Security Engineer

Focus

- Permissions
- Cookies
- File handling
- Input validation

---

Principal Performance Engineer

Focus

- Memory
- Startup
- Download performance
- Compose recomposition

---

Principal Accessibility Engineer

Focus

- TalkBack
- Font scaling
- Contrast
- Navigation

---

Principal UX Engineer

Focus

- Consistency
- Discoverability
- Instagram-first workflow

---

Principal Open Source Maintainer

Focus

- Documentation
- Readability
- Licensing
- Contributor experience

---

# 5. Development Lifecycle

Every Work Package must follow the same lifecycle.

Requirements

↓

Architecture Review

↓

Seal Review

↓

Implementation Plan

↓

Implementation

↓

Compilation

↓

Testing

↓

Regression

↓

Documentation

↓

Review

↓

Merge

Skipping steps is prohibited.

---

# 6. Seal Review Requirement

Before implementing any subsystem:

Inspect Seal.

Document:

- Relevant files
- Relevant classes
- Current behavior
- Reusable implementation
- Differences required for InstaFlow

If Seal already provides a mature implementation, reuse it unless InstaFlow requirements explicitly differ.

---

# 7. Architecture Review

Before implementation answer:

What subsystem changes?

What dependencies are affected?

Does this violate architecture?

Will future maintenance become easier?

Can the change be tested?

Would this likely be accepted upstream?

Only proceed when the review is satisfactory.

---

# 8. Definition of Ready

A Work Package is ready only if:

- Requirements are clear.
- Acceptance criteria exist.
- Architecture impact is understood.
- Dependencies identified.
- Seal comparison completed.
- Test plan written.

Otherwise implementation must not begin.

---

# 9. Definition of Done

A Work Package is complete only if:

✓ Implementation complete

✓ Builds successfully

✓ Tests pass

✓ Regression passes

✓ Documentation updated

✓ No critical defects

✓ Code reviewed

✓ Evidence collected

---

# 10. Mandatory Evidence

Every Work Package must produce:

Build log

Test summary

Regression summary

Files changed

Screenshots (UI changes)

Git diff summary

Architecture notes

Known limitations

Without evidence the Work Package remains open.

---

# 11. Coding Standards

Every contribution should:

- Follow Kotlin conventions.
- Use descriptive names.
- Avoid duplicated logic.
- Keep functions focused.
- Prefer composition over inheritance.
- Keep files reasonably small.
- Minimize public APIs.

Never introduce:

- TODOs in production code.
- FIXME markers.
- Dead code.
- Duplicate implementations.
- Hidden side effects.

---

# 12. Architectural Invariants

These rules are never broken.

- UI never accesses yt-dlp directly.
- UI never accesses Room directly.
- Business logic never lives in Composables.
- ViewModels communicate through repositories.
- Downloads always use Download Manager.
- WorkManager controls background work.
- Cookies are managed centrally.
- Every feature remains independently testable.

---

# 13. Git Standards

Commit frequently.

Each commit should represent one logical change.

Recommended Conventional Commit format:

feat(download): add carousel selection

fix(parser): handle mixed-media posts

refactor(repository): simplify metadata cache

docs(governance): update review process

Avoid vague commit messages.

---

# 14. Branch Strategy

main

Always releasable.

develop (optional)

Integration branch.

feature/<name>

Single feature.

fix/<issue>

Bug fix.

docs/<topic>

Documentation only.

release/<version>

Release preparation.

---

# 15. Pull Request Requirements

Every Pull Request must include:

Summary

Motivation

Screenshots (if UI)

Testing performed

Regression impact

Documentation updates

Known limitations

Seal comparison (if architecture changed)

---

# 16. Failure Policy

If any quality gate fails:

Stop implementation.

Identify the root cause.

Fix the issue.

Repeat all affected verification steps.

Do not continue until the failure is resolved.

---

# 17. Regression Policy

Every completed Work Package must verify:

Application launches.

Instagram URL accepted.

Image posts work.

Video posts work.

Carousel works.

Queue works.

History works.

Settings work.

Notifications work.

No regressions introduced.

---

# 18. Documentation Policy

Every architecture change requires:

- Documentation update.
- ADR if applicable.
- Implementation Plan update if roadmap changes.

Documentation is part of the feature, not an afterthought.

---

# 19. Release Governance

A release is permitted only when:

✓ All planned Work Packages complete.

✓ No critical bugs.

✓ CI green.

✓ Regression clean.

✓ Documentation complete.

✓ GPL attribution verified.

✓ APK tested.

✓ AAB tested.

✓ Release notes prepared.

---

# 20. Continuous Improvement

After every phase conduct a retrospective.

Document:

What worked well.

What caused problems.

Architecture lessons.

Testing lessons.

Seal adaptation lessons.

Potential improvements.

Feed these findings back into the documentation before beginning the next phase.

---

# 21. Engineering Success

Governance is successful when:

- The architecture remains consistent.
- Contributors can understand the project quickly.
- AI agents produce predictable results.
- New features integrate cleanly.
- Regression rates remain low.
- The codebase is maintainable over many years.

Every engineering decision should move the project toward these goals.