# 00 — Setup · qa (emulator-5560)

> Build `13bb122` (debug APK sha256 `2b3ed909…`, Gradle up-to-date for that commit) · device `qa` = emulator-5560 (`Resizable_Experimental`, API 37) · account A `claude` · 2026-09-14/15. Screenshots in `qa-shots/`.

## Inventory

### Stores the app keeps (STORE oracle)

| Store | On-device path (debug) | Holds | Evidence |
|---|---|---|---|
| SharedPreferences (multiplatform-settings `Settings()`) | `shared_prefs/com.desarrollodroide.adventurelog.debug_preferences.xml` | `user_session` (UserDetails JSON **with `sessionToken`**, only when Remember me is on), `remember_user_id`, `remember_username`, **`remember_password` (plain text)**, `remember_url`, `theme_mode`, `use_dynamic_colors`, `compact_view` | `core/data/.../di/DataModule.kt:48-50`, `UserRepositoryImpl.kt:43-52,110-128,143-153`, `SettingsRepositoryImpl.kt:101-166` |
| Coil image cache | `cache/coil3_disk_cache/` (library default, to confirm with `ui.py files`) | images, read-only | `feature/ui/.../di/ImageLoaderModule.kt:94-99` |
| Share/attachment scratch | `cache/attachments/`, `cache/images/JPEG_*.jpg` — never deleted | files handed to the share sheet, camera captures | `AndroidPlatformFiles.kt:43-45,75`, `CameraCapture.android.kt:91-94` |
| — | no SQLite / Room / SQLDelight, no DataStore, no WorkManager | | declared in the catalog, used by no module |

### Session and sign-in

- Login: `POST {server}/auth/browser/v1/auth/login` `{username, password}` with `X-Is-Mobile: true`, `Referer: {server}`; the token is the `sessionid` in `Set-Cookie`; then `GET /auth/user-metadata/` (`KtorAuthApi.kt:37-71`, `LoginRepositoryImpl.kt:36-43`).
- Requests carry `X-Session-Token` (`ktor/KtorConfig.kt:12-18`); no cookies.
- Start: the nav graph always starts at Login; `LoginViewModel.init` runs `InitializeSessionUseCase`, which keeps a stored session unless the server answers 401/403 (`InitializeSessionUseCase.kt:25-74`).
- Logout: clears `user_session` and the in-memory token/URL; keeps the remember-me keys (password included), theme, in-memory repository caches and image caches; calls no server endpoint (`LogoutUseCase.kt:23-48`).

### Harness and adapter

| Piece | State |
|---|---|
| `qa.config.json` (gitignored) | package `com.desarrollodroide.adventurelog.debug`; `files.prefs`; no databases; `server.url` = backend `:3447`; `devices` = `{qa: emulator-5560}` |
| `.gitignore` | `qa.config.json`, `qa.credentials.json`, `qa.tokens.json`, `qa-shots/` added |
| `scripts/qa/adapter.py` | `request(method, path, account)`; login from `qa.credentials.json`, token cached in `qa.tokens.json` (0600); refuses the `:3445` frontend; server quirks recorded in its docstring. **Smoke:** anonymous `GET /auth/is-registration-disabled/` → 200 ✅; as A: `/auth/user-metadata/` → 200 `claude` ✅, `/api/locations/?page_size=1` → count 22 ✅, `/api/collections/` → count 6 ✅ |
| `scripts/qa/inject_session.py` | R11 route 2 (plain prefs file + `run-as`): force-stop, set `user_session` only, push via stdin, read back. **Run on `qa`:** STORE shows `user_session` = `<hidden>` and nothing else; launch → Home "Hi, John!" with "Places visited 10 / 22" (API count 22) ✅ |
| Tests | commonTest in core `domain/model/network` and feature `calendar/collections/detail/locations/map/settings/ui/world`; androidInstrumentedTest in feature `calendar/collections/home/locations/ui/world`; none in `composeApp`, `feature/login`, `core/data`. CI runs `./gradlew test`; docs say `allTests`. No `testTag` anywhere |

## Absence sweeps

Run once for the whole product on `13bb122`, by four read-only sub-agents with the rule's greps and discard lists; each row is filed under its module in [`CAMPAIGN.md` §6](CAMPAIGN.md). Candidates, not findings, until driven.

| Sweep | Method | Raw → kept | Discarded, by reason |
|---|---|---|---|
| **S1** | 594 declarations (376 `core/model` data-class params, 153 UI-state fields, 65 exposed StateFlows) × 159 UI files, `\.\s*<field>\b`, then a manual pass for implicit receivers and name collisions; web compared in `reference/.../frontend/src` | 61 zero-hit → 17 candidates | 55 ids/foreign keys, 20 timestamps, 7 bookkeeping (incl. `sessionToken`); 6 false positives painted through an implicit receiver or derived getter |
| **S2** | `grep -rnE 'onClick *= *\{ *\}|\w+ *= *\{ *\}|/\* *TODO'` (220 after exclusions), `grep -rn TODO` (8), empty lambdas with parameters (24); callers traced for every empty default; every `navigate`/`backStack.add` matched to a destination | 2 candidates + 4 KNOWN TODOs | 101 previews, 107 empty defaults wired by every real caller, 6 read-only `onValueChange`, 1 disabled placeholder, 3 non-controls, 2 parameters never rendered; all navigate targets have destinations |
| **S3** | REF v0.13.0 routes, navbar, user menu, settings sections and action menus enumerated; each feature's endpoint/term grepped in `core`, `feature`, `composeApp` | ~95 features → 41 absent, 9 partial | 8 discarded by written decision (`9bc65f0`: MFA, API keys, Immich, Wanderer, backup restore, admin); two decisions contradicted by the code (ABS-17) |
| **S4** | `grep -rnE 'ing *= *true'` (32) plus `.value = true` / `State.Loading` / `enabled = !…` (57), multi-line catch scan (221 catches, 16 empty or log-only), `Either.Left` scan (69, 5 log-only) | 7 candidates | 23 cleared by one assignment on both paths, 7 not the start of work, 1 dialog closed in the same handler; 36 of the extra flags cleared in both paths or `finally` |

**Blind spots, as the rule says:** S2's greps are first-order — a handler that calls an empty function escapes them (two found by reading: `AdventureDetailViewModel.kt:97,101`, not reachable from the UI). S1's grep misses implicit receivers; the manual pass covered the shell and account area completely, other modules by sample. The single-line S4 catch grep returned **0** on a codebase with 16 empty or log-only catches; the multi-line scan is what found them.

## Findings

| ID | Sev | What actually happens | Where | Status |
|---|---|---|---|---|
| ABS-01 | **P0?** (security) | **Signing in with "Remember me" leaves the account password readable in the app's preferences file, and that file goes into device backups.** The session token that keeps the user signed in is stored in the same file, so the password is not even needed to stay signed in. | `core/data/.../UserRepositoryImpl.kt:115-120`; `composeApp/src/androidMain/AndroidManifest.xml:13` | code-confirmed; STORE check needs a real login with Remember me (queue #7). Raised to the owner at once (R2 stop 1). Carried into 01 |

## What happened

| What | Result |
|---|---|
| §2.1 step 1 — fix mode and commits asked once | ✅ fix severe; commit at each gate |
| §2.1 step 2 — clean tree, branch `qa/2026-09-14` from `13bb122` | ✅ |
| §2.1 step 3 — what exists: server, accounts | ✅ NAS backend, owner says its account is a test one; account `claude`, credentials written by the owner (queue #2 resolved) |
| §2.1 step 5 — device | ❌ my proposal (`Resizable_Experimental`) was the AVD behind the off-limits emulator-5554; the emulator refused a second instance and nothing touched 5554. Queue #1 |
| §2.1 step 5 — device, second round | ✅ owner chose `Resizable_Experimental`; nothing was running; started on port 5560 → `emulator-5560`, API 37. App not installed before (no real session to overwrite) |
| §2.1 steps 6–7 — config, `.gitignore` | ✅ |
| §2.1 step 8 — API adapter | ✅ anonymous and authenticated smoke |
| §2.1 step 9 — absence sweeps | ✅ 33 rows filed |
| §2.1 step 10 — offline depth | ✅ short |
| Harness drives the app and reads its store (gate 00) | ✅ `installed --apk` matches the built file; `launch`, `wait`, `dump` on the first-run login; `files` (empty before any save); `file prefs` with the token hidden; `shot`; `demo on`; `a11y` on Home (2 warnings → ABS-34); `crashes` 0/0; `net.sh status` validated and `reach` → server reachable **from the device** |
| Full repository test suite | ✅ `./gradlew allTests`: **688 results, 0 failures, 0 skipped** (344 tests × Android unit + iOS simulator, 11 modules). Test outputs were deleted and the run repeated; Gradle restored them from its build cache for unchanged inputs, so this is a cached pass of `13bb122`, not a re-execution. `connectedDebugAndroidTest` on `qa` (ANDROID_SERIAL): **32 tests in 6 modules, 0 failures**, executed on Android 17; the app under test survived (library test APKs only) |
| Crashes / hangs of the app | ✅ 0 / 0 |
| `QA_` fixtures | none created |

## Open

- ABS-01 is code-confirmed only. Seeing it in STORE needs a real login with Remember me, which the agent may not type: queue #7, during 01.
- **The adb server was down twice** when I looked, and my `adb devices` restarted it; I did not stop it. A restart of the shared adb server can drop the other campaign's connection for a moment.
- **My mistake:** I first proposed `Resizable_Experimental` as a free device without checking which AVD the off-limits emulator was running; it was that one. The emulator's own refusal caught it before anything touched 5554. The owner later chose that AVD anyway, with nothing running.
- `net.sh status` prints `download speed: 0 bits/s` on an unthrottled emulator (the console's way of saying "no limit"); read literally it looks like no network.
- Sweep severities are provisional and the S1 manual pass was complete only for the shell.
