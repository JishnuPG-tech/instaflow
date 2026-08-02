# ADR 001: OpenAPI Contract-Driven Code Generation

## Status
Accepted

## Context
InstaSave consists of an Android application (`insta-save-android`) and a FastAPI extraction service (`backend/`). In typical mobile development, hand-crafting Retrofit network interfaces and DTO models leads to client/server schema drift, unhandled nullability bugs, and breaking runtime exceptions when the API contract evolves.

## Decision
We establish `API_SPEC.yaml` (OpenAPI 3.1.0) as the single source of truth for all network contracts between the Android client and the FastAPI backend.
- The Python backend enforces request/response shapes via Pydantic models derived from `API_SPEC.yaml`.
- The Android application generates typed Retrofit interfaces and Kotlin DTO models using `openapi-generator-cli` (`jvm-retrofit2` library) into `core/network/generated/`.
- Hand-editing generated network files is strictly prohibited.
- CI/CD validates `API_SPEC.yaml` using `openapi-spec-validator` on every build.

## Consequences
- **Positive**: Complete type safety between client and backend; client/server drift becomes a build-time compiler error rather than a runtime crash.
- **Positive**: Zero boilerplate code required for Retrofit request/response mappings.
- **Negative**: Client code generation step added to the build pipeline.

## Compliance
Verified via `openapi-spec-validator API_SPEC.yaml` during Work Package 0.1.
