package com.desarrollodroide.adventurelog.feature.login

import androidx.lifecycle.SavedStateHandle
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RememberMeCredentialsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.testing.AdventureLogNetworkStub
import com.desarrollodroide.adventurelog.core.testing.FakeUserRepository
import com.desarrollodroide.adventurelog.core.testing.LoginRepositoryStub
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import com.desarrollodroide.adventurelog.feature.login.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Nobody signed in; [remembered] is what "Remember me" kept from an earlier login. */
    private class NoSession(private val remembered: Account? = null) : FakeUserRepository(session = null) {
        override fun getRememberMeCredentials(): Flow<Account?> = flowOf(remembered)
    }

    private fun viewModel(handle: SavedStateHandle, remembered: Account? = null): LoginViewModel {
        val users = NoSession(remembered)
        return LoginViewModel(
            loginUseCase = LoginUseCase(object : LoginRepositoryStub() {}),
            initializeSessionUseCase = InitializeSessionUseCase(users, object : AdventureLogNetworkStub() {}),
            saveSessionUseCase = SaveSessionUseCase(users),
            rememberMeCredentialsUseCase = RememberMeCredentialsUseCase(users),
            savedStateHandle = handle
        )
    }

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `what was typed before the app was killed comes back`() = runTest(dispatcher) {
        val handle = SavedStateHandle(
            mapOf("login_user_name" to "claude", "login_server_url" to "https://qa.test", "login_remember" to true)
        )

        val vm = viewModel(handle)
        testScheduler.advanceUntilIdle()

        val form = vm.loginFormState.value
        assertEquals("claude", form.userName)
        assertEquals("https://qa.test", form.serverUrl)
        assertTrue(form.rememberSession)
    }

    @Test
    fun `typed text wins over the remembered account`() = runTest(dispatcher) {
        val vm = viewModel(
            SavedStateHandle(mapOf("login_user_name" to "typed-name")),
            remembered = Account(userName = "remembered-name", password = "", serverUrl = "https://remembered.test")
        )
        testScheduler.advanceUntilIdle()

        assertEquals("typed-name", vm.loginFormState.value.userName)
        assertEquals("https://remembered.test", vm.loginFormState.value.serverUrl)
    }

    @Test
    fun `with nothing typed the remembered account fills the form`() = runTest(dispatcher) {
        val vm = viewModel(
            SavedStateHandle(),
            remembered = Account(userName = "remembered-name", password = "", serverUrl = "https://remembered.test")
        )
        testScheduler.advanceUntilIdle()

        assertEquals("remembered-name", vm.loginFormState.value.userName)
        assertEquals("https://remembered.test", vm.loginFormState.value.serverUrl)
    }

    @Test
    fun `typing is kept for the next process but the password never is`() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        testScheduler.advanceUntilIdle()

        vm.updateUserName("claude")
        vm.updatePassword("hunter2")

        assertEquals("claude", handle.get<String>("login_user_name"))
        assertFalse(handle.keys().any { handle.get<Any>(it) == "hunter2" }, "the password went into saved state")
    }

    @Test
    fun `the launch check is its own state and not a login in progress`() = runTest(dispatcher) {
        val vm = viewModel(SavedStateHandle())

        // Before the check has run: the screen shows a spinner here, and keeps the form for
        // LoginUiState.Loading, which only a login the user started may set.
        assertEquals(LoginUiState.CheckingSession, vm.uiState.value)

        testScheduler.advanceUntilIdle()
        assertEquals(LoginUiState.Empty, vm.uiState.value)
    }
}
