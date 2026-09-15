# 01 — Shell · qa (Resizable_Experimental, API 37, 1080×2400 @ 420)

> Driven on build `13bb122` (debug APK sha256 `2b3ed909…`); fixes verified on the working tree built 2026-09-15 10:08 (sha256 `d3521217…`) · device `qa` = emulator-5560 · account A (`claude`) · 2026-09-15. Screenshots in `qa-shots/`.

## Inventory

Derived from the UI code and the manifest on 2026-09-15, build `13bb122` (R3). Paths are shortened to
the file name; the full paths are in `00-setup.md` and the modules `feature/login`, `feature/home`
(shell), `feature/settings`, `composeApp`. **Suspects** are unproven until driven.

### Launch and start destination — `AdventureLogApplication.kt`, `MainActivity.kt`, `App.kt`, `AdventurelogNavGraph.kt`

| # | Control | Kind | What it should do | Oracles | Suspect? |
|---|---|---|---|---|---|
| L1 | cold start, no session | launch | Login form | UI + STORE (no prefs) | blank while `Loading` (LoginScreen.kt:90-92) |
| L2 | cold start, stored session | launch | Login VM validates it (`/auth/user-metadata/`) → Home | UI + STORE + API | |
| L3 | cold start, stored session, server says 401 | launch | session cleared → Login form | UI + STORE | |
| L4 | cold start, stored session, offline | launch | Home with "No internet connection." | UI | |
| L5 | process death on Home, relaunch | R9 | Home restored and still talking to the server | UI + API | **S1**: base URL only set in LoginViewModel |

### Login — `LoginScreen.kt` and `ui/components/*`

| # | Control | Kind | What it should do | Oracles | Suspect? |
|---|---|---|---|---|---|
| 1 | Server URL | field (URL keyboard, Next) | live "Invalid server url" unless http/https + host | UI | placeholder only, no label; `onClick = { }` on focus loss |
| 2 | User | field (email keyboard, Next) | "Invalid username" when blank | UI | not trimmed; icon unnamed |
| 3 | Password | field (Done) | "Required" when blank | UI | Done does not submit |
| 4 | Eye "Toggle Password Visibility" | icon button | show/hide | UI + EYE | label never changes |
| 5 | Login | button | validate → POST login → Home; errors in a snackbar | UI + STORE + API | blank screen while loading, no timeout |
| 6 | Remember me | checkbox | persist session across restarts | UI + STORE | **S2**: password stored in plain text; unchecking wipes saved credentials at once; label not connected to the box |
| 7 | "View instructions guide in website" | link | opens GitHub | UI (intent) | |
| 8 | snackbar ✕ | action | dismiss | UI | |
| 9 | system back | system | closes the app | UI | |

Error mapping to drive: wrong password → "Invalid username or password"; unreachable/TLS → "Network unavailable"; 404/5xx/HTML → "Error getting user credentials, try again later"; frontend `:3445` → the "no session" explanation; invalid URL → inline.

### Main scaffold — `MainShell.kt`, `HomeBottomBar.kt`, `CurrentScreen.kt`

| # | Control | Kind | What it should do | Oracles | Suspect? |
|---|---|---|---|---|---|
| 10 | Home / Places / Collections / Map / World | nav items | switch tab, keep each tab's state | UI + EYE | icons unnamed (text label present) |
| 11 | title / "N places visited · M trips ahead" | text | per screen | UI + API | Settings/People/Calendar highlight no tab |
| 12 | breadcrumb chevron "Back", Home icon | clickable icons (inner collection route) | up / home | UI | below 48 dp, no button role |
| 13 | Add place (≥1000 dp app bar only) | button | add place | UI | |
| 14 | Search "Search everything" | button | global search sheet | UI | |
| 15 | avatar | icon button | account sheet | UI | no name when there is no photo |
| 16 | app bar collapse | gesture | collapses on scroll | EYE | one scroll state for every tab |

### Global search — `GlobalSearchSheet.kt`, `GlobalSearchViewModel.kt`

| # | Control | Kind | What it should do | Oracles | Suspect? |
|---|---|---|---|---|---|
| 17 | "Search everything" | field | ≥2 chars, 300 ms debounce, `/api/search/` | UI + API | reopens with the old query |
| 18 | ✕ "Clear the search" | icon button | clear | UI | |
| 19 | place / collection result | row | opens detail | UI | collection title in the route, not encoded |
| 20 | other results | row | not clickable (deliberate) | UI | |
| 21 | scrim / drag / back | gesture | close | UI | |

### Account sheet — `ProfileMenu.kt`

| # | Control | Kind | What it should do | Oracles | Suspect? |
|---|---|---|---|---|---|
| 22 | account card "Open settings" | card | Settings | UI | |
| 23 | Calendar | row | Calendar | UI | |
| 24 | People | row | People | UI | no `launchSingleTop` |
| 25 | Settings | row | Settings | UI | |
| 26 | Server {url} | info | shows the backend | UI + STORE | |
| 27 | Sign out | row | logout, no confirmation | UI + STORE + API | inconsistent with Settings' confirm |

### Settings — `SettingsScreen.kt` and components

| # | Control | Kind | What it should do | Oracles | Suspect? |
|---|---|---|---|---|---|
| 28 | account header "Edit your name and username" | card → dialog | edit profile | UI + API | dialog closes before saving; refusal loses the typed name |
| 29 | Public profile | switch | PATCH `public_profile` | UI + API | ⚠️ exposes data — only toggled and restored with the owner's OK |
| 30 | Units / Currency / Map style | row → sheet | PATCH the field | UI + API | endless re-save if the server normalises |
| 31 | Theme (system / light / dark) | row → sheet | local pref | UI + STORE + EYE | status/nav bar icons |
| 32 | Dynamic colours | switch | local pref | UI + STORE + EYE | initial state `true` vs stored default false |
| 33 | Change password | row → dialog | ⚠️ real credential change — **not driven** (R10 side effect) | — | |
| 34 | Email addresses (retry, ⋮ verify / primary / remove, add) | rows, menu, dialog | ⚠️ sends real email — **not driven** beyond reading the list | UI + API | |
| 35 | Update visited regions | row | refresh, snackbar | UI + API | shares a message slot with backup |
| 36 | Download a backup | row | `/api/backup/export/` → share sheet | UI + STORE (`cache/attachments`) | silent with empty server URL |
| 37 | Media storage Retry | text button | reload | UI | |
| 38 | Server / Account / App version | info | backend, username, version | UI + STORE + API | |
| 39 | Server setup guide / Source code | rows | browser | UI (intent) | crash without a browser |
| 40 | Send feedback | row | mailto | UI (intent) | Spanish hard-coded subject |
| 41 | Terms of use / Privacy policy | full-screen dialogs | read, back | UI + EYE | **Shiori's** terms; "[Insert Date Here]" |
| 42 | Sign out card | row → "Sign out?" dialog | logout | UI + STORE + API | wording about the password |

### People — `UsersScreen.kt`

| # | Control | Kind | What it should do | Oracles | Suspect? |
|---|---|---|---|---|---|
| 43 | Try again | text button | reload `/auth/users/` | UI + API | |
| 44 | Search people (>8 users) | field | local filter | UI | empty result shows no message |
| 45 | user cards | display | — | UI + API | |

### Entry points that are not controls

| # | Entry point | Declared in | Lands on | Oracles |
|---|---|---|---|---|
| E1 | MAIN / LAUNCHER | `AndroidManifest.xml:31-35` | Login → (session) Home | UI |
| — | no deep links, share targets, notifications, widgets, shortcuts, services or receivers | `AndroidManifest.xml` (the only manifest) | — | — |

### UI state fields

| Field | Painted in | S1? |
|---|---|---|
| `LoginViewModel.uiState` Loading | nowhere (blank) | S4, not S1 |
| `LoginViewModel.uiState` Error / Empty / Success | snackbar / form / navigation | |
| `LoginViewModel.loginFormState.*` + error flags | fields, inline errors | |
| `HomeViewModel.userDetails` | title, avatar, account sheet | |
| `HomeUiState.Success.userName` | nowhere (dead duplicate) | yes — dead |
| `GlobalSearchViewModel.query/hits/isSearching/error` | sheet (spinner only with no hits) | |
| `SettingsViewModel.compactView` | nowhere | **yes (ABS-10)** |
| `SettingsViewModel.user.disablePassword` | nowhere | **yes (ABS-18)** |
| `SettingsViewModel.profile.saved` | comparison only | no (bookkeeping) |
| everything else in Settings / Users | its row, dialog or snackbar | |
| plain `remember`: `currentScreen`, `searchOpen`, sheet `open`, Settings dialog flags and dialog field values | lost on process death | R9 |

### Suspects carried out of this inventory

| # | What | Why it looks wrong |
|---|---|---|
| S1 | process death on Home → nothing reaches the server | the base URL and token are set only by LoginViewModel (ABS-02) |
| S2 | Remember me stores the password in plain text, and it is backed up | ABS-01 |
| S3 | route arguments not encoded | a `/` in a collection name should break navigation (ABS-04) |
| S4 | logout keeps remember-me, in-memory caches, and the server session | ABS-07, ABS-08 |
| S5 | blank login while loading, no HTTP timeout | ABS-03 |
| S6 | search reopens stale, People stacks duplicates | ABS-11, ABS-12 |
| S7 | status/nav bar icon colours and login background ignore the app theme | ABS-13 |
| S8 | Terms and privacy text | ABS-06 |

## Findings

Severity per SKILL.md §2.3. "Seen red" = the fix reverted by one change, the test failing on its own assertion with the message shown, then restored and green.

| ID | Sev | What actually happens | Where | Status |
|---|---|---|---|---|
| SH-01 (ABS-02) | **P1** | **When Android kills the app in the background on Home, every screen fails when the user comes back, although the session is still valid.** Home says "Could not load your dashboard", Places prints "Base URL is not initialized, login must be called first", World "Error loading countries"; "Try again" never works. The server and token lived only in memory and only the login screen set them, while Android restores the task straight onto Home. UI + logcat + API (the same token answered 200) | `core/data/.../UserRepositoryImpl.kt` (load), `core/data/.../di/DataModule.kt` (`createdAtStart`) | ✅ **fixed and verified in the running system**: `ui.py kill` on Home → relaunch → dashboard and Places load; `kill` on a place's page → restored onto that page with its data, back → Home. 2 tests (`UserRepositoryImplTest`), seen red: *expected (https://qa.test, token-1) but was null*. Negative pair yes (*no stored session → network left alone*, red on an unconditional init) |
| SH-02 (ABS-04) | **P1** | **Opening a collection whose name contains "/" closes the app.** The name went into the route unencoded, `collection/<id>/QA_Route/Slash` matched no destination and `navigate` threw `IllegalArgumentException` (crash buffer, `MainShell.kt:244`). The same builders put note/lodging/checklist/place JSON in query strings, where `&`, `#` or `%` in user text end the argument | `core/common/.../navigation/NavigationRoutes.kt` (`routeArg`) | ✅ **fixed and verified**: the `QA_Route/Slash` fixture opens from global search, breadcrumb reads "QA_Route/Slash", 0 crashes; Edit place still opens through the encoded JSON route with name and link filled, nothing written (API `updated_at` unchanged). 4 tests (`NavigationRoutesTest`), seen red: *expected [collection, c56ae212, Madrid%2FBarcelona] but was [collection, c56ae212, Madrid, Barcelona]* and *a # would cut the route into a fragment*. Negative pair yes (plain values untouched) |
| SH-03 (ABS-05) | **P1** | **When the session ends while the app is open (expired, or signed out on the web), screens tell the user their data is gone.** After revoking the session on the server: Places "No places yet – Tap + to add the first one" over 22 places, Collections "Failed to fetch collections with status: 400 Bad Request" and "0 collections", World "Session expired" with a Try Again that can't work. The server answers an anonymous caller differently per endpoint (200 empty / 400 / 401), and only the startup check ever ended a session | `core/network/.../KtorAdventureLogNetwork.kt` (401 interceptor), `core/domain/.../EndRejectedSessionsUseCase.kt`, `feature/home/.../HomeViewModel.kt` (`signedOut`, `recheckSession`), `MainShell.kt` (`MainShellRoute`) | ✅ **fixed and verified**: revoke while backgrounded → back to the app → Login, `user_session` gone from STORE, back exits; revoke with the app in front → open World → Login; revoke → open Places → Login (401 on `/api/categories/`). 3 network tests (`SessionRejectionTest`) + 6 ViewModel tests (`HomeViewModelSessionTest`), each seen red (*expected 1 but was 0*; *expected 0 but was 1* for 403 and for no token; *Expected value to be true/false*). Negative pairs yes (403, no token, offline recheck, signed-in user) |
| SH-04 (ABS-01) | **P0?** (security) | **"Remember me" stored the account password in plain text in the app's preferences, next to the session token, and that file went into device backups.** The token alone already kept the user signed in | `core/data/.../UserRepositoryImpl.kt`, `UserRepository.kt`, `RememberMeCredentialsUseCase.kt`, `LoginViewModel.kt`, `composeApp/src/androidMain/AndroidManifest.xml` + `res/xml/backup_rules.xml`, `data_extraction_rules.xml` | ✅ **fixed and verified**: R14 upgrade on `qa` — a `remember_password` planted in the old build's store (invented value) is gone after installing the new build over it, username, URL and session kept; the login form pre-fills server and username, not the password. Shared preferences are excluded from cloud backup and device transfer (`aapt2 dump` of the built APK shows both attributes). 2 tests, seen red (*the plain-text password survived the upgrade*; *expected claude but was null* for the pair). **Backup exclusion: device-only, no test — because proving it needs a backup transport enabled on the emulator, a system setting.** A real login with Remember me on the fixed build is queue #7 |
| SH-05 (ABS-03) | P2 | **Logging in to an address that doesn't answer shows a blank screen for ~72 s**, no spinner, no cancel, then "Network unavailable" although the phone's network is fine. The loading dialog is unreachable and no HTTP timeout is set | `LoginScreen.kt:90-92,111-114`, `NetworkModule.kt:42-54` | ⏸ deferred, queue #8 |
| SH-06 | P2 | **In landscape the Login button is off-screen and the form doesn't scroll**; "Hello!" sits under the status bar clock | `LoginScreen.kt:145-150` | ⏸ deferred, queue #8 — reopens at 08 (tablet landscape has no portrait workaround) |
| SH-07 | P2 | **A half-filled login form is lost when Android kills the app** (URL, user and password typed; `kill` → relaunch → empty fields) | `LoginViewModel.kt:30` (no SavedStateHandle) | ⏸ deferred, queue #8 |
| SH-08 | P2 | Password's Done key doesn't submit (0 login requests); tapping "Remember me" text doesn't toggle the box; the checkbox has no name and overlaps the Login button by 9 px (`a11y`) | `PasswordTextField.kt`, `RememberSessionSection.kt:16-31` | ⏸ deferred, queue #8 |
| SH-09 (ABS-11) | P2 | Global search reopens with the previous query and results | `GlobalSearchViewModel.kt:48-51` | ⏸ deferred, queue #8 (the web keeps *recent searches*, so this may be wanted) |
| SH-10 (ABS-12) | P2 | Opening People twice from the account sheet stacks two People screens: back returns to People | `MainShell.kt:419` | ⏸ deferred, queue #8 |
| SH-11 | P2 | **After scrolling Settings, Home opens with the app bar collapsed** — no greeting, search or account button until the user swipes down (one scroll state for every tab) | `MainShell.kt:151,280` | ⏸ deferred, queue #8 |
| SH-12 (ABS-13) | P2 | With Theme = Follow the system and the phone switched to dark while the app is open, status-bar icons stay dark on a dark background (EYE) | `MainActivity.kt:43-60` | ⏸ deferred, queue #8 |
| SH-13 (ABS-06) | P1 (content) | **Terms of use describe "Shiori", another app**; the privacy policy reads "Effective as of [Insert Date Here]" (UI + EYE) | `TermsOfUseScreen.kt:61-86`, `PrivacyPolicyScreen.kt:70` | ⏸ deferred, queue #4 — legal text is the owner's to write |
| SH-14 (ABS-08) | P2 (security) | Sign out never revokes the server session: the token the app held answered 200 after sign-out. The server does support it (`DELETE /auth/browser/v1/auth/session` → token 401, measured) | `LogoutUseCase.kt:23-48` | ⏸ deferred, queue #8 |
| SH-15 | P2 | Sign out leaves the downloaded backup (`cache/attachments/adventurelog-backup-2026-09-15.zip`, 8.7 MB of the account's places and photos) and the image cache on the device | `BackupExporter.kt`, `AndroidPlatformFiles.kt:43-45` | ⏸ deferred, queue #8 |
| SH-16 | P2 | **"Send feedback" does nothing** on Android 11+: `resolveActivity` returns null without a `<queries>` entry, so the mail app (Gmail is installed) is never found and nothing is said | `AndroidPlatformActions.kt:15-24` | ⏸ deferred, queue #8 |
| SH-17 (ABS-14) | P2 | Edit profile with an invalid username: the dialog closes at once, the server's reason is shown in a snackbar ("Enter a valid username…"), but what was typed is lost | `SettingsScreen.kt:247-249`, `SettingsViewModel.kt:214-216` | ⏸ deferred, queue #8 |
| SH-18 | P2 | a11y: Settings' Public profile and Dynamic colours switches have no name; the collection breadcrumb's Back is 28×48 dp | `SettingsGroup.kt:188-199`, `MainShell.kt:307` | ⏸ deferred, queue #8 |
| SH-19 (ABS-10) | P2 | `compactView` is stored and exposed but has no row and changes nothing | `SettingsViewModel.kt:113,325-329` | ⏸ deferred, queue #8 (remove or build) |
| — (ABS-09) | — | "You will need your password to sign back in" was untrue while the password was pre-filled; with SH-04 it is true | `SettingsScreen.kt:271` | ✅ resolved by SH-04, no change of its own |

## What happened

| What | Result |
|---|---|
| Stored session → Home (L2); tabs Home/Places/Collections/Map/World, titles, back from each → Home, back from Home → launcher | ✅ |
| Process death on Home (L5, R9) | ❌ SH-01 → ✅ after the fix; also on a place's page |
| Account sheet: name, @username, Server row = STORE = config; Calendar, People, Settings | ✅ (People twice → SH-10) |
| Global search: place result → detail → back; collection result | ✅ / ❌ SH-02 on a `/` → ✅ after the fix; reopen → SH-09 |
| Collection breadcrumb: chevron, Home icon, system back | ✅ |
| Settings: Theme Dark/Light/System — STORE `theme_mode` 0/1/2 matches, EYE per theme | ✅ / SH-12 on a live system switch |
| Settings: Units Metric → Imperial → Metric — UI and API agree at each step, restored | ✅ |
| Settings: Edit profile refused by the server (invalid username) — API unchanged | ✅ nothing written / SH-17 |
| Settings: Media storage 8.3 MB · 29 files vs API `8722982` bytes / 29 files | ✅ |
| Settings: Download a backup → share sheet, zip in cache; Server setup guide → browser; Send feedback | ✅ / ✅ / ❌ SH-16 |
| Terms / Privacy (EYE); Sign-out dialog, Cancel | SH-13 / ✅ |
| Sign out from the account sheet: Login, `user_session` gone, back exits | ✅ (SH-14, SH-15) |
| Login validation: empty submit, `ftp://`; wrong credentials (invented user) → "Invalid username or password" | ✅ |
| Login to an unroutable address; Done key; Remember-me label; landscape; process death with a filled form | SH-05, SH-08, SH-06, SH-07 |
| Cold start offline with a stored session (L4) → "No internet connection." → network back → Try again | ✅ |
| Cold start with a revoked session (L3) → Login, session cleared | ✅ |
| Session revoked with the app open (SH-03) | ❌ → ✅ after the fix (background, World, Places) |
| R14: old build's store with a plain-text password → new build installed over it | ✅ password removed, rest kept |
| Build on the device is the build with the fixes | ✅ `installed --apk` matches `composeApp-debug.apk` built 10:08:11 (sha256 `d3521217…`) |
| Full repository test suite | ✅ `./gradlew allTests`: **722 results, 0 failures, 0 skipped** (361 tests × Android unit + iOS simulator; 17 new). `connectedDebugAndroidTest` on `qa`: **32 tests in 6 modules, 0 failures**. The first full run **failed**: Kotlin/Native rejects a `,` in a backticked test name, which every Android-only run of the new test had passed (R13 doing its job); renamed, re-run green |
| Crashes / hangs of the app | ✅ 0 / 0 after the fixes (1 crash before: SH-02) |
| `QA_` fixtures cleaned and verified through the API | ✅ `QA_Route/Slash` deleted (204); API lists 6 collections, none `QA_`; profile `claude` / John Doe / metric / private as at start |

## Open

- **Not driven, on purpose:** Change password, add/verify/make-primary/remove email (send real email), Public profile switch (exposes the account), Update visited regions (writes data the campaign didn't create), People search (appears above 8 public users; the server has 0).
- **Not driven, needs a real password:** the "server sent no session" branch (frontend `:3445`), a trailing space in the username, and a real login with Remember me on the fixed build — queue #7.
- **Not reproduced:** the logout race in the inventory (logout now navigates from the session itself, so the race is gone either way); Dynamic colours starting `true` (the switch showed the stored `false`); nav-bar icon colour with 3-button navigation (would need a device setting change).
- **SH-03's limit:** an ended session is noticed on the next 401 or the next return to the foreground. A screen whose only request answers 200-empty for a nobody (`/api/locations/`) and that is opened first, in the same foreground stretch, would still misread it — in the run, Places' own `/api/categories/` 401 caught it first.
- **My mistakes (R12):** I pressed `key BACK` to hide a keyboard that wasn't showing and exited the app twice, losing the typed form; I then wrote a guarded helper. I used `ui.py find` as a predicate — it exits 0 with no match — so one loop reported "message after 4 s" over a blank screen; the 72 s figure comes from the re-measurement. `inject_session.py` read the cached token before the request that would have refreshed it, and injected a revoked token once (app went to Login); fixed and re-run.
- **Harness (R8):** `a11y` flagged the app bar's search and account buttons as UNNAMED while the collapsing bar clipped their icons; expanded, both are named. Not a finding.
