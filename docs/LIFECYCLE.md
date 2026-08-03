# LIFECYCLE & MAINTENANCE PLAN — InstaFlow

- **Project**: InstaFlow (Specialization of JunkFood02/Seal)
- **Document Version**: `1.0.0`
- **Status**: 🟢 ACTIVE GOVERNANCE DOCUMENT

---

## 1. Release Cadence & Versioning Strategy

InstaFlow adheres to **Semantic Versioning 2.0.0 (`MAJOR.MINOR.PATCH`)**:

| Release Type | Version Pattern | Scope & Trigger | Example |
|:---|:---|:---|:---|
| **Patch Release** | `1.0.x` | Critical bug fixes, Instagram extractor hotfixes, upstream security patches. **No API or structural UI changes.** | `1.0.1` |
| **Minor Release** | `1.x.0` | New Instagram specialization features (e.g. metadata viewer, enhanced cookie manager, queue filters). | `1.1.0` |
| **Major Release** | `x.0.0` | Architectural shifts, database schema migrations, major UI redesigns. | `2.0.0` |

---

## 2. Upstream Sync & Merge Policy (`JunkFood02/Seal`)

As an engineering fork of `JunkFood02/Seal`, InstaFlow maintains upstream compatibility through a structured merge policy tracked in [`docs/UPSTREAM_RELEASE_TRACKER.md`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/docs/UPSTREAM_RELEASE_TRACKER.md):

1. **Upstream Release Audit**: Upon each Seal upstream release tag:
   - Audit upstream security patches and `youtubedl-android` dependency updates.
   - Verify isolated Instagram components (`com.junkfood.seal.util.Instagram*`) remain decoupled from modified core paths.
2. **Merge Protocol**:
   - Fetch `upstream/main` into a temporary `sync/upstream-vX.Y.Z` branch.
   - Resolve conflicts in core engine files (`DownloaderV2`, `DownloadUtil`, `HomePageViewModel`).
   - Run unit test suite (`:app:testGenericDebugUnitTest`).
   - Merge into `main` after verification.

---

## 3. Instagram Breakage & Hotfix Policy

Instagram frequently alters internal API payloads, DOM structures, and CDN URL formatting. When an Instagram extraction breakage occurs:

```
Instagram Payload Change
          │
          ▼
Failing Extractor / Bug Reported
          │
          ▼
Create Hotfix Branch: `hotfix/ig-extractor-YYYYMMDD`
          │
          ▼
Update JVM-safe Regex / Resolver Payload Rules in `InstagramCarouselDetector` or `InstagramMediaResolver`
          │
          ▼
Run Unit Test Suite & Verify Payload Samples
          │
          ▼
Merge to `main` & Issue Emergency Patch Release (`v1.0.x`)
```

---

## 4. Platform Support Policy

### Supported Platforms
- **OS**: Android 8.0 (API Level 26) through Android 15 (API Level 35)
- **ABIs**: `arm64-v8a`, `armeabi-v7a`, `x86_64`
- **Architectures**: Universal APK and Split ABI APKs

### Explicitly Unsupported
- Android 7.1 and lower (API < 26)
- Root-only framework hacks or non-standard Android ROM modifications

---

## 5. Technical Debt Register

| Debt ID | Subsystem | Description | Target Version |
|:---|:---|:---|:---|
| **TD-01** | Cookie Manager | Add UI validation for imported Netscape `cookies.txt` format before writing to storage | `v1.1.0` |
| **TD-02** | Carousel Progress | Expose per-item sub-progress bar in `VideoCardV2` for multi-video carousels | `v1.1.0` |
| **TD-03** | Queue Re-ordering | Support drag-and-drop reordering of enqueued download tasks | `v1.2.0` |

---

## 6. Permanent AI Operating Principles

When contributing to InstaFlow, all AI agents and human developers MUST enforce the **Optimization Governance Rule**:

> **NEVER OPTIMIZE SIMPLY BECAUSE A BETTER SOLUTION EXISTS.**  
> Optimize ONLY when:
> 1. It improves **correctness**.
> 2. It improves **maintainability**.
> 3. It improves **performance measurably**.
> 4. It preserves **upstream compatibility**.
> 5. It does not violate the active Work Package boundaries.

---

## 7. Post-Release Product Roadmap

### **Version 1.1.0 Roadmap**
- Enhanced metadata viewer & media info inspector
- Improved cookie import/export & validation manager
- Queue search & download filter options

### **Version 1.2.0 Roadmap**
- InstaFlow Collections & Favorites tagger
- Quick share target integrations for social apps
- Custom download location rules by media type

### **Version 2.0.0 Roadmap**
- Multi-platform provider plugin abstraction
- Next-gen Material You Compose interface overhaul
