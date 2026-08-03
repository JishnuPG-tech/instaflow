# ADR 001: Adoption of Koin for Lightweight Dependency Injection

## Status
Accepted

## Context
Seal requires a modular, clean dependency injection framework to wire ViewModels, `DownloaderV2`, and context references without heavy annotation processor overhead or complex Dagger/Hilt setup.

## Decision
Adopt **Koin 4.0.0** (`io.insert-koin:koin-android` and `koin-androidx-compose`).

## Consequences
- **Positive**: Zero code generation compile time overhead (unlike Dagger/Hilt or KSP-heavy DI). Concise Kotlin DSL (`module { single { ... } viewModel { ... } }`).
- **Negative**: Runtime resolution errors if a dependency is omitted from Koin modules.
