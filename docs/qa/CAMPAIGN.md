# QA campaign — AdventureLog (Android)

**Started:** 2026-09-14 · **Build at start:** `13bb122` (branch `qa/2026-09-14`, cut from `feat/adaptive-tablet`) · **Skill at start:** qa-campaign `8940b4d` · **Status:** paused after gate 01 (2026-09-15), as scoped — next is 02 Home

**Offline depth:** **short** — the app talks to a server and keeps no data of its own: no database, no WorkManager, no pending/dirty/syncStatus field; every repository but User/Settings depends only on the network source, writes go to the server first and only in-memory caches are updated afterwards (`core/data/.../di/DataModule.kt:73-129`, `CountriesRepositoryImpl.kt:162-168`). Local storage is the session and preferences in one SharedPreferences file, plus Coil's image cache (SKILL.md R1) ·
**Fix mode:** fix severe · **Commits:** at each gate, on `qa/2026-09-14` ·
**Docs live in:** `docs/qa/` (versioned) — asked once at setup (SKILL.md §2.1), not per finding.

**What already existed** (SKILL.md §2.1) —
**Devices:** **emulator-5554 is OFF-LIMITS** (another QA campaign) and so is its network. It had been running the `Resizable_Experimental` AVD; on 2026-09-15 nothing was running and the owner chose that same AVD as the test device. The AVD also holds the other campaign's apps (`com.desarrollodroide.nextcloudapps.debug`, `…pagekeeper.staging` and their test APKs): never touched. The Pixel 6a was not attached ·
**Test server:** the owner's AdventureLog backend on a Synology NAS, `https://ds224.boga-aeolian.ts.net:3447` (Tailscale); shared, not disposable. The owner says its account is a test account and may be used ·
**Accounts:** A = `claude` (John Doe) — the demo account seeded with 22 places, 6 collections and 17 visits for the store screenshots; credentials written by the owner into `qa.credentials.json`; no B or C yet · **Data not to touch:** anything the campaign did not create (R10) — in particular the 22 seeded places and 6 collections, which are screenshot material; fixtures carry `QA_`.
Anything the campaign built or started on top of that: the `Resizable_Experimental` emulator, started on **port 5560** (serial `emulator-5560`, so the off-limits serial keeps meaning the other campaign) with `-no-snapshot-save` (owner's choice, 2026-09-15). `scripts/qa/adapter.py` and `scripts/qa/inject_session.py` are campaign code, not infrastructure.

---

## 1. Scope and order

This session's scope, set by the owner: **00 and 01; stop after gate 01.**

| # | Process | Area | Where | Gate |
|---|---|---|---|---|
| 00 | Setup | harness config, API adapter, session injection, absence sweeps for the whole product (§6) | — | the harness drives the app and reads its store |
| 01 | Shell | launch, login screen + injected session, logout, bottom bar / rail, app bar, global search, account sheet, Settings, People, theme, process death. **Entry points:** launcher only — no deep links, share targets, notifications, widgets or shortcuts (`composeApp/src/androidMain/AndroidManifest.xml`) | test device | every entry point reachable, back stack sane, logout leaves nothing behind |
| 02 | Home (dashboard) | control sweep online + short offline, §6 rows | test device | inventory covered; P0/P1 fixed, P2 fixed or deferred |
| 03 | Places (list, detail, add/edit, visits, trails, images, attachments) | same | test device | same |
| 04 | Collections (list, detail tabs and views, notes, checklists, lodging, transport, sharing UI) | same | test device | same |
| 05 | World (countries, country detail, regions) | same | test device | same |
| 06 | Map | same | test device | same |
| 07 | Calendar | same | test device | same |
| 08 | Tablet / wide window | two-pane Places & Collections, nav rail, adaptive grids, window resize across the breakpoint | test device resized | no dead ends specific to this layout |
| 09 | Multi-context | collection sharing from **B**, nothing from **C** (R7); same account on the web and the app | test device + web | permissions verified on each side |
| 10 | Release build | R8 release APK smoke, packaging; **no local store migration exists** (no DB), so R14 reduces to the prefs keys | test device | the release artefact runs and upgrades over the previous one |

No offline gate (depth short): each module checks that it says it can't reach the server, nothing spins forever, nothing pretends a write was saved, and it recovers.

## 2. Devices

| Alias | What | Notes |
|---|---|---|
| `qa` | emulator-5560 — AVD `Resizable_Experimental`, API 37 (Android 17), 1080×2400 @ 420 dpi, Google Play image | the only entry in `devices`; resized for process 08. emulator-5554 is never added |

## 3. Accounts

| Alias | Role |
|---|---|
| A | owner — `claude` / John Doe |
| B | recipient of a shared collection — **does not exist yet** (queue #3; needed at 09) |
| C | negative control — **does not exist yet** (queue #3; needed at 09) |

What signs each account in lives in `qa.credentials.json` (gitignored). **No password is ever typed by the agent** (R11): the session is the server's `sessionid`, obtained by `scripts/qa/adapter.py` from those credentials and cached in `qa.tokens.json` (gitignored), and injected by `scripts/qa/inject_session.py` — it force-stops the app and writes the `user_session` JSON into `shared_prefs/<package>_preferences.xml` through `run-as`, via stdin. **App under test:** `com.desarrollodroide.adventurelog.debug` — it was **not installed** on `qa` before the campaign (checked with `ui.py installed`), so no real session could be overwritten; the other apps on that AVD belong to the other campaign.

## 4. Fixtures

Everything the campaign creates carries **`QA_`** in `name` (to be confirmed by creating one fixture and reading it back through the API) and is deleted at the gate that created it. Destructive tests run only against these (R10).

⚠️ Permanent deletes — to be confirmed per module against the backend: places, collections, notes, checklists, lodging, transportations, visits, images and attachments have no trash in the web client.

⚠️ Side effects the clean-up can't undo: **adding an email address** or **sending a verification email** (Settings → Sign-in) sends real email; **sharing a collection** notifies the recipient; **public profile / public collection** exposes data to anyone with the URL; **changing the password** invalidates other sessions. None is exercised without asking.

## 5. Oracles

| Oracle | How it is queried here |
|---|---|
| UI | `ui.py dump` / `ui.py a11y` (text and desc selectors only: no `testTag`, no `testTagsAsResourceId`) |
| STORE | `ui.py file prefs` — the one SharedPreferences file (session JSON, remember-me keys, theme). No SQLite exists. Coil disk cache and `cache/attachments` via `ui.py files` |
| API | `scripts/qa/adapter.py <METHOD> <path> [--account A]` |
| EYE | `ui.py shot` + review |
| REF | AdventureLog web client **v0.13.0** (`reference/adventurelog-web` @ `5673ef5`, `v0.13.0-1-g5673ef5`) and the live web at `:3445` |

## 6. Absence sweeps (R4: S1 data never shown · S2 dead control · S3 expected absence · S4 stuck when something fails)

Run once at setup, on `13bb122`, from the code. **Every row is a candidate until the module's process drives it**; severities are provisional. Method, commands and discard counts: [`00-setup.md`](00-setup.md#absence-sweeps).

| ID | Module | Sweep | Sev | Finding | Evidence | Status |
|---|---|---|---|---|---|---|
| ABS-01 | shell | S4/sec | P0? | "Remember me" stores the **password in plain text** next to the session token, and `allowBackup="true"` with no rules puts that file into device backups | `UserRepositoryImpl.kt:115-120`, `AndroidManifest.xml:13` | ✅ fixed at 01 (SH-04) |
| ABS-02 | shell | S4 | P1? | After process death on Home the network base URL is never set again (only LoginViewModel initialises it): every screen errors until the app is swiped away | `KtorAdventureLogNetwork.kt:207-230,340-345`, `AdventurelogNavGraph.kt:25` | ✅ fixed at 01 (SH-01) |
| ABS-03 | shell | S4 | P2? | Login and cold start show a **blank screen** while loading (the loading dialog is unreachable) and no HTTP timeout is installed (~100 s on a silent address) | `LoginScreen.kt:90-92,111-114`, `NetworkModule.kt:42-54` | ✅ fixed at 01 (SH-05) |
| ABS-04 | shell | S2 | P1? | Route arguments are not URL-encoded: a collection named with `/`, `?`, `#` or `%` should fail to navigate or crash | `NavigationRoutes.kt:20-93`, `MainShell.kt:243-250` | ✅ fixed at 01 (SH-02) |
| ABS-05 | shell | S4 | P2? | No app-wide 401 handling: an expired session leaves "Session expired" on every screen; only Sign out escapes | `NetworkModule.kt:42-54`, `GetDashboardUseCase.kt:17` | ✅ fixed at 01 (SH-03) |
| ABS-06 | shell | S3 | P1? | Terms of use are **Shiori's** (another app) and the privacy policy reads "Effective as of [Insert Date Here]" — a store blocker | `TermsOfUseScreen.kt:61-86`, `PrivacyPolicyScreen.kt:70` | confirmed at 01 — SH-13, queue #4 |
| ABS-07 | shell | S4 | P2? | Logout keeps in-memory repository caches (collections, countries, stats) — a second account could see the first's data | `DataModule.kt:60-129`, `CollectionsRepositoryImpl.kt:75-83` | not reproducible with one account — 09 (B) |
| ABS-08 | shell | S3 | P2 | Sign out never revokes the server session (web DELETEs it) | `LogoutUseCase.kt:23-48` vs REF `routes/+page.server.ts:45` | ✅ fixed at 01 (SH-14) |
| ABS-09 | shell | S4 | P2 | Settings' sign-out dialog says "You will need your password to sign back in" while remember-me pre-fills it | `SettingsScreen.kt:271`, `LoginViewModel.kt:59-89` | ✅ resolved by SH-04 |
| ABS-10 | shell | S1 | P2 | `compactView` is persisted and exposed but shown and used nowhere | `SettingsViewModel.kt:113,325-329` | ✅ fixed at 01 (SH-19) |
| ABS-11 | shell | S4 | P2 | Global search reopens with the previous query and results | `GlobalSearchViewModel.kt:48-51` | ✅ fixed at 01 (SH-09) |
| ABS-12 | shell | S2 | P2 | Account sheet → People navigates without `launchSingleTop`: repeated taps stack duplicates | `MainShell.kt:419` | ✅ fixed at 01 (SH-10) |
| ABS-13 | shell | S4 | P2 | Theme: status-bar style read from `configuration.uiMode` (not observed; `uiMode` is in `configChanges`), nav-bar icons always light, login background follows the system, not the app theme | `MainActivity.kt:32,43-56`, `LoginScreen.kt:246` | ✅ fixed at 01 (SH-12) |
| ABS-14 | shell | S4 | P2 | Settings: Edit profile closes before saving and loses the typed name on refusal; dialogs undismissable while a request hangs; backup silently does nothing with an empty server URL; hard-coded Spanish "Desconocida" / "Feedback para AdventureLog" | `SettingsScreen.kt:247-249`, `SettingsViewModel.kt:47,214-216`, `AndroidPlatformActions.kt` | ✅ fixed at 01 (SH-17, SH-20, Spanish strings in SH-16) |
| ABS-15 | shell | S3 | — | No sign-up, forgot/reset password, email-verification link, social/OIDC login, or MFA code at login — MFA or SSO-only accounts cannot sign in | REF `routes/signup`, `routes/user/reset-password`, `routes/login/+page.server.ts:97` | queue #5 (scope) |
| ABS-16 | shell | S3 | — | English only; the web ships 24 locales | REF `routes/+layout.svelte` | queue #5 (scope) |
| ABS-17 | shell | S3 | — | README and commit `9bc65f0` say Users directory and backup are out of scope; commits `3c32f51` and `dbb8dcb` built the Users screen and backup export | `README.md`, `UsersScreen.kt:55`, `VisitedRegionsCard.kt:108` | ✅ README corrected at 01 (queue #6) |
| ABS-18 | shell | S1 | P2 | `UserDetails.disablePassword` never shown (SSO accounts in Settings → Sign-in) | `UserDetails.kt:20` | not testable with `claude` (has a password) |
| ABS-19 | home | S1 | P2 | Pending-invite count (`Dashboard.inviteCount`) not shown on Home; the web shows it | `Dashboard.kt:15` | 02 |
| ABS-20 | places | S4 | P2? | Pull-to-refresh spinner never stops after a failed refresh or failed duplicate; the error and Retry never show when places are listed | `LocationsViewModel.kt:232,316`, `LocationScreen.kt:164-166,343` | suspect — 03 |
| ABS-21 | places | S1 | P2 | Place detail doesn't show city / region / country; saving with photos shows no progress (`isSavingLocation`, upload counts unused) | `Location.kt:26-28`, `AddEditLocationViewModel.kt:46-48` | 03 |
| ABS-22 | places | S1/S3 | — | Visit activities (Strava/GPX) never shown or imported; image set-primary/delete and attachment upload absent; no locate-me, copy link, sunrise/sunset | `Visit.kt:13`, `AdventureDetailScreen.kt:204` | 03 — queue #5 (scope) |
| ABS-23 | places | S4 | P2 | Save warnings ("visits/trails not saved", "images not uploaded") flash or never show because the screen closes at once | `AddEditLocationScreen.kt:116,124` | 03 |
| ABS-24 | collections | S4 | **P0?** | If loading **Edit collection** fails, a blank form appears; saving it overwrites the collection's description and dates with empty values | `AddEditCollectionViewModel.kt:40-47`, `AddEditCollectionScreen.kt:74` | suspect — 04 (QA_ fixture, network cut) |
| ABS-25 | collections | S1 | **P0?** | `Note.links` and `Lodging.rating` are not in the edit forms — if update replaces the record, an edit wipes them | `Note.kt:12`, `Lodging.kt:12` | suspect — 04 (API before/after) |
| ABS-26 | collections | S2 | P2? | In a collection, a place card's ⋮ → **Duplicate** and **Share externally** close the sheet and do nothing | `CollectionDetailScreen.kt:468,602`, `AdventureItem.kt:47-48` | suspect — 04 |
| ABS-27 | collections | S4 | P2 | A failed collection save discards everything typed; a failed reload replaces the collection detail with "Error: …" and no retry; Manage-collections refresh fails silently | `AddEditCollectionScreen.kt:39`, `CollectionDetailViewModel.kt:182,311` | 04 |
| ABS-28 | collections | S2 | P2 | A collection opened from a place's chip sends every add/edit action to Home (commented as deliberate) | `AdventurelogNavGraph.kt:69-131` | 04 — queue (accept?) |
| ABS-29 | collections | S1/S3 | — | Transport distance/timezones, lodging rating, recommendation phone/hours not shown; no leave-collection, import, cover picker, itinerary reorder | `Transportation.kt:28-30`, `Recommendation.kt:26-28` | 04 — queue #5 |
| ABS-30 | world | S4/S3 | P2 | Pull-to-refresh shows no progress (flag set and cleared at once); no city drill-down or city visits | `WorldScreen.kt:85-88` | 05 |
| ABS-31 | map / calendar | S3 | — | Map: no search, tap-to-add, locate-me, activity layers; Calendar: agenda only, no month grid, no ICS | REF `routes/map`, `routes/calendar` | 06/07 — queue #5 |
| ABS-32 | settings | S4 | P2 | After an email action, a failed list reload leaves stale verified/primary badges silently | `SettingsViewModel.kt:283-297` | not driven at 01 (email actions send real email) |
| ABS-33 | shell | sec | P2 | `FileProvider` exposes the whole root of every declared storage (`path="."` ×5) | `composeApp/src/androidMain/res/xml/file_paths.xml` | read at 01, not driven |
| ABS-34 | home | a11y | P2 | Home's two "See all" buttons are 47 dp tall (harness `a11y`, not clipped) | `ui.py a11y` on Home, `[874,1215][1038,1339]` | 02 |

## 7. User queue (R2)

| # | What | Why it needs you | Recommendation | Gates closed assuming | Reopen on resolution |
|---|---|---|---|---|---|
| 1 | ~~Test device~~ | — | **resolved 2026-09-15:** the owner chose the existing `Resizable_Experimental` AVD | 00 | if the other campaign needs that AVD back |
| 2 | ~~Account A~~ | — | **resolved 2026-09-15:** `claude`, credentials in `qa.credentials.json` by the owner | 00 | — |
| 7 | ~~One real login by hand, Remember me checked, on the fixed build~~ | — | **resolved 2026-09-15:** done by the owner; STORE has no password, the stored token answers 200 | 01 (SH-04) | — |
| 8 | ~~P2s from 01~~ | — | **resolved 2026-09-15:** the owner said fix all but SH-13; fixed and verified (01-shell.md) | 01 | — |
| 3 | Accounts B and C | sharing exists (collections), R7 needs a recipient and a negative control; the agent never creates accounts | two accounts on the NAS, before process 09 | — | 09 |
| 4 | Terms of use (Shiori's) and privacy policy placeholder date | legal content | rewrite both for AdventureLog before any store upload | — | 10 |
| 5 | Scope of the REF features the app lacks (ABS-15/16/22/29/31) | product decisions | one list at gate 01 | — | the module that owns each |
| 6 | ~~README / `9bc65f0` vs the code~~ | — | **resolved 2026-09-15:** README corrected to what exists | — | — |

## 8. Log

| Process | Build | Date | Covered | Result | Gate |
|---|---|---|---|---|---|
| 00 | `13bb122` | 2026-09-14/15 | [`00-setup.md`](00-setup.md) | config, adapter, session injection, harness smoke on `qa`, 33 sweep rows; suite 688 host + 32 instrumented, 0 failures | ✅ closed 2026-09-15 |
| 01 | `13bb122` + fixes | 2026-09-15 | [`01-shell.md`](01-shell.md) | 45 controls + launcher entry point; 4 severe fixed with 17 tests seen red (process death, `/` crash, ended session, plain-text password); 14 P2 queued; suite 722 host + 32 instrumented, 0 failures; `QA_` fixture deleted | ✅ closed 2026-09-15 |
| 01 (P2 round) | `584b4e1` + P2 fixes | 2026-09-15 | [`01-shell.md`](01-shell.md) | 14 P2s fixed (SH-05…SH-12, SH-14…SH-19) + SH-20 found and fixed; 20 new tests seen red; APK sha256 `69ed4bda…`; suite 754 host + 32 instrumented, 0 failures (the first run failed to compile two stale test fakes in feature/locations — R13) | ✅ |

## 9. Close-out

*(written when the last gate closes)*
