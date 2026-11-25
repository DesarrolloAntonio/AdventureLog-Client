<div align="center">

<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/banner.png"><img src="docs/readme/light/banner.png" alt="AdventureLog - every trip, in one journal" width="100%"></picture>

<p>
<a href="https://play.google.com/store/apps/details?id=com.desarrollodroide.adventurelog"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/btn-play.png"><img src="docs/readme/light/btn-play.png" alt="Get it on Google Play" height="48"></picture></a>
<a href="https://github.com/DesarrolloAntonio/AdventureLog-Client/releases/latest"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/btn-apk.png"><img src="docs/readme/light/btn-apk.png" alt="Download the APK" height="48"></picture></a>
<a href="https://github.com/seanmorley15/AdventureLog"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/btn-server.png"><img src="docs/readme/light/btn-server.png" alt="Host the AdventureLog server" height="48"></picture></a>
</p>

<p>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/badge-selfhosted.png"><img src="docs/readme/light/badge-selfhosted.png" alt="Self-hosted" height="26"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/badge-kmp.png"><img src="docs/readme/light/badge-kmp.png" alt="Kotlin Multiplatform" height="26"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/badge-m3e.png"><img src="docs/readme/light/badge-m3e.png" alt="Material 3 Expressive" height="26"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/badge-android.png"><img src="docs/readme/light/badge-android.png" alt="Android 7.0 and up" height="26"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/badge-ios.png"><img src="docs/readme/light/badge-ios.png" alt="iOS under construction" height="26"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/badge-servers.png"><img src="docs/readme/light/badge-servers.png" alt="AdventureLog servers 0.13 and 0.14" height="26"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/badge-mit.png"><img src="docs/readme/light/badge-mit.png" alt="MIT licence" height="26"></picture>
</p>

[![CI](https://github.com/DesarrolloAntonio/AdventureLog-Client/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/DesarrolloAntonio/AdventureLog-Client/actions/workflows/ci.yml)

</div>

A mobile client for [AdventureLog](https://github.com/seanmorley15/AdventureLog), the self-hosted travel journal. Log the places you have
been, group them into trips, and see it all on a map, a calendar and a world of visited countries -
on your own server.

The logic is shared through Kotlin Multiplatform; the interface is not. **Android** is Jetpack
Compose in Material 3 Expressive, on phones and tablets, and is what is released. **iOS** is a
SwiftUI app in Liquid Glass, under construction and not distributed yet.

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-screenshots.png"><img src="docs/readme/light/h-screenshots.png" alt="Screenshots" width="100%"></picture>

<table>
  <tr>
    <td align="center" width="33%"><img src="docs/screenshots/home.png" width="235" alt="Home"/><br/><sub><b>Home</b><br/>what is coming up, and where you have been</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/places.png" width="235" alt="Places"/><br/><sub><b>Places</b><br/>everywhere you have logged, searchable</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/place_detail.png" width="235" alt="A place"/><br/><sub><b>A place</b><br/>photos, notes, links and dates</sub></td>
  </tr>
  <tr>
    <td align="center" width="33%"><img src="docs/screenshots/collections.png" width="235" alt="Collections"/><br/><sub><b>Collections</b><br/>trips, each gathering its own places</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/collection_detail.png" width="235" alt="Inside a collection"/><br/><sub><b>Inside a collection</b><br/>places, transport, lodging and a daily plan</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/calendar.png" width="235" alt="Calendar"/><br/><sub><b>Calendar</b><br/>the journal by date</sub></td>
  </tr>
  <tr>
    <td align="center" width="33%"><img src="docs/screenshots/map.png" width="235" alt="Map"/><br/><sub><b>Map</b><br/>everything with coordinates, clustered</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/world.png" width="235" alt="World"/><br/><sub><b>World</b><br/>countries, regions and cities visited</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/search.png" width="235" alt="Search"/><br/><sub><b>Search</b><br/>one box for places, trips and regions</sub></td>
  </tr>
</table>

<sub>Android, on a Pixel 6a, light and dark theme. Photos come from Wikimedia Commons through the
server's own image search.</sub>

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-ios.png"><img src="docs/readme/light/h-ios.png" alt="iOS - under construction" width="100%"></picture>

The iOS app is being built in SwiftUI with Liquid Glass (iOS 26). It shares the ViewModels and
everything below them with Android, and draws its own screens. It is not distributed yet;
screenshots will appear here when it is.

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-features.png"><img src="docs/readme/light/h-features.png" alt="Features" width="100%"></picture>

<div align="center">
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/card-places.png"><img src="docs/readme/light/card-places.png" alt="Places" width="49%"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/card-collections.png"><img src="docs/readme/light/card-collections.png" alt="Collections" width="49%"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/card-map.png"><img src="docs/readme/light/card-map.png" alt="Map and calendar" width="49%"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/card-world.png"><img src="docs/readme/light/card-world.png" alt="World" width="49%"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/card-search.png"><img src="docs/readme/light/card-search.png" alt="Search" width="49%"></picture>
<picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/card-tablets.png"><img src="docs/readme/light/card-tablets.png" alt="Phones and tablets" width="49%"></picture>
</div>

<details>
<summary><b>Everything it does</b></summary>
<br>

- **Places**: add, edit and search them, with photos, notes, links, dates, ratings and categories
- **Collections**: trips that gather their own places, transport, lodging, notes and checklists
- **Itinerary**: a day-by-day plan inside each collection
- **Map**: everything with coordinates, clustered and filterable; on a tablet, the places in view beside it
- **Calendar**: everything with a date on it, as an agenda or a month
- **World**: countries, regions and cities visited, with statistics and a stamp per country
- **Search**: one box for places, collections, cities and countries
- **Sharing**: invite someone with a public profile to a collection, or remove them again
- **Markdown descriptions**: rendered as written on the web
- **Tablets**: navigation rail, adaptive grids, and list and detail side by side in Places, Collections and World
- **Sign-in**: password, two-step verification, and the server's providers (OIDC, GitHub...) on 0.14
- **Servers**: AdventureLog 0.13 and 0.14

</details>

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-install.png"><img src="docs/readme/light/h-install.png" alt="Install" width="100%"></picture>

This is a *client*: it needs a [self-hosted AdventureLog server](https://github.com/seanmorley15/AdventureLog) to talk to.

Get it on [Google Play](https://play.google.com/store/apps/details?id=com.desarrollodroide.adventurelog), or download the APK from the [latest release](https://github.com/DesarrolloAntonio/AdventureLog-Client/releases/latest).
Android 7.0 and up. Then sign in with your server's address and your account.

Bug reports are welcome as [issues](https://github.com/DesarrolloAntonio/AdventureLog-Client/issues/new):
what you did, what happened instead, and the device and Android version. A screenshot helps.

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-architecture.png"><img src="docs/readme/light/h-architecture.png" alt="Architecture" width="100%"></picture>

Clean Architecture split into Gradle modules: `core` holds the domain, data and network layers,
and each screen lives in its own `feature` module. ViewModels expose immutable UI state as
`StateFlow`.

UI is per platform: ViewModels and everything below them are shared, every `@Composable` lives in
`androidMain`, and iOS is SwiftUI, which sees the shared code through [SKIE](https://skie.touchlab.co/).
See the [design](docs/superpowers/specs/2026-11-21-native-ui-split-design.md).

**Stack:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
Expressive · [Compose Navigation](https://developer.android.com/guide/navigation/design), with
[Navigation 3](https://developer.android.com/guide/navigation/navigation-3) for the list-detail panes ·
[Koin](https://insert-koin.io/) · [Ktor](https://ktor.io/) (OkHttp on Android, Darwin on iOS) ·
[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) ·
[Coil](https://coil-kt.github.io/coil/) ·
[Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings) · Coroutines and Flow ·
SwiftUI on iOS

<details>
<summary><b>Project layout</b></summary>
<br>

```
AdventureLog/
├── composeApp/           # Android app and iOS framework, app-wide navigation and DI
├── iosApp/               # SwiftUI app (Xcode project)
├── build-logic/          # Gradle convention plugins
├── core/
│   ├── common/           # Utilities and extensions
│   ├── data/             # Repository implementations
│   ├── domain/           # Use cases and repository interfaces
│   ├── model/            # Domain models
│   ├── network/          # Ktor client and API DTOs
│   ├── permissions/      # Runtime permissions
│   ├── resources/        # Fonts and strings (Android only)
│   └── testing/          # Hand-written fakes shared by the tests
└── feature/
    ├── calendar/         # Dated events as an agenda
    ├── collections/      # Collections and what is inside them
    ├── detail/           # A place's own page
    ├── home/             # App shell, navigation bar and dashboard
    ├── locations/        # The places list, and adding or editing one
    ├── login/            # Sign-in
    ├── map/              # Map
    ├── settings/         # Account and app settings
    ├── ui/               # Shared Android UI components and theme
    └── world/            # Countries, regions and cities
```

</details>

### Module Graph

```mermaid
%%{
  init: {
    'theme': 'neutral'
  }
}%%

graph LR
  :feature:calendar --> :core:common
  :feature:calendar --> :core:domain
  :feature:calendar --> :core:model
  :feature:calendar --> :feature:ui
  :feature:calendar --> :core:testing
  :feature:login --> :core:common
  :feature:login --> :core:domain
  :feature:login --> :core:model
  :feature:login --> :feature:home
  :feature:login --> :feature:ui
  :feature:login --> :core:testing
  :feature:collections --> :core:common
  :feature:collections --> :core:domain
  :feature:collections --> :core:model
  :feature:collections --> :feature:detail
  :feature:collections --> :feature:map
  :feature:collections --> :feature:ui
  :feature:collections --> :core:testing
  :feature:locations --> :core:testing
  :feature:locations --> :core:model
  :feature:locations --> :core:common
  :feature:locations --> :core:domain
  :feature:locations --> :feature:detail
  :feature:locations --> :feature:ui
  :feature:settings --> :core:common
  :feature:settings --> :core:domain
  :feature:settings --> :core:model
  :feature:settings --> :feature:ui
  :feature:settings --> :core:testing
  :composeApp --> :core:resources
  :composeApp --> :feature:login
  :composeApp --> :feature:home
  :composeApp --> :feature:locations
  :composeApp --> :feature:settings
  :composeApp --> :feature:detail
  :composeApp --> :feature:ui
  :composeApp --> :feature:calendar
  :composeApp --> :feature:collections
  :composeApp --> :feature:world
  :composeApp --> :feature:map
  :composeApp --> :core:model
  :composeApp --> :core:domain
  :composeApp --> :core:data
  :core:network --> :core:model
  :feature:home --> :core:testing
  :feature:home --> :core:resources
  :feature:home --> :core:common
  :feature:home --> :core:domain
  :feature:home --> :core:model
  :feature:home --> :feature:settings
  :feature:home --> :feature:locations
  :feature:home --> :feature:ui
  :feature:home --> :feature:calendar
  :feature:home --> :feature:collections
  :feature:home --> :feature:world
  :feature:home --> :feature:map
  :feature:home --> :feature:detail
  :core:testing --> :core:common
  :core:testing --> :core:model
  :core:testing --> :core:domain
  :core:testing --> :core:network
  :core:data --> :core:common
  :core:data --> :core:domain
  :core:data --> :core:network
  :core:data --> :core:model
  :core:data --> :core:testing
  :feature:world --> :core:common
  :feature:world --> :core:domain
  :feature:world --> :core:model
  :feature:world --> :feature:ui
  :feature:world --> :core:testing
  :core:domain --> :core:common
  :core:domain --> :core:network
  :core:domain --> :core:model
  :core:domain --> :core:testing
  :feature:ui --> :core:common
  :feature:ui --> :core:domain
  :feature:ui --> :core:model
  :feature:detail --> :core:testing
  :feature:detail --> :core:model
  :feature:detail --> :core:common
  :feature:detail --> :core:domain
  :feature:detail --> :feature:ui
  :feature:map --> :core:testing
  :feature:map --> :core:model
  :feature:map --> :core:common
  :feature:map --> :core:domain
  :feature:map --> :feature:ui
  :feature:map --> :core:data
```
### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-build.png"><img src="docs/readme/light/h-build.png" alt="Build from source" width="100%"></picture>

**Requirements:** an Android Studio recent enough for AGP 9.2, JDK 17, and Xcode 26 for iOS
(iOS 26 minimum).

```bash
git clone https://github.com/DesarrolloAntonio/AdventureLog-Client.git
cd AdventureLog-Client
./gradlew :composeApp:assembleDebug
```

- **Android**: open the project in Android Studio and run the `composeApp` configuration.
- **iOS**: run `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, then open
  `iosApp/iosApp.xcodeproj` in Xcode and run it. The SwiftUI screens are still under construction.

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-tests.png"><img src="docs/readme/light/h-tests.png" alt="Tests" width="100%"></picture>

- **Unit tests** (`commonTest`, on Android and iOS): models, mappers, use cases, the network and
  data layers, and every feature's ViewModels, using the fakes in `core/testing`. Tests of
  Android-only UI logic live in `androidUnitTest`.
- **Shared-code check**: `scripts/check-common-compose.sh` fails if Compose reaches `commonMain`.
- **UI tests** (`androidInstrumentedTest`, need a device or emulator): screens and shared
  components in every feature module - `calendar`, `collections`, `detail`, `home`, `locations`,
  `login`, `map`, `settings`, `ui` and `world`.
- **Flows**: one Maestro flow in `.maestro/`.

```bash
./gradlew allTests testDebugUnitTest      # unit tests, every target
./gradlew connectedDebugAndroidTest       # UI tests, with a device attached
```

CI runs the unit tests, compiles the iOS framework and builds the app; the UI tests and the Maestro
flow run locally.

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-issues.png"><img src="docs/readme/light/h-issues.png" alt="Known issues" width="100%"></picture>

- **Sign-in providers** (OIDC, GitHub...) need server 0.14: 0.13 has no way for an app to use them.
  The server must also list `adventurelog://auth/callback` in `MOBILE_AUTH_REDIRECT_URIS`, which
  it does unless that setting was changed.
- **iOS**: the SwiftUI app is under construction and not distributed yet.

### <picture><source media="(prefers-color-scheme: dark)" srcset="docs/readme/dark/h-license.png"><img src="docs/readme/light/h-license.png" alt="License" width="100%"></picture>

MIT - see [LICENSE](LICENSE).
