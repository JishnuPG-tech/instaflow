# ADR 002: Adoption of MMKV for Key-Value Preference Storage

## Status
Accepted

## Context
Standard Android SharedPreferences cause thread blocking and disk I/O bottlenecks. Jetpack DataStore introduces asynchronous migration complexity.

## Decision
Adopt **Tencent MMKV 1.3.12** for non-volatile key-value storage.

## Consequences
- **Positive**: Memory-mapped files (`mmap`) deliver near-instant synchronous reads/writes without main-thread blocking. Multi-process safe.
- **Negative**: Native `.so` binary size contribution.
