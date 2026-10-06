package com.desarrollodroide.adventurelog.feature.home

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.EndRejectedSessionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetDashboardUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.testing.AdventureLogNetworkStub
import com.desarrollodroide.adventurelog.core.testing.DashboardRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.FakeUserRepository
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** Home coming back into view asks the server again, without losing what it shows. */
class HomeDashboardRefreshTest {

    private val dispatcher = StandardTestDispatcher()

    private class Server(var answer: Either<ApiResponse, Dashboard>) : DashboardRepositoryStub() {
        var calls = 0
        override suspend fun getDashboard(): Either<ApiResponse, Dashboard> = answer.also { calls++ }
    }

    private fun places(n: Int) = Either.Right(Dashboard(stats = UserStats(locationCount = n)))

    private fun viewModel(server: Server) = HomeViewModel(
        getLocationsUseCase = GetLocationsUseCase(object : LocationsRepositoryStub() {}),
        getDashboardUseCase = GetDashboardUseCase(server),
        logoutUseCase = LogoutUseCase(FakeUserRepository(), object : AdventureLogNetworkStub() {}),
        userRepository = FakeUserRepository(),
        initializeSessionUseCase = InitializeSessionUseCase(FakeUserRepository(), object : AdventureLogNetworkStub() {}),
        endRejectedSessionsUseCase = EndRejectedSessionsUseCase(object : AdventureLogNetworkStub() {}, LogoutUseCase(FakeUserRepository(), object : AdventureLogNetworkStub() {}))
    )

    private fun HomeViewModel.shownPlaces() = (uiState.value as HomeUiState.Success).dashboard.stats.locationCount

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `coming back to Home shows what the server has now`() = runTest(dispatcher) {
        val server = Server(places(22))
        val vm = viewModel(server)
        testScheduler.advanceUntilIdle()

        server.answer = places(23)
        vm.refreshDashboard()
        testScheduler.advanceUntilIdle()

        assertEquals(23, vm.shownPlaces())
    }

    @Test
    fun `a refresh that fails keeps the dashboard on screen`() = runTest(dispatcher) {
        val server = Server(places(22))
        val vm = viewModel(server)
        testScheduler.advanceUntilIdle()

        server.answer = Either.Left(ApiResponse.IOException)
        vm.refreshDashboard()
        testScheduler.advanceUntilIdle()

        assertIs<HomeUiState.Success>(vm.uiState.value, "a failed refresh replaced the dashboard")
        assertEquals(22, vm.shownPlaces())
    }

    @Test
    fun `Home appearing during its first load does not ask twice`() = runTest(dispatcher) {
        val server = Server(places(22))
        val vm = viewModel(server)

        vm.refreshDashboard()   // the start effect fires while init's load is still on its way
        testScheduler.advanceUntilIdle()

        assertEquals(1, server.calls)
    }
}
