package com.desarrollodroide.adventurelog.feature.locations

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DuplicateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetShareImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetUserStatsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.locations.model.LocationFilters
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState
import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The Places list.
 *
 * Most of what this view model does is choose which request to make, and both choices come back
 * looking like a list of places - so the wrong one is invisible until someone counts what is
 * missing. These tests watch the choice rather than the result.
 */
class LocationsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun before() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun after() = Dispatchers.resetMain()

    private val place = Location(
        id = "1",
        name = "Prado",
        createdAt = "2024-01-01",
        updatedAt = "2024-01-01",
        user = testUser
    )

    /** Notes down which of the two list endpoints was asked for, and with what. */
    private class RecordingLocations(
        private val onShare: Either<ApiResponse, ByteArray>? = null,
        private val onDuplicate: Either<ApiResponse, Location>? = null,
        private val onDelete: Either<ApiResponse, Unit>? = null
    ) : LocationsRepositoryStub() {
        var plainCalls = 0
        var filteredCalls = 0
        var lastIncludeCollections: Boolean? = null
        var lastSortBy: String? = null
        var lastIsVisited: Boolean? = null
        var lastSearch: String? = null

        override fun getLocationsPagingData(): Flow<PagingData<Location>> {
            plainCalls++
            return flowOf(PagingData.empty())
        }

        override fun getLocationsPagingDataFiltered(
            categoryNames: List<String>?,
            sortBy: String?,
            sortOrder: String?,
            isVisited: Boolean?,
            searchQuery: String?,
            includeCollections: Boolean
        ): Flow<PagingData<Location>> {
            filteredCalls++
            lastIncludeCollections = includeCollections
            lastSortBy = sortBy
            lastIsVisited = isVisited
            lastSearch = searchQuery
            return flowOf(PagingData.empty())
        }

        override suspend fun getShareImage(locationId: String, aspect: String) =
            onShare ?: super.getShareImage(locationId, aspect)

        override suspend fun duplicateLocation(locationId: String) =
            onDuplicate ?: super.duplicateLocation(locationId)

        override suspend fun deleteLocation(adventureId: String) =
            onDelete ?: super.deleteLocation(adventureId)
    }

    private class RecordingCategories(
        private val answer: Either<ApiResponse, List<Category>> = Either.Right(emptyList()),
        private val onCreate: Either<ApiResponse, Category>? = null
    ) : CategoriesRepositoryStub() {
        var lastCreatedName: String? = null
        var lastCreatedDisplayName: String? = null
        var getCalls = 0

        override suspend fun getCategories(): Either<ApiResponse, List<Category>> {
            getCalls++
            return answer
        }

        override suspend fun createCategory(name: String, displayName: String, icon: String?):
            Either<ApiResponse, Category> {
            lastCreatedName = name
            lastCreatedDisplayName = displayName
            return onCreate ?: Either.Right(
                Category(id = "c", name = name, displayName = displayName, icon = icon ?: "", numAdventures = "0")
            )
        }
    }

    private class NoCollections : CollectionsRepositoryStub() {
        override suspend fun getAllCollections(
            forceRefresh: Boolean
        ): Either<ApiResponse, List<UltraSlimCollection>> = Either.Right(emptyList())
    }

    /** Says no to everything, which is how a device with no share sheet behaves. */
    private class RefusingFiles : PlatformFiles {
        override suspend fun open(bytes: ByteArray, fileName: String) = false
        override suspend fun share(bytes: ByteArray, fileName: String) = false
    }

    private class AcceptingFiles : PlatformFiles {
        var sharedAs: String? = null
        override suspend fun open(bytes: ByteArray, fileName: String) = true
        override suspend fun share(bytes: ByteArray, fileName: String): Boolean {
            sharedAs = fileName
            return true
        }
    }

    private fun viewModel(
        locations: LocationsRepositoryStub = RecordingLocations(),
        categories: RecordingCategories = RecordingCategories(),
        files: PlatformFiles = AcceptingFiles(),
        stats: Either<ApiResponse, UserStats> = Either.Right(UserStats())
    ): LocationsViewModel {
        val collections = NoCollections()
        return LocationsViewModel(
            getLocationsPagingUseCase = GetLocationsPagingUseCase(locations),
            getCategoriesUseCase = GetCategoriesUseCase(categories),
            getAllCollectionsUseCase = GetAllCollectionsUseCase(collections),
            observeCollectionsUseCase = ObserveCollectionsUseCase(collections),
            deleteLocationUseCase = DeleteLocationUseCase(locations),
            createCategoryUseCase = CreateCategoryUseCase(categories),
            updateCategoryUseCase = UpdateCategoryUseCase(categories),
            deleteCategoryUseCase = DeleteCategoryUseCase(categories),
            updateLocationCollectionsUseCase = UpdateLocationCollectionsUseCase(locations, collections),
            duplicateLocationUseCase = DuplicateLocationUseCase(locations),
            getShareImageUseCase = GetShareImageUseCase(locations),
            platformFiles = files,
            getUserStatsUseCase = GetUserStatsUseCase(UserRepositoryStub(stats = stats)),
            userRepository = UserRepositoryStub(stats = stats)
        )
    }

    // --- which request the list makes -------------------------------------------------

    @Test
    fun anUntouchedListAsksThePlainEndpoint() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val vm = viewModel(locations)
        testScheduler.advanceUntilIdle()

        val job = launch { vm.adventuresPagingData.collect { } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        // includeCollections defaults to true, and testing it for truth rather than for a
        // departure from the default made every launch take the filtered endpoint. Both return
        // the same 22 places on the server, so the wrong choice cost a request and showed nothing.
        assertEquals(1, locations.plainCalls)
        assertEquals(0, locations.filteredCalls)
    }

    @Test
    fun turningCollectionsOffIsAFilterAndTakesTheFilteredEndpoint() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val vm = viewModel(locations)
        testScheduler.advanceUntilIdle()

        vm.onFiltersChanged(LocationFilters(includeCollections = false))
        val job = launch { vm.adventuresPagingData.collect { } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(1, locations.filteredCalls)
        assertEquals(false, locations.lastIncludeCollections)
    }

    @Test
    fun aVisitFilterReachesTheRepositoryAsABoolean() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val vm = viewModel(locations)
        testScheduler.advanceUntilIdle()

        vm.onFiltersChanged(LocationFilters(visitedFilter = VisitedFilter.NOT_VISITED))
        val job = launch { vm.adventuresPagingData.collect { } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        // Not null, which is "either", and not true.
        assertEquals(false, locations.lastIsVisited)
    }

    @Test
    fun theDefaultSortIsNotSentAtAll() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val vm = viewModel(locations)
        testScheduler.advanceUntilIdle()

        vm.onFiltersChanged(LocationFilters(visitedFilter = VisitedFilter.VISITED))
        val job = launch { vm.adventuresPagingData.collect { } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        assertNull(locations.lastSortBy)
    }

    @Test
    fun aChosenSortIsSentAsTheServersOwnValue() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val vm = viewModel(locations)
        testScheduler.advanceUntilIdle()

        vm.onFiltersChanged(LocationFilters(sortField = LocationSortField.VISIT_DATE))
        val job = launch { vm.adventuresPagingData.collect { } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        // "date", not "visit_date" and not "created_at": the server silently sorts by name for
        // anything it does not recognise.
        assertEquals("date", locations.lastSortBy)
    }

    // --- search -----------------------------------------------------------------------

    @Test
    fun typingDoesNotSearchUntilItIsSubmitted() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val vm = viewModel(locations)
        testScheduler.advanceUntilIdle()

        vm.onSearchQueryChange("prado")
        val job = launch { vm.adventuresPagingData.collect { } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        assertEquals("prado", vm.searchQuery.value)
        assertEquals("", vm.actualSearchQuery.value)
        assertEquals(0, locations.filteredCalls)
    }

    @Test
    fun submittingASearchTrimsIt() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val vm = viewModel(locations)
        testScheduler.advanceUntilIdle()

        vm.onSearchQueryChange("  prado  ")
        vm.executeSearch()
        val job = launch { vm.adventuresPagingData.collect { } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        assertEquals("prado", locations.lastSearch)
        // The field keeps what was typed, so refining a search does not mean retyping it.
        assertEquals("  prado  ", vm.searchQuery.value)
    }

    // --- the filter badge -------------------------------------------------------------

    @Test
    fun anUntouchedFilterSetWearsNoBadge() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(0, vm.activeFilterCount())
    }

    @Test
    fun everyCategoryCountsSeparatelyAndSortCountsOnce() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onFiltersChanged(
            LocationFilters(
                categoryNames = listOf("museum", "food"),
                sortField = LocationSortField.NAME,
                sortDirection = SortDirection.ASCENDING,
                visitedFilter = VisitedFilter.VISITED,
                includeCollections = false
            )
        )

        // Two categories, one for sort (field and direction together), one for the visit filter,
        // one for hiding collections.
        assertEquals(5, vm.activeFilterCount())
    }

    @Test
    fun clearingPutsBackTheDefaultsNotAnEmptyEverything() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onFiltersChanged(LocationFilters(includeCollections = false))
        vm.clearFilters()

        assertEquals(LocationFilters(), vm.filters.value)
        assertTrue(vm.filters.value.includeCollections)
        assertEquals(0, vm.activeFilterCount())
    }

    // --- acting on one place ----------------------------------------------------------

    @Test
    fun sharingSendsAPngNamedAfterThePlace() = runTest(dispatcher) {
        val files = AcceptingFiles()
        val vm = viewModel(
            locations = RecordingLocations(onShare = Either.Right(byteArrayOf(1, 2, 3))),
            files = files
        )
        testScheduler.advanceUntilIdle()

        vm.shareLocation(place)
        testScheduler.advanceUntilIdle()

        assertEquals("Prado.png", files.sharedAs)
        assertNull(vm.actionMessage.value)
        assertNull(vm.busyLocationId.value)
    }

    @Test
    fun aDeviceThatCannotShareSaysSoRatherThanFailingSilently() = runTest(dispatcher) {
        val vm = viewModel(
            locations = RecordingLocations(onShare = Either.Right(byteArrayOf(1))),
            files = RefusingFiles()
        )
        testScheduler.advanceUntilIdle()

        vm.shareLocation(place)
        testScheduler.advanceUntilIdle()

        assertEquals("Nothing on this device can share an image.", vm.actionMessage.value)
    }

    @Test
    fun aRefusedShareCarriesTheServersReason() = runTest(dispatcher) {
        val vm = viewModel(
            locations = RecordingLocations(onShare = Either.Left(ApiResponse.IOException))
        )
        testScheduler.advanceUntilIdle()

        vm.shareLocation(place)
        testScheduler.advanceUntilIdle()

        assertEquals("No internet connection.", vm.actionMessage.value)
    }

    @Test
    fun aSecondActionIsIgnoredWhileOneIsRunning() = runTest(dispatcher) {
        val gate = CompletableDeferred<Either<ApiResponse, ByteArray>>()
        val locations = object : LocationsRepositoryStub() {
            override suspend fun getShareImage(
                locationId: String,
                aspect: String
            ): Either<ApiResponse, ByteArray> = gate.await()
        }
        val vm = viewModel(locations = locations)
        testScheduler.advanceUntilIdle()

        vm.shareLocation(place)
        runCurrent()
        assertEquals("1", vm.busyLocationId.value)

        // Still busy: a second action must not start while the first is waiting on the server.
        // duplicateLocation is not overridden here, so reaching it would throw.
        vm.duplicateLocation(place)
        runCurrent()

        gate.complete(Either.Right(byteArrayOf(1)))
        testScheduler.advanceUntilIdle()

        assertNull(vm.busyLocationId.value)
    }

    @Test
    fun aDuplicateReportsTheNameTheCopyGot() = runTest(dispatcher) {
        val copy = place.copy(id = "2", name = "Copy of Prado")
        val vm = viewModel(
            locations = RecordingLocations(onDuplicate = Either.Right(copy)),
            stats = Either.Right(UserStats())
        )
        testScheduler.advanceUntilIdle()

        vm.duplicateLocation(place)
        testScheduler.advanceUntilIdle()

        assertEquals("Duplicated as \"Copy of Prado\"", vm.actionMessage.value)
        assertNull(vm.busyLocationId.value)
    }

    @Test
    fun aFailedDeleteKeepsItsMessageUntilItIsCleared() = runTest(dispatcher) {
        val vm = viewModel(
            locations = RecordingLocations(onDelete = Either.Left(ApiResponse.IOException))
        )
        testScheduler.advanceUntilIdle()

        vm.deleteAdventure("1")
        testScheduler.advanceUntilIdle()

        assertEquals(
            LocationsViewModel.DeleteState.Error("No internet connection. Please check your network."),
            vm.deleteState.value
        )

        vm.clearDeleteState()
        assertEquals(LocationsViewModel.DeleteState.Idle, vm.deleteState.value)
    }

    // --- categories -------------------------------------------------------------------

    @Test
    fun aNewCategoryKeepsTheTypedNameForDisplayAndSlugsTheOneItSends() = runTest(dispatcher) {
        val categories = RecordingCategories()
        val vm = viewModel(categories = categories)
        testScheduler.advanceUntilIdle()

        vm.createCategory("Street Food", "🍜")
        testScheduler.advanceUntilIdle()

        assertEquals("street_food", categories.lastCreatedName)
        assertEquals("Street Food", categories.lastCreatedDisplayName)
    }

    @Test
    fun categoriesThatFailedToLoadCanBeAskedForAgain() = runTest(dispatcher) {
        val categories = RecordingCategories(answer = Either.Left(ApiResponse.IOException))
        val vm = viewModel(categories = categories)
        testScheduler.advanceUntilIdle()

        assertEquals(
            CategoriesState.Error("No internet connection. Please check your network."),
            vm.categoriesState.value
        )
        assertEquals(1, categories.getCalls)

        vm.retryLoadCategories()
        testScheduler.advanceUntilIdle()

        assertEquals(2, categories.getCalls)
    }

    // --- the header counts ------------------------------------------------------------

    @Test
    fun theHeaderCountsTheWholeLibraryNotThePageOnScreen() = runTest(dispatcher) {
        val vm = viewModel(stats = Either.Right(UserStats(locationCount = 22, visitedCountryCount = 9)))
        testScheduler.advanceUntilIdle()

        assertEquals(22, vm.libraryCounts.value?.locationCount)
    }
}
