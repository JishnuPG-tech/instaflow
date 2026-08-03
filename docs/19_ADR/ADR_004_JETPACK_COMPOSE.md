# ADR 004: 100% Declarative Jetpack Compose & Material Design 3

## Status
Accepted

## Context
Legacy Android XML layouts require verbose ViewHolders, adapter boilerplate, and state synchronization code.

## Decision
Build 100% of Seal's user interface using Jetpack Compose and Material Design 3.

## Consequences
- **Positive**: Declarative unidirectional data flow, built-in animation system, rapid UI composition, and native dynamic color support.
- **Negative**: Requires strict recomposition hygiene to avoid redundant rendering.
