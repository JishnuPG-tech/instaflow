# CI/CD Workflows — GitHub Actions

## 1. GitHub Actions Workflows ([`.github/workflows`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/.github/workflows))

- **Android CI (`android.yml`)**: Runs on pull requests and pushes to `main`. Executes `./gradlew ktlintCheck` and `./gradlew testDebugUnitTest`.
- **Nightly / Preview Release (`nightly.yml`)**: Automated daily build of `githubPreview` APKs signed with repository secrets and uploaded to GitHub Releases.
- **Production Release (`release.yml`)**: Triggered on release tags (`v*`). Builds signed APKs (`generic`, `fdroid`, ABI splits) and publishes release artifacts.
