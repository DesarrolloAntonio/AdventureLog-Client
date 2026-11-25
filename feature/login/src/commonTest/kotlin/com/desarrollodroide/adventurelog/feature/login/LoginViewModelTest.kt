package com.desarrollodroide.adventurelog.feature.login

import androidx.lifecycle.SavedStateHandle
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RememberMeCredentialsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.VerifyLoginCodeUseCase
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LoginCodeError
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.LoginResult
import com.desarrollodroide.adventurelog.core.model.UserDetails
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Nobody signed in; [remembered] is what "Remember me" kept from an earlier login. */
    private class NoSession(private val remembered: Account? = null) : FakeUserRepository(session = null) {
        var published: UserDetails? = null
        var rememberedAfterLogin: Pair<String, String>? = null
        override fun getRememberMeCredentials(): Flow<Account?> = flowOf(remembered)
        override fun setActiveSession(userDetails: UserDetails) { published = userDetails }
        override suspend fun saveUserSession(userDetails: UserDetails) { published = userDetails }
        override suspend fun saveRememberMeCredentials(url: String, username: String) {
            rememberedAfterLogin = url to username
        }
    }

    /** A server: [twoStep] accounts answer the password with a request for a code. */
    private class FakeLogins(private val twoStep: Boolean) : LoginRepositoryStub() {
        var codeAnswers = ArrayDeque<Either<LoginCodeError, UserDetails>>()
        val codesSent = mutableListOf<String>()
        var cancelled = 0

        override suspend fun sendLogin(url: String, username: String, password: String): Either<ApiResponse, LoginResult> =
            Either.Right(if (twoStep) LoginResult.CodeRequired else LoginResult.SignedIn(user("password-token")))

        override suspend fun sendLoginCode(url: String, code: String): Either<LoginCodeError, UserDetails> {
            codesSent += code
            return codeAnswers.removeFirstOrNull() ?: Either.Right(user("code-token"))
        }

        override fun cancelPendingLogin() {
            cancelled++
        }
    }

    private lateinit var users: NoSession

    private fun viewModel(
        handle: SavedStateHandle,
        remembered: Account? = null,
        logins: LoginRepositoryStub = object : LoginRepositoryStub() {},
    ): LoginViewModel {
        users = NoSession(remembered)
        return LoginViewModel(
            loginUseCase = LoginUseCase(logins),
            initializeSessionUseCase = InitializeSessionUseCase(users, object : AdventureLogNetworkStub() {}),
            saveSessionUseCase = SaveSessionUseCase(users),
            rememberMeCredentialsUseCase = RememberMeCredentialsUseCase(users),
            verifyLoginCodeUseCase = VerifyLoginCodeUseCase(logins),
            savedStateHandle = handle,
            urlValidator = { it.startsWith("https://") },
        )
    }

    /** A view model with the form filled in and ready to sign in. */
    private fun signingIn(logins: LoginRepositoryStub, remember: Boolean = true): LoginViewModel {
        val vm = viewModel(SavedStateHandle(), logins = logins)
        dispatcher.scheduler.advanceUntilIdle()
        vm.updateServerUrl("https://qa.test")
        vm.updateUserName("claude")
        vm.updatePassword("secret")
        vm.updateRememberSession(remember)
        dispatcher.scheduler.advanceUntilIdle()
        return vm
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

    @Test
    fun `an account without two-step verification signs straight in`() = runTest(dispatcher) {
        val logins = FakeLogins(twoStep = false)
        val vm = signingIn(logins)

        vm.login()
        testScheduler.advanceUntilIdle()

        val state = assertIs<LoginUiState.Success>(vm.uiState.value)
        assertEquals("password-token", state.userDetails.sessionToken)
        assertEquals("https://qa.test", users.published?.serverUrl)
        assertTrue(logins.codesSent.isEmpty())
    }

    @Test
    fun `an account with two-step verification asks for the code, then signs in with it`() = runTest(dispatcher) {
        val logins = FakeLogins(twoStep = true)
        val vm = signingIn(logins, remember = true)

        vm.login()
        testScheduler.advanceUntilIdle()
        assertEquals(LoginUiState.CodeRequired(), vm.uiState.value)
        assertNull(users.published, "nobody is signed in before the code")

        vm.updateLoginCode("123 456")
        assertEquals("123456", (vm.uiState.value as LoginUiState.CodeRequired).code)
        vm.verifyLoginCode()
        testScheduler.advanceUntilIdle()

        val state = assertIs<LoginUiState.Success>(vm.uiState.value)
        assertEquals("code-token", state.userDetails.sessionToken)
        assertEquals(listOf("123456"), logins.codesSent)
        // What the form said when the password went is what the finished sign-in keeps.
        assertEquals("https://qa.test", users.published?.serverUrl)
        assertEquals("https://qa.test" to "claude", users.rememberedAfterLogin)
    }

    @Test
    fun `a wrong code stays on the code screen and says so`() = runTest(dispatcher) {
        val logins = FakeLogins(twoStep = true).apply {
            codeAnswers.addLast(Either.Left(LoginCodeError.IncorrectCode))
        }
        val vm = signingIn(logins)
        vm.login()
        testScheduler.advanceUntilIdle()

        vm.updateLoginCode("000000")
        vm.verifyLoginCode()
        testScheduler.advanceUntilIdle()

        val state = assertIs<LoginUiState.CodeRequired>(vm.uiState.value)
        assertEquals(VerifyLoginCodeUseCase.INCORRECT_CODE, state.error)
        assertEquals("", state.code)
        assertFalse(state.verifying)

        // The next code still finishes the same sign-in.
        vm.updateLoginCode("123456")
        assertNull((vm.uiState.value as LoginUiState.CodeRequired).error, "typing clears the complaint")
        vm.verifyLoginCode()
        testScheduler.advanceUntilIdle()
        assertIs<LoginUiState.Success>(vm.uiState.value)
    }

    @Test
    fun `a sign-in that timed out goes back to the password form`() = runTest(dispatcher) {
        val logins = FakeLogins(twoStep = true).apply {
            codeAnswers.addLast(Either.Left(LoginCodeError.Expired))
        }
        val vm = signingIn(logins)
        vm.login()
        testScheduler.advanceUntilIdle()

        vm.updateLoginCode("123456")
        vm.verifyLoginCode()
        testScheduler.advanceUntilIdle()

        assertEquals(LoginUiState.Error(VerifyLoginCodeUseCase.EXPIRED), vm.uiState.value)
        assertEquals(1, logins.cancelled)
        assertEquals("claude", vm.loginFormState.value.userName, "the form keeps what was typed")
    }

    @Test
    fun `a code of the wrong length is caught before the server`() = runTest(dispatcher) {
        val logins = FakeLogins(twoStep = true)
        val vm = signingIn(logins)
        vm.login()
        testScheduler.advanceUntilIdle()

        vm.updateLoginCode("1234")
        vm.verifyLoginCode()
        testScheduler.advanceUntilIdle()

        assertEquals(VerifyLoginCodeUseCase.ENTER_A_CODE, (vm.uiState.value as LoginUiState.CodeRequired).error)
        assertTrue(logins.codesSent.isEmpty())
    }

    @Test
    fun `the code field takes digits only, up to a recovery code's eight`() = runTest(dispatcher) {
        val vm = signingIn(FakeLogins(twoStep = true))
        vm.login()
        testScheduler.advanceUntilIdle()

        vm.updateLoginCode("12ab34-5678 90")

        assertEquals("12345678", (vm.uiState.value as LoginUiState.CodeRequired).code)
    }

    @Test
    fun `going back from the code drops the waiting sign-in`() = runTest(dispatcher) {
        val logins = FakeLogins(twoStep = true)
        val vm = signingIn(logins)
        vm.login()
        testScheduler.advanceUntilIdle()

        vm.cancelLoginCode()

        assertEquals(LoginUiState.Empty, vm.uiState.value)
        assertEquals(1, logins.cancelled)
        assertNull(users.published)
    }
}

private fun user(token: String) = UserDetails(
    uuid = "u", username = "claude", firstName = "John", lastName = "Doe",
    dateJoined = "2024-01-01", sessionToken = token,
)
