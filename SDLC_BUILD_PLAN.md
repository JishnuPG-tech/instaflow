# SDLC_BUILD_PLAN.md — InstaSave AI-Assisted Build Protocol

**Purpose:** This is the document to hand to an AI coding agent (Claude Code) at the start of *any* work session. It enforces single-phase focus and a real SDLC gate cycle, so nothing gets marked "done" without evidence.

**Model used:** Incremental/Iterative SDLC. Each phase from IMPLEMENTATION_PLAN.md is run as its own mini-waterfall — Requirements → Design → Build → Test → Review → Done — with a hard gate between phases. This is deliberately stricter than typical vibe-coding, and deliberately lighter than full Scrum ceremony, because it's one person + one AI agent, not a team that needs standups.

**Golden rule, non-negotiable:** *One phase per session. No phase starts until the previous phase's exit gate has passed. No "while I'm at it" scope creep mid-phase.*

---

## 0. How to use this document

At the start of any build session, paste this into the agent along with the specific phase you're working:

> "We're following SDLC_BUILD_PLAN.md. Today we are working ONLY on **Phase N**. Read PRD.md, ARCHITECTURE.md, UI_UX_DESIGN.md, and AGENT.md for context. Do not touch anything outside this phase's scope. Follow the six-stage cycle in section 2 for this phase. Do not mark the phase complete until every item in the Exit Gate checklist is independently verified — show me the evidence for each one."

If the agent tries to jump ahead, refuse and redirect it back to the current phase's cycle.

---

## 1. Master Phase Map (from IMPLEMENTATION_PLAN.md, restated as SDLC units)

| Phase | SDLC Unit Name | Depends on | Risk level |
|---|---|---|---|
| 0 | Environment & Repo Setup | — | Low |
| 1 | Extraction Engine (backend, no UI) | Phase 0 | **Highest — build/test first** |
| 2 | Minimal App: Paste → Resolve → Download | Phase 1 | Medium |
| 3 | Share-Sheet + Resolution Picker UI | Phase 2 | Medium |
| 4 | Login / Session Management | Phase 2 | High (security-sensitive) |
| 5 | Stories & Expiry Handling | Phase 4 | Medium |
| 6 | Download Manager, History, Resilience/Fallbacks | Phase 3, 4 | High (core reliability) |
| 7 | UI/UX Full Polish (per UI_UX_DESIGN.md) | Phase 6 | Medium |
| 8 | Release Hardening & Distribution | Phase 7 | Medium |

Phases 1 and 6 carry the most real risk (Instagram's behavior, resilience logic) — give those the most scrutiny, not the UI polish phase, even though UI bugs are the most *visible* ones.

---

## 2. The Six-Stage Cycle (applied inside every phase)

### Stage 1 — Requirements Recap
Before writing code, the agent restates in its own words:
- What this phase must deliver (pull directly from IMPLEMENTATION_PLAN.md's phase description)
- What is explicitly OUT of scope for this phase (anything belonging to a later phase)
- What "done" means — copy the phase's Exit Test verbatim

**Gate to move to Stage 2:** you confirm the restated scope matches your intent. If it doesn't, correct it before any code is written — this is the cheapest place to catch a misunderstanding.

### Stage 2 — Design Recap
Agent states which parts of ARCHITECTURE.md / UI_UX_DESIGN.md apply to this phase specifically (e.g., Phase 3 → §4.2/§4.3 of UI_UX_DESIGN.md, quality-chip and carousel-grid component specs). No new architectural decisions get invented ad hoc — if something isn't covered by the existing docs, that's flagged as a decision needed, not silently improvised.

**Gate to move to Stage 3:** any new decision is explicitly surfaced and agreed, not buried in a code diff.

### Stage 3 — Build
Agent implements only what Stage 1 scoped. Small, reviewable commits — one logical change per commit, referencing the phase (`feat(phase-3): resolution picker bottom sheet`).

**Rule:** after every meaningful chunk, the agent runs the build. It does not write five files and check at the end — it builds incrementally and catches compile errors immediately, the way IMPLEMENTATION_PLAN.md's exit tests assume.

### Stage 4 — Test
This is the stage most sessions skip — don't let it be skipped. Required, matched to what the phase actually touches:

| Phase touches | Required verification |
|---|---|
| Backend/extraction logic | Real request against a real Instagram URL (not a mock) — show the actual response |
| UI screen | Real screenshot from a running emulator/device, compared against the relevant UI_UX_DESIGN.md wireframe/spec |
| Business logic (use-cases, repositories) | Unit test added and passing |
| Storage/downloads | Manual test: kill app mid-download, confirm resume; check file lands in correct MediaStore location |
| Session/login | Manual test: full login flow, then a request that previously needed auth now succeeds |
| Anything with a UI | Manual pass on at least one real device, not emulator-only |

**Gate to move to Stage 5:** every test in this table relevant to the phase has been run and its result shown to you, not just claimed.

### Stage 5 — Review
- You review the diff (or have the agent walk you through it) — specifically checking: does this match the design tokens exactly? Any hardcoded secrets/paths? Any TODOs without a linked follow-up?
- Agent self-checks against AGENT.md §4 Definition of Done and §6 "Things NOT to do"
- Any deviation from PRD/ARCHITECTURE/UI_UX docs is called out explicitly, with reasoning — not silently shipped

**Gate to move to Stage 6:** you explicitly say "approved" — this is the one stage that requires a human, not the agent self-certifying.

### Stage 6 — Done / Checkpoint
- Final commit for the phase
- Exit Test from IMPLEMENTATION_PLAN.md run one more time, cleanly, as the last action
- Note anything deferred or discovered-but-out-of-scope in a running `NOTES.md` (so it's not lost, but also doesn't derail the current phase)
- **Only now** does the next phase's Stage 1 begin — in a fresh context/session if possible, to avoid drift from a long, cluttered conversation

---

## 3. Exit Gate Template (fill in per phase before declaring done)

```
PHASE: ___
[ ] Stage 1 scope confirmed by human before build started
[ ] Stage 2 design recap matched existing docs, or new decisions were explicitly surfaced
[ ] All commits reference this phase
[ ] Build passes clean (no warnings suppressed silently)
[ ] Required tests from Stage 4 table run, with evidence shown (screenshot / log / test output)
[ ] IMPLEMENTATION_PLAN.md's Exit Test for this phase passes, demonstrated live
[ ] Human reviewed the diff and said "approved"
[ ] No secrets, session tokens, or credentials in the diff
[ ] Deferred items logged in NOTES.md, not silently dropped
[ ] AGENT.md Definition of Done checklist satisfied
```
If any box is unchecked, the phase is not done — regardless of what the agent claims.

---

## 4. Rules That Apply Across All Phases

1. **No phase touches another phase's files without explicit approval.** If Phase 3 work requires touching something Phase 1 built, stop and confirm that's actually necessary rather than letting scope quietly balloon.
2. **If a gate fails twice in a row, stop and re-scope** rather than attempting a third patch — usually means the phase was cut wrong or a design doc gap exists, not that the next attempt will magically work.
3. **Fresh session per phase where practical.** Long single sessions accumulate context drift; a new session re-reading the docs from disk is more reliable than one agent's memory of a 3-hour-old decision.
4. **The design docs are the source of truth, not the agent's taste.** If UI output doesn't match UI_UX_DESIGN.md, that's a bug in the build, not a valid alternative interpretation.
5. **Instagram-side failures are not phase failures.** If Phase 1/6 testing reveals Instagram rate-limiting or extractor breakage, that's expected — the fix is confirming the fallback/error-state behavior works correctly, not endlessly trying to "fix" Instagram's block.

---

## 5. Copy-Paste Session Starter Prompts

**Starting a new phase:**
> "We're following SDLC_BUILD_PLAN.md, working only on Phase [N]: [name]. Read PRD.md, ARCHITECTURE.md, UI_UX_DESIGN.md, AGENT.md, and IMPLEMENTATION_PLAN.md's Phase [N] section. Begin at Stage 1 (Requirements Recap) — restate the scope and exit test before writing any code, and wait for my confirmation."

**Mid-phase, forcing the test stage:**
> "Before continuing, complete Stage 4 (Test) for what you've built so far. Show me real evidence — screenshot, live request/response, or test output — not a description of what should work."

**Claiming done:**
> "Fill out the Exit Gate Template from SDLC_BUILD_PLAN.md section 3 for this phase, with evidence for each line. Don't check a box you haven't actually verified."

**If it tries to scope-creep:**
> "That belongs to Phase [N+X], not this one. Log it in NOTES.md and stay in scope for Phase [N]."
