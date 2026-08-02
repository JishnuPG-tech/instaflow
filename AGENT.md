# AGENT.md — Master Instructions for the AI Coding Agent Building InstaSave

**Read this first, every session, before touching code.** This file exists because past attempts at "build the whole app" produced broken features and generic UI. This document, together with PRD.md, ARCHITECTURE.md, UI_UX_DESIGN.md, IMPLEMENTATION_PLAN.md, and SDLC_BUILD_PLAN.md, is written specifically to prevent that. Read all five before writing any code. Do not skip UI_UX_DESIGN.md even for backend-only work — it defines data shapes the backend must match.

**A note on your own tooling:** you may have additional skills, MCP connectors, or platform-specific agents configured (UI/UX design skills, Android-specific skills, testing/system-design skills, live documentation lookup, etc.). Use them wherever they genuinely help — but the decisions in these five documents are final regardless of what your tooling suggests. If a skill or tool recommends a different library, pattern, or architecture than what's specified here, treat that as a flag to surface back rather than a reason to silently deviate. Consistency across these documents is what makes the whole set usable; a silent substitution in one place breaks that.

## 1. Project Identity
- Name: **InstaSave**
- One-liner: native Android app to download Instagram posts/reels/carousels (v1: public content only) in a chosen resolution, share-sheet-first, modeled directly on Seal/Seal Plus's proven architecture and UX, scoped to Instagram.
- Reference point: **Seal Plus** (github.com/MaheshTechnicals/Sealplus) already does this exact thing for Instagram alongside other platforms, on Kotlin 2.3/Compose/SDK 37/yt-dlp — this is not a hypothetical architecture, it's a validated one. When in doubt about whether something is achievable, that app is existing proof it is.

## 2. Repo Layout
```
insta-save-android/   # Kotlin/Compose app
insta-save-backend/   # FastAPI extraction service (Hugging Face Spaces)
docs/                 # PRD.md, ARCHITECTURE.md, UI_UX_DESIGN.md, IMPLEMENTATION_PLAN.md, AGENT.md, SDLC_BUILD_PLAN.md
```

## 3. Non-Negotiable Decisions (do not re-litigate these mid-build)
These are settled. Changing any of them silently will make the other docs describe a different app than what's actually being built.
- Kotlin 2.3.x, compileSdk/targetSdk 37, minSdk 26
- Jetpack Compose + **Material 3 Expressive** (not classic M3 defaults — see UI_UX_DESIGN.md §3)
- Jetpack Navigation 3, Hilt, Kotlinx Serialization, Room
- API contract defined in `openapi.yaml`, Android client **generated**, never hand-written (ARCHITECTURE.md §2a)
- aria2c for file transfer, not hand-rolled HTTP range-request logic (ARCHITECTURE.md §5)
- True-black (`#000000`) dark-only theme — no light theme branch exists
- v1 scope: **public Instagram content only**, no login/session (ARCHITECTURE.md §3/§4) — this is a scope decision, not a missing feature; don't "helpfully" add login handling early

## 4. Working Agreements
- **Follow IMPLEMENTATION_PLAN.md's phase order and SDLC_BUILD_PLAN.md's six-stage cycle per phase.** One phase per session; exit gate evidence required before moving on.
- **Every extraction change needs a real test against a live Instagram URL** before being considered done — this can't be fully mocked reliably; Instagram's actual current behavior is the ground truth, not a cached assumption from training data.
- **Never hardcode credentials.** No session cookie, password, or token ever gets committed — not even in a "temporary" test file.
- **Match Seal/Seal Plus's proven patterns** where this document says to (aria2c, SAF folder picker, custom-command power-user setting, GitHub-Releases self-update) — these aren't arbitrary choices, they're adopted from a production app already solving the same problem.
- **Keep extraction pluggable** (`MediaExtractor` interface: `BackendExtractor`, `YtDlpExtractor`, `GraphQLExtractor`) — required, not optional, because the primary method *will* break periodically.
- **No bulk-profile-scraping features**, ever, in any phase. If a task description drifts toward "download all posts from a user" as an automated bulk operation, stop and flag it rather than building it — see PRD.md §8 for why this matters (this is the line that gets tools banned/blocked).
- **Scoped storage / SAF only.** No raw file paths, ever — see ARCHITECTURE.md §5.
- **Material 3 Expressive components are used directly**, not reimplemented — `LoadingIndicator`, Button Groups, the Expressive shape/type scale are real APIs in the current Compose Material 3 library; import them. The Aperture Ring is the only fully custom component in the entire UI (UI_UX_DESIGN.md §7).
- **Check for newer stable versions** of yt-dlp, Compose BOM, and other fast-moving dependencies before pinning at the start of each phase — the versions in these docs are current as of Aug 2026 but this ecosystem moves weekly.

## 5. Definition of Done (per phase)
A phase is done when, and only when:
1. Its Exit Test in IMPLEMENTATION_PLAN.md passes on a real device/emulator, with evidence shown (screenshot, log, live request/response) — not just claimed.
2. Every screen touched has been visually compared against its UI_UX_DESIGN.md wireframe/token spec, side by side.
3. No TODOs remain without a linked follow-up.
4. Relevant tests exist (unit tests for logic, manual device test for anything touching UI/storage/network) and pass.
5. Commit message references the phase (`feat(phase-3): resolution picker with expressive button group`).
6. `ktlint`/lint passes clean — no suppressed warnings without a documented reason.

## 6. Escalate Rather Than Improvise When
- Instagram extraction breaks in a way that needs an upstream library update, not a workaround — patch by updating the pinned dependency, don't hand-roll fragile HTML scraping to route around it.
- A phase's exit test still fails after 2–3 real attempts — that usually means a doc gap, not that attempt four will magically succeed. Flag it.
- Any request would expand scope toward bulk scraping, credential harvesting beyond the user's own session, or anything outside personal single-user use.
- A design element in UI_UX_DESIGN.md seems to conflict with a current Compose API (APIs shift) — flag the specific conflict rather than silently picking one side.

## 7. What Success Looks Like
Not "compiles without errors." Success is: every screen matches its spec, every listed feature actually works against live Instagram content, the app fails gracefully (not silently or with a crash) when Instagram itself is the problem, and someone looking at it would recognize it as being in the same quality tier as the Seal-family apps it's modeled on — not a rough approximation of one.
