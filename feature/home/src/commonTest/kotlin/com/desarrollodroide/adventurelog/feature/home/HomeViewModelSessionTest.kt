package com.desarrollodroide.adventurelog.feature.home

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.EndRejectedSessionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetDashboardUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.testing.AdventureLogNetworkStub
import com.desarrollodroide.adventurelog.core.testing.DashboardRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.UserRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testUser
import com.desarrollodroide.adventurelog.feature.home.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeViewModelSessionTest {

    private val dispatcher = StandardTestDispatcher()

    private class SessionStore(initial: UserDetails?) : UserRepositoryStub() {
        val session = MutableStateFlow(initial)
        override fun getUserSession(): Flow<UserDetails?> = session
        override suspend fun getUserSessionOnce(): UserDetails? = session.value
        override val activeSession: UserDetails? get() = session.value
        override fun setActiveSession(userDetails: UserDetails) { session.value = userDetails }
        override suspend fun clearUserSession() { session.value = null }
    }

    private class Server(var userDetails: () -> UserDetailsDTO = { throw IOException("offline") }) : AdventureLogNetworkStub() {
        val rejections = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        override val sessionRejections: Flow<Unit> = rejections
        override fun initializeFromSession(serverUrl: String, sessionToken: String?) = Unit
        override fun clearSession() = Unit
        override fun endServerSession() = Unit
        override suspend fun getUserDetails(): UserDetailsDTO = userDetails()
    }

    private val noDashboard = object : DashboardRepositoryStub() {
        override suspend fun getDashboard(): Either<ApiResponse, Dashboard> = Either.Left(ApiResponse.IOException)
    }

    private fun viewModel(store: SessionStore, server: Server) = HomeViewModel(
        getLocationsUseCase = GetLocationsUseCase(object : LocationsRepositoryStub() {}),
        getDashboardUseCase = GetDashboardUseCase(noDashboard),
        logoutUseCase = LogoutUseCase(store, server),
        userRepository = store,
        initializeSessionUseCase = InitializeSessionUseCase(store, server),
        endRejectedSessionsUseCase = EndRejectedSessionsUseCase(server, LogoutUseCase(store, server))
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a signed-in user is not sent to Login`() = runTest(dispatcher) {
        val vm = viewModel(SessionStore(testUser), Server())
        testScheduler.advanceUntilIdle()

        assertFalse(vm.signedOut.value)
    }

    @Test
    fun `an app restored with no session is sent to Login`() = runTest(dispatcher) {
        // "Remember me" off keeps the session in memory only; after process death there is none.
        val vm = viewModel(SessionStore(null), Server())
        testScheduler.advanceUntilIdle()

        assertTrue(vm.signedOut.value)
    }

    @Test
    fun `signing out sends the user to Login`() = runTest(dispatcher) {
        val vm = viewModel(SessionStore(testUser), Server())
        testScheduler.advanceUntilIdle()

        vm.logout()
        testScheduler.advanceUntilIdle()

        assertTrue(vm.signedOut.value)
    }

    @Test
    fun `a session the server rejects on any request signs the user out`() = runTest(dispatcher) {
        val server = Server()
        val vm = viewModel(SessionStore(testUser), server)
        testScheduler.advanceUntilIdle()

        server.rejections.emit(Unit)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.signedOut.value)
    }

    @Test
    fun `coming back to a session the server no longer accepts signs the user out`() = runTest(dispatcher) {
        val server = Server(userDetails = { throw HttpException(401, "Authentication credentials were not provided.") })
        val vm = viewModel(SessionStore(testUser), server)
        testScheduler.advanceUntilIdle()

        vm.recheckSession()
        testScheduler.advanceUntilIdle()

        assertTrue(vm.signedOut.value)
    }

    @Test
    fun `coming back without a network keeps the session`() = runTest(dispatcher) {
        val vm = viewModel(SessionStore(testUser), Server(userDetails = { throw IOException("offline") }))
        testScheduler.advanceUntilIdle()

        vm.recheckSession()
        testScheduler.advanceUntilIdle()

        assertFalse(vm.signedOut.value)
    }
}
