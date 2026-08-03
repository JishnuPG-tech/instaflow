# START HERE — First Read Protocol for AI Engineers & Contributors

Welcome to **InstaFlow**. This document is the required entry point for any human contributor or AI coding agent before inspecting code or performing any task.

---

## 1. Required Document Reading Order

Before attempting any code modification, inspect documents in this exact order:

1. [`docs/core/MASTER_BUILD_PROMPT.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/core/MASTER_BUILD_PROMPT.md) — Master directives and engineering standard.
2. [`docs/core/AI_CONTEXT.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/core/AI_CONTEXT.md) — Architectural philosophy and vision.
3. [`docs/core/PROJECT_STATUS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/core/PROJECT_STATUS.md) — Current phase dashboard and active Work Package.
4. [`docs/core/AI_AGENT_PLAYBOOK.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/core/AI_AGENT_PLAYBOOK.md) — 13-step execution loop and Definition of Done.
5. [`docs/instaflow/INSTAFLOW_REQUIREMENTS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/instaflow/INSTAFLOW_REQUIREMENTS.md) — Product requirements and media model.
6. [`docs/instaflow/FEATURE_MATRIX.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/instaflow/FEATURE_MATRIX.md) — Supported media types & action matrix.
7. [`docs/instaflow/USER_FLOWS.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/instaflow/USER_FLOWS.md) — User journey diagrams and interactions.
8. [`docs/instaflow/MEDIA_MODEL.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/instaflow/MEDIA_MODEL.md) — Carousel, Reel, Story, and Post data entities.
9. [`docs/seal/SEAL_ARCHITECTURE.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/seal/SEAL_ARCHITECTURE.md) — Upstream Seal reverse-engineered architecture.
10. [`docs/seal/KEEP_MODIFY_REPLACE_REMOVE_MATRIX.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/seal/KEEP_MODIFY_REPLACE_REMOVE_MATRIX.md) — Adaptation classifications.
11. [`docs/knowledge/CLASS_CATALOG.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/knowledge/CLASS_CATALOG.md) — Searchable engineering catalogs.
12. [`IMPLEMENTATION_BACKLOG.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/IMPLEMENTATION_BACKLOG.md) — Work Package queue.

---

## 2. Core Execution Protocol

- **One Work Package per session**.
- **One logical feature per commit**.
- **Always run verification**: Compile, ktfmtCheck, unit tests, integration tests.
- **Save evidence**: Store build logs and reports in `docs/evidence/WP_X_Y/`.
