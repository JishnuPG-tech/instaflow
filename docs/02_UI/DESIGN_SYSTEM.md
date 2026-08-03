# Design System — Jetpack Compose & Material 3

## 1. Overview

Seal's design system is built entirely on **Material Design 3 (M3)** using Jetpack Compose (`androidx.compose.material3`). It emphasizes accessibility, responsive layout sizing, dark-theme contrast, and seamless dynamic color integration.

---

## 2. Color Palette & Dynamic Theming

- **Material 3 Expressive Color Roles**: Primary, Secondary, Tertiary, Surface, SurfaceVariant, Outline, Error.
- **Dynamic Color Support**: Uses `DynamicColors.applyToActivitiesIfAvailable(this)` in [`App.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/App.kt) to extract dynamic system wallpaper palettes on Android 12+ (API 31+).
- **Custom Accent Engine (`:color` module)**: Allows user selection of seed colors, generating custom M3 color schemes for pre-Android 12 devices or users preferring custom themes.

---

## 3. Typography & Shapes

- **Font Family**: System Default / Roboto, styled through Material 3 `Typography` scale (Display, Headline, Title, Body, Label).
- **Shape Scale**: Material 3 rounded corners (`ExtraSmall`, `Small`, `Medium`, `Large`, `ExtraLarge`, `Full`).

---

## 4. Key Composable Primitives

- `Scaffold`: Structural layout container with `TopAppBar` and `BottomBar` support.
- `ModalBottomSheet` & `ActionSheet`: Used for quick download configuration, format selection, and playlist index selection.
- `Card` & `ElevatedCard`: Container wrappers for download items, history lists, and preference settings.
