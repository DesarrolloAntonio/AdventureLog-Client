# Map engines: Google Maps + MapLibre, and a Google-free `fdroid` flavor

Approved by Antonio on 2026-11-27 (proposal in the session that started this work).

## Why

- A GrapheneOS user without Play Services crashes on every map: the Maps SDK never initialises and
  `CameraUpdateFactory` throws from the camera effects.
- Issue #1 asks for F-Droid, whose policy forbids non-free binaries; it wants a FLOSS build flavor.
- MapLibre can draw the account's own basemap, which Google cannot.

## What

- **Engines.** Google Maps (today's) and MapLibre, behind one app map composable. Screens never see
  an engine.
- **Choice.** Settings › Appearance › "Map engine": Auto (default) / Google Maps / MapLibre.
  Auto = Google when its backend is installed and `MapsInitializer.initialize(context)` returned
  SUCCESS (checked once per process, cached), otherwise MapLibre. A stored "Google" falls back the
  same way. Without Play Services the Google entry is disabled with a note; in `fdroid` the row
  is hidden. Existing users see no change.
- **Flavors** (dimension `distribution`, `composeApp` only, same applicationId and versionCode):
  `play` = both engines + the selector; `fdroid` = MapLibre only, no `com.google.android.gms` or
  `com.google.maps` artifact on its runtime classpath.

## How

- **Library:** `org.maplibre.compose:maplibre-compose` 0.20.0 with
  `maplibre-compose-runtime-opengl-android` 0.20.0, pinned. It forces kotlin-stdlib 2.4.20,
  lifecycle 2.11, coroutines 1.11 and Compose 1.12.1 on Android. Fallback if it misbehaves:
  `org.maplibre.gl:android-sdk:13.8.0` in an `AndroidView`, invisible outside the component.
- **Component** (`feature/ui` androidMain, package `feature.ui.map`): `AppMap(...)` with engine-neutral
  `GeoPoint`/`GeoBounds`, an `AppMapCamera` (fit bounds / centre+zoom, animated), composable
  markers, a clustered marker set, polylines, content padding, tap-to-pick, a visible-bounds
  callback (the view's corners less the start padding, as today), dark styling and my-location.
  - Google backend: maps-compose as today (`MarkerComposable`, `Clustering`, night style).
  - MapLibre backend: markers are composables rendered to bitmaps and put into the style before
    their points join the source (an icon that arrives after its tile was laid out stays blank on a
    still map); clusters come from a clustered GeoJSON source and their bubbles are the same
    composable, rendered per count when the map asks (`missingImageResolver`). Google's info
    windows become a label placed over the tapped marker.
- **Modules:** MapLibre backend in `feature/ui`; Google backend in a new `:feature:map-google`
  (only `play` depends on it, registered by a Koin module in `composeApp/src/androidPlay`; the
  `androidFdroid` module registers nothing). `feature/ui`, `map`, `detail` and `locations` drop the
  Google artifacts. The `API_KEY` meta-data moves to the `play` manifest.
- **Basemaps on MapLibre** (web URLs from `frontend/src/lib/index.ts`): `default` = OpenFreeMap
  Liberty (light) / Dark (dark theme); `carto-*` = their OpenFreeMap equivalent, because CARTO now
  requires an API key and keyless tiles come back watermarked; `*-terrain-3d` = their 2D base
  (MapLibre Native has no 3D terrain); everything else = the web's raster tiles. Explicit picks do
  not follow the theme, as on the web. Google keeps today's `MapRendering` mapping.
- **Requests:** every MapLibre request carries `User-Agent: AdventureLog/<version> (Android)`; the
  OSM tile policy blocks generic agents. Attribution stays visible.
- **Size:** MapLibre adds ~12 MB of native code per ABI (stored uncompressed). `fdroid` compresses
  its native libraries and drops x86.

## Not doing

- No location permission or "my location" button: no screen uses it today; the parameter exists.
- No 3D terrain or hillshade. No fastlane texts, screenshots or fdroiddata submission.
