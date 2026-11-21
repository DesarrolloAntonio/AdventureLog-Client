# Native UI Split, Phases 0, 1 and 1b: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prove how Swift consumes the shared ViewModels, then move every line of Compose out of `commonMain` so Android and iOS can each get their own UI.

**Architecture:** Feature modules stay Kotlin Multiplatform. ViewModels, UI-state types and Koin modules stay in `commonMain`; every `@Composable` goes to `androidMain`; iOS becomes a SwiftUI app that links the shared framework. A throwaway spike decides the Swift bridge before anything moves.

**Tech Stack:** Kotlin 2.4.10, Compose Multiplatform plugin 1.12.0, AGP 9.2.1, Koin 4.2.2, `androidx.compose.material3` 1.5.x, SwiftUI on iOS 26, Xcode 27.2.

**Spec:** `docs/superpowers/specs/2026-11-21-native-ui-split-design.md`

## Global Constraints

- `commonMain` holds only what Swift consumes plus its dependencies. "Could be multiplatform" is not a reason to keep a file there.
- Phase 1 changes no Android behaviour. Anything that would is Phase 1b.
- No visual design in this plan. The look of both platforms comes from Claude Design in Phases 2 and 3.
- iOS deployment target 26.0. iOS may show only a placeholder after Phase 1.
- Targets stay `androidTarget`, `iosArm64`, `iosSimulatorArm64`. minSdk 24, compileSdk/targetSdk 37.
- Tests: `kotlin.test`, coroutines `TestScope`, hand-rolled fakes. No MockK on native.
- Commits: Conventional Commits, subject ≤ 72 characters.
- Device checks on the Pixel 6a (`28241JEGR08993`) only; the app targets the backend port `:3447`; never log the owner out.
- Merges: Phases 1 and 1b go to `develop` as soon as they pass. The spike branch is never merged.

## Review Focus

1. **A type a ViewModel uses is declared in a file that also has Compose.** Moving that file to `androidMain` compiles on Android and breaks iOS. Expected: the type is split out first (Task 4) and the iOS link passes.
2. **A Compose dependency left in a `commonMain` dependency block.** Nothing imports it, the build passes, and Skia is still linked into the iOS binary. Expected: Task 5's check script also scans build files.
3. **A shared test that imports Compose.** `LocationFormSaverTest` and `UriOpeningTest` stop compiling for iOS once their subjects move. Expected: they move with their subjects (Task 5).
4. **A screen that reads state without a ViewModel** (`rememberCurrentUserId`, `rememberMapRendering`, the image client in `App.kt`). Expected: unchanged on Android after the move; listed in the decision note as iOS work, not fixed here.
5. **Android look or behaviour shifting with the material3 swap.** `androidx` 1.5 may change defaults (app-bar heights, motion). Expected: Task 6's device walk compares every screen against `main`, in light and dark.

---

## File structure

| Path | After this plan |
|---|---|
| `scripts/check-common-compose.sh` | New. Fails if any `commonMain` source or dependency block references Compose. |
| `build-logic/convention/.../ComposeMultiplatformConventionPlugin.kt` | Compose dependencies in `androidMain` only. |
| `feature/*/src/commonMain` | ViewModels, state, form data, Koin modules. No Compose. |
| `feature/*/src/androidMain` | All Compose, navigation graphs, previews. Same packages as before. |
| `feature/*/src/iosMain` | Non-UI `actual`s only. |
| `composeApp/src/androidMain` | `App.kt`, nav graph, theme. |
| `composeApp/src/iosMain/.../IosKoin.kt` | New. `fun doInitKoin()` and the bridge chosen in Task 3. |
| `composeApp/src/desktopMain` | Deleted. |
| `iosApp/iosApp/` | Placeholder SwiftUI root; deployment target 26.0. |

---

## Phase 0: bridge spike (throwaway)

Work on `spike/swift-bridge`, branched from `feat/native-ui-split`. Nothing here is merged. No tests are written; the deliverable is the decision note.

### Task 1: Does SKIE build on Kotlin 2.4.10?

**Files:**
- Modify: `gradle/libs.versions.toml`, `composeApp/build.gradle.kts`

- [ ] **Step 1:** Add the SKIE Gradle plugin (`co.touchlab.skie`, newest published version, 0.10.16 at the time of writing) to `composeApp`, and add `export(projects.core.model)`, `export(projects.core.domain)`, `export(projects.feature.world)`, `export(projects.feature.locations)` to the framework, changing those four dependencies to `api`.
- [ ] **Step 2:** Run `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.
  Expected: either BUILD SUCCESSFUL, or a SKIE error naming the unsupported Kotlin version.
- [ ] **Step 3:** Record the result. If it fails, remove the plugin, keep the exports, and continue with hand-written wrappers.

### Task 2: World in SwiftUI against the real ViewModel

**Files:**
- Create: `composeApp/src/iosMain/kotlin/com/desarrollodroide/adventurelog/IosKoin.kt`
- Create: `iosApp/iosApp/Spike/WorldSpikeView.swift`, `iosApp/iosApp/Spike/ViewModelOwner.swift`
- Modify: `iosApp/iosApp/iOSApp.swift`, `iosApp/iosApp.xcodeproj/project.pbxproj` (deployment target 26.0)

**Interfaces:**
- Consumes: `WorldViewModel.uiState: StateFlow<WorldUiState>`, `onSearchQueryChanged(String)`, `onRegionSelected(WorldRegion)`, `onFilterModeChanged(FilterMode)`, `onRefresh()`; `CountryDetailViewModel.uiState`, `load(countryCode: String)`, `onRegionToggled(regionId: String)`.
- Produces (Kotlin, `IosKoin.kt`): `fun doInitKoin()`; `class IosViewModelOwner : ViewModelStoreOwner { fun <T : ViewModel> get(kClass: KClass<T>): T; fun clear() }`, which resolves from Koin and clears its `ViewModelStore` on `clear()`.

- [ ] **Step 1:** Write `IosKoin.kt` with the two declarations above.
- [ ] **Step 2:** Write `ViewModelOwner.swift`: an `ObservableObject` that holds an `IosViewModelOwner` and calls `clear()` in `deinit`.
- [ ] **Step 3:** Write `WorldSpikeView`: a `NavigationStack` with a searchable `List` of countries from `uiState`, pushing a detail list of regions with a toggle. Plain system components; no styling.
- [ ] **Step 4:** Run on the iOS 27 simulator against the demo server. Log in is out of scope: read the demo account from the untracked `qa.credentials.json` at build time; never commit a credential, even on the spike branch.
- [ ] **Step 5:** Answer, with evidence, in notes kept for Task 3:
  - Does state reach SwiftUI and update on search, filter and toggle?
  - Add a `println` in `WorldViewModel.onCleared()`: is it called when the view is popped?
  - Does fast typing in the search field lose characters when the text round-trips through the ViewModel? Does keeping the text in `@State` fix it?
  - How do a sealed type and a data class with default arguments look from Swift, with and without SKIE?

### Task 3: Paging, `SavedStateHandle`, and the decision note

**Files:**
- Create: `iosApp/iosApp/Spike/LocationsSpikeView.swift`
- Modify: `docs/superpowers/specs/2026-11-21-native-ui-split-design.md` (append "Decision note"; this file is cherry-picked to `feat/native-ui-split`)

**Interfaces:**
- Consumes: `LocationsViewModel.adventuresPagingData: Flow<PagingData<Location>>`; `MapViewModel` and `LoginViewModel`, whose constructors default `savedStateHandle = SavedStateHandle()`.

- [ ] **Step 1:** Try to show `adventuresPagingData` in a SwiftUI `List` with load-more on scroll. Time-box: half a day. Try the paging library's presenter API from Kotlin in `iosMain`, exposing a snapshot list and a `loadMore()` to Swift.
- [ ] **Step 2:** Resolve `MapViewModel` and `LoginViewModel` through `IosViewModelOwner` and confirm they construct.
- [ ] **Step 3:** Append a "Decision note" section to the spec with one line each: bridge (SKIE or wrappers), ViewModel lifetime pattern, paging (works as is / `iosMain` presenter / change the ViewModels), `SavedStateHandle`, text-input pattern, and the list of shared-ViewModel changes Phase 1b must make (may be empty).
- [ ] **Step 4:** Commit the note on `feat/native-ui-split` only:

```bash
git switch feat/native-ui-split
git checkout spike/swift-bridge -- docs/superpowers/specs/2026-11-21-native-ui-split-design.md
git commit -m "docs(design): bridge spike decision note"
```

- [ ] **Step 5:** **Stop. The owner reads the note before Phase 1 starts.**

---

## Phase 1: the move

On `feat/native-ui-split`.

### Task 4: Split shared types out of Compose files

Four types that ViewModels use are declared inside files full of Compose. They get their own files so the Compose files can leave. Four is a floor: it comes from the direct imports of `*ViewModel.kt` files only, and Task 5's iOS compile may find more.

**Files:**
- Create in `feature/collections/src/commonMain/.../feature/collections/ui/components/`: `CollectionTab.kt`, `CollectionView.kt`, `ShareSheetState.kt` (holds `ShareSheetState` and `SheetMessage`)
- Create: `feature/locations/src/commonMain/.../feature/locations/ui/screens/addEdit/VisitForm.kt` (holds `visitFormOf` and the type it returns)
- Modify: `CollectionsTabs.kt`, `CollectionViews.kt`, `ShareCollectionSheet.kt`, `AddEditLocationScreen.kt` (declarations removed)
- Create: `scripts/check-common-compose.sh`

**Interfaces:**
- Produces: the same names in the same packages, so no import changes anywhere.
- Produces: `scripts/check-common-compose.sh`, exit 0 when clean. It fails, listing offenders, if (a) any `*.kt` under a `src/commonMain` or `src/commonTest` directory outside `build/` matches `androidx\.compose|org\.jetbrains\.compose`, or (b) any `build.gradle.kts` or convention plugin has a Compose, `navigation-compose`, `koin.compose`, `coil.compose` or `lifecycle.runtimeCompose` dependency inside a `commonMain` block. Generated-resources use of `compose.components.resources` in `core/common` is the one allowed exception, if `Res` is still referenced; otherwise remove it too.

- [ ] **Step 1:** Write `scripts/check-common-compose.sh`. Run it. Expected: FAIL, listing about 130 files.
- [ ] **Step 2:** Move the four declarations into the new files, with no Compose imports. If a declaration holds a Compose type (an `ImageVector`, a `Color`), replace that member with a plain value (an enum or a string key) and map it to the Compose type at the call site.
- [ ] **Step 3:** Add a guard for Review Focus 1: extend the script with (c) for every `import` of a project type in a `*ViewModel.kt`, the declaring file must not import Compose. Run. Expected: (c) passes, (a) still fails.
- [ ] **Step 4:** Run `./gradlew allTests`. Expected: PASS.
- [ ] **Step 5:** Commit: `refactor(ui): split shared state types out of Compose files`.

### Task 5: Move Compose to `androidMain`

**Files:**
- Modify: `build-logic/convention/src/main/kotlin/ComposeMultiplatformConventionPlugin.kt`; every `build.gradle.kts` that applies it (`composeApp`, `core/common`, ten `feature/*`)
- Move: every `commonMain` file matched by check (a), to the same package path under `androidMain`
- Move: `LocationFormSaverTest.kt`, `UriOpeningTest.kt` to `androidUnitTest` in their modules
- Modify: `feature/settings/.../LegalTexts.kt` (stays in `commonMain`, Compose import removed, text as plain strings)
- Delete: Compose `actual`s in `iosMain` (`MapView.ios.kt`, `CameraCapture.ios.kt`, `ImagePicker.ios.kt`, `EmojiPicker.ios.kt`, `PlatformBackHandler.ios.kt`, `feature/detail` and `feature/map` map views, `LocationMapSection.kt`, `Theme.ios.kt`, `DynamicColorSupport.ios.kt`) together with their `expect` declarations, which become plain Android functions
- Delete: `composeApp/src/desktopMain/`, `composeApp/src/iosMain/.../MainViewController.kt`
- Create: `composeApp/src/iosMain/.../IosKoin.kt` (final version of the spike's file, per the decision note)

**Interfaces:**
- Consumes: the decision note (bridge choice, SKIE on or off).
- Produces: `doInitKoin()` for Task 7; framework exports `core:model`, `core:domain` and all ten feature modules.

- [ ] **Step 1:** Change the convention plugin so all Compose dependencies are added to `androidMain`. Leave `components.resources` in `commonMain` only where `Res` is used from shared code.
- [ ] **Step 2:** Move the files with `git mv`, driven by the script's list from check (a). Keep packages. Non-Composable files that import Compose (`CollectionsNavigation`, `LocationsNavigation`, `HomeBottomBar`, `LocationFormSaver`, `UriOpening`, `NavigationAnimations`) move too.
- [ ] **Step 3:** Collapse each `expect`/`actual` composable to a single Android function and delete the iOS `actual`. Keep non-UI `actual`s (`IOSPlatformFiles`, `IOSImageBytesProvider`, `UrlValidator.ios.kt`, `IosPlatformActions`, `Utils.ios.kt`, `Platform.ios.kt`) only if shared code still calls their `expect`.
- [ ] **Step 4:** In each module's build file, move Compose-flavoured libraries from `commonMain.dependencies` to `androidMain.dependencies`. In `composeApp`, add the exports and make the exported modules `api`.
- [ ] **Step 5:** Replace Compose-flavoured artifacts that shared code genuinely needs with their plain versions in `commonMain`: the catalog alias `androidx-lifecycle-viewmodel` points at `lifecycle-viewmodel-compose`, so add a `lifecycle-viewmodel` alias for shared code and keep the Compose one for `androidMain`; the Koin modules' `viewModel { }` DSL must resolve from a non-Compose Koin artifact (`koin-core-viewmodel`), with `koin.composeVM` in `androidMain` only.
- [ ] **Step 6:** Run `scripts/check-common-compose.sh`. Expected: PASS.
- [ ] **Step 7:** Check the real oracle, the resolved iOS graph: `./gradlew :composeApp:dependencies --configuration iosSimulatorArm64CompileKlibraries | grep -iE "compose|skiko"`. Expected: no output, or only `components-resources` if it was kept.
- [ ] **Step 8:** Run `./gradlew build allTests :composeApp:linkDebugFrameworkIosSimulatorArm64`. Expected: BUILD SUCCESSFUL. The iOS compile is what finds any shared type Task 4 missed; split it out the same way and rerun.
- [ ] **Step 9:** Add the script as a step in the `test` job of `.github/workflows/ci.yml`.
- [ ] **Step 10:** Commit: `refactor(ui): move all Compose from commonMain to androidMain`.

### Task 6: Android on `androidx.compose.material3` 1.5

**Files:**
- Modify: `gradle/libs.versions.toml`, `ComposeMultiplatformConventionPlugin.kt`

- [ ] **Step 1:** Record the baseline: run `./gradlew :composeApp:dependencyInsight --configuration debugRuntimeClasspath --dependency <name>` for `material3`, `ui`, `foundation` and `runtime`, and note the resolved `androidx` versions.
- [ ] **Step 2:** Read the newest 1.5.x stable `.module` file for the `ui`, `foundation` and `runtime` versions it requires. Libraries for Developers runs 1.5.0 on Compose 1.13; this project's plugin is 1.12. If 1.5.x requires a newer Compose than the baseline, **stop and report**: the choices are raising the Compose Multiplatform plugin to 1.13 (its own change, before this task) or using the JetBrains `material3` artifact in `androidMain` at a version that has the Expressive APIs, as NextcloudApps does.
- [ ] **Step 2b:** Otherwise add `androidx.compose.material3:material3` 1.5.x in `androidMain` in place of `composeDeps.material3`, and rerun the four `dependencyInsight` calls. Expected: `material3` at 1.5.x; `ui`, `foundation` and `runtime` unchanged from the baseline.
- [ ] **Step 3:** No Expressive API is adopted here. Run `./gradlew build allTests`. Expected: PASS.
- [ ] **Step 4:** Install the debug build on the Pixel 6a and walk every screen in the spec's list, light and dark, against screenshots taken from `main` first. Open every screenshot. Expected: no visible difference; any difference is fixed here or the swap is reverted and reported.
- [ ] **Step 5:** Commit: `build(android): use androidx material3 1.5 for Expressive`.

### Task 7: iOS placeholder

**Files:**
- Modify: `iosApp/iosApp/iOSApp.swift`, `iosApp/iosApp/ContentView.swift`, `iosApp/iosApp.xcodeproj/project.pbxproj`

**Interfaces:**
- Consumes: `doInitKoin()` from Task 5.

- [ ] **Step 1:** `iOSApp.init()` calls `doInitKoin()`. `ContentView` is a `ContentUnavailableView` titled "AdventureLog" with the description "The iOS app is being rebuilt." No `ComposeView`.
- [ ] **Step 2:** Set `IPHONEOS_DEPLOYMENT_TARGET = 26.0` in all four build configurations.
- [ ] **Step 3:** Run `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build`. Expected: BUILD SUCCEEDED.
- [ ] **Step 4:** Launch in the simulator and screenshot. Expected: the placeholder, no crash, Koin started (no `KoinApplication has not been started` in the log).
- [ ] **Step 5:** Commit: `feat(ios): SwiftUI placeholder on iOS 26, Compose removed`.

### Task 8: Documents

**Files:**
- Modify: `AGENTS.md`, `docs/design.md`, `README.md`

- [ ] **Step 1:** `AGENTS.md`: state the `commonMain` rule and the check script; Compose lives in `androidMain`; remove `./gradlew :composeApp:run` and the desktop mention; point to `iosApp/` as a SwiftUI app.
- [ ] **Step 2:** `docs/design.md`: replace the "Nothing Material in a shared view" section with two lines saying UI is no longer shared and linking the spec. Keep the layout traps and form rules, marked as Android.
- [ ] **Step 3:** Run `./gradlew createModuleGraph` if module dependencies changed.
- [ ] **Step 4:** Commit: `docs: UI is per platform, commonMain rule and check script`.
- [ ] **Step 5:** Open a PR from `feat/native-ui-split` to `develop` with the test checklist and the Task 6 screenshots.

---

## Phase 1b: shared ViewModel changes

### Task 9: Apply the decision note's ViewModel changes

Skip if the note's list is empty. Each change is test-first in the module's `commonTest`, with the Android screen adapted in the same commit so Android behaviour is unchanged.

The one change that can be specified now, if the note says paging cannot be consumed from Swift:

**Files:**
- Modify: `LocationsViewModel.kt`, `CollectionsViewModel.kt`, their tests, and the two Android list screens

**Interfaces:**
- Produces, on each of the two ViewModels: `val items: StateFlow<List<T>>`, `val isLoadingMore: StateFlow<Boolean>`, `val endReached: StateFlow<Boolean>`, `fun loadMore()`, with `T` being `Location` and `UltraSlimCollection`. `adventuresPagingData` and `collectionsPagingData` are removed.

- [ ] **Step 1:** Write failing tests in `LocationsViewModelTest`: `loadMore appends the next page`, `loadMore does nothing while a page is loading`, `loadMore does nothing after the last page`, `changing the search query resets to the first page`, `a failed page keeps the items already loaded`.
- [ ] **Step 2:** Run `./gradlew :feature:locations:allTests`. Expected: FAIL, unresolved `items`.
- [ ] **Step 3:** Implement on `LocationsViewModel`; adapt `LocationListScreen` to a `LazyVerticalGrid` that calls `loadMore()` near the end.
- [ ] **Step 4:** Run the tests. Expected: PASS. Check the list on the Pixel 6a: scrolling loads further pages, search and filters reset the list.
- [ ] **Step 5:** Repeat steps 1 to 4 for `CollectionsViewModel`.
- [ ] **Step 6:** Commit per ViewModel: `refactor(locations): plain list and loadMore instead of PagingData`.

Any other change in the note gets the same shape: failing test, change, Android screen adapted, device check, commit.
