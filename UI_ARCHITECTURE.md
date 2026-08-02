# UI_ARCHITECTURE.md — InstaSave Compose Implementation Structure

**Purpose:** UI_UX_DESIGN.md specifies what things look like and feel like. ARCHITECTURE.md specifies the app's overall layering. Neither specifies the actual **code shape** of the UI layer — file structure, state containers, navigation graph, component composition tree. This file is that missing piece. Without it, an AI agent building screen-by-screen across multiple sessions tends to invent a slightly different pattern each time (one screen with a `ViewModel`, another with logic inline in the Composable, a third with its own ad-hoc state shape) — this file exists so that doesn't happen.

**For the AI agent:** every screen in this app follows the exact pattern in §2, with no exceptions. If a screen seems to need a different pattern, that's a signal to re-read this file, not to improvise.

---

## 1. Governing Pattern: MVVM + Unidirectional Data Flow (UDF)

```
User action (Composable)
      │  calls
      ▼
ViewModel.onEvent(UiEvent)
      │  invokes
      ▼
UseCase (domain layer, per ARCHITECTURE.md §7)
      │  returns Flow/Result
      ▼
ViewModel updates StateFlow<UiState>
      │  observed via collectAsStateWithLifecycle()
      ▼
Composable re-renders
```

**Non-negotiable rules:**
- Composables **never** call a Repository or UseCase directly. They call `viewModel.onEvent(...)` and observe `viewModel.uiState`. No exceptions, including "simple" screens.
- Composables **never** contain business logic — no network calls, no database access, no retry logic inside a `@Composable` function body. If a Composable needs to *decide* something (not just *display* something), that decision belongs in the ViewModel.
- Each screen has exactly one `UiState` data class and one `UiEvent` sealed interface. Don't scatter state across multiple `mutableStateOf` calls inside the ViewModel — one state holder per screen, always.
- State is hoisted to the top of each screen's Composable tree and passed down as parameters — child composables take plain data + lambdas, never a ViewModel reference.

---

## 2. The Standard Screen Pattern (apply this exactly, every screen)

```kotlin
// 1. UiState — one immutable data class per screen
data class HomeUiState(
    val urlInput: String = "",
    val isResolving: Boolean = false,
    val recentDownloads: List<DownloadSummary> = emptyList(),
    val clipboardSuggestion: String? = null,
    val error: UiError? = null   // see §5 for UiError shape
)

// 2. UiEvent — one sealed interface per screen, one entry per user action
sealed interface HomeUiEvent {
    data class UrlChanged(val url: String) : HomeUiEvent
    data object PasteFromClipboard : HomeUiEvent
    data object FetchClicked : HomeUiEvent
    data object DismissError : HomeUiEvent
}

// 3. ViewModel — owns UiState, handles all UiEvents, calls UseCases
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val resolveLinkUseCase: ResolveLinkUseCase,
    private val downloadHistoryRepository: DownloadHistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.UrlChanged -> _uiState.update { it.copy(urlInput = event.url) }
            is HomeUiEvent.PasteFromClipboard -> { /* ... */ }
            is HomeUiEvent.FetchClicked -> resolveCurrentUrl()
            is HomeUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun resolveCurrentUrl() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResolving = true) }
            resolveLinkUseCase(_uiState.value.urlInput)
                .onSuccess { mediaInfo -> /* navigate to ResolutionPicker with result */ }
                .onFailure { error -> _uiState.update { it.copy(error = error.toUiError(), isResolving = false) } }
        }
    }
}

// 4. Screen Composable — stateful "container," wires ViewModel to stateless content
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToResolutionPicker: (MediaInfo) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

// 5. Content Composable — stateless, takes only data + a single event lambda, fully previewable
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit
) {
    // Layout per UI_UX_DESIGN.md §4.1 — paste field, recent list, etc.
    // Every interactive element calls onEvent(...), nothing else.
}

// 6. Preview — always accompanies the Content composable, using fake state, no ViewModel
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun HomeContentPreview() {
    InstaSaveTheme {
        HomeContent(uiState = HomeUiState(recentDownloads = fakeRecentDownloads()), onEvent = {})
    }
}
```

**Why the Screen/Content split matters:** `HomeContent` never touches Hilt, ViewModels, or navigation — it's a pure function of `UiState` and an event callback. This makes every screen trivially previewable with fake data (required — see §6) and testable without a running ViewModel or backend.

---

## 3. File Structure (maps directly to ARCHITECTURE.md §7's `ui/` module, expanded)

```
ui/
 ├─ home/
 │   ├─ HomeScreen.kt          (stateful container)
 │   ├─ HomeContent.kt         (stateless, previewable)
 │   ├─ HomeViewModel.kt
 │   ├─ HomeUiState.kt
 │   └─ HomeUiEvent.kt
 ├─ resolver/
 │   ├─ ResolutionPickerSheet.kt
 │   ├─ ResolutionPickerContent.kt
 │   ├─ ResolutionPickerViewModel.kt
 │   ├─ ResolutionPickerUiState.kt
 │   ├─ ResolutionPickerUiEvent.kt
 │   └─ CarouselGridContent.kt         (variant content per UI_UX_DESIGN.md §4.3)
 ├─ queue/
 │   ├─ DownloadQueueScreen.kt
 │   ├─ DownloadQueueContent.kt
 │   ├─ DownloadQueueViewModel.kt
 │   ├─ DownloadQueueUiState.kt
 │   └─ DownloadQueueUiEvent.kt
 ├─ history/
 │   ├─ HistoryScreen.kt
 │   ├─ HistoryContent.kt
 │   ├─ HistoryViewModel.kt
 │   ├─ HistoryUiState.kt
 │   └─ HistoryUiEvent.kt
 ├─ settings/
 │   ├─ SettingsScreen.kt
 │   ├─ SettingsContent.kt
 │   ├─ SettingsViewModel.kt
 │   ├─ SettingsUiState.kt
 │   └─ SettingsUiEvent.kt
 ├─ login/                              (POST-MVP — do not create in v1, see ARCHITECTURE.md §4)
 ├─ components/                         (shared across screens — see §4)
 │   ├─ ApertureRing.kt
 │   ├─ QualitySelector.kt              (wraps M3 Expressive Button Group)
 │   ├─ DownloadHistoryCard.kt
 │   ├─ ErrorState.kt
 │   ├─ EmptyState.kt
 │   └─ InstaSaveTopBar.kt
 ├─ navigation/
 │   ├─ InstaSaveNavGraph.kt
 │   └─ Destinations.kt
 └─ theme/
     ├─ Color.kt
     ├─ Type.kt
     ├─ Shape.kt
     ├─ Motion.kt                       (shared animation spec constants — durations, easings)
     └─ Theme.kt
```

**Rule:** a component goes in `components/` only if it's used by two or more screens. A component used by exactly one screen stays local to that screen's folder (e.g., `CarouselGridContent.kt` stays in `resolver/` even though it's substantial, because it's only used there).

---

## 4. Shared Component Tree

```
InstaSaveTheme (theme/Theme.kt)
 └─ InstaSaveNavGraph (navigation/InstaSaveNavGraph.kt)
     ├─ HomeScreen
     │   └─ HomeContent
     │       ├─ InstaSaveTopBar
     │       ├─ UrlInputField                    (screen-local, home/)
     │       └─ DownloadHistoryCard × N           (shared)
     ├─ ResolutionPickerSheet (modal, not a nav destination — see §5)
     │   └─ ResolutionPickerContent
     │       └─ QualitySelector                   (shared, wraps Expressive Button Group)
     │   — or, if carousel —
     │   └─ CarouselGridContent (screen-local)
     ├─ DownloadQueueScreen
     │   └─ DownloadQueueContent
     │       ├─ ApertureRing × N                   (shared — the one hand-built component)
     │       └─ ErrorState / EmptyState            (shared)
     ├─ HistoryScreen
     │   └─ HistoryContent
     │       └─ DownloadHistoryCard × N            (shared, same component as Home's recent list)
     └─ SettingsScreen
         └─ SettingsContent (screen-local subsections, no shared components needed)
```

`DownloadHistoryCard` is deliberately the same component on both Home ("Recent") and History (full list) — one card component, two usage sites, not two near-duplicate implementations.

---

## 5. Navigation Graph (Jetpack Navigation 3, type-safe destinations)

```kotlin
// navigation/Destinations.kt
@Serializable
sealed interface Destination {
    @Serializable data object Home : Destination
    @Serializable data object Queue : Destination
    @Serializable data object History : Destination
    @Serializable data object Settings : Destination
    // ResolutionPicker and Login are NOT destinations — see note below
}

// navigation/InstaSaveNavGraph.kt
@Composable
fun InstaSaveNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Destination.Home) {
        composable<Destination.Home> {
            HomeScreen(onNavigateToResolutionPicker = { mediaInfo ->
                // triggers a modal, not a navigation destination
            })
        }
        composable<Destination.Queue> { DownloadQueueScreen() }
        composable<Destination.History> { HistoryScreen() }
        composable<Destination.Settings> { SettingsScreen() }
    }
}
```

**Important distinction — modal flows vs. nav destinations:** `ResolutionPickerSheet` (and, post-MVP, `LoginWebViewScreen`) are **modal overlays**, not entries in the nav graph. They're triggered from within a screen (e.g., a `ModalBottomSheet` shown conditionally based on state) and return to wherever they were invoked from — they do not get their own `composable<>` route. Treat this as fixed: don't add them to `Destinations.kt`.

Bottom navigation bar wraps `Home`/`Queue`/`History`/`Settings` per UI_UX_DESIGN.md §3 — implement via `NavigationBar` keyed off the current back stack entry, standard pattern, no customization needed beyond InstaSave's token styling.

---

## 6. Preview Requirement (non-negotiable, ties to AGENT.md's Definition of Done)

Every `*Content.kt` file must have at least one `@Preview` using fake/sample data — never a live ViewModel or real network call in a preview. This exists specifically so a screen's actual visual output can be checked against UI_UX_DESIGN.md's wireframes without running the full app — use this as the first verification step in SDLC_BUILD_PLAN.md's Stage 4 (Test), before a real device screenshot.

```kotlin
// Fake data lives in a shared `PreviewData.kt` per feature folder, not inline per preview
// resolver/PreviewData.kt
fun fakeMediaInfo() = MediaInfo(
    id = "AbCdEfGhIj", type = "reel", author = "sample_user",
    formats = listOf(/* ... */)
)
```

---

## 7. Error/Result Shape Shared Across All ViewModels

```kotlin
// core/model/UiError.kt — one shared shape, used by every ViewModel, mapped from ErrorResponse.code (API_SPEC.yaml)
data class UiError(
    val code: String,        // matches ErrorResponse.code from API_SPEC.yaml
    val message: String      // pre-formatted, matches the exact copy in UI_UX_DESIGN.md §4.8 — never a raw exception message
)

fun Throwable.toUiError(): UiError = when (this) {
    is LoginRequiredException -> UiError("LOGIN_REQUIRED", "This content needs a logged-in session. Not supported in this version yet.")
    is RateLimitedException -> UiError("RATE_LIMITED", "Too many requests right now. Retrying automatically in 30s.")
    // ... one branch per ErrorResponse.code in API_SPEC.yaml — keep these two files in sync
    else -> UiError("EXTRACTION_FAILED", "Instagram changed something on their end. Try updating InstaSave, or check back shortly.")
}
```

**Rule:** a raw exception message or stack trace must never reach a Composable. Every error path goes through `toUiError()` first. This is what AGENT.md's "no raw stack traces visible to user anywhere" requirement actually looks like in code.

---

## 8. What NOT To Do (common Compose anti-patterns to explicitly avoid here)

- Don't put `remember { mutableStateOf(...) }` for anything that represents app/business state inside a Composable — that's ViewModel state, always. `remember` is only for pure UI-local state (e.g., "is this dropdown expanded right now") that has zero meaning outside this Composable's lifetime.
- Don't pass a `NavController` deep into child composables — navigation callbacks (`onNavigateToX: () -> Unit`) are passed down instead, keeping `Content` composables navigation-agnostic and previewable.
- Don't create a second `ViewModel` per screen for "just this one extra thing" — one ViewModel per screen, full stop; if a screen feels like it needs two, that's a sign the screen itself should be split.
- Don't fetch data in `LaunchedEffect(Unit) { viewModel.someRepositoryCall() }` directly from a Composable bypassing `onEvent` — initial data loading still goes through a `UiEvent` (e.g., `HomeUiEvent.ScreenEntered`) dispatched from `LaunchedEffect`, keeping the same event-driven pattern consistent even for initial loads.
