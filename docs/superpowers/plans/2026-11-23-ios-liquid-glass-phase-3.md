# iOS Liquid Glass (Phase 3) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A native SwiftUI iOS app over the shared ViewModels, drawn from the Claude Design canvas `design/native-ui/iOS-*.dc.html`, with the system's Liquid Glass.

**Architecture:** Kotlin `iosMain` in `composeApp` holds the bridge the spike proved (a ViewModel owner per screen with a `SavedStateRegistry`, a paging presenter); Swift observes each `StateFlow` with SKIE in `.task` and keeps its own navigation. Screens are built one per task, in the spec's order, each checked on the simulator in light and dark.

**Tech Stack:** Kotlin 2.4.10 / SKIE 0.10.16 / Koin / androidx.lifecycle + savedstate (iOS), app.cash.paging 0.5.1 (androidx.paging 3.3.0-alpha02 on iOS), SwiftUI on iOS 26 (Xcode 27.2), MapKit, PhotosUI.

**Spec:** `docs/superpowers/specs/2026-11-21-native-ui-split-design.md` (Phase 3, Testing, and the "Decision note (bridge spike)" section). Spike reference code: local branch `spike/swift-bridge` (`IosKoin.kt`, `IosPaging.kt`, `IosSavedStateProbe.kt`, `iosApp/iosApp/Spike/*`); read it, do not merge it.

## Global Constraints

- iOS 26.0 deployment target; build against the iOS 26 SDK so standard bars, tab bar, search and sheets get Liquid Glass for free.
- Custom `.glassEffect` only for controls floating over content (map controls, the place page's actions), grouped in a `GlassEffectContainer`.
- Structure: `iosApp/iosApp/App/`, `Core/{DI,Bridge,Theme,Components}`, `Features/<Feature>/` — synchronized folders, no per-file `pbxproj` edits.
- Navigation: `TabView`, a `NavigationStack` per tab, `NavigationSplitView` on regular width. No routes shared with Android.
- Observe state with `.task { for await s in vm.uiState { state = s } }`; switch on sealed types with SKIE's `onEnum(of:)`, never `as?`.
- Text fields keep their text in `@State` and push it with `.onChange`; never bind a field to a value read back from a `StateFlow`.
- One `IosViewModelOwner` per screen in a `@StateObject`, cleared in `deinit`. Nothing that captures the view (Binding, closure, stored Task) may outlive it.
- Swift cannot use Kotlin default arguments or method generics: typed accessors on the owner, Kotlin factories for previews.
- Kotlin called from Swift catches its own exceptions or is `@Throws`.
- No shared ViewModel changes unless a task names them; `scripts/check-common-compose.sh` keeps passing.
- Look comes from the canvas (accent `#007A85`, New York serif display titles, SF text); every screen is compared with its artboard in light and dark.
- Simulator: a dedicated device named `AL iOS` (iPhone 17 Pro, the iOS 27 runtime installed here; create it if missing); never drive a simulator another session is using.
- Branch `feat/ios-liquid-glass` off `develop`; one commit per task.

## Review Focus

1. A session that expires while browsing (401 from any screen): the app should return to Login, not show endless errors. Test in Task 3.
2. Popping a screen must run its ViewModels' `onCleared()`; a leaked Binding silently stopped it in the spike. Test in Task 1 (Kotlin) and checked per screen by the owner's log in debug.
3. Fast typing in search and form fields must not drop characters. Rule above; checked by an XCUITest in Task 3 (login fields).
4. Offline or server down at launch: Login's `CheckingSession` must end in the form with an error, not a spinner forever. Test in Task 3.
5. Large Dynamic Type sizes: titles and rows wrap, nothing clips. Checked per screen at the largest accessibility size.

---

### Task 1: Bridge in `iosMain`

**Files:**
- Create: `composeApp/src/iosMain/kotlin/com/desarrollodroide/adventurelog/IosViewModelOwner.kt`, `.../PagingListPresenter.kt`
- Modify: `composeApp/build.gradle.kts` (iosMain: `paging-common`, lifecycle-viewmodel-savedstate)
- Test: `composeApp/src/iosTest/kotlin/com/desarrollodroide/adventurelog/IosViewModelOwnerTest.kt`

**Interfaces:**
- Produces: `class IosViewModelOwner : ViewModelStoreOwner, SavedStateRegistryOwner` with `fun clear()` and one typed accessor per ViewModel, named `<lowerCamelName>()` (`loginViewModel()`, `homeViewModel()`, `locationsViewModel()`, …, every class under `feature/*/src/commonMain/**/viewmodel/`), resolved through Koin's `resolveViewModel` with `SAVED_STATE_REGISTRY_OWNER_KEY` and `VIEW_MODEL_STORE_OWNER_KEY` extras (the spike's `SavedStateViewModelOwner`).
- Produces: `class PagingListPresenter<T : Any>(flow: Flow<PagingData<T>>, scope: CoroutineScope)` with `items: StateFlow<List<T>>`, `status: StateFlow<PagingStatus>`, `fun itemAccessed(index: Int)`, `fun retry()`, `fun refresh()`; `data class PagingStatus(isRefreshing, isLoadingMore, endReached, error: String?)`. The owner exposes `locationsPresenter()` and `collectionsPresenter()`, one each per owner.

- [ ] **Step 1: Write the failing tests** — `clearRunsOnCleared()` (a test ViewModel registered in a Koin test module flips a flag in `onCleared`; `owner.clear()` asserts it true) and `ownerResolvesViewModelsThatTakeASavedStateHandle()` (resolving `LoginViewModel` does not throw).
- [ ] **Step 2: Run `./gradlew :composeApp:iosSimulatorArm64Test`** — expect FAIL (unresolved `IosViewModelOwner`).
- [ ] **Step 3: Implement both files** from the spike, without its `println`s, `spikeSetPageSize` or `SpikePagingConfig`.
- [ ] **Step 4: Run the tests** — PASS; `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` links.
- [ ] **Step 5: Commit** `feat(ios): view-model owner and paging presenter for SwiftUI`.

### Task 2: App skeleton, theme and CI

**Files:**
- Modify: `iosApp/iosApp/iOSApp.swift`; delete `ContentView.swift`
- Create: `iosApp/iosApp/Core/Bridge/ViewModelHost.swift`, `Core/Theme/Theme.swift`, `Core/Components/` (as screens need them), `App/RootView.swift`
- Modify: `.github/workflows/ci.yml` (an `xcodebuild … build` step on `macos-latest`, after the framework link)

**Interfaces:**
- Produces: `final class ViewModelHost: ObservableObject { let owner: IosViewModelOwner }` (clears in `deinit`); `enum Theme` with `accent = Color(hex 0x007A85)`, `displayFont(size:) -> Font` (New York serif, `.serif` design), and the canvas's corner radii as constants.
- Produces: `RootView` — shows Login until a session exists, then the tab shell (Task 4).

- [ ] **Step 1:** Convert the app target's folder to a synchronized root group with `App/`, `Core/`, `Features/`.
- [ ] **Step 2:** Implement `ViewModelHost`, `Theme`, `RootView` (Login placeholder for now).
- [ ] **Step 3: Build** `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=AL iOS' build` — BUILD SUCCEEDED.
- [ ] **Step 4:** Add the CI step; check the hosted runner's Xcode has the iOS 26 SDK (spec: "checked then"); if not, pin `macos-26` or record why the step is skipped.
- [ ] **Step 5: Commit** `feat(ios): app skeleton, theme and CI build`.

### Task 3: Login and the session gate

**Files:** `Features/Login/LoginView.swift`, `App/RootView.swift`, `iosAppUITests/LoginUITests.swift` (new UI-test target)

**Interfaces:** Consumes `IosViewModelOwner.loginViewModel()`; `LoginViewModel.uiState: StateFlow<LoginUiState>` (`CheckingSession`, `Empty`, `Loading`, `Error(message)`, `Success(userDetails)`), `loginFormState`, `updateServerUrl/UserName/Password/RememberSession`, `login()`. Logged-out detection: `UserRepository.getUserSession()` emitting null — add `IosViewModelOwner.sessionFlow()` returning that flow.

- [ ] **Step 1: Write the UI tests** — `testSignInDisabledUntilAllFieldsFilled`, `testTypingFastKeepsEveryCharacter` (types `https://example.test` at full speed, asserts the field's value), `testUnreachableServerEndsInTheFormWithAnError` (launch argument pointing at `http://127.0.0.1:9`, asserts the error text and no spinner after 15 s).
- [ ] **Step 2: Run** `xcodebuild test -scheme iosApp -destination 'platform=iOS Simulator,name=AL iOS' -only-testing:iosAppUITests` — FAIL.
- [ ] **Step 3: Implement `LoginView`** from `iOS-Login` (title in serif, server field with the canvas's hint, Sign In, "How do I set up a server?"), and `RootView` switching on `sessionFlow()` so a lost session returns to Login.
- [ ] **Step 4: Run the tests** — PASS. Simulator check light/dark against the artboard; sign in with the demo account from the project's QA notes (never a real password typed by Claude outside that account).
- [ ] **Step 5: Commit** `feat(ios): login and session gate`.

### Task 4: Tab shell

**Files:** `App/MainTabView.swift`

**Interfaces:** Produces `MainTabView` with the tabs the `iOS-Home` artboard's tab bar shows (Home, Places, Collections, Map) plus the system search tab (`Tab(role: .search)`) opening global search; one `NavigationStack` per tab; `NavigationSplitView` when `horizontalSizeClass == .regular`. Profile/settings from the avatar in Home's top bar, as the canvas draws it.

- [ ] **Step 1:** Implement with placeholder roots per tab.
- [ ] **Step 2:** Build and check on the simulator: glass tab bar, tab switching keeps each stack.
- [ ] **Step 3: Commit** `feat(ios): tab shell`.

### Tasks 5–19: Screens, one commit each

Order (spec): Home, Places, Place, Collections, Collection, Place form, Collection form, Transport/Lodging/Note/Checklist forms, Map, Calendar, World + Country, Settings, Users (People), Legal. Each task:

- [ ] **Step 1:** Read the artboard (`design/native-ui/iOS-<Screen>.dc.html`, light and dark) and list its controls against the ViewModel's state and actions; anything the canvas shows that the ViewModel cannot supply is written down, not invented.
- [ ] **Step 2:** Add the owner accessor if missing (Task 1's naming), and a Kotlin factory for the UI state if a preview needs one.
- [ ] **Step 3:** Implement `Features/<Feature>/<Screen>View.swift`; lists use `PagingListPresenter` where the ViewModel pages (call `itemAccessed(index)` in `.onAppear` of each row); forms follow the `@State` rule; destructive actions confirm with a system dialog. Platform pieces are the system's: MapKit for every map, `PhotosPicker` for photos, the system camera picker for the camera.
- [ ] **Step 4:** Build; simulator check light and dark, the largest Dynamic Type size, and a pop that logs the owner's `clear()` in debug. Screenshots compared with the artboard.
- [ ] **Step 5: Commit** `feat(ios): <screen>`. The Place form task also adds the spec's create/edit XCUITest (`testCreatePlaceThenItAppearsInPlaces`, against the demo account, deleting what it created).

### Task 20: Documents

- [ ] Update `docs/design.md` with the iOS section (structure, glass rule, state and text-input rules), the spec's "Calls made by Claude" list with what was decided, and the README's iOS status. Commit `docs(ios): phase 3 notes`.
