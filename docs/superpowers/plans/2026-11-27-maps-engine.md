# Map engines Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** One app map composable with a Google Maps and a MapLibre backend, an engine setting with automatic fallback, and a Google-free `fdroid` flavor.

**Architecture:** `feature/ui` owns the API, the MapLibre backend, basemaps and the engine choice. `:feature:map-google` implements `GoogleMapBackend`; only the `play` flavor depends on it and registers it in Koin. Screens call `AppMap` only.

**Tech Stack:** Kotlin 2.4, Compose 1.12, maplibre-compose 0.20.0 (OpenGL ES runtime), maps-compose 2.15.0, Koin 4.2, multiplatform-settings 1.3.0.

**Spec:** `docs/superpowers/specs/2026-11-27-maps-engine-design.md`

## Global Constraints

- No commits, no push, no PR: everything stays uncommitted for Antonio's review.
- `feature/ui`, `feature/map`, `feature/detail`, `feature/locations` must not depend on `play-services-maps`, `maps-compose` or `maps-compose-utils`.
- `fdroidReleaseRuntimeClasspath` has no `com.google.android.gms` / `com.google.maps` artifact.
- Nothing new in `commonMain` (the engine is Android-only); `scripts/check-common-compose.sh` passes.
- Same applicationId and versionCode for both flavors; the Play job builds `bundlePlayRelease`.
- Device checks on the Pixel 6a over USB only; never log Antonio out.

## Review Focus

- A stored `GOOGLE` preference on an `fdroid` install, or on a phone that lost Play Services → MapLibre, no crash (test in Task 1).
- Unknown or null basemap code → the default style, never a blank map (test in Task 2).
- Points on both sides of the antimeridian → `GeoBounds.contains` still right (test in Task 1).
- Theme switch while a map is open → marker images re-render in the new colours (ids carry the theme; checked on device).
- A minified `fdroid` release → maps still draw (R8 keeps javacpp/maplibre classes; checked on device).

---

### Task 1: Engine choice, geo types and preference

**Files:** Create in `feature/ui/src/androidMain/kotlin/.../feature/ui/map/`: `GeoTypes.kt`, `MapEngine.kt`, `MapEngineStore.kt`. Tests in `feature/ui/src/androidUnitTest/.../map/`: `MapEngineTest.kt`, `GeoBoundsTest.kt`, `MapEngineStoreTest.kt`.

**Produces:**
- `data class GeoPoint(val latitude: Double, val longitude: Double)`; `data class GeoBounds(val south: Double, val west: Double, val north: Double, val east: Double) { fun contains(p: GeoPoint): Boolean; companion object { fun of(points: Collection<GeoPoint>): GeoBounds? } }` (null for an empty list; `west > east` means the box crosses the antimeridian).
- `enum class MapEngine { AUTO, GOOGLE, MAPLIBRE }`; `enum class MapRenderer { GOOGLE, MAPLIBRE }`; `enum class GoogleMapsStatus { NOT_INSTALLED, UNAVAILABLE, AVAILABLE }`; `fun resolveMapRenderer(preference: MapEngine, google: GoogleMapsStatus): MapRenderer`.
- `class MapEngineStore(settings: Settings) { val engine: StateFlow<MapEngine>; fun set(engine: MapEngine) }`, key `map_engine`, unknown value → `AUTO`.

- [ ] Tests: `auto uses Google only when available`, `stored Google falls back without Play Services`, `stored Google on fdroid falls back`, `MapLibre is always MapLibre`; `contains` inside/outside, across the antimeridian, `of(empty) == null`; store default `AUTO`, persists, garbage → `AUTO`.
- [ ] Run `./gradlew :feature:ui:testDebugUnitTest --tests '*map*'` → FAIL (unresolved), implement, → PASS.

### Task 2: Basemaps for MapLibre

**Files:** Create `feature/ui/.../map/maplibre/Basemaps.kt`; test `Basemaps Test` in `androidUnitTest/.../map/maplibre/BasemapsTest.kt`.

**Produces:** `sealed interface Basemap { data class StyleUrl(val url: String); data class Raster(val tiles: List<String>, val attribution: String, val underlay: List<String>? = null) }`, `fun basemapFor(code: String?, dark: Boolean): Basemap`, `fun Basemap.Raster.toStyleJson(): String`.

Values: `default` → `https://tiles.openfreemap.org/styles/liberty` / `.../dark`; `openfreemap-liberty` → liberty; `carto-light`, `carto-positron`, `carto-positron-labels` → `.../positron`; `carto-dark` → `.../dark`; `carto-voyager`, `terrain-3d` → liberty; `satellite-terrain-3d` → Esri World_Imagery raster; `topo-terrain-3d` → Esri World_Topo_Map raster; the rest exactly the web's URLs, `{s}` expanded to a/b/c, `satellite-labels` = Boundaries_and_Places over World_Imagery.

- [ ] Tests: default light/dark URLs; every `carto-*` code is a StyleUrl on `tiles.openfreemap.org`; `osm-france` has three tiles without `{s}`; `satellite-labels` has an underlay; null/unknown code == default; every code in `MapStyles.options` resolves to something that does not contain `cartocdn`.
- [ ] Run → FAIL, implement, → PASS.

### Task 3: `AppMap` and the MapLibre backend

**Files:** Create in `feature/ui/.../map/`: `AppMap.kt` (API + dispatch), `AppMapCamera.kt`, `GoogleMapBackend.kt` (interface), `MarkerBitmaps.kt`, `maplibre/MapLibreMap.kt`, `maplibre/MapLibreSetup.kt`. Modify `gradle/libs.versions.toml`, `feature/ui/build.gradle.kts`.

**Produces:**
- `data class MapMarker(val key: String, val position: GeoPoint, val title: String? = null, val snippet: String? = null, val iconKey: Any = key, val onClick: (() -> Unit)? = null, val content: (@Composable () -> Unit)? = null)` (null content = the engine's default pin).
- `data class MarkerClusters(val items: List<MapMarker>, val clusterContent: @Composable (count: Int) -> Unit)`; `data class MapPolyline(val points: List<GeoPoint>, val color: Color, val width: Dp)`.
- `@Stable class AppMapCamera { suspend fun fit(bounds: GeoBounds, padding: Dp, animate: Boolean = true); suspend fun moveTo(center: GeoPoint, zoom: Float, animate: Boolean = true) }`, `@Composable fun rememberAppMapCamera(center: GeoPoint, zoom: Float): AppMapCamera`.
- `data class AppMapSpec(...)` carrying every `AppMap` parameter; `interface GoogleMapBackend { fun isAvailable(context: Context): Boolean; @Composable fun Map(spec: AppMapSpec, modifier: Modifier) }`.
- `@Composable fun AppMap(modifier, camera, markers = emptyList(), clusters: MarkerClusters? = null, polylines = emptyList(), contentPadding = PaddingValues(), onMapClick: ((GeoPoint) -> Unit)? = null, onVisibleBoundsChanged: ((GeoBounds) -> Unit)? = null, myLocation = false, zoomControls = false)`.
- `@Composable fun rememberMapRenderer(): MapRenderer` (store + Koin `GoogleMapBackend` + cached availability).

- [ ] Compile `./gradlew :feature:ui:compileDebugKotlinAndroid` → OK.

### Task 4: `:feature:map-google`

**Files:** Create `feature/map-google/build.gradle.kts`, `.../feature/mapgoogle/GoogleMapsBackend.kt`, `GoogleMapsAvailability.kt`, `GoogleMapContent.kt` (maps-compose rendering moved from today's screens, plus `MapThemeProperties`/`GoogleMapType`), test `GoogleMapsAvailabilityTest.kt`. Modify `settings.gradle.kts`.

**Produces:** `class GoogleMapsBackend : GoogleMapBackend`; `class GoogleMapsAvailability(initialize: (Context) -> Int) { fun isAvailable(context: Context): Boolean }` calling `initialize` once per instance (process singleton in the backend).

- [ ] Tests: `checks once and caches`, `non-success status is unavailable`. FAIL → implement → PASS.

### Task 5: Flavors in `composeApp`

**Files:** Modify `composeApp/build.gradle.kts`, `composeApp/src/androidMain/AndroidManifest.xml`, `AdventureLogApplication.kt`, `di/AndroidUiModule.kt` (registers `MapEngineStore`). Create `composeApp/src/androidPlay/AndroidManifest.xml` (API key), `src/androidPlay/kotlin/.../di/FlavorModule.kt` (`val flavorModule = module { single<GoogleMapBackend> { GoogleMapsBackend() } }`), `src/androidFdroid/kotlin/.../di/FlavorModule.kt` (`val flavorModule = module { }`).

`fdroid`: `ndk.abiFilters` = arm64-v8a, armeabi-v7a, x86_64; native libs compressed (`androidComponents.onVariants` for fdroid sets `packaging.jniLibs.useLegacyPackaging`).

- [ ] `./gradlew :composeApp:assemblePlayDebug :composeApp:assembleFdroidDebug` → OK; `./gradlew :composeApp:dependencies --configuration fdroidReleaseRuntimeClasspath | grep -E 'com.google.android.gms|com.google.maps'` → no output.

### Task 6: Screens onto `AppMap`

**Files:** Rewrite `feature/map/.../components/AdventureMapView.kt`, adapt `MapContent.kt`, `MapScreen.kt` (`GeoBounds`); `feature/detail/.../components/MapView.kt`; `feature/locations/.../LocationMapSection.kt`; `feature/ui/.../components/MapView.android.kt`. Delete `feature/ui/.../map/GoogleMapType.kt`, `MapThemeProperties.kt` (moved to the Google module). Remove the Google deps from the four build files.

- [ ] `./gradlew :feature:map:compileDebugKotlinAndroid :feature:detail:... :feature:locations:... :feature:collections:...` → OK; `grep -rn 'com.google' feature/{ui,map,detail,locations,collections}/src` → nothing.

### Task 7: Settings row and copy

**Files:** Modify `feature/settings/.../SettingsChoiceSheet.kt` (`ChoiceEntry.enabled`), `AppearanceGroup.kt` (+ `MapEngineRow`), `PreferencesGroup.kt` (map style note), `core/model/.../MapStyle.kt` KDoc, `docs/design.md` (Google-only trap line).

- [ ] Build; check on device in Task 9.

### Task 8: CI, README, scripts

**Files:** `.github/workflows/ci.yml` (`assemblePlayDebug`, `assemblePlayRelease`, new `assembleFdroidRelease`, release job `assemblePlayRelease bundlePlayRelease` and the new output paths), `README.md` commands, `scripts/release/create_signing_secrets.sh` (AAB path).

### Task 9: Verify

- [ ] `./gradlew allTests`, `scripts/check-common-compose.sh`, `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.
- [ ] `assemblePlayRelease assembleFdroidRelease`; dependency check above; APK sizes.
- [ ] Pixel 6a USB: the four maps with Google and with MapLibre (setting), `fdroid` debug, a minified `fdroid` build under a separate suffix; light and dark; open every screenshot and send them.
