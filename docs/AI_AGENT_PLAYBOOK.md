# AI AGENT PLAYBOOK — Standard Operating Protocol for InstaFlow

## 1. Core Operating Directive

You are an expert AI software engineer contributing to **InstaFlow**, an Instagram-first media manager inspired by **Seal**.

---

## 2. Two-Level Project Initialization Protocol

### Level 1: Full Initialization (Once at project start or after major doc updates)
- Read all core specifications in `docs/core/`, `docs/instaflow/`, `docs/seal/`, and `docs/knowledge/`.
- Build an internal knowledge index.

### Level 2: Targeted Pre-Execution Pass (Per Work Package)
For every work package, do **not** reread every markdown file. Instead:
1. Read [`docs/PROJECT_STATUS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/PROJECT_STATUS.md) to identify active WP.
2. Read **only** the documents relevant to that specific WP.
3. Read the matching Seal source code.
4. Output a mandatory **Project Understanding Summary** before writing any code.

---

## 3. Mandatory Pre-Execution Template: Project Understanding Summary

Before writing a single line of code for any Work Package, output the following structured summary:

```markdown
### Project Understanding Summary
- **Current Phase**: [e.g., Phase 1]
- **Current Work Package**: [e.g., WP 1.2]
- **Objective**: [Clear objective description]
- **Seal Subsystem**: [Relevant upstream files/classes]
- **Target Files Affected**: [List of target files]
- **Requirements & Acceptance Criteria**: [Key criteria to fulfill]
- **Identified Risks**: [Potential failure modes]
- **Implementation Plan**: [Short step-by-step approach]
```

---

## 4. Work Package Execution Loop

1. **Output Project Understanding Summary**.
2. **Implement minimal correct change** for the active WP.
3. **Execute verification**:
   - Compile: `./gradlew assembleDebug`
   - Format check: `./gradlew ktfmtCheck`
   - Lint check: `./gradlew lint`
   - Unit tests: `./gradlew testDebugUnitTest`
4. **Collect evidence**: Save reports and logs under `docs/evidence/WP_X_Y/`.
5. **Update Dashboard**: Mark WP completed in [`docs/PROJECT_STATUS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/PROJECT_STATUS.md).
