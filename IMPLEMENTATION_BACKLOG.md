# IMPLEMENTATION BACKLOG — Work Package Queue & Release Gates

## Release Gates (Phase Transition Rules)

- 🟢 **Gate A (Baseline Certified)**: WP 1.1–1.5 complete, baseline tagged (`baseline-instaflow-start`). **Gate A Closed & Phase 2 Unlocked**.
- 🔒 **Gate B (Single Media Verified)**: WP 2.1–2.9 complete (Images, Videos, Reels, Stories, Profiles). **Unlocks Phase 3**.
- 🔒 **Gate C (Carousel Verified)**: WP 3.1–3.10 complete (Swipe, Multi-select, Batch download). **Unlocks Phase 4**.
- 🔒 **Gate D (UI Complete)**: WP 4.1–4.9 complete (Instagram Action Sheet & Previewer). **Unlocks Phase 5**.
- 🔒 **Gate E (Cleanup Complete)**: WP 5.1–5.6 complete (Remove YouTube/SponsorBlock code). **Unlocks Phase 6**.
- 🔒 **Gate F (Production Certified)**: Rebranding, namespace migration, release APK signed & verified.

---

## Phase 1: Baseline Certification & Upstream Baseline Lock (Gate A - CLOSED)
- [x] **WP 1.1**: Upstream Baseline Build, Test Suite Certification (`docs/evidence/WP_1_1/`)
- [x] **WP 1.2**: Static Analysis & Lint Baseline Recording (`docs/evidence/WP_1_2/`)
- [x] **WP 1.3**: Runtime Verification & Functional Mapping (`docs/evidence/WP_1_3/`)
- [x] **WP 1.4**: Performance & Binary Footprint Baseline (`docs/evidence/WP_1_4/`)
- [x] **WP 1.5**: Upstream Baseline Lock & Tag (`baseline-instaflow-start`) (`docs/evidence/WP_1_5/`)

---

## Phase 2: Instagram Single Media Spike (Gate B - UNLOCKED)
- [ ] **WP 2.1**: Instagram URL Validator
- [ ] **WP 2.2**: Media Resolver
- [ ] **WP 2.3**: Media Model Data Classes
- [ ] **WP 2.4**: Single Image Posts
- [ ] **WP 2.5**: Single Video Posts
- [ ] **WP 2.6**: Reels Support
- [ ] **WP 2.7**: Stories Support
- [ ] **WP 2.8**: Highlights Support
- [ ] **WP 2.9**: Profile Pictures Support

---

## Phase 3: Instagram Carousel Specialization (Gate C)
- [ ] **WP 3.1**: Detect Carousel Posts
- [ ] **WP 3.2**: Media Preview Swipe Composable
- [ ] **WP 3.3**: Individual Item Selection
- [ ] **WP 3.4**: Multi-Item Selection
- [ ] **WP 3.5**: Select All Action
- [ ] **WP 3.6**: Download Selected Items
- [ ] **WP 3.7**: Download All Items
- [ ] **WP 3.8**: Maintain Original Instagram Media Order
- [ ] **WP 3.9**: Independent Progress Tracking Per Item
- [ ] **WP 3.10**: Independent Retry Per Item

---

## Phase 4: Instagram UX Specialization (Gate D)
- [ ] **WP 4.1**: Home Screen URL & Quick Actions
- [ ] **WP 4.2**: Instagram Media Previewer Modal
- [ ] **WP 4.3**: Media Quality Action Sheet
- [ ] **WP 4.4**: Task Queue Card UI
- [ ] **WP 4.5**: Download History Screen with Media Badges
- [ ] **WP 4.6**: Instagram Cookie WebView Sync Screen
- [ ] **WP 4.7**: Metadata Viewer Modal
- [ ] **WP 4.8**: About Screen Attribution
- [ ] **WP 4.9**: Motion & Micro-Animations

---

## Phase 5: Code Cleanup & Feature Stripping (Gate E)
- [ ] **WP 5.1**: Remove SponsorBlock Engine & Preference Items
- [ ] **WP 5.2**: Remove Subtitle Selection UI & VTT Processing
- [ ] **WP 5.3**: Remove Playlist Index Picker
- [ ] **WP 5.4**: Remove Generic Multi-Site Selection Preference Items
- [ ] **WP 5.5**: Remove Unused Legacy Strings & Icons
- [ ] **WP 5.6**: Remove Unused Resources

---

## Phase 6: Identity, Rebranding & Release Packaging (Gate F)
- [ ] **WP 6.1**: Refactor Namespace (`com.junkfood.seal` → `com.instaflow.app`)
- [ ] **WP 6.2**: Update Application ID & Build Namespace
- [ ] **WP 6.3**: Update App Icons & Vector Drawables
- [ ] **WP 6.4**: Update Splash Screen Branding
- [ ] **WP 6.5**: Update Application Label & Strings (`app_name`)
- [ ] **WP 6.6**: Launcher & Deep Link Integration
- [ ] **WP 6.7**: GitHub Repository Release Assets Setup
- [ ] **WP 6.8**: Play Store / F-Droid Assets Preparation
- [ ] **WP 6.9**: Performance & Accessibility Audit Pass
- [ ] **WP 6.10**: Final Production Release APK & AAB Packaging (`./gradlew assembleRelease`)
