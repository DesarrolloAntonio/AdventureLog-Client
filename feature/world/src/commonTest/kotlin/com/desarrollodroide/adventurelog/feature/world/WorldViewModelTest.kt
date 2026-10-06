package com.desarrollodroide.adventurelog.feature.world

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedCitiesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RefreshCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SetRegionVisitedUseCase
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.core.testing.CountriesRepositoryStub
import com.desarrollodroide.adventurelog.feature.world.viewmodel.WorldViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The world list (QA 05: WO-01, WO-02, WO-04). */
class WorldViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private class Repo : CountriesRepositoryStub() {
        val flow = MutableStateFlow(listOf(japan(3)))
        override val countriesFlow = flow
        var refresh = CompletableDeferred<Either<ApiResponse, List<Country>>>(Either.Right(listOf(japan(3))))
        override suspend fun getCountries(): Either<ApiResponse, List<Country>> = Either.Right(flow.value)
        override suspend fun refreshCountries(): Either<ApiResponse, List<Country>> = refresh.await()
        override suspend fun getVisitedRegions(): Either<ApiResponse, List<VisitedRegion>> = Either.Right(emptyList())
        override suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>> = Either.Right(emptyList())
        override suspend fun markRegionVisited(regionId: String): Either<ApiResponse, VisitedRegion> =
            Either.Left(ApiResponse.IOException)
    }

    private fun viewModel(repo: Repo) = WorldViewModel(
        getCountriesUseCase = GetCountriesUseCase(repo),
        refreshCountriesUseCase = RefreshCountriesUseCase(repo),
        getVisitedRegionsUseCase = GetVisitedRegionsUseCase(repo),
        getVisitedCitiesUseCase = GetVisitedCitiesUseCase(repo),
        observeCountriesUseCase = ObserveCountriesUseCase(repo)
    )

    @Test
    fun aRegionTickedElsewhereReachesTheList() = runTest(dispatcher) {
        val repo = Repo()
        val vm = viewModel(repo)
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.countries.single().numVisits)

        repo.flow.value = listOf(japan(4))
        advanceUntilIdle()

        assertEquals(4, vm.uiState.value.countries.single().numVisits)
        assertEquals(4, vm.uiState.value.filteredCountries.single().numVisits)
    }

    @Test
    fun aPullShowsItIsWorkingUntilTheAnswerArrives() = runTest(dispatcher) {
        val repo = Repo().apply { refresh = CompletableDeferred() }
        val vm = viewModel(repo)
        advanceUntilIdle()

        vm.onRefresh()
        advanceUntilIdle()
        assertTrue(vm.isRefreshing.value)

        repo.refresh.complete(Either.Left(ApiResponse.IOException))
        advanceUntilIdle()
        assertFalse(vm.isRefreshing.value)
    }

    @Test
    fun aTickThatCannotReachTheServerSaysSoInTheAppsOwnWords() = runTest(dispatcher) {
        assertEquals(
            Either.Left("Can't reach the server. Check your connection."),
            SetRegionVisitedUseCase(Repo())("JP-23", visited = true)
        )
    }

    private companion object {
        fun japan(visits: Int) = Country(1, "Japan", "JP", "", 47, visits, "Eastern Asia", "Tokyo", null, null)
    }
}
