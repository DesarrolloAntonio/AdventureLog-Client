# 02 — Home (dashboard) · qa (Resizable_Experimental, API 37, 1080×2400 @ 420)

> Driven on `df3b9c5`; fixes verified on the working tree built 2026-09-15 16:18 (checked with `ui.py installed --apk`) · device `qa` = `avd:Resizable_Experimental` · account A `claude`, plus an invented account **B** on `scripts/qa/fake_adventurelog.py` · 2026-09-15. Screenshots in `qa-shots/02-*`.

**Resumed with skill `cc914b8`** (campaign began on `8940b4d`). Steps the skill added since, checked before any work (§2.1):
- **Device listed by AVD:** `qa.config.json` now has `"qa": "avd:Resizable_Experimental"` and `campaign`, and `inject_session.py` asks `ui.py serial`.
- **R11, real login vs injection:** the owner signed in by hand on `qa` (I typed server and username, the owner the password). Field by field, all 17 fields of the stored `user_session` are equal to what `inject_session.py` writes; the token answers `/auth/user-metadata/` 200. The only difference is the `remember_*` keys a real Remember-me login adds, which the injection leaves alone on purpose. **Gate 01 not reopened.**
- **R7/R11, a second account and a switch inside one process:** the owner chose an invented B on a fake server now, a real B before process 09 (queue #3).
- **R6 time limit:** every test task now stops after 15 min (root `build.gradle.kts`); runs launched from the shell also go through `perl -e 'alarm …'`.

## Inventory

From `DashboardScreen.kt`, `HomeViewModel.kt`, `GetDashboardUseCase`, `DashboardRepositoryImpl`, `DashboardDTO` and the `home` destination in `MainShell.kt`. The shell around Home is 01's.

### Controls

| # | Control | Kind | Does | Oracles | Suspect? |
|---|---|---|---|---|---|
| 1 | loading card | state | spinner until `/api/stats/dashboard/` answers | UI | no cancel; no read timeout |
| 2 | error text + **Try again** | state, button | message from `GetDashboardUseCase`; retry reloads the whole screen | UI | a 403 gives "Session expired" but doesn't sign out |
| 3 | empty account: **Add a place**, **Create a collection** | buttons | `adventures/add`, `add_collection` | UI | |
| 4 | hero card (active trip, else first upcoming) | card | collection detail | UI + API | label from server `status` / `days_until_start` |
| 5 | hero **Open trip** | button | same as the card | UI | no `launchSingleTop` |
| 6 | hero **Add a place** | button | `adventures/add`, not tied to the trip | UI | REF has no such button |
| 7 | stats: Countries / Regions / Cities / Places visited | read-only rows | visited / total | UI + API | |
| 8 | Coming up **See all** | text button | Calendar | UI | 47 dp tall (ABS-34) |
| 9 | Coming up trip row | row | collection detail | UI + API | trips also appear as calendar events |
| 10 | Coming up event row | row, not clickable | — | UI | has a `collectionId` |
| 11 | Recently updated **See all** | text button | Places tab | UI | 47 dp tall |
| 12 | Recently updated cards (3, horizontal) | cards | place detail | UI + API | header shows the total place count |
| 13 | wide only: "Plan the trip" card, "Add place" in the bar | card, button | collection / add | UI | process 08 |

No pull-to-refresh, no snackbar. Home makes one request, `GET /api/stats/dashboard/`, plus the shell's `/auth/user-metadata/` on every return to the foreground.

### Entry points into Home (not controls)

| # | Entry | Lands | Oracles |
|---|---|---|---|
| E1 | login / stored session | `home_graph` → Home | UI |
| E2 | Home tab, back from any tab | Home, state restored (no reload before this process) | UI + API |
| E3 | breadcrumb Home icon in a collection | Home, new entry | UI |
| E4 | **a collection opened from a place's page** (root graph): its add/edit actions fall back to Home (ABS-28) | `popBackStack(Home.graph)` | UI |

### UI state fields (S1, re-run because Home's code changed in 01)

| Field | Painted in | S1? |
|---|---|---|
| `HomeUiState.Success.userName` | nowhere (greeting reads the session) | yes — dead |
| `HomeUiState.Success.today` | only read by `EventRow`/`EventWhen`, which nothing calls | yes — dead |
| `Dashboard.inviteCount` | nowhere; REF shows an invitations card | yes (ABS-19) |
| `stats.tripsCount` | only in the empty-account check | partly |
| `CalendarEvent.type/allDay/category/collectionId`, `ComingUp.key` | nowhere | yes, low |
| everything else (`stats`, `activeTrip`, `upcomingTrips`, `recentLocations`, `upcomingEvents`) | hero, stats, Coming up, Recently updated | no |

S2 (dead controls): none reachable — every default lambda in `DashboardScreen` is replaced by a real caller. S4: `loadDashboard` clears Loading on both paths; the rest are in the findings.

## Findings

| ID | Sev | What actually happens | Where | Status |
|---|---|---|---|---|
| HM-01 | **P0** (privacy) | **After one account signs out and another signs in on the same phone, the new account sees the previous one's private travel data.** Measured in one process (pid unchanged): A (`claude`) opened World, Map and Collections, signed out; invented account B signed in against the fake server, which answers B's lists empty. B's World read "Travel Progress 2%, Visited 6, Partial 6" and **Japan 3/47** — A's six countries, confirmed through A's API (Croatia, Iceland, Japan, Jordan, Morocco, Norway); B's Map read **13 Regions**; B's Collections header read **8 collections** over an empty list. The fake server's log shows the app never even asked B's server for countries: repositories are Koin singletons for the whole process, and sign-out never emptied their in-memory caches (`CountriesRepositoryImpl.kt:36` returns the cache whenever it isn't empty). Release builds have it | `core/data/…/CountriesRepositoryImpl.kt`, `CollectionsRepositoryImpl.kt`, `AdventuresRepositoryImpl.kt`, `UserRepositoryImpl.kt`, `core/domain/…/AccountDataCache.kt`, `LogoutUseCase.kt`, `DataModule.kt`, `DomainModule.kt` | ✅ **fixed and verified in the running system** (owner told at once, R2 stop 1): every repository that keeps account data in memory implements `AccountDataCache` and is bound as one; sign-out empties them all. Same switch on the fixed build, same process: B's World "0 countries, 0%" (and the app asked B's server for `/api/countries/`), Map "0 Regions", Collections "0 collections", Japan not found. 6 tests (`AccountCachesTest`, `LogoutUseCaseTest`), each seen red on its own assertion (*the previous account's visits were served from memory expected 0 but was 3*; *…collections… expected 0 but was 2*; the Koin binding list without `CountriesRepositoryImpl`; *expected [countries, collections] but was []*; …). Negative pairs yes (same account → countries not fetched twice; one failing cache doesn't stop the rest). **The `DomainModule` injection of the caches into `LogoutUseCase` is covered by the device run only** — no test builds the domain module. Residual: a request sent as A that answers after sign-out could refill a cache; not provoked |
| HM-02 | **P1** | **Tapping Edit place (or any add/edit) in a collection opened from a place's page leaves a blank white screen**; Back leaves the app. The fallback popped the back stack to the Home *graph* route, which left only the graph's own entry | `composeApp/…/AdventurelogNavGraph.kt` (`navigateToHome`) | ✅ **fixed and verified**: the same path now lands on Home. **Device-only, no test — because it is the root NavHost's back stack, and composeApp has no test source set to host one** |
| HM-03 | **P1** | **Home never shows anything newer than its first load.** With Home on 22 places, a place added on the server (API 23) was still 22 after switching tabs and after returning from the background; only a restart updated it. A place added from Home's own button came back to the old counts | `feature/home/…/HomeViewModel.kt` (`refreshDashboard`), `MainShell.kt` (Home destination) | ✅ **fixed and verified**: Home reloads behind what it shows each time it comes back into view (another tab, a place's page, an add screen, the background), keeping the dashboard if the server can't be reached. On `qa`: 23 → 24 after a tab switch, → 25 after the background. 3 tests (`HomeDashboardRefreshTest`), seen red (*expected 23 but was 22*; *a failed refresh replaced the dashboard*; *expected 1 but was 2*). Pair yes (a failed refresh keeps the data) |
| HM-04 | P2 | "Coming up" lists each trip twice — once as a trip, once as its calendar event — and the trip in the hero again as an event row with its past start date (QA_Trip_Now 14 SEP, QA_Trip_Tomorrow ×2, Peru ×2). REF shows trips and calendar events in separate sections | `DashboardScreen.kt:911-938` | ⏸ queue #9 |
| HM-05 | P2 | The subtitle counts the trip happening now as a trip ahead ("3 trips ahead" with one in progress and two upcoming) | `MainShell.kt` (`dashboardSubtitle`) | ⏸ queue #9 |
| HM-06 | P2 | A double tap on Open trip opens the collection twice (Back returns to the same collection) | `MainShell.kt:506-513` | ⏸ queue #9 |
| HM-07 | P2 | A server that accepts the connection and never answers leaves Home on a spinner for about 2 min, then says "No internet connection." — the phone's network was fine | `NetworkModule.kt` (no socket timeout), `GetDashboardUseCase.kt:15` | ⏸ queue #9 |
| HM-08 | P2 | A collection opened from a place's page has no app bar and no Back, and its text runs under the status bar (EYE) | root-graph `collectionsScreen` | ⏸ queue #9 — belongs to 04 |
| HM-09 | P2 (product) | Hero "Add a place" doesn't add the place to that trip; on a phone with no trip ahead Home has no way to add a place; "Recently updated (22)" shows the total place count over three cards of visited places; event rows can't be opened; pending invitations aren't shown (ABS-19) | `DashboardScreen.kt` | ⏸ queue #9 — product decisions |
| — | — | *(owner's question)* **One Back closing the keyboard and the search sheet together is not the app.** The keyboard on `qa` is Gboard's floating keyboard (reached from its stylus pill's menu). With it up, one Back closed keyboard and screen in Global search, in the Places search field (main window, no sheet) **and in the system Settings app's search** | — | not filed; see Open |

## What happened

| What | Result |
|---|---|
| Resume checks: skill diff, config by AVD, device claim, R11 comparison with the owner's real login | ✅ equal; gate 01 stands |
| Back with the keyboard up: search sheet, Places field, Edit profile dialog, Settings app (control) | ✅ same in every app — keyboard behaviour, not a finding |
| Home as A: hero, stats, Coming up, Recently updated read against `/api/stats/dashboard/` | ✅ figures match the API |
| Dates: QA_ trips in progress and starting tomorrow (fixtures through the API) | ✅ "HAPPENING NOW 14/09 – 16/09", "Starts tomorrow"; HM-04, HM-05 |
| Collection from a place's page → Edit place | ❌ HM-02 → ✅ |
| Home after a server-side change | ❌ HM-03 → ✅ |
| Double tap on Open trip | HM-06 |
| Account switch in one process (A → invented B on the fake server) | ❌ HM-01 → ✅; Home, Places and search were already clean |
| Dashboard that never answers (fake server, 600 s delay) | HM-07 |
| Full repository test suite | ✅ `allTests` green: **405 results written by this run, 0 failures** (the modules this process changed; the other modules' reports are from earlier runs with unchanged inputs, not counted). `connectedDebugAndroidTest` on `qa`: **32 tests, 0 failures** |
| Crashes / hangs of the app | ✅ 0 / 0 |
| `QA_` fixtures cleaned and verified through the API | ✅ 3 places and 2 collections deleted (204 each); API: 22 places, 6 collections, no `QA_`, dashboard back to Peru only |

## Open

- **HM-01's reach:** the switch was to an invented account on a fake server. A real B on the NAS (queue #3) is still needed for sharing in 09, and to repeat this with two real accounts.
- **Not provoked:** a 403 on the dashboard (the server answers 401, which signs out); an explicit JSON `null` in a non-nullable dashboard field (the server's own code always sends strings and lists there — `calendar_events.py`, `stats_view.py`); a request from A answering after sign-out and refilling a cache.
- **Not driven:** large font scale and 3-button navigation (system settings I don't change); the wide layout (process 08).
- **The keyboard question:** a docked Gboard keyboard was not tried — dragging the floating one to the bottom didn't dock it. What was shown is that the floating keyboard closes the screen with it in an app that isn't ours.
- **My mistakes (R12):** a coordinate parsed with `awk` sent two taps nowhere and two Backs out of the app; a zsh variable holding the harness command didn't run it (the Back controls had to be repeated); the first B login went to `10.0.2.2`, which the app can't reach on this emulator even though `net.sh reach` said the device could (it checks as the shell user) — `adb reverse` and `127.0.0.1` worked.
