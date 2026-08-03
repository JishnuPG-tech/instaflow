# Localization & Translation Architecture

## 1. Multi-Language Framework

- **Source Translations Directory**: [`translations/`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/translations)
- **Android Localization Spec**: `app/src/main/res/values-*/strings.xml`
- **Supported Languages**: Over 40+ community-contributed locales (English, Simplified/Traditional Chinese, Spanish, German, French, Italian, Russian, Japanese, Korean, Hindi, etc.).
- **Automatic Locale Generation**: Enabled via `androidResources { generateLocaleConfig = true }` in [`app/build.gradle.kts`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/build.gradle.kts).
