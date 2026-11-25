# Android Expressive redesign: canvas vs app audit (2026-11-22)

Each artboard in `design/native-ui/` (light block only) against the current Android screen and the ViewModel/UiState it reads, which must not change in this phase. Labels: **provided** (field named), **derive** (computed on Android from exposed data), **cross-VM** (needs a second shared ViewModel on the Android screen), **drop**. Artboards are 412x915 and clipped, so "not drawn" means "needs a home", never "removed". Paths: `<module>/…` = `feature/<module>/src/androidMain/kotlin/com/desarrollodroide/adventurelog/feature/<module>/`; `common/…` = the same under `commonMain`.

## Shell (applies to every tab)

- Artboards give each tab its own large title and no shared app bar. The shell's `TopAppBar` (`home/…/ui/screen/MainShell.kt:319-476`) carries three things that must survive: **global search** (`:437-451`, opens `GlobalSearchSheet`), the **account avatar** (`ProfileMenu`, `:453-465`) and the collection **breadcrumb** with Back/Home (`:324-388`). Drawn homes: avatar on Home (`Account`) and inside the Map search pill. Suggestion: a 48dp trailing action row on every tab title (search icon + avatar), as Places/Collections already draw for their own icons.
- Account sheet (`home/…/ui/components/ProfileMenu.kt:69-153`): account header (opens Settings), Calendar, People, Settings, Server (read-only URL), Sign out. Not drawn; keep the sheet as is behind the avatar. Calendar and People have no other entry point.
- Nav: the 5 destinations and their order match (`HomeBottomBar.kt:61-70`). `NavigationSuiteScaffold` (`MainShell.kt:302`) turns the bar into a rail on tablets; keep it.

## Home — `Main.dc.html` vs `home/…/ui/screen/DashboardScreen.kt`

Data:
- "Hi John": **provided** `userDetails.firstName` (shell uses `fullName`, `MainShell.kt:186`).
- "your next trip starts in 8 days": **provided** `dashboard.activeTrip ?: upcomingTrips.first()` (`DashboardScreen.kt:166`) `.daysUntilStart`; the copy must also cover `status == IN_PROGRESS`, 0 and 1 days, as `tripSubtitle` does (`:545-551`), and no trip at all.
- Ticket name, photo, "Planned": **provided** `UltraSlimCollection.name`, `featuredImage`, `status`.
- Ticket "Liguria, Italy": **drop**. `UltraSlimCollection` has no place; `description` is the only text available.
- FROM / TO: **provided** `startDate` / `endDate` (nullable; format as `tripDates`, `:666`). PLACES: **provided** `adventureCount`.
- "Your map so far · 22 places · 9 countries": **provided** `stats.locationCount`; "9 countries" is visited + planned on the canvas, but `UserStats` only has `visitedCountryCount`. Relabel as "N countries visited".
- Country strip (NO, JP, IS, MA + "IT Planned"): **drop**. `Dashboard` has counts only. Taking countries from `recentLocations[].country` shows only the handful of recent places and passes them off as the whole map. **cross-VM** alternative: `WorldViewModel.uiState.countries.filter { numVisits > 0 }`, which costs a full countries request on Home. "Planned" countries exist nowhere: **drop**.
- "Recently visited": **drop the claim**, keep "Recently updated" (`:280`, `:439`). `recentLocations` is the server's `recent_locations` (`DashboardDTO.kt:21`), not visits. Photo-only tiles need a fallback for places without images (`PlaceMiniCard`, `:1056`).

Controls:
- Account (JD): drawn → `ProfileMenu`.
- Ticket tap → trip: drawn. Ticket "Open trip" and "Add a place" (into that trip, `onAddPlaceToTrip`, `:646-661`): not drawn. Suggestion: one tonal "Add a place" pill on the ticket.
- "See all" → Places: drawn. "22 places · 9 countries" links to World: drawn (new target).
- Global search: not drawn (see Shell).
- Stats card (Countries/Regions/Cities/Places visited with totals, `:725-777`): not drawn. Suggestion: the "Your map so far" link row expands into it, or a 4-up figure row under the heading.
- Coming up (other trips + `upcomingEvents`, rows open place/collection, "See all" → Calendar; `:404-432`): not drawn. Suggestion: a "Coming up" section after the ticket, same list-row style as Calendar's "Coming up".
- Invites banner (`inviteCount`, `:121-126`, opens Collections on Invites): not drawn. Suggestion: tonal banner above the headline.
- "Plan the trip" card and two-column layout ≥1000dp (`:201-245`, `:334-367`), app-bar "Add place" ≥1000dp (`MainShell.kt:420-435`): not drawn; keep for tablets.
- First run (Add a place / Create a collection, `:465-511`), error "Try again" (`:103-115`), subtitle "N places visited · N trips ahead" (`MainShell.kt:1083`): not drawn; keep.

## Places — `Android-Places` vs `locations/…/ui/screens/locationsList/LocationScreen.kt`

Data:
- "22 places": **provided** `libraryCounts.locationCount` (`LocationScreen.kt:286-300`). "9 countries": same issue as Home, relabel to `visitedCountryCount` "countries visited" or **drop**.
- Chips "All 22 / Visited 17 / Planned 5": **provided** `filters.visitedFilter` ALL/VISITED/NOT_VISITED (`common/…/model/LocationFilters.kt:28`); counts `locationCount`, `visitedLocationCount`, planned **derive** = difference.
- Chips Country, Rating, Collection: **drop**. `LocationFilters` has no such filters (`includeCollections` is a different thing). Category: **provided** `filters.categoryNames`.
- Grouping by country with per-country counts ("Norway 3"): **drop**. The list is server-paged (`AdventuresPagingSourceFiltered`, `page_size`) and sorted by `updated_at` by default; `LocationSortField` has no country sort, so groups would fragment and counts would cover loaded pages only.
- Row "Trolltunga · Vestland · Hiking · ★ 5": **provided** `name`, `region?.name` (mapped in `LocationDTO.kt:105-107` when the server sends it), `category.displayName`, `rating`; thumbnail `images`.

Controls:
- Search icon: drawn → `SimpleSearchBar` (`:308-315`, `onSearchQueryChange` + `executeSearch`); the icon must expand into the field.
- Sort icon: drawn → sort part of `LocationsFilterBottomSheet` (field UPDATED_AT/VISIT_DATE/NAME/RATING + direction).
- More (⋮): drawn, empty. Home for "Include places from collections" and "Manage categories" (`:129-135`, `AddEditCategorySheet`).
- Filter badge count (`activeFilterCount`, `:317-323`): not drawn. Suggestion: show it on the Category chip.
- FAB "New place": drawn (`:327-338`).
- Per-row overflow `LocationActionsSheet` (Open details, Edit, Duplicate, Share externally, Manage collections, Delete + confirm; `ui/…/components/LocationActionsSheet.kt:48-64`): not drawn. Suggestion: long-press, plus a trailing ⋮ on each row.
- Card inline chips "Add photo", "+ Category", Private, collections, tags, price (`ui/…/components/AdventureItem.kt:198-248`): not drawn. Suggestion: keep "Add photo" in the overflow sheet; the others go.
- Pull-to-refresh (`:347`), paging error/retry, empty and no-results states, snackbars: not drawn; keep.
- Tablet two-pane with auto-selected first place (`home/…/ui/screen/PlacesPane.kt`, `ListDetailSceneStrategy`): not drawn; keep.

## Place — `Android-Place` vs `detail/…/ui/screen/AdventureDetailScreen.kt`

Data:
- Photo pager "1 / 4": **provided** `images`. Category, "Visited", name, stars: **provided** `category`, `isVisited`, `name`, `rating`.
- "Vestland, Norway": **provided** `region.name` + `country.name` (the app currently shows `location.location`).
- VISITED "6 JUL 26": **derive** latest `visits[].startDate`. VISITS: **provided** `visits.size`.
- TRIP "Fjords": **provided** `AdventureDetailViewModel.collections`; there can be several (`:264-273`).
- About: **provided** `description` (Markdown, `AboutBody`).
- Visit row "6 Jul 2026 · Norwegian Fjords": the date is **provided**; the trip name **derive** by matching the visit date into a `collections` start/end range (a heuristic, `Visit` has no collection), else **drop**.

Controls:
- Back: drawn (hidden in two-pane, `showBack`). Share: drawn → `shareLocation`. Edit FAB: drawn → `onEditClick`.
- Open in maps: drawn → `OpenInMapsSheet` (`:110-118`), only when lat/lon exist. Website: drawn → `location.link`, hide when blank (replaces the "Link" section, `:293-297`).
- "Add visit": no backing action. Route it to the edit form scrolled to Visits, or **drop**.
- Not drawn, keep below the fold: Photos carousel and fullscreen viewer (`:244-248`); no-photo "Add photo" card (`:234-236`); "Where" map preview (`:250-256`, opens the maps sheet); Attachments with open progress (`:275-285`); Trails (open link, `:287-291`); tags and Public chips (`CategoryTags`); Details (price, Added, Last updated, `:300-311`); load error with "Go Back".

## Collections — `Android-Collections` vs `collections/…/ui/screens/CollectionsScreen.kt`

Data:
- "6 collections · Mine": **provided** `collectionsHeader(tab, collectionCount, tabContent)`.
- Tabs Mine / Shared / Archived / Invites with badge "1": **provided** `CollectionsTab`, `pendingInvites.size`.
- Card name, photo, "IN 8 DAYS" / "PLANNED", DATES, PLACES: **provided** `UltraSlimCollection.name`, `featuredImage`, `status` + `daysUntilStart`, `startDate`/`endDate`, `adventureCount`. FOLDER collections (no dates) need a variant.

Controls:
- Search icon: drawn → `SimpleSearchBar` (`:328-333`). FAB "New collection": drawn (`:361-372`).
- Tabs: drawn. Today the Mine/Shared/Archived set lives in the filter sheet's "Show" (`CollectionsFilterSheet.kt`) and Invites through the banner; the tabs replace both.
- Filter & sort sheet (trip status, sort NAME/START_DATE/UPDATED_AT, direction, Reset; `:335-341`) and active-filter chips (`:346-351`): not drawn. Suggestion: Sort and ⋮ icons as on Places.
- Card overflow `CollectionActionsSheet` (Open, Edit, Share with people → `ShareCollectionSheet`, Share externally, Duplicate, Download PDF, Export ZIP, Archive/Restore, Delete + confirm dialog; `CollectionActionsSheet.kt:107-135`, `CollectionsScreen.kt:250-272`): not drawn. Suggestion: long-press + ⋮ per card.
- Invite Accept / Decline (`:815-817`): not drawn. Suggestion: two buttons on each Invites-tab card.
- Pull-to-refresh (`:381`), paging error/retry, empty state, busy label, snackbars: keep.
- Tablet two-pane (`home/…/ui/screen/CollectionsPane.kt:86-138`): keep.

## Collection — `Android-Collection` vs `collections/…/ui/screens/CollectionDetailScreen.kt`

Data (`CollectionDetailUiState.collection: Collection`):
- Name, "5 SEP → 9 SEP": **provided** `name`, `startDate`/`endDate`.
- "PLANNED" and "8 DAYS TO GO": `Collection` has no `status` or `daysUntilStart` (only `UltraSlimCollection` does). **derive** from the dates against today on Android, or **cross-VM** from `allCollections` by id (same VM, `CollectionDetailViewModel.kt:135`).
- "LIGURIA, ITALY": **derive** from the most common `locations[].region/country`, else **drop**.
- "Packing list · 3 of 7": **derive** from `checklists` (`items.count { isChecked }` / `items.size`); decide which list when there are several.
- Tabs All / Places / Transport / Lodging / Notes / Checklists: **provided** `CollectionTab`.
- Day-grouped "All" ("SEP 5 Saturday · Day 1 · arrival", train leg, places): this is the Itinerary view's data, **provided** `itinerary` / `itineraryDays` through `collection.itinerary()`. It is empty until built, and the app's "All" tab today is type sections (`:424-830`). A fallback to type sections is needed.
- Transport row "La Spezia → Vernazza · Train", place rows "Liguria · General": **provided** `Transportation` (summary, type) and `Location`.

Controls:
- Back: drawn. Home breadcrumb (`onHomeClick`, `:835-875`): not drawn; Back is enough on Android.
- Edit collection: drawn; the route exists (`navigateToEditCollection`, `MainShell.kt:818`) but it is not on this screen today.
- Share collection: drawn; sharing and export live on `CollectionsViewModel` (`openSharing`, `exportCollection`), not on the detail VM, so **cross-VM**. Suggestion: the Share button opens a sheet with Share with people / Share externally / PDF / ZIP.
- **View switcher** Items / Itinerary / Map / Calendar / Nearby / Stats (`CollectionView.kt:11-17`, `:349-422`): not drawn; this is the biggest gap. Suggestion: a segmented row above the tabs, or an overflow "View as" menu in the header.
- Itinerary controls ("Build the itinerary", add to a day → `ItineraryItemPicker`, remove from a day, trip-context bucket; `CollectionItineraryView.kt:91-273`) and Nearby controls (anchor, query, category, radius, Search, Add; `CollectionRecommendationsView.kt:123-269`): not drawn; they follow the view switcher.
- "Add to collection" FAB: drawn, single. Today each tab has its own "+" (`TabHeading`, `:1314`) for place, transport, lodging, note, checklist. Suggestion: an FAB menu with those five.
- Place row overflow (`LocationActionsSheet` with Remove from collection; `ManageCollectionsDialog`, `:262-276`), transport card menu (Edit, Delete + confirm, "Not in itinerary date range", `:1014-1092`), lodging/note/checklist cards (tap to edit, delete + confirm, `:1360-1414`): not drawn. Suggestion: tap opens, long-press/⋮ for the rest.
- Public/Private and Active/Archived chips, description, tab counts, owner vs guest (`currentUserId`): not drawn; keep chips in the header line.

## Editors — `Android-*Form` vs the `AddEdit*Screen.kt` files

Eyebrow "CINQUE TERRE" on the child forms: no form state carries the collection name, and the routes carry only `collectionId` (`core/common/…/NavigationRoutes.kt:69-93`). **derive** by adding a name argument to these Android-only routes, else **drop**.

- **PlaceForm** (`locations/…/ui/screens/addEdit/AddEditLocationScreen.kt`). Drawn and **provided** in `formData`: name, category chips + "+ Category" (`CategoryDropdown`, "Add new category"), rating, Add photo (`ImagesSection`), "Where it is" search, Save/Cancel. With many categories the chips need horizontal scroll. Not drawn: Website link, Description + "Generate from Wikipedia", Public, Price + currency (`BasicInfoSection.kt:157-309`); Location text, results list, map preview, clear (`LocationSection.kt`); Tags; Trails; Visits (all day, timezone, start/end, notes, edit/delete; `DateSection.kt`, `VisitItem.kt`); discard dialog; load error retry. Suggestion: collapsed "More details" sections below the fold.
- **CollectionForm** (`AddEditCollectionScreen.kt` + `addEdit/components`). Name, description, link, start, end, public: all **provided**. Nothing lost.
- **LodgingForm** (`addEditLodging/AddEditLodgingScreen.kt`). Name, check-in/out, reservation, price + currency: **provided**. Type shows 5 of 11 (`AddEditLodgingViewModel.kt:18`: hotel, hostel, resort, bnb, campground, cabin, apartment, house, villa, motel, other); needs a scrollable chip row. "Search an address": the VM has no geocoding, `location` is plain text, so **drop** the search affordance. Not drawn: Booking link, Notes, Public.
- **TransportForm** (`addEditTransportation/`). Type shows 5 of 8 (adds bike, walking, other, `TransportationFormData.kt:74-82`). FROM/TO: **provided**, with search (`LocationTransportationSection.kt`); subtitles "Centrale station" **drop**. Swap: **derive** (swap the two form values). Departs/Arrives, Name: **provided**. "Keep to the trip's dates": **provided** ("Constrain to Collection Dates", `collectionStart`/`collectionEnd`). Not drawn: All day, Flight number (plane), Link, Description + Generate, Route map.
- **NoteForm** (`addEditNote/AddEditNoteScreen.kt`). Title, note, date: **provided**. Formatting toolbar (B / I / list / link): no backing; **derive** by inserting Markdown into `content`, or **drop**. Not drawn: Public note.
- **ChecklistForm** (`addEditChecklist/AddEditChecklistScreen.kt`). Title, items with checkboxes, add item, "3 / 7": **provided** (`lines`, `draft`, `doneCount`). Not drawn: remove item (`:288`), Date (optional), Public checklist.

## Map — `Android-Map` vs `map/…/ui/screen/MapScreen.kt`

Data:
- Markers, clusters with count: **provided** `MapUiState.locations` → `mapMarkers(filters)`, clustering (`AdventureMapView.kt:184-198`). Cluster label "4 places in Iceland": count **provided**, country **drop** (clusters are spatial).
- Selected card "Geirangerfjord · Møre og Romsdal · ★ 5 · visited": **provided** from the tapped `Location`.

Controls:
- Filter All / Visited / Planned: drawn → `toggleVisitedFilter` / `togglePlannedFilter` (two toggles today; All = both on).
- Map layers: drawn → `MapFilterSheet` (categories, visited regions, visited cities; `MapFilterSheet.kt:54-209`).
- Marker tap: today it opens the place directly (`AdventureMapView.kt:186-189`); the canvas selects it and shows a card with Open (→ detail) and Directions (**derive** → `OpenInMapsSheet` from `feature/ui`).
- "Search your map": no backing; **derive** a local name filter over `locations`, or **drop**. "My location": none today (`isMyLocationEnabled = false`, `:106`) and it needs a location permission. **drop** for this phase.
- Avatar in the search pill: drawn → `ProfileMenu`.
- Not drawn: visited / planned / regions counts and the filter button (`ClearStatsSection`, `MapScreen.kt:42-52`; suggestion: counts on the chips); "Only places with coordinates are shown" note; error + "Try again" (`MapContent.kt:50-65`). `onAddAdventureClick` is passed in but unused (`MapScreen.kt:20`).

## Calendar — `Android-Calendar` vs `calendar/…/ui/screen/CalendarScreen.kt`

Data:
- Month grid with the "Cinque Terre" bar on 5-9: **derive** from `CalendarUiState.days` (`CalendarEvent.start`/`end`). The window is a year back and two forward (`CalendarViewModel.kt:94-110`); months before `windowStart` need `loadEarlier()`.
- "Coming up" row "Cinque Terre · 5–9 Sep · in 8 days": title and dates **provided** (`type == "collection"` events); "in 8 days" **derive** from `today`; "2 places" **drop** (`CalendarEvent` has no count).

Controls:
- Month / Agenda toggle, previous / next month: drawn, new; Agenda is the current screen.
- Back: drawn. Today the shell has no Back here (reached from the account sheet); Back → Home.
- Not drawn: type filter chips All + per type (`:250-278`; suggestion: under the toggle); "Show everything before …" with its retry (`:281-320`); Today marker; event rows open place or trip (`target()`); error "Try again"; empty state.

## World + Country — `Android-World`, `Android-Country` vs `world/…/ui/screen/WorldScreen.kt`, `CountryDetailScreen.kt`

World data (`WorldUiState`):
- "7 countries visited": **provided** `visitedCountriesCount`. "2 more planned": **drop** (no planned countries).
- REGIONS 14: **derive** Σ `countries.numVisits` (`numVisits` is visited regions, `CountryCard.kt:102`). PLACES 22: **drop**, or **cross-VM** (`UserStats.locationCount`). CONTINENTS 3: **derive** from `subregion` → continent table over visited countries.
- Row "NO · Norway · 3 regions · 3 places": `countryCode`, `name`, `numVisits` **provided**; "places" **drop**.

World controls:
- Search icon: drawn → `onSearchQueryChanged` (`WorldScreen.kt:162`).
- Chips Visited / Planned / All: Visited **derive** (filter `filteredCountries` by `numVisits > 0`); Planned **drop**; All = `FilterMode.ALL`.
- Not drawn: Complete / Partial / Not visited modes (`:318-366`), world-region dropdown (`WorldRegion`, `:231-300`), "Map view" button → Map tab (`:167-169`), pull-to-refresh (`:109`), progress section, empty state. Suggestion: a ⋮ / filter icon beside Search holding region and the finer modes.

Country data (`CountryDetailUiState`):
- "3 / 15 regions visited": **provided** `visitedCount` / `regions.size`.
- "Europe": **derive** from `country.subregion` (or show the subregion itself). "· 3 places": **drop**.
- Region rows Visited / Mark visited: **provided** `RegionRow.visited`, `onRegionToggled`. "N cities" (`:277`): not drawn; keep as supporting text.

Country controls: Back, Search regions, toggle: all drawn. Not drawn: error retry (`:97`), message snackbar, "no regions listed" state.

## Settings, People, Legal — `feature/settings/…`

Settings data:
- Name, "@claude · [server]": **provided** `UserDetails` + `getServerUrl()`. "Online": **drop** (no reachability state).
- Theme, Dynamic colour, Map style, Units, Currency, Public profile: **provided** (`AppearanceGroup`, `PreferencesGroup`, `ProfileForm`). The canvas moves Map style under Appearance, which is fine.

Settings controls (`ui/screen/SettingsScreen.kt`):
- Profile card tap → Edit profile dialog (`:180`): drawn card, keep the action.
- "Sign-in & email" row: drawn as one row. It hides Change / Set password and the email list (add, verify, make primary, remove; `SignInGroup.kt:66-275`). Suggestion: a sub-page behind the row.
- Not drawn (below the fold): Your data (Update visited regions, Download a backup; `VisitedRegionsCard.kt`), Media storage + Retry (`StorageSection.kt`), About (Server, Account, App version, Server setup guide, Source code, Send feedback, Terms, Privacy; `AboutSection.kt:45-98`), Sign out + confirm (`:288-326`).

People (`ui/screen/UsersScreen.kt`, `UsersUiState`):
- Name, "@ana", initials: **provided** `PublicUser.displayName`, `username`, `profilePic`.
- "41 places · 12 countries" per row: **drop**. Stats exist only per opened profile (`ProfileState.stats`).
- Search: drawn → `onQueryChange`. Row tap → `ProfileSheet` with stats + retry (`:249-295`): not drawn, keep. Error "Try again", empty state: keep.

Legal (today two screens, `TermsOfUseScreen.kt` / `PrivacyPolicyScreen.kt`, opened from About):
- Terms / Privacy tabs: drawn, new. "Last updated": **provided** `LEGAL_EFFECTIVE`. Numbered sections: **provided** `LegalText.sections`. "Contact us": **provided** `LEGAL_CONTACT` (`common/…/ui/components/LegalTexts.kt:10-17`).

## Login — `Android-Login` vs `login/…/ui/screen/LoginScreen.kt`

Data:
- Server, Username, Password: **provided** `LoginFormState`. Server "Found" badge: **drop** (no server-probe state in `LoginUiState`).

Controls:
- Show password, Sign in: drawn (`PasswordTextField`, `LoginButton`). "How do I set up a server?": new; reuse the "Server setup guide" URL from `AboutSection.kt:74`.
- Not drawn: Remember me (`RememberSessionSection`, `:236-239`; suggestion: a row under Sign in); field errors (`userNameError`, `passwordError`, `urlError`) and the `LoginUiState.Error` message; the Loading and CheckingSession states.
