# Android Material 3 Expressive (Phase 2) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Every Android screen redrawn in Material 3 Expressive, following the Claude Design canvas in `design/native-ui/`, with no change to shared ViewModels.

**Architecture:** The theme (colour roles, Roboto Flex, expressive shapes, `MaterialExpressiveTheme`) lands first, then the shell (floating toolbar on compact windows, rail on larger ones), then the screens one area per commit in the spec's order. Reusable pieces (ticket card, passport chip, date tile, segmented list) go into `feature/ui` before the first screen that needs them.

**Tech Stack:** Kotlin, Jetpack Compose, `androidx.compose.material3` 1.5.0 (Expressive APIs are `@ExperimentalMaterial3ExpressiveApi`), Compose resources in `:core:resources`.

**Spec:** `docs/superpowers/specs/2026-11-21-native-ui-split-design.md` (Phase 2). Visual source: `design/native-ui/Main.dc.html` and `design/native-ui/Android-*.dc.html`. Read the markup, not a screenshot of it (`docs/design.md`, first paragraph).

## Global Constraints

- Branch `feat/android-expressive` off `develop`; merges once, when every screen is done, as one release. No Play release with half the screens Expressive.
- Android UI only: nothing in `commonMain`, no shared ViewModel edits. `scripts/check-common-compose.sh` stays green.
- Colours are theme roles from `Color.kt`, never hex in a screen. Brand seed `#006973`; dynamic colour stays off by default.
- Every screen is walked on the Pixel 6a (`28241JEGR08993`) in light and dark, and every screenshot is opened before the screen is called done.
- Never log the demo account out; theme mode changes on the device are put back to Light afterwards. Instrumented tests run with `ANDROID_SERIAL=28241JEGR08993` (the phone is attached over three transports) and only from feature modules: `:composeApp:connected*AndroidTest` uninstalls the app afterwards, which logs it out.
- Screenshots attached to a PR crop out the account email (Settings) and the server URL (account sheet).
- The canvas is drawn with sample content; the screen's existing data and controls win. Before Task 4, `docs/superpowers/plans/2026-11-22-android-expressive-audit.md` records, per screen, what the artboard shows that the UiState lacks (dropped, never faked) and where every existing control goes.
- The demo account's trips are all in the past by 2026-11-22; Home's "next trip" state is pinned by the instrumented test, and on the device by a clearly named throwaway QA collection with future dates, deleted afterwards.
- Form rules in `docs/design.md` ("One shape for every form") still hold: label kept when filled, Save full width with Cancel under it, "New …"/"Edit …" titles, currency code beside a money icon, discard guard.
- Copy in English; placeholders the canvas marks with `[…]` stay out of the app.
- Known traps: `ButtonGroup` crashes inside lazy lists; a floating toolbar's first item must be interactive; a hide-on-scroll toolbar sits in a full-size overlay lifted by an offset.

## Review Focus

1. **Font scale 1.3 and 2.0.** Condensed display text and the toolbar's selected label must wrap or ellipsize, never clip (`main` already wraps "Collections" and World's "Partial" at 1.3).
2. **Window width ≥ 600dp.** The floating toolbar is for compact windows; tablets and foldables keep the navigation rail and the Places two-pane.
3. **No upcoming trip / no visits / no photos.** Home's hero needs a fallback line, a country strip with nothing visited shows an empty state, and photo slots keep `LocationPlaceholder`.
4. **Long names.** A 60-character place or collection name in a ticket card or a passport chip ellipsizes in one or two lines.
5. **Content under the toolbar.** The last list item and any FAB stay reachable above the floating toolbar and the gesture bar.

Each item has its test in the task that owns the code (Tasks 2, 3, 4). Where each existing control lands, and what the canvas shows without data, is in `2026-11-22-android-expressive-audit.md`; every screen task reads its section first.

---

### Task 1: Expressive theme

**Files:**
- Create: `feature/ui/src/androidMain/assets/fonts/roboto_flex.ttf` (copy of `LibrariesForDevelopers/Repos/res/font/roboto_flex.ttf`, OFL 1.1). Display styles set the optical-size axis to their own size: the canvas's browser does that on its own, and without it width 38 is barely narrower (92% at opsz 14 against 81% at opsz 44).
- Create: `feature/ui/src/androidMain/kotlin/com/desarrollodroide/adventurelog/feature/ui/theme/AdventureType.kt`
- Create: `composeApp/src/androidMain/kotlin/com/desarrollodroide/adventurelog/theme/Shape.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/desarrollodroide/adventurelog/theme/Type.kt`, `Theme.kt`, `Color.kt`
- Modify: `feature/ui/build.gradle.kts` (androidMain gets `projects.core.resources`)
- Test: `feature/ui/src/androidUnitTest/kotlin/com/desarrollodroide/adventurelog/feature/ui/theme/AdventureTypeTest.kt`

**Interfaces:**
- Produces: `@Composable fun robotoFlex(): FontFamily` and `@Composable fun robotoFlexCondensed(): FontFamily` (width axis 38, weights 700–900) in `AdventureType.kt`; `object AdventureType { @Composable fun display(size: TextUnit): TextStyle; @Composable fun stamp(): TextStyle }` — `display` is the canvas's `font-stretch: 38%; font-weight: 800` voice, `stamp` the country chip's `60%`/800 with 1sp tracking. Both are built from plain, font-free specs `DisplaySpec(size: TextUnit): TextStyle` and `StampSpec: TextStyle` (no `fontFamily`), which the composables `.copy(fontFamily = …)`; the font loading is `@Composable` and can't run in a unit test.
- Produces: `AppTheme` wraps `MaterialExpressiveTheme(colorScheme, motionScheme = MotionScheme.expressive(), shapes = AdventureShapes, typography = AdventureTypography)`.

- [ ] **Step 1:** Write `AdventureTypeTest` on the specs: `DisplaySpec(44.sp)` has `fontWeight == FontWeight(800)` and line height `≤ 1.0 × fontSize` (the canvas's `line-height: 0.95`); `StampSpec` has `letterSpacing == 1.sp`.
- [ ] **Step 2:** Run `./gradlew :feature:ui:testDebugUnitTest --tests '*AdventureTypeTest*'`. Expected: FAIL, unresolved `DisplaySpec`.
- [ ] **Step 3:** Implement `AdventureType.kt` with `org.jetbrains.compose.resources.Font(Res.font.roboto_flex, weight, variationSettings = FontVariation.Settings(FontVariation.weight(w), FontVariation.width(wd)))`, as LibrariesForDevelopers' `Type.kt` does with `R.font`.
- [ ] **Step 4:** Rebuild `Type.kt` on Roboto Flex: the LibrariesForDevelopers scale and weights, with display/headline on the condensed family (width 38) instead of the wide one, and the 15 `*Emphasized` styles. Delete the Quicksand comment and the raised small sizes; drop `quicksand_variable_font_wght.ttf` and `lugrasimo_regular.ttf` if nothing else references them (`grep -r "quicksand\|lugrasimo"`).
- [ ] **Step 5:** `Shape.kt`: `AdventureShapes = Shapes(extraSmall = 4.dp, small = 12.dp, medium = 16.dp, large = 24.dp, extraLarge = 32.dp)` corners (the canvas: cards 24–28, chips 8–12, toolbar 32).
- [ ] **Step 6:** `Theme.kt`: `MaterialExpressiveTheme` as in Interfaces. Check `Color.kt` has every surface-container role for light and dark (it does for the static schemes; keep the medium/high-contrast schemes only if referenced, else delete them).
- [ ] **Step 7:** Run the test (PASS), `./gradlew :composeApp:assembleDebug`, install, screenshot Home and Settings in light and dark. Expected: Roboto Flex everywhere, the condensed width visibly applied to display text (compare against the canvas), no layout broken by the font swap; open every screenshot.
- [ ] **Step 8:** Commit `feat(theme): Material 3 Expressive theme, Roboto Flex`.

### Task 2: Shell — floating toolbar on phones, rail on tablets

**Files:**
- Create: `feature/home/src/androidMain/kotlin/com/desarrollodroide/adventurelog/feature/home/ui/components/MainToolbar.kt`
- Modify: `feature/home/.../ui/components/HomeBottomBar.kt` (keep `Destination` and the rail items), `feature/home/.../ui/screen/MainShell.kt`
- Test: `feature/home/src/androidInstrumentedTest/kotlin/com/desarrollodroide/adventurelog/feature/home/MainToolbarTest.kt`

**Interfaces:**
- Consumes: `CurrentScreen`, the `Destination` enum (made `internal`).
- Decided with the owner (2026-11-22): content runs behind the toolbar and the toolbar slides away while content scrolls down (exit-always), back on scroll up - stopping content above it read as a docked bar on a white band. A screen's main action goes beside the toolbar (`rememberShellAction` in `feature/ui`), as the canvas draws Places and Collections; the current destination drops its name when name and action would not fit with a 12dp margin. Screens outside the five (Settings, Calendar, People) show all five as icons.
- Produces: `@Composable fun MainToolbar(current: CurrentScreen, onSelect: (CurrentScreen) -> Unit, modifier: Modifier = Modifier)` — the canvas's dark pill: `inverseSurface` container, 64dp tall, 32dp corners, selected item a 48dp `primaryContainer` pill with icon and label, the others 48dp icon buttons with `contentDescription` = label. Dark theme inverts as the canvas does (container `inverseSurface` is light there).

- [ ] **Step 1:** Write `MainToolbarTest`: the selected destination shows its label text; each unselected one is found by content description and has no text; clicking "Map" calls `onSelect(CurrentScreen.MAP)`; at `fontScale = 2f` the selected label is displayed and the toolbar's height stays 64dp (Review Focus 1).
- [ ] **Step 2:** Run on the Pixel: `./gradlew :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.desarrollodroide.adventurelog.feature.home.MainToolbarTest`. Expected: FAIL, unresolved `MainToolbar`.
- [ ] **Step 3:** Implement `MainToolbar` on `HorizontalFloatingToolbar` (first child interactive). In `MainShell`, choose by `currentWindowAdaptiveInfo().windowSizeClass`: compact width → `Scaffold` content plus `MainToolbar` in a full-size overlay aligned bottom-centre, 24dp above the gesture bar, hidden on scroll via an offset; otherwise keep `NavigationSuiteScaffold` forced to the rail.
- [ ] **Step 4:** Lists under the shell get bottom content padding of toolbar height + 24dp + navigation-bar inset, exposed as `LocalToolbarClearance: ProvidableCompositionLocal<Dp>` from `MainToolbar.kt` (Review Focus 5). Add to the test: a `LazyColumn` with 50 items under the shell scrolls its last item fully above the toolbar.
- [ ] **Step 5:** Run the tests (PASS). On the device: phone portrait shows the toolbar on all five tabs, light and dark; `adb shell wm size 1800x2400` shows the rail and the Places two-pane (Review Focus 2); reset with `wm size reset`.
- [ ] **Step 6:** Commit `feat(shell): floating toolbar on phones, rail on tablets`.

### Task 3: Shared Expressive pieces in `feature/ui`

**Files:**
- Create in `feature/ui/src/androidMain/kotlin/com/desarrollodroide/adventurelog/feature/ui/components/`: `TicketCard.kt`, `PassportChip.kt`, `DateTile.kt`, `SegmentedList.kt`
- Test: `feature/ui/src/androidInstrumentedTest/kotlin/com/desarrollodroide/adventurelog/feature/ui/ExpressivePiecesTest.kt`

**Interfaces:**
- Produces:
  - `TicketCard(onClick, photo: @Composable () -> Unit, title, subtitle, stubs: List<TicketStub>, modifier, badge: (@Composable () -> Unit)? = null, notchColor = surface)` — 28dp corners, hairline `outlineVariant`, dashed perforation with two 24dp notches in the screen's background colour, stub labels 11sp/700/1.4sp tracking above `AdventureType.display(26.sp)` values.
  - `PassportChip(code: String, name: String, visited: Boolean, modifier: Modifier = Modifier)` — visited: `primary`/`onPrimary`; planned: 2dp dashed `outline` border.
  - `DateTile(month: String, day: Int, highlighted: Boolean, modifier: Modifier = Modifier)` — the calendar-sheet tile (month over day) used by Calendar and the itinerary.
  - `SegmentedList(items: List<T>, modifier, containerColor, row: @Composable (T) -> Unit)` and `SegmentedListDefaults.shapeFor(index, count)` for lazy lists — first/last 24dp outer corners, 6dp inner, 3dp gaps; replaces divider lists in Settings and Country.

- [ ] **Step 1:** Write `ExpressivePiecesTest`: a `TicketCard` titled with 60 characters shows at most two lines with ellipsis (Review Focus 4); a planned `PassportChip` is announced as "IT Italy, planned"; `SegmentedList` of 1 item gets all four corners at 24dp.
- [ ] **Step 2:** Run it on the Pixel. Expected: FAIL, unresolved.
- [ ] **Step 3:** Implement the four composables; `@Preview` each in light and dark.
- [ ] **Step 4:** Run the tests (PASS); render the previews (`android-cli` skill) and open them.
- [ ] **Step 5:** Commit `feat(ui): ticket card, passport chip, date tile, segmented list`.

### Tasks 4–14: Screens, one commit each, in the spec's order

Each screen task has the same shape. **Files:** the screen file(s) listed; the existing instrumented test for that screen if there is one. **Steps:** (1) update or add the instrumented test for what the canvas adds (named below); (2) run it, FAIL; (3) rebuild the screen from its artboard, reusing Task 3; (4) run it, PASS; (5) Pixel walk in light and dark at font scale 1.0 and 1.3, every screenshot opened; (6) commit `feat(<area>): Expressive <screen>`.

| Task | Screen | Artboard | Files | Test pins |
|---|---|---|---|---|
| 4 | Home | `Main` | `feature/home/.../DashboardScreen.kt` | hero reads "your next trip starts in N days" from `activeTrip ?: upcomingTrips.first()`, covering in progress, today, tomorrow; with no trip it reads "N places visited" and no ticket (Review Focus 3); no country strip and no ticket location (audit: no data); "Recently updated" keeps its name; stats, Coming up and the invites banner keep a home |
| 5 | Places | `Android-Places` | `feature/locations/.../LocationScreen.kt` | no grouping by country (the list is paged); sort and filter buttons keep their 48dp targets; the overflow sheet per card stays |
| 6 | Place | `Android-Place` | `feature/detail/.../AdventureDetailScreen.kt` | rating announced "Rated 5 out of 5"; "Add visit" reachable; no-photo place keeps `NoPhotoCard` |
| 7 | Collections | `Android-Collections` | `feature/collections/.../CollectionsScreen.kt` | tabs Active/Shared/Archived/Invites match `CollectionTab`; each card is a `TicketCard` |
| 8 | Collection | `Android-Collection` | `feature/collections/.../CollectionDetailScreen.kt` | the six view tabs keep `CollectionTabsTest` green; checklist progress "3 of 7 done" |
| 9 | Editors (no floating toolbar over a form: the canvas draws forms with Close and no toolbar, and today it covers the form's end until scrolled) | `Android-PlaceForm`, `-CollectionForm`, `-LodgingForm`, `-TransportForm`, `-NoteForm`, `-ChecklistForm` | the six `AddEdit*Screen.kt` and their section files | `FormFieldsTest`, `DiscardOnLeaveTest`, `TransportDatePickersTest` stay green; title "New …"/"Edit …"; Close in the top bar asks before discarding |
| 10 | Map | `Android-Map` | `feature/map/.../MapScreen.kt` | layer and filter buttons over the map; selected pin card opens the place |
| 11 | Calendar | `Android-Calendar` | `feature/calendar/.../CalendarScreen.kt` | `CalendarScreenTest` and `TodayScrollIndexTest` green; month header with previous/next |
| 12 | World + Country | `Android-World`, `Android-Country` | `feature/world/.../WorldScreen.kt`, `CountryDetailScreen.kt` | "Partial" stat does not wrap at 1.3; Country shows "3 of 15 regions visited" from real data, not the placeholder |
| 13 | Settings, People, Legal | `Android-Settings`, `-Users`, `-Legal` | `feature/settings/.../SettingsScreen.kt`, `UsersScreen.kt`, `TermsOfUseScreen.kt`, `PrivacyPolicyScreen.kt` | `SegmentedList` groups; Terms/Privacy as one screen with a tab row |
| 14 | Login | `Android-Login` | `feature/login/.../LoginScreen.kt` | fields keep labels; show-password toggle announced; keyboard never covers Sign in (`imePadding`) |

### Status, 2026-11-23

Tasks 1-14 are done, one commit per screen on `feat/android-expressive`. Task 15: docs and the full
walk done (phone light and dark, 1800x2400 window); `build allTests` and the check script pass; the
minified release builds but was not opened on the Pixel, whose release slot holds the owner's own
signed 0.0.9-alpha. Font scale 2.0 is covered by `MainToolbarTest` only: `font_scale` has no effect
on this Pixel.

### Task 15: Documents and the release

**Files:** Modify `docs/design.md`, `README.md` (screenshots), `composeApp/build.gradle.kts` (versionCode/versionName).

- [x] **Step 1:** `docs/design.md`: point the visual source at `design/native-ui/`, drop `dashboard-redesign.dc.html`, keep the layout traps and form rules, add the traps this phase found.
- [x] **Step 2:** Full walk of every screen in light and dark on the Pixel, plus the 1800×2400 window, plus font scale 2.0 on Home, Places and a form.
- [ ] **Step 3:** `./gradlew build allTests`, `scripts/check-common-compose.sh`, and a minified release APK opened on the device.
- [x] **Step 4:** Commit, open the PR to `develop` with before/after screenshots per screen.
