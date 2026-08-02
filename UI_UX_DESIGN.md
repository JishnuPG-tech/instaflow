# UI_UX_DESIGN.md — InstaSave

**v2 — rewritten around Material 3 Expressive** (Google's current design language, shipped with Android 16 / SDK 36-37, validated across 46 rounds of user research and 18,000 participants) **and grounded in what Seal/Seal Plus actually ship today** — a gradient dark theme, hardware-accelerated animations, and a "cockpit, not a feed" feel. This is not a generic dark-mode reskin; every choice below is either a deliberate InstaSave identity decision or a direct adoption of something proven in the exact app category this project models itself on.

**This file is written for an AI coding agent to build from.** Follow it literally — exact hex values, exact component names, exact Compose APIs. Where "Material 3 Expressive" is referenced, that means the actual `androidx.compose.material3` Expressive components (opt-in via `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`), not a hand-rolled imitation.

---

## 1. Design Philosophy

InstaSave downloads photos and videos, so its visual language is a **camera/viewfinder instrument** — not Instagram's own gradient-and-rounded-card social-feed language, and not a generic Material default either. Two things anchor the identity:

1. **The Aperture Ring** — a custom circular progress component built from segmented blades, closing as a download completes, snapping open with a shutter-click on finish. This is the one deliberately custom, ownable visual element.
2. **Material 3 Expressive as the foundation everywhere else** — Google's 2026 design language is explicitly about motion, spatial effects (elevation, morphing, shared-axis transitions), and "emotion-driven UX" rather than static Material 3 defaults. Use this instead of hand-building animation systems from scratch; it's the current, actively-maintained, hardware-accelerated path, and it's what "beautiful and modern" concretely means on Android in 2026.

**How this differs from a naive "make it look nice" build**: static UI is now the tell of an undercooked Compose app — Expressive's whole premise is that things should visibly respond, morph, and transition, not just sit there in the right colors. Build motion in from the component level, not as a final polish pass.

---

## 2. Design Tokens

### 2.1 Color

| Token | Hex | Usage |
|---|---|---|
| `bg.base` | `#000000` | True black — OLED-optimized, pixels fully off |
| `bg.surface` | `#0D0D0D` | Cards, sheets, elevated containers |
| `bg.surfaceHigh` | `#161616` | Pressed/active states, top app bar |
| `border.hairline` | `#262626` | 1dp dividers/card outlines — elevation via borders, not shadows (shadows don't render on true black) |
| `text.primary` | `#F2F2F2` | Default body/label text — a hair off pure white to reduce OLED halation on large text blocks |
| `text.high-emphasis` | `#FFFFFF` | Headlines, active nav item, key numbers |
| `text.secondary` | `#9E9E9E` | Captions, timestamps, metadata |
| `text.disabled` | `#5C5C5C` | Disabled controls |
| `accent.primary` | `#3DE8FF` (Aperture Cyan) | Primary actions, active states, the Aperture Ring, links |
| `accent.onAccent` | `#000000` | Text/icons on accent-filled surfaces |
| `state.success` | `#4ADE80` | Download complete |
| `state.error` | `#FF5C5C` | Failed download, login expired |
| `state.warning` | `#FFC24B` | Story expiring soon, rate-limit warning |

**Why cyan, not Instagram's own pink/purple/orange gradient**: matching IG's palette reads as a knockoff of the thing being downloaded from. A single cold precise cyan on true black reinforces the instrument framing.

### 2.2 Typography — Material 3 Expressive type scale

M3 Expressive uses **bigger, bolder type with more emphasis contrast** than classic Material 3 — this is a deliberate, research-backed change, not a stylistic whim, so don't default back to the smaller classic M3 scale.

| Role | Typeface | Size / Line height | Weight | Use |
|---|---|---|---|---|
| Display | Space Grotesk | 32/38 | Bold | Screen titles — sized up from classic M3's 28/34 per Expressive guidance |
| Title | Space Grotesk | 22/28 | Medium | Sheet headers, section titles |
| Body | Inter | 15/22 | Regular | Standard content |
| Label | Inter | 13/18 | Medium | Buttons, chips, nav labels |
| Caption | Inter | 12/16 | Regular | Timestamps, secondary metadata |
| Mono-data | JetBrains Mono | 13/18 | Medium | Resolution/quality/file-size tags |

Bundle all three as variable fonts in `res/font/` — no runtime Google Fonts fetch, for offline reliability.

### 2.3 Shape — Material 3 Expressive shape scale

M3 Expressive's actual shape scale (use `MaterialTheme.shapes`, don't hardcode radii per-component):

```kotlin
val InstaSaveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),   // buttons — Expressive moved button corners from 12dp to 16dp
    extraLarge = RoundedCornerShape(24.dp)
)
```
Expressive's design principle here is literally called **"Embrace Tension"**: deliberately combine sharp/angular forms with soft/rounded ones rather than making everything uniformly rounded — e.g., the Aperture Ring (angular blade segments) sitting inside otherwise soft `large`/`extraLarge` rounded cards is exactly this principle in practice, not an inconsistency to fix.

### 2.4 Spacing & Touch Targets
- Base unit: 4dp grid (4/8/12/16/24/32/48)
- **Touch targets: 48–56dp**, not just the classic-M3 48dp floor — Expressive's research pushed toward larger targets for primary actions specifically; use 56dp for the primary download/fetch button, 48dp minimum elsewhere.

### 2.5 Iconography
- Lucide-style icon set, thin 1.5–2dp stroke, geometric, no filled glyphs except the active bottom-nav item
- Custom icons: Aperture Ring, Carousel-grid glyph, Story-ring glyph (cyan instead of IG's gradient ring)

---

## 3. Motion & Material 3 Expressive Components (the part that makes this feel current, not static)

This section is new in v2 and is the direct answer to "how should this look and feel" — Expressive is fundamentally about this layer, more than color.

| Situation | Component / Technique | Why |
|---|---|---|
| Short waits (<5s) — resolving a link, fetching thumbnail | **`LoadingIndicator`/`ContainedLoadingIndicator`** (M3 Expressive) | These are the *current recommended replacement* for indeterminate `CircularProgressIndicator` — they morph between shapes rather than just spinning, and are what a 2026 Compose app is expected to use for short waits |
| Active download progress | **Custom Aperture Ring** (bespoke, not a stock component) | This is the one place a fully custom component is worth it — see §4 |
| Quality-picker / filter selection | **M3 Expressive Button Groups** (standard or connected) | Purpose-built for exactly this "select one of several options" pattern — don't hand-roll a `Row` of individually-styled buttons |
| Screen-to-screen transitions | Shared-axis / spatial transitions via Navigation 3 + Compose's `AnimatedContent` | Expressive treats spatial continuity (elevation shifts, morphing) as a first-class pillar, not just a fade |
| Sheet open/close | Slide up + background scrim opacity fade (not color fade — see §6 of the wireframe section) | Standard, but keep the motion snappy (150–250ms `FastOutSlowIn`) — Expressive is bolder, not slower |
| Completion states (download done) | Shape-morph micro-animation (Aperture Ring blades snapping fully open) + haptic tick | Ties the signature element directly into Expressive's "spatial effects" pillar rather than treating it as a decoration bolted onto a static design |

**Rule for the agent building this**: if a screen has zero motion beyond a basic screen transition, that's a sign a component was under-built relative to this spec, not that the design is "clean." Static-by-default is explicitly what Expressive is designed to move away from.

---

## 4. Screen-by-Screen Specification

### 4.1 Home
```
┌─────────────────────────────────────┐
│  InstaSave                      ⚙   │
│                                       │
│   ┌─────────────────────────────┐   │
│   │  Paste an Instagram link      │   │  ← bg.surface, hairline border,
│   │  [________________________]  │   │    cyan glow + shape-morph focus
│   │              [ Paste ] →      │   │    ring on focus (Expressive)
│   └─────────────────────────────┘   │
│                                       │
│   or share directly from Instagram   │
│                                       │
│   ── Recent ──────────────────────   │
│   [thumb] Reel · 1080p · Done        │
│   [thumb] Post · 2 slides · Done     │
│   [thumb] Story · Expired            │
└─────────────────────────────────────┘
```
- Clipboard-detect surfaces an inline "Paste from clipboard?" affordance, not a popup
- Empty state: static Aperture Ring icon (open), copy: *"Paste a link or share one from Instagram to get started."*
- Resolving state: `ContainedLoadingIndicator` inline below the input, not a full-screen blocker

### 4.2 Resolution Picker (Modal Bottom Sheet)
```
┌─────────────────────────────────────┐
│  ▂▂  (drag handle)                   │
│  [thumbnail]  Reel by @username      │
│  "Caption text truncated to two..."  │
│                                       │
│  Choose quality                      │
│  ┌───────┐ ┌───────┐ ┌───────┐      │
│  │ 1080p │ │ 720p  │ │ 480p  │      │  ← M3 Expressive connected
│  │ 24 MB │ │ 12 MB │ │ 6 MB  │      │    button group, not plain chips
│  └───────┘ └───────┘ └───────┘      │
│  ○ Audio only (.m4a)                 │
│                                       │
│  [        Download        ]         │  ← 56dp height (Expressive primary
│                                       │    action sizing), large shape
└─────────────────────────────────────┘
```

### 4.3 Carousel Grid
```
┌─────────────────────────────────────┐
│  Carousel · 6 items      [Select all]│
│  ┌────┐ ┌────┐ ┌────┐               │
│  │ ✓1 │ │  2 │ │ ✓3 │  3-col grid,  │
│  └────┘ └────┘ └────┘  cyan check   │
│  ┌────┐ ┌────┐ ┌────┐  overlay with │
│  │  4 │ │ ✓5 │ │  6 │  shape-morph  │
│  └────┘ └────┘ └────┘  on select    │
│  [   Download 3 selected   ]        │
└─────────────────────────────────────┘
```

### 4.4 Download Queue
```
┌─────────────────────────────────────┐
│  Queue                               │
│  ┌───┐  reel_DApNLu.mp4              │
│  │◐  │  1080p · 14.2 / 24.6 MB       │  ← Aperture Ring closing
│  └───┘  ⏸ pause    ✕ cancel          │    proportionally, aria2c-backed
│                                       │    multi-connection speed shown
│  ┌───┐  post_carousel_2of6.jpg       │
│  │○  │  Queued                       │
│  └───┘                               │
│  ┌───┐  story_expired.mp4            │
│  │!  │  Failed — story expired       │
│  └───┘  ↻ retry                      │
└─────────────────────────────────────┘
```
- Persistent system notification mirrors the top item, including a simplified Aperture Ring rendering where the platform allows

### 4.5 History
```
┌─────────────────────────────────────┐
│  History              [🔍 search]    │
│  ┌────┐┌────┐┌────┐                 │
│  │img ││img ││img │  3-col thumbnail│
│  └────┘└────┘└────┘  grid, long-    │
│  ┌────┐┌────┐┌────┐  press for      │
│  │img ││img ││img │  quick actions  │
│  └────┘└────┘└────┘  (share/delete) │
└─────────────────────────────────────┘
```

### 4.6 Login (WebView modal) — POST-MVP, not built in v1
Kept here for when Phase 7 (ARCHITECTURE.md §4) is actually built:
```
┌─────────────────────────────────────┐
│  ← Cancel        Log in to Instagram │
│  ─────────────────────────────────  │
│         [ WebView: instagram.com ]   │
│  ─────────────────────────────────  │
│  🔒 Your credentials go directly to  │
│     Instagram. InstaSave only stores │
│     the resulting session, encrypted.│
└─────────────────────────────────────┘
```

### 4.7 Settings
```
┌─────────────────────────────────────┐
│  Settings                            │
│  Downloads                           │
│  Save location    Movies/InstaSave → │  ← tap opens SAF folder picker
│  Default quality               1080p │    (see ARCHITECTURE.md §5)
│  Wi-Fi only                      ○●  │
│  Max concurrent downloads            2│
│  Advanced                             │
│  Custom yt-dlp templates           → │  ← power-user escape hatch,
│                                       │    adopted from Seal, optional
│  About                               │
│  Version 1.0.0            Check for  │
│                            updates → │
└─────────────────────────────────────┘
```

### 4.8 Error / Empty States — exact copy
| Situation | Copy |
|---|---|
| Login-gated content | *"This content needs a logged-in session. Not supported in this version yet."* |
| Story expired | *"This story is no longer available — stories expire after 24 hours."* |
| Extractor broken (all fallbacks failed) | *"Instagram changed something on their end. Try updating InstaSave, or check back shortly."* → [Check for updates] |
| Rate-limited | *"Too many requests right now. Retrying automatically in 30s."* (auto-countdown) |
| Empty history | *"Nothing downloaded yet. Paste a link above to get started."* |
| No network | *"You're offline. Queued downloads will resume automatically."* |

---

## 5. Component Library

| Component | Spec |
|---|---|
| **Primary button** | `accent.primary` fill, black text, `shapes.large` (16dp), 56dp height for primary actions |
| **Secondary button** | Transparent fill, hairline outline, `text.primary` label |
| **Quality selector** | M3 Expressive **Button Group** (connected variant), not plain chips |
| **Text field** | `bg.surface`, hairline border, cyan glow + shape-morph on focus |
| **Bottom sheet** | `bg.surface`, `shapes.extraLarge` top corners (24dp), drag handle `#3A3A3A` |
| **Card** | `bg.surface`, 1dp hairline border, `shapes.medium` (12dp), no shadow |
| **Aperture Ring** | Custom `Canvas` composable, 6–8 segmented blades, `Animatable`-driven by progress `Flow<Float>`, shutter-click shape-morph on completion |
| **Loading indicator (short waits)** | M3 Expressive `LoadingIndicator`/`ContainedLoadingIndicator` — do not use plain `CircularProgressIndicator` |
| **Switch** | Track: hairline (off) / cyan (on); thumb: white |
| **Bottom nav bar** | `bg.base`, 1dp top hairline; active icon+label in cyan/high-emphasis white; inactive in secondary gray |

---

## 6. Accessibility

- Contrast: `text.primary` on `bg.base` = 19.6:1 (WCAG AAA); `accent.primary` on black = 13.7:1
- Never rely on color alone: error/success/warning pair color with icon + text
- Touch targets ≥48dp (56dp for primary actions, per §2.4)
- Dynamic text scaling to 200% without truncation — test History grid and Resolution Picker specifically
- `TalkBack` labels: Aperture Ring announces "Downloading, 58 percent"; quality selector announces resolution + file size; carousel checkboxes announce selected state
- Respect system "reduce motion" — Aperture Ring and Expressive transitions degrade to simple determinate indicators when enabled

---

## 7. Compose Implementation Notes

```kotlin
// theme/Color.kt
val BgBase = Color(0xFF000000)
val BgSurface = Color(0xFF0D0D0D)
val AccentPrimary = Color(0xFF3DE8FF)
// ... (full token set per §2.1)

// theme/Theme.kt
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstaSaveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BgBase,
            surface = BgSurface,
            primary = AccentPrimary,
            onPrimary = Color.Black,
            // ...
        ),
        shapes = InstaSaveShapes,   // from ARCHITECTURE.md-adjacent shape scale, §2.3
        typography = InstaSaveTypography,   // Space Grotesk / Inter / JetBrains Mono, §2.2
        content = content
    )
}
// Force dark theme always — hardcode darkColorScheme, no isSystemInDarkTheme() branching.
// The black identity is fixed, not a toggle.
```

**AI agent build notes for this file specifically:**
- Use the actual `androidx.compose.material3` Expressive APIs (`LoadingIndicator`, Button Groups, the Expressive shape/type scale) — do not hand-build imitation versions of these components. They exist in the current Compose Material 3 library; import and use them directly.
- The Aperture Ring is the **one** component that should be hand-built via `Canvas`/`drawArc` — everything else should use stock (Expressive) Material 3 components styled with InstaSave's tokens, not custom-built from scratch.
- If a stock Expressive component doesn't visually match the mockup exactly, prefer restyling the stock component's parameters over abandoning it for a fully custom one — this keeps behavior (accessibility, animation, state handling) that a hand-rolled component would have to reimplement from zero.
- Do not implement a light theme. `darkColorScheme` is hardcoded and final.

---

## 8. What Makes This Not-Generic (self-critique, retained from v1)

- Rejected: Instagram's own pink/purple gradient palette — reads as a clone
- Rejected: uniform rounded corners everywhere — Expressive's "Embrace Tension" principle specifically calls for contrast between angular (Aperture Ring) and soft (cards/sheets) forms
- Rejected: static screens with only a fade transition — Expressive's whole premise is that this reads as under-built in 2026, not "clean minimalism"
- Kept deliberately restrained: one accent color, one fully-custom component (Aperture Ring) — everything else leans on Expressive's own proven component set rather than competing with it
