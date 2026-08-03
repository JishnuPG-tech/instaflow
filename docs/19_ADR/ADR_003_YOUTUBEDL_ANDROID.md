# ADR 003: Native `yt-dlp` Execution via `youtubedl-android`

## Status
Accepted

## Context
Extremely fast, reliable media extraction across 1000+ video hosting sites requires `yt-dlp`, which is written in Python.

## Decision
Integrate JunkFood02's custom fork of `youtubedl-android` (`io.github.junkfood02.youtubedl-android`), bundling compiled Python runtime binaries, `yt-dlp`, `ffmpeg`, and `aria2c`.

## Consequences
- **Positive**: Direct access to `yt-dlp` features without needing cloud servers or custom backend scraping APIs.
- **Negative**: Native binary size (~30-50MB APK size), requiring ABI APK splitting.
