# Pre-Implementation Architecture Review & Discovery — Phase 2 (Android Core UI & Single-Activity Navigation)

**Phase Scope**: Material 3 Expressive Theme System, Single-Activity Jetpack Compose Navigation 3 Graph, Main Layout Navigation Bar, Home Screen UDF Architecture, Share Sheet Intent Resolution.
**Engineering Standard**: Google / AOSP Open-Source Production Grade
**Governance**: Strict Single-Phase Isolation, Pre-Implementation Seal Comparison, UDF State Contract.

---

## 1. Project Documentation Summary (Phase 2 Baseline)

### PRD.md Requirements
- **Flow A (Share Sheet)**: Instagram Share → Share to InstaSave → resolve link in background / bottom sheet.
- **Flow B (In-app Paste)**: Auto-detect clipboard URL, paste button, single-tap fetch.
- **FR8 (Settings & Navigation)**: Single-activity Compose application targeting SDK 35 (Android 15), Material 3 Expressive design tokens, true-black (`#000000`) dark theme.

### UI_UX_DESIGN.md Requirements
- **Color Palette**: Pure dark background `#000000` (AMOLED true-black), Primary Accent `#E1306C` (Instagram Gradient Coral/Pink), Surface Dark `#121212`, Surface Variant `#1E1E1E`.
- **Expressive Shapes**: Material 3 Expressive rounded corners (28dp extra-large, 16dp large, 12dp medium, 8dp small).
- **Typography**: Inter / System Roboto with explicit scale tokens (`titleLarge`, `bodyMedium`, `labelSmall`).

### UI_ARCHITECTURE.md Requirements
- **MVVM + Unidirectional Data Flow (UDF)**:
  - Each screen consists of: `UiState` (immutable data class), `UiEvent` (sealed interface), `ViewModel` (`@HiltViewModel`), `Screen` (stateful wrapper), `Content` (stateless `@Composable`).
- **No Direct Repositories in Composables**: Composables communicate exclusively via `viewModel.onEvent(...)` and observe `viewModel.uiState`.
- **Navigation 3 / Compose Navigation**: Type-safe routes via `ScreenRoute` sealed hierarchy.

---

## 2. Mandatory Seal Reference Comparison Table

| Subsystem / Area | InstaSave Spec | Seal Implementation | Final Decision | Rationale for Deviation / Adaptation |
|---|---|---|---|---|
| **Theme System** | Material 3 Expressive True-Black (`#000000`) + IG Accent (`#E1306C`) | Material You Dynamic Colors + Dark Theme | **InstaSave Instagram Expressive Theme**: True-black background + Instagram coral accent | Purpose-built for Instagram media viewing on OLED displays. |
| **Navigation Graph** | Single-Activity Compose Navigation (`2.8.7`) with type-safe `ScreenRoute` | Single-Activity `androidx.navigation.compose` | **Adapt Seal Navigation Pattern**: Single Activity + Compose Navigation bar & graph | Battle-tested single-activity architecture from Seal. |
| **Home Screen UDF** | `HomeUiState`, `HomeUiEvent`, `HomeViewModel` | `HomeViewModel` + StateFlow | **Strict UDF Pattern**: Single state container + sealed event interface per `UI_ARCHITECTURE.md §2` | Guarantees deterministic state changes and eliminates state drift across screens. |
| **Share Sheet Intent** | `SEND` / `SEND_MULTIPLE` Intent Handler in `MainActivity` | `MainActivity` intent handling for share links | **Adapt Seal Intent Handler**: Extract URL from `Intent.EXTRA_TEXT` in `MainActivity` | Enables one-tap resolution directly from Instagram app. |

---

## 3. Affected Modules & File Inventory

| Module / Component | Action | Target File Path | Purpose |
|---|---|---|---|
| **Theme Colors** | `MODIFY` | [Color.kt](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/app/src/main/java/com/instasave/app/ui/theme/Color.kt) | Define true-black `#000000`, Instagram coral `#E1306C`, surface colors. |
| **Theme Setup** | `MODIFY` | [Theme.kt](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/app/src/main/java/com/instasave/app/ui/theme/Theme.kt) | `InstaSaveTheme` with Material 3 Expressive color scheme. |
| **Navigation Routes** | `NEW` | `app/src/main/java/com/instasave/app/ui/navigation/ScreenRoute.kt` | Type-safe route definitions (`Home`, `History`, `Downloads`, `Settings`). |
| **Navigation Graph** | `NEW` | `app/src/main/java/com/instasave/app/ui/navigation/AppNavigation.kt` | Compose `NavHost` setup. |
| **Main Screen Layout** | `NEW` | `app/src/main/java/com/instasave/app/ui/navigation/MainScreen.kt` | `Scaffold` with Material 3 Expressive `NavigationBar`. |
| **Home Contract** | `NEW` | `app/src/main/java/com/instasave/app/ui/home/HomeContract.kt` | `HomeUiState` and `HomeUiEvent` interfaces. |
| **Home ViewModel** | `NEW` | `app/src/main/java/com/instasave/app/ui/home/HomeViewModel.kt` | `@HiltViewModel` managing home state and API resolution. |
| **Home Screen** | `NEW` | `app/src/main/java/com/instasave/app/ui/home/HomeScreen.kt` | Stateful `HomeScreen` & stateless `HomeContent` with clipboard paste. |
| **Main Activity** | `MODIFY` | [MainActivity.kt](file:///c:/Users/JISHNU%20PG/Music/InstaFlow/app/src/main/java/com/instasave/app/MainActivity.kt) | Setup `MainScreen` & handle Instagram `SEND` intent. |

---

## 4. Dependencies & Version Lock

- **Android SDK**: `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`
- **Jetpack Compose BOM**: `2025.02.00`
- **Material 3 Expressive**: `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`
- **Navigation Compose**: `2.8.7`
- **Hilt Navigation Compose**: `1.2.0`
- **Hilt DI**: `2.55`

---

## 5. Architectural Risks & Mitigation Strategies

| Risk ID | Risk Description | Severity | Mitigation Strategy |
|---|---|---|---|
| **RISK-01** | Clipboard permission prompt or null clipboard in Android 10+ (API 29+). | **MEDIUM** | Gracefully handle `ClipboardManager` security exceptions and check clipboard primary clip description. |
| **RISK-02** | Share sheet intent containing extra text surrounding Instagram URL. | **LOW** | Use regex extractor (`https?://(www\.)?instagram\.com/[^\s]+`) to isolate clean URL from shared text. |
| **RISK-03** | State loss during orientation change or process death. | **LOW** | Store persistent state in `SavedStateHandle` inside `HomeViewModel`. |

---

## 6. Assumptions & Constraints

1. **True-Black OLED Design**: Theme defaults to pure dark `#000000` for maximum battery efficiency and premium aesthetic.
2. **Strict UDF Enforcement**: All UI actions pass through `HomeUiEvent` to `HomeViewModel`.
3. **Remote Codespace & GitHub Actions CI Testing**: All builds and tests are executed remotely in the cloud / GitHub Codespaces.
