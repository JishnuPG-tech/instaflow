# AI Integration & Context Principles

## 1. Principles for AI Coding Agents Working on Seal / InstaFlow

1. **Upstream Fidelity**: Treat [JunkFood02/Seal](https://github.com/JunkFood02/Seal) as the single source of truth for core infrastructure (Room, WorkManager, SAF, `youtubedl-android`, MMKV, Koin).
2. **Empirical Verification**: Never hallucinate API methods or package structures; inspect source files directly.
3. **No Unnecessary Rewrites**: Preserve mature engineering patterns from Seal when building InstaFlow specializations.
