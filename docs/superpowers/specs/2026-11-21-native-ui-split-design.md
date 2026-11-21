# Native UI split: Material 3 Expressive on Android, Liquid Glass on iOS

Date: 2026-11-21 · Branch: `feat/native-ui-split` · Status: awaiting owner approval

## Intent

AdventureLog draws one Compose UI on both platforms. On iOS that is a Material app in a
`ComposeUIViewController`, and on Android the shared UI is held back to what also looks tolerable
on iOS ("Nothing Material in a shared view", `docs/design.md`). Neither platform gets its own
design language.

The owner wants each platform to look native at full strength, the way his other apps now do:

- **Android:** a Material 3 Expressive redesign, as in Shiori and Libraries for Developers.
- **iOS:** real SwiftUI with Liquid Glass.
- **Shared:** ViewModels and everything below them. Not UI.

This replaces the 2026-09-03 plan (Compose content under a native SwiftUI shell).

## Goals

- No Compose in the iOS binary. `commonMain` holds only what Swift consumes, plus its dependencies.
- Android UI free to use any Android-only Compose API.
- Every screen that exists today exists on both platforms when the work ends, driven by the same
  ViewModel.
- CI builds the Swift app, not just the Kotlin framework, so a shared change that breaks Swift is
  caught. (From Phase 3, when there is Swift worth compiling.)

## Non-goals

- New features, new screens, or API changes.
- Desktop. There is no JVM target today (`desktopMain` is a leftover folder); it is deleted, and
  AGENTS.md stops advertising `:composeApp:run`.
- Sharing strings or images with Swift. Strings are hardcoded today (0 `stringResource` calls);
  localization is a later, separate job.
- Supporting iOS below 26.
- Migrating the rest of Android navigation to Navigation 3.

## Anti-goals

- A Compose imitation of Liquid Glass, or SwiftUI made to look like Material.
- Business logic duplicated in Swift. If a Swift view needs a decision made, it goes in the shared
  ViewModel.
- iOS wrappers that must be hand-edited every time a UI-state field is added, if a tool can avoid it.

## Constraints and facts

| Fact | Source |
|---|---|
| 20 ViewModels in feature `commonMain`, none imports Compose | grep |
| ~128 of 214 feature `commonMain` files contain Compose (35k lines total) | grep |
| 20 screens: Login, Dashboard, LocationList, AdventureDetail, AddEditLocation, Collections, CollectionDetail, AddEdit{Collection,Checklist,Lodging,Note,Transportation}, Map, Calendar, World, CountryDetail, Users, Settings, PrivacyPolicy, TermsOfUse | grep |
| Targets: `androidTarget`, `iosArm64`, `iosSimulatorArm64` only | `KotlinMultiplatform.kt` |
| Kotlin 2.4.10, Compose Multiplatform 1.12.0, AGP 9.2.1, compileSdk/targetSdk 37, minSdk 24 | `libs.versions.toml` |
| iOS deployment target 15.3, Xcode 27.2 installed, static framework `ComposeApp`, no exports | `project.pbxproj`, `composeApp/build.gradle.kts` |
| 4 ViewModels use Paging or `SavedStateHandle` (Map, Locations, Collections, Login) | grep |
| Maps, camera, image picker, emoji picker are already `expect`/`actual` per platform | `feature/*/src/{androidMain,iosMain}` |
| CI already links the iOS framework on `macos-latest` (`linkDebugFrameworkIosSimulatorArm64`); it does not compile Swift | `.github/workflows/ci.yml` |
| iOS has no users; it may have no UI during the transition | owner, 2026-11-21 |

From the sibling projects:

- `androidx.compose.material3` 1.5.0 is stable and has `MaterialExpressiveTheme`; it needs AGP ≥ 9.1
  and compileSdk 37 (both met) and minSdk 24 (met).
- NextcloudApps bridges ViewModels to Swift by hand: a 513-line Kotlin helper, one Swift wrapper
  per ViewModel, forwarders for sealed actions. Its recorded costs: Kotlin default arguments do not
  reach Swift, so a new state field breaks every Swift call site; ViewModels are singletons whose
  scope is never cancelled; search text echoed through the ViewModel drops characters.

## Approaches

1. **Move first (chosen).** Spike the Swift bridge on one screen, then move all Compose to
   `androidMain` in one mechanical change. After it, the platforms proceed independently. iOS has
   no UI until its SwiftUI screens exist.
2. **Screen by screen, Compose kept alive on iOS.** iOS never breaks, but Expressive must compile
   for iOS, every screen is touched twice, and the Compose shell must host both kinds of screen
   for months.
3. **Native shell around shared Compose content** (the September plan). Rejected by the owner:
   Expressive lives inside the content, so the content cannot be shared.

The owner picked 1 on 2026-11-21, accepting that iOS is broken in between.

## Design

### Module layout

Feature modules stay KMP. Inside each:

- `commonMain`: ViewModels, UI-state models, Koin modules, anything Swift references.
- `androidMain`: every `@Composable`, navigation graphs, previews, theme.
- `iosMain`: only non-UI `actual`s that a shared ViewModel needs (file access, URL validation).
  Compose `actual`s (MapView, CameraCapture, ImagePicker, EmojiPicker) are deleted; iOS uses
  MapKit, PhotosPicker and the system emoji keyboard from Swift.

The rule, taken from NextcloudApps: a type belongs in `commonMain` only if Swift uses it or
something Swift uses depends on it. "Could be multiplatform" is not a reason.

`ComposeMultiplatformConventionPlugin` puts Compose dependencies in `androidMain` only.
`feature/ui` becomes Android-only in content. `composeApp` keeps the Android app and the iOS
framework, and exports `core:model`, `core:domain` and every feature module so Swift sees
unprefixed type names.

### Phase 0: bridge spike (throwaway)

One screen, against today's `commonMain` ViewModels, before anything moves. **World** is the
subject: a list, a detail, no forms, no paging. Then one paged list (Locations) to settle Paging.

Questions it must answer:

- **SKIE or hand-written wrappers?** SKIE gives Swift `AsyncSequence` for flows, exhaustive sealed
  types and default arguments. Whether a SKIE release supports Kotlin 2.4.10 is unverified; the
  spike finds out by building. If it does not, fall back to the NextcloudApps `FlowWrapper`.
- **ViewModel lifetime.** Try the lifecycle library's `ViewModelStore` owned by a Swift object per
  screen, so `viewModelScope` is cancelled when the screen goes. Singletons are the fallback.
- **Paging.** Whether `PagingData` can be presented from Swift. If not, the shared ViewModels
  expose a plain list plus `loadMore()` and Android adapts.
- **`SavedStateHandle`** construction from Swift, for the ViewModels that take one.

Output: a decision note appended to this document, and the pattern every later iOS screen copies.
The owner reads the note before Phase 1 starts. The spike's code is discarded.

### Phase 1: the move

One mechanical change, no behaviour change on Android:

1. Convention plugin: Compose in `androidMain` only.
2. Move every Compose file from `commonMain` to `androidMain`, keeping packages.
3. Delete Compose `actual`s in `iosMain`, `MainViewController`, and `desktopMain`.
4. `composeApp`: add exports, strip Compose-flavoured libraries from `commonMain` dependencies.
5. Swap `compose.material3` for `androidx.compose.material3` 1.5.x on Android.
6. iOS app shows a placeholder SwiftUI view. Deployment target becomes 26.0.
7. `LegalTexts` loses its Compose import and stays in `commonMain` as plain data, so the legal
   text is not duplicated in Swift.

Done when: `./gradlew build` and `allTests` pass, the Android app behaves as before on the Pixel 6a,
a grep finds no `androidx.compose` under any `commonMain`, and the iOS app launches to the
placeholder.

Things that will need a judgment call during the move, and how to make it:

- Non-Composable files that import Compose (`LocationFormSaver`, `LegalTexts`, `UriOpening`,
  navigation files): to `androidMain` unless a ViewModel needs them, in which case the Compose
  type is removed from them.
- Screen tests in `androidInstrumentedTest` stay where they are.

### Phase 1b: shared ViewModel changes the spike called for

Separate from the move, because it is not mechanical: whatever the decision note requires of the
shared ViewModels (for example a plain list plus `loadMore()` in place of `PagingData`, with the
Android screens adapted). It lands before Phases 2 and 3 start, so neither edits a ViewModel under
the other. Empty if the spike needs no changes.

### Phase 2: Android, Material 3 Expressive

Recipe from Shiori and Libraries for Developers, in this order, one commit per area:

1. **Theme.** `MaterialExpressiveTheme`, brand palette from `#006973` with every surface-container
   role (dark ramp starting above `surface`), Roboto Flex with per-size optical-size families and
   all weights to 900, expressive shapes. Dynamic colour stays off by default.
2. **Shell.** Navigation bar/rail, floating toolbar in place of top-bar actions where it fits.
3. **Screens**, in the order: Dashboard, Places list, Place detail, Collections, Collection detail,
   the editors, Map, Calendar, World, Settings, Login.
4. **`docs/design.md`** rewritten: the "Nothing Material in a shared view" section goes; the
   layout traps and form rules stay.

A design canvas comes first (Claude Design, as with the dashboard redesign) and the owner picks
from it before screens are built. The existing 1a/2a dashboard is the starting point, not a
constraint.

Known traps to carry over: `ButtonGroup` crashes inside lazy lists; a floating toolbar's first
item must be interactive; a hide-on-scroll toolbar must sit in a full-size overlay lifted by an
offset; every screen is walked in light and dark on the device.

### Phase 3: iOS, SwiftUI with Liquid Glass

- **Structure:** `App/`, `Core/{DI,Bridge,Theme,Components}`, `Features/<Feature>/`.
- **Navigation:** `TabView` (Home, Places, Collections, Map, more), a `NavigationStack` per tab,
  `NavigationSplitView` on regular width. Navigation state is Swift's own; no routes are shared.
- **Glass:** let the system draw it. Standard toolbars, tab bar, search and sheets get Liquid Glass
  by building against iOS 26. Custom `.glassEffect` only for floating controls over content (map
  controls, the detail page's actions), grouped in a `GlassEffectContainer`.
- **Platform pieces:** MapKit, PhotosPicker, camera via the system picker.
- **Order:** Login, tab shell, then the same screen order as Android.
- **Text input:** fields keep their text in local `@State` and push to the ViewModel; they do not
  render the echoed value.

A design canvas for iOS comes first here too.

### Testing

- ViewModel tests stay in `commonTest`, unchanged, and now cover both platforms' logic.
- Android: existing instrumented screen tests are updated per redesigned screen; device check on
  the Pixel 6a.
- iOS: simulator check per screen; XCUITests for login and one create/edit flow once those exist.
- CI: the existing framework link stays; an `xcodebuild` step for `iosApp` is added with the first
  real SwiftUI screen. Whether hosted runners have Xcode with the iOS 26 SDK is checked then.

## Order of work

Phase 0 → Phase 1 → Phase 1b → Phases 2 and 3 independently. Each phase gets its own
implementation plan; the first plan covers Phases 0, 1 and 1b.

## Merging and releases

**Open, owner to confirm.** Proposed:

- Phases 1 and 1b merge to `develop` as soon as they pass: Android is unchanged, so `develop`
  stays releasable and Play hotfixes are unaffected.
- Phase 2 lives on its own branch off `develop` and merges once, when every screen is redesigned,
  as one release (as Shiori 1.53.01 did). No Play release with half the screens Expressive.
- Phase 3 merges to `develop` screen by screen; iOS is not published, so partial is fine.

## Left to the builder

- Exact SwiftUI component breakdown per screen.
- Which Expressive components each Android screen uses, within the approved canvas.
- Commit granularity inside the move.
- Lifting ambient state read in composables (`rememberCurrentUserId`, `rememberMapRendering`, the
  authenticated image client in `App.kt`) into shared code when iOS needs it.

## Calls made by Claude, not yet confirmed by the owner

- iOS 26 as the minimum.
- World as the spike screen.
- `TabView` with five tabs as the iOS root.
- Exporting every feature module from the framework.
- Deleting `desktopMain`.
- Legal text kept shared as plain data.
- The spike's ViewModel changes as their own step (Phase 1b), after the owner reads the note.
- Swift compiled in CI only from Phase 3.
- Android moves to `androidx.compose.material3` rather than the Compose Multiplatform artifact.
