# 🏕️ Adventure Log

![CI](https://github.com/DesarrolloAntonio/AdventureLog-Client/actions/workflows/ci.yml/badge.svg?branch=main)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-blue.svg)
![KMP](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-purple.svg)

A mobile client for [AdventureLog](https://github.com/seanmorley15/AdventureLog), the self-hosted
travel journal, built with Kotlin Multiplatform: shared logic, Jetpack Compose on Android and
SwiftUI on iOS. Log the places you have been, group them into trips, and see it all on a map,
a calendar and a world of visited countries.

**Android** is the primary platform and is where everything is tested. **iOS** is a SwiftUI app
that is still a placeholder and is not distributed yet.

## 🌟 Features

- **Places**: add, edit and search them, with photos, notes, links, dates and categories
- **Collections**: trips that gather their own places, transport and lodging
- **Map**: everything with coordinates, clustered and filterable
- **Calendar**: everything with a date on it, as an agenda you can scroll and filter
- **World**: countries, regions and cities visited, with statistics
- **Search**: one box for places, collections, cities and countries
- **Sharing**: invite someone with a public profile to a collection, or remove them again
- **Markdown descriptions**: rendered as written on the web
- **Tablets**: navigation rail, adaptive grids and list-detail panes for Places and Collections

## 📲 Install

This is a *client*: it needs a
[self-hosted AdventureLog server](https://github.com/seanmorley15/AdventureLog) to talk to.

<a href="https://play.google.com/store/apps/details?id=com.desarrollodroide.adventurelog"><img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80" alt="Get it on Google Play"/></a>

Get it on [Google Play](https://play.google.com/store/apps/details?id=com.desarrollodroide.adventurelog),
or download the APK from the [latest release](https://github.com/DesarrolloAntonio/AdventureLog-Client/releases/latest).
Android 7.0 and up. Then sign in with your server's address and your account.

Bug reports are welcome as [issues](https://github.com/DesarrolloAntonio/AdventureLog-Client/issues/new):
what you did, what happened instead, and the device and Android version. A screenshot helps.

## 📱 Screenshots

<table>
  <tr>
    <td align="center" width="33%"><img src="docs/screenshots/home.png" width="235" alt="Home"/><br/><sub><b>Home</b><br/>what is coming up, and where you have been</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/places.png" width="235" alt="Places"/><br/><sub><b>Places</b><br/>everywhere you have logged, searchable</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/place_detail.png" width="235" alt="A place"/><br/><sub><b>A place</b><br/>photos, notes, links and dates</sub></td>
  </tr>
  <tr>
    <td align="center" width="33%"><img src="docs/screenshots/collections.png" width="235" alt="Collections"/><br/><sub><b>Collections</b><br/>trips, each gathering its own places</sub></td>
    <td align="center" width="33%"><img src="docs/screenshots/collection_detail.png" width="235" alt="Inside a collection"/><br/><sub><b>Inside a collection</b><br/>places, transport and lodging</sub></td>
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

## 🏗️ Architecture

Clean Architecture split into Gradle modules: `core` holds the domain, data and network layers,
and each screen lives in its own `feature` module. ViewModels expose immutable UI state as
`StateFlow`.

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

UI is per platform: ViewModels and everything below them are shared, every `@Composable` lives in
`androidMain`, and iOS is SwiftUI. See the
[design](docs/superpowers/specs/2026-11-21-native-ui-split-design.md).

**Stack:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with
Material 3 · [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) · [Koin](https://insert-koin.io/) · [Ktor](https://ktor.io/) ·
[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) ·
[Coil](https://coil-kt.github.io/coil/) ·
[Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings) · Coroutines and Flow

## 🚀 Building

**Requirements:** an Android Studio recent enough for AGP 9.2, JDK 17, and Xcode 26 for iOS
(iOS 26 minimum).

```bash
git clone https://github.com/DesarrolloAntonio/AdventureLog-Client.git
cd AdventureLog-Client
```

- **Android**: open the project in Android Studio and run the `composeApp` configuration.
- **iOS**: a placeholder for now. Run `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, then open
  `iosApp/iosApp.xcodeproj` in Xcode and run it.

## 🧪 Tests

- **Unit tests** (`commonTest`, on Android and iOS): models, mappers, use cases, the network and
  data layers, and every feature's ViewModels, using the fakes in `core/testing`. Tests of
  Android-only UI logic live in `androidUnitTest`.
- **Shared-code check**: `scripts/check-common-compose.sh` fails if Compose reaches `commonMain`.
- **UI tests** (`androidInstrumentedTest`, need a device or emulator): screens and shared
  components in `collections`, `home`, `calendar`, `locations` and `ui`.
- **Flows**: one Maestro flow in `.maestro/`.

```bash
./gradlew allTests testDebugUnitTest      # unit tests, every target
./gradlew connectedDebugAndroidTest       # UI tests, with a device attached
```

CI runs the unit tests and builds the app; the UI tests and the Maestro flow run locally.

## ⚠️ Known issues

- **MFA and SSO**: accounts that use them cannot sign in yet.
- **iOS**: the SwiftUI app is a placeholder; the real screens come in a later phase.

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
  :feature:locations --> :core:common
  :feature:locations --> :core:domain
  :feature:locations --> :core:model
  :feature:locations --> :feature:detail
  :feature:locations --> :feature:ui
  :feature:locations --> :core:testing
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
  :feature:detail --> :core:common
  :feature:detail --> :core:domain
  :feature:detail --> :core:model
  :feature:detail --> :feature:ui
  :feature:detail --> :core:testing
  :feature:map --> :core:common
  :feature:map --> :core:domain
  :feature:map --> :core:model
  :feature:map --> :feature:ui
  :feature:map --> :core:data
  :feature:map --> :core:testing
```

## 📄 License

MIT - see [LICENSE](LICENSE).
