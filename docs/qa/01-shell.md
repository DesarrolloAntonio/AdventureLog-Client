# 01 — Shell · qa (Resizable_Experimental, API 37, 1080×2400 @ 420)

> Build `13bb122` (debug APK sha256 `2b3ed909…`) · device `qa` = emulator-5560 · account A (`claude`) · 2026-09-15. Screenshots in `qa-shots/`.

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
