package com.desarrollodroide.adventurelog.feature.map

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedCitiesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.core.testing.CountriesRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testUser
import com.desarrollodroide.adventurelog.feature.map.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * What the map decides to draw, and what the card above it claims.
 *
 * Every number on that card has been wrong at some point in a way nothing flagged: the visited
 * count once came from the collection count, so an account with two collections read "33 visited".
 */
class MapViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun before() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun after() = Dispatchers.resetMain()

    private fun place(
        name: String,
        lat: String? = "40.4",
        lon: String? = "-3.7",
        visited: Boolean = false,
        category: String? = null,
        tags: List<String> = emptyList()
    ) = Location(
        id = name,
        name = name,
        createdAt = "2024-01-01",
        updatedAt = "2024-01-01",
        latitude = lat,
        longitude = lon,
        isVisited = visited,
        tags = tags,
        category = category?.let {
            Category(id = it, name = it.lowercase(), displayName = it, icon = "", numAdventures = "1")
        },
        user = testUser
    )

    private class Countries(
        private val regions: Either<ApiResponse, List<VisitedRegion>> = Either.Right(emptyList()),
        private val cities: Either<ApiResponse, List<VisitedCity>> = Either.Right(emptyList())
    ) : CountriesRepositoryStub() {
        override suspend fun getVisitedRegions() = regions
        override suspend fun getVisitedCities() = cities
    }

    private fun viewModel(
        locations: Either<ApiResponse, List<Location>> = Either.Right(emptyList()),
        regions: Either<ApiResponse, List<VisitedRegion>> = Either.Right(emptyList()),
        cities: Either<ApiResponse, List<VisitedCity>> = Either.Right(emptyList()),
        savedState: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle()
    ): MapViewModel {
        val locationsRepo = object : LocationsRepositoryStub() {
            override suspend fun getAllLocations() = locations
        }
        val countries = Countries(regions = regions, cities = cities)
        return MapViewModel(
            getAllLocationsUseCase = GetAllLocationsUseCase(locationsRepo),
            getVisitedRegionsUseCase = GetVisitedRegionsUseCase(countries),
            getVisitedCitiesUseCase = GetVisitedCitiesUseCase(countries),
            savedStateHandle = savedState
        )
    }

    /** A server that answers differently the second time, for the retry. */
    private class Flaky(var answer: Either<ApiResponse, List<Location>>) : LocationsRepositoryStub() {
        override suspend fun getAllLocations() = answer
    }

    private fun viewModelWith(locations: Flaky): MapViewModel {
        val countries = Countries()
        return MapViewModel(
            getAllLocationsUseCase = GetAllLocationsUseCase(locations),
            getVisitedRegionsUseCase = GetVisitedRegionsUseCase(countries),
            getVisitedCitiesUseCase = GetVisitedCitiesUseCase(countries)
        )
    }

    private fun region(name: String) = VisitedRegion(
        id = name.hashCode(), userId = "u", regionId = name, name = name,
        longitude = 0.0, latitude = 0.0
    )

    private fun city(name: String) = VisitedCity(
        id = name.hashCode(), userId = "u", cityId = name, name = name,
        longitude = 0.0, latitude = 0.0
    )

    // --- what reaches the map ---------------------------------------------------------

    @Test
    fun aPlaceWithNoCoordinatesIsNotDrawn() = runTest(dispatcher) {
        val vm = viewModel(
            locations = Either.Right(
                listOf(place("Prado"), place("Nowhere", lat = null, lon = null))
            )
        )
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Prado"), vm.uiState.value.locations.map { it.name })
    }

    @Test
    fun nullIslandIsNotACoordinate() = runTest(dispatcher) {
        // "0.0"/"0.0" is what the server stores for a place whose location was never resolved,
        // and drawing it puts a pin in the Atlantic off Ghana.
        val vm = viewModel(
            locations = Either.Right(
                listOf(place("Prado"), place("Unresolved", lat = "0.0", lon = "0.0"))
            )
        )
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Prado"), vm.uiState.value.locations.map { it.name })
    }

    @Test
    fun aBlankCoordinateIsNotACoordinateEither() = runTest(dispatcher) {
        val vm = viewModel(locations = Either.Right(listOf(place("Blank", lat = "", lon = ""))))
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.locations.isEmpty())
    }

    // --- the counts on the card -------------------------------------------------------

    @Test
    fun theCountsAddUpToWhatIsOnTheMap() = runTest(dispatcher) {
        val vm = viewModel(
            locations = Either.Right(
                listOf(
                    place("Prado", visited = true),
                    place("Peñalara", visited = false),
                    // Visited, but with nowhere to draw it - so it must not be counted either.
                    place("Ghost", lat = null, lon = null, visited = true)
                )
            )
        )
        testScheduler.advanceUntilIdle()

        val filters = vm.uiState.value.filters
        assertEquals(1, filters.visitedCount)
        assertEquals(1, filters.plannedCount)
        assertEquals(
            vm.uiState.value.locations.size,
            filters.visitedCount + filters.plannedCount
        )
    }

    @Test
    fun theRegionCountIsTheVisitedRegionsNotTheCollectionsNorADefault() = runTest(dispatcher) {
        // tripsCount - the number of collections - sat in this field once, and an account with
        // two collections reported 33 visited regions. Then it came from stats that answer a
        // failure with zeros (QA 06). It is the visited regions the map loads.
        val vm = viewModel(
            regions = Either.Right(listOf(region("JP-26"), region("NO-46")))
        )
        assertFalse(vm.uiState.value.regionsLoaded)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.regionsLoaded)
        assertEquals(2, vm.uiState.value.filters.regionCount)
    }

    @Test
    fun regionsThatFailToLoadLeaveTheirCountUnknown() = runTest(dispatcher) {
        val vm = viewModel(regions = Either.Left(ApiResponse.IOException))
        testScheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.regionsLoaded)
    }

    @Test
    fun theCityCountIsHowManyCitiesCameBack() = runTest(dispatcher) {
        val vm = viewModel(cities = Either.Right(listOf(city("Madrid"), city("Lisboa"))))
        testScheduler.advanceUntilIdle()

        assertEquals(2, vm.uiState.value.filters.cityCount)
        assertEquals(listOf("Madrid", "Lisboa"), vm.uiState.value.visitedCities.map { it.name })
    }

    // --- the chips --------------------------------------------------------------------

    @Test
    fun categoryChipsAreCountedOverThePlacesTheMapDraws() = runTest(dispatcher) {
        val vm = viewModel(
            locations = Either.Right(
                listOf(
                    place("Prado", category = "Museum"),
                    place("Reina Sofía", category = "Museum"),
                    place("Café", category = "Food"),
                    // Not drawn, so it must not inflate the Museum chip.
                    place("Ghost", lat = null, lon = null, category = "Museum")
                )
            )
        )
        testScheduler.advanceUntilIdle()

        // Sorted by how many there are, so the chip a user is most likely to want comes first.
        assertEquals(listOf("Museum" to 2, "Food" to 1), vm.uiState.value.categoryCounts)
    }

    @Test
    fun anImportersOwnTagsAreNotOfferedAsActivities() = runTest(dispatcher) {
        val vm = viewModel(
            locations = Either.Right(
                listOf(
                    place("Prado", tags = listOf("art", "3f2504e0-4f89-11d3-9a0c-0305e82c3301")),
                    place("Peñalara", tags = listOf("walking", "art"))
                )
            )
        )
        testScheduler.advanceUntilIdle()

        // No UUID, no duplicate, alphabetical.
        assertEquals(listOf("art", "walking"), vm.uiState.value.activityTypes)
    }

    // --- the switches -----------------------------------------------------------------

    @Test
    fun theVisitSwitchesStartOnSoTheFirstMapShowsEverything() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.filters.showVisited)
        assertTrue(vm.uiState.value.filters.showPlanned)
        // Regions and cities are overlays, and start off: they cover the pins underneath.
        assertEquals(false, vm.uiState.value.filters.showRegions)
        assertEquals(false, vm.uiState.value.filters.showCities)
    }

    @Test
    fun togglingACategoryAddsItThenTakesItAway() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.toggleCategory("Museum")
        assertEquals(setOf("Museum"), vm.uiState.value.filters.selectedCategories)

        vm.toggleCategory("Museum")
        assertTrue(vm.uiState.value.filters.selectedCategories.isEmpty())
    }

    @Test
    fun clearingPutsBackEverythingRatherThanTurningEverythingOff() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.toggleCategory("Museum")
        vm.toggleActivityTypeFilter("art")
        vm.toggleVisitedFilter()
        vm.clearFilters()

        val filters = vm.uiState.value.filters
        assertTrue(filters.selectedCategories.isEmpty())
        assertTrue(filters.selectedActivityTypes.isEmpty())
        // Leaving showVisited off would make "clear filters" hide half the map.
        assertTrue(filters.showVisited)
        assertTrue(filters.showPlanned)
    }

    // --- failure ----------------------------------------------------------------------

    @Test
    fun aFailedLoadStopsTheSpinnerAndSaysSo() = runTest(dispatcher) {
        val vm = viewModel(locations = Either.Left(ApiResponse.IOException))
        testScheduler.advanceUntilIdle()

        assertEquals("Failed to load places", vm.uiState.value.error)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun regionsFailingDoesNotTakeTheMapWithIt() = runTest(dispatcher) {
        val vm = viewModel(
            locations = Either.Right(listOf(place("Prado"))),
            regions = Either.Left(ApiResponse.IOException)
        )
        testScheduler.advanceUntilIdle()

        // The overlay is off by default anyway; the pins are the point of the screen.
        assertEquals(listOf("Prado"), vm.uiState.value.locations.map { it.name })
        assertNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.visitedRegions.isEmpty())
    }

    @Test
    fun refreshingAsksAgain() = runTest(dispatcher) {
        var calls = 0
        val locationsRepo = object : LocationsRepositoryStub() {
            override suspend fun getAllLocations(): Either<ApiResponse, List<Location>> {
                calls++
                return Either.Right(listOf(place("Prado")))
            }
        }
        val countries = Countries()
        val vm = MapViewModel(
            getAllLocationsUseCase = GetAllLocationsUseCase(locationsRepo),
            getVisitedRegionsUseCase = GetVisitedRegionsUseCase(countries),
            getVisitedCitiesUseCase = GetVisitedCitiesUseCase(countries)
        )
        testScheduler.advanceUntilIdle()
        assertEquals(1, calls)

        vm.refresh()
        testScheduler.advanceUntilIdle()

        assertEquals(2, calls)
    }

    @Test
    fun aMapThatFailedCanBeTriedAgain() = runTest(dispatcher) {
        // With no network the map showed "Error loading map data" and stayed there: nothing
        // reloaded it, not the tab, not coming back to the app (measured).
        val server = Flaky(Either.Left(ApiResponse.IOException))
        val vm = viewModelWith(server)
        testScheduler.advanceUntilIdle()
        assertEquals("Failed to load places", vm.uiState.value.error)

        server.answer = Either.Right(listOf(place("Prado")))
        vm.refresh()
        testScheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.error)
        assertEquals(listOf("Prado"), vm.uiState.value.locations.map { it.name })
    }

    // --- through process death (QA 06, MP-03) ----------------------------------------------

    @Test
    fun theChosenFiltersComeBackAfterTheProcessDies() = runTest(dispatcher) {
        val saved = androidx.lifecycle.SavedStateHandle()
        val before = viewModel(savedState = saved)
        before.toggleVisitedFilter()
        before.toggleShowCities()
        before.toggleCategory("Nature")
        before.toggleCategory("City")

        // A new view model over the same saved state is what Android hands back after a kill.
        val after = viewModel(savedState = saved)
        testScheduler.advanceUntilIdle()

        with(after.uiState.value.filters) {
            assertEquals(false, showVisited)
            assertEquals(true, showCities)
            assertEquals(setOf("Nature", "City"), selectedCategories)
        }
    }

    @Test
    fun aMapWithNothingSavedStartsFromTheDefaults() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        with(vm.uiState.value.filters) {
            assertEquals(true, showVisited)
            assertEquals(true, showPlanned)
            assertEquals(false, showCities)
            assertEquals(emptySet(), selectedCategories)
        }
    }

}
