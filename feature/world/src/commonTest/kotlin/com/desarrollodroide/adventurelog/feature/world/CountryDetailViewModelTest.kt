package com.desarrollodroide.adventurelog.feature.world

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SetRegionVisitedUseCase
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.feature.world.viewmodel.CountryDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CountryDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeCountries : CountriesRepository {
        var marked = mutableListOf<String>()
        var unmarked = mutableListOf<String>()
        var markResult: Either<ApiResponse, VisitedRegion> =
            Either.Right(VisitedRegion(1, "u", "ES-MD", "Madrid", null, null))

        override val countriesFlow: StateFlow<List<Country>> = MutableStateFlow(emptyList())
        override val visitedRegionsFlow: StateFlow<List<VisitedRegion>> = MutableStateFlow(emptyList())
        override val visitedCitiesFlow: StateFlow<List<VisitedCity>> = MutableStateFlow(emptyList())

        override suspend fun getCountries() = Either.Right(
            listOf(Country(1, "Spain", "ES", "", 19, 1, "Southern Europe", "Madrid", null, null))
        )
        override suspend fun getRegions(countryCode: String) = Either.Right(
            listOf(
                Region("ES-VC", "Valencia", "Spain", 3, null, null, 1),
                Region("ES-MD", "Madrid", "Spain", 1, null, null, 1)
            )
        )
        override suspend fun getVisitedRegions() = Either.Right(
            listOf(VisitedRegion(1, "u", "ES-MD", "Madrid", null, null))
        )
        override suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>> = Either.Right(emptyList())
        override suspend fun refreshVisitedRegions(): Either<ApiResponse, Pair<Int, Int>> = Either.Right(0 to 0)
        override suspend fun markRegionVisited(regionId: String): Either<ApiResponse, VisitedRegion> {
            marked += regionId
            return markResult
        }
        override suspend fun unmarkRegionVisited(regionId: String): Either<ApiResponse, Unit> {
            unmarked += regionId
            return Either.Right(Unit)
        }
        override suspend fun refreshCountries() = getCountries()
    }

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeCountries) = CountryDetailViewModel(
        getRegionsUseCase = GetRegionsUseCase(repo),
        getVisitedRegionsUseCase = GetVisitedRegionsUseCase(repo),
        getCountriesUseCase = GetCountriesUseCase(repo),
        setRegionVisitedUseCase = SetRegionVisitedUseCase(repo)
    )

    @Test
    fun regionsArriveSortedWithTheVisitedOnesMarked() = runTest(dispatcher) {
        val vm = viewModel(FakeCountries())
        vm.load("ES")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Madrid", "Valencia"), vm.uiState.value.regions.map { it.region.name })
        assertEquals(1, vm.uiState.value.visitedCount)
        assertEquals("Spain", vm.uiState.value.country?.name)
    }

    @Test
    fun tickingARegionFollowsTheFingerAndReachesTheServer() = runTest(dispatcher) {
        val repo = FakeCountries()
        val vm = viewModel(repo)
        vm.load("ES")
        testScheduler.advanceUntilIdle()

        vm.onRegionToggled("ES-VC")
        // The tick lands before the round trip: someone about to tick fifteen provinces should
        // not wait for fifteen of them.
        assertTrue(vm.uiState.value.regions.first { it.region.id == "ES-VC" }.visited)

        testScheduler.advanceUntilIdle()
        assertEquals(listOf("ES-VC"), repo.marked)
        assertEquals(2, vm.uiState.value.visitedCount)
    }

    @Test
    fun tickingAnAlreadyVisitedRegionRemovesIt() = runTest(dispatcher) {
        val repo = FakeCountries()
        val vm = viewModel(repo)
        vm.load("ES")
        testScheduler.advanceUntilIdle()

        vm.onRegionToggled("ES-MD")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("ES-MD"), repo.unmarked)
        assertEquals(0, vm.uiState.value.visitedCount)
    }

    @Test
    fun aRefusedTickIsPutBack() = runTest(dispatcher) {
        val repo = FakeCountries().apply { markResult = Either.Left(ApiResponse.HttpError) }
        val vm = viewModel(repo)
        vm.load("ES")
        testScheduler.advanceUntilIdle()

        vm.onRegionToggled("ES-VC")
        testScheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.regions.first { it.region.id == "ES-VC" }.visited)
        assertEquals("Could not save that, try again later", vm.uiState.value.message)
    }

    @Test
    fun searchFiltersTheRegions() = runTest(dispatcher) {
        val vm = viewModel(FakeCountries())
        vm.load("ES")
        testScheduler.advanceUntilIdle()

        vm.onSearchQueryChanged("val")
        assertEquals(listOf("Valencia"), vm.uiState.value.filtered.map { it.region.name })
    }
}
