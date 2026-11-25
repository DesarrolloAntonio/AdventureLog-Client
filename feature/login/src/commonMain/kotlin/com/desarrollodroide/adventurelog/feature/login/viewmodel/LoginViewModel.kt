package com.desarrollodroide.adventurelog.feature.login.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RememberMeCredentialsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.VerifyLoginCodeUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ProviderSignInUseCase
import com.desarrollodroide.adventurelog.core.model.ProviderSignInAttempt
import com.desarrollodroide.adventurelog.core.model.ProviderSignInOptions
import com.desarrollodroide.adventurelog.core.model.SignInProvider
import com.desarrollodroide.adventurelog.feature.login.ProviderSignInRedirects
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import com.desarrollodroide.adventurelog.core.model.LoginResult
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import isValidUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val initializeSessionUseCase: InitializeSessionUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val rememberMeCredentialsUseCase: RememberMeCredentialsUseCase,
    private val verifyLoginCodeUseCase: VerifyLoginCodeUseCase,
    private val providerSignInUseCase: ProviderSignInUseCase,
    private val providerSignInRedirects: ProviderSignInRedirects,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    // The Android check runs on android.net.Uri, which a host test does not have.
    private val urlValidator: (String) -> Boolean = ::isValidUrl,
) : ViewModel() {

    /** The sign-in a code is being asked for: what the form said when its password went. */
    private data class PendingSignIn(val url: String, val username: String, val rememberSession: Boolean)

    private var pendingSignIn: PendingSignIn? = null

    private val logger = co.touchlab.kermit.Logger.withTag("LoginViewModel")
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.CheckingSession)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    /** What the server at the typed address offers, kept beside the buttons it put on screen. */
    private var providerOptions: ProviderSignInOptions? = null
    private var providersJob: Job? = null

    private val browserLaunches = Channel<String>(Channel.BUFFERED)

    /**
     * Pages to open in the system browser, once each: the start of a provider sign-in. An event,
     * not state - the browser leaves the app, and nothing on this screen waits for it to return.
     */
    val browserRequests: Flow<String> = browserLaunches.receiveAsFlow()

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            try {
                val existingSession = initializeSessionUseCase()

                if (existingSession != null) {
                    logger.d { "Found existing user session for: ${existingSession.username}" }
                    // Already signed in: a provider answer that arrives now belongs to nothing.
                    providerSignInRedirects.take()
                    _uiState.update { LoginUiState.Success(existingSession) }
                    return@launch
                }

                loadRememberMeCredentials()
                restoreTypedFields()
                _uiState.update { LoginUiState.Empty }

            } catch (e: Exception) {
                logger.e(e) { "Error during initialization" }
                _loginFormState.update { LoginFormState() }
                _uiState.update { LoginUiState.Empty }
            }
            refreshProviders(_loginFormState.value.serverUrl, debounce = false)
            // Only now: the form is there to go back to if the answer is a failure.
            providerSignInRedirects.arrivals.filterNotNull().collect { finishProviderSignIn() }
        }
    }

    private suspend fun loadRememberMeCredentials() {
        try {
            val account = rememberMeCredentialsUseCase.get().first()

            if (account != null) {
                logger.d { "Found saved credentials for: ${account.userName}" }
                _loginFormState.update {
                    LoginFormState(
                        userName = account.userName,
                        password = "",
                        serverUrl = account.serverUrl,
                        rememberSession = true,
                        userNameError = false,
                        passwordError = false,
                        urlError = false
                    )
                }
            } else {
                logger.d { "No saved credentials found" }
                _loginFormState.update {
                    LoginFormState(
                        userName = "",
                        password = "",
                        serverUrl = "",
                        rememberSession = false,
                        userNameError = false,
                        passwordError = false,
                        urlError = false
                    )
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error loading remember me credentials" }
            _loginFormState.update { LoginFormState() }
        }
    }

    /**
     * What the user typed before Android killed the app - switching to a password manager is
     * enough - wins over the remembered account. The password is not kept: saved state is written
     * out of the process, and a password has no business there.
     */
    private fun restoreTypedFields() {
        val userName = savedStateHandle.get<String>(KEY_USER_NAME)
        val serverUrl = savedStateHandle.get<String>(KEY_SERVER_URL)
        val remember = savedStateHandle.get<Boolean>(KEY_REMEMBER)
        if (userName == null && serverUrl == null && remember == null) return
        _loginFormState.update {
            it.copy(
                userName = userName ?: it.userName,
                serverUrl = serverUrl ?: it.serverUrl,
                rememberSession = remember ?: it.rememberSession
            )
        }
    }

    fun updateUserName(newUserName: String) {
        savedStateHandle[KEY_USER_NAME] = newUserName
        _loginFormState.value = _loginFormState.value.copy(
            userName = newUserName,
            userNameError = newUserName.isBlank()
        )
    }

    fun updatePassword(newPassword: String) {
        _loginFormState.value = _loginFormState.value.copy(
            password = newPassword,
            passwordError = newPassword.isBlank()
        )
    }

    fun updateServerUrl(newUrl: String) {
        savedStateHandle[KEY_SERVER_URL] = newUrl
        val changed = newUrl != _loginFormState.value.serverUrl
        _loginFormState.value = _loginFormState.value.copy(
            serverUrl = newUrl,
            urlError = !urlValidator(newUrl)
        )
        if (changed) refreshProviders(newUrl, debounce = true)
    }

    /**
     * Asks the server at [url] which providers it signs in through, once typing has paused. The
     * buttons go the moment the address changes: they belong to the server that answered.
     */
    private fun refreshProviders(url: String, debounce: Boolean) {
        providersJob?.cancel()
        providerOptions = null
        if (_loginFormState.value.providers.isNotEmpty()) {
            _loginFormState.update { it.copy(providers = emptyList()) }
        }
        if (!urlValidator(url)) return
        providersJob = viewModelScope.launch {
            if (debounce) delay(PROVIDERS_DEBOUNCE_MS)
            val options = try {
                providerSignInUseCase.providers(url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.w { "Could not ask for sign-in providers: ${e.message}" }
                null
            }
            if (_loginFormState.value.serverUrl != url) return@launch
            providerOptions = options
            _loginFormState.update { it.copy(providers = options?.providers.orEmpty()) }
        }
    }

    /** Opens the browser on [provider]'s sign-in; the answer comes back through [ProviderSignInRedirects]. */
    fun signInWithProvider(provider: SignInProvider) {
        val form = _loginFormState.value
        val options = providerOptions ?: return
        if (!urlValidator(form.serverUrl)) {
            _loginFormState.update { it.copy(urlError = true) }
            return
        }
        val attempt = providerSignInUseCase.start(form.serverUrl, options, provider, form.rememberSession)
        keepAttempt(attempt)
        logger.d { "Opening the browser to sign in with ${provider.name}" }
        browserLaunches.trySend(attempt.browserUrl)
    }

    /** The phone has no browser to open: the sign-in cannot start. */
    fun browserUnavailable() {
        takeAttempt()
        _uiState.update { LoginUiState.Error(NO_BROWSER) }
    }

    private suspend fun finishProviderSignIn() {
        val uri = providerSignInRedirects.take() ?: return
        if (ProviderSignInUseCase.callbackParameters(uri) == null) return
        // Taken before the code goes anywhere: one attempt, one trade.
        val attempt = takeAttempt()
        _uiState.update { LoginUiState.Loading }
        try {
            when (val result = providerSignInUseCase.finish(attempt, uri)) {
                is Either.Left -> {
                    logger.e { "Provider sign-in failed: ${result.value}" }
                    _uiState.update { LoginUiState.Error(result.value) }
                }
                is Either.Right -> {
                    val user = result.value
                    // finish() only succeeds for an attempt it was given.
                    val done = attempt ?: return
                    completeSignIn(user, done.serverUrl, user.username, done.rememberSession)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error finishing the provider sign-in" }
            _uiState.update { LoginUiState.Error(ProviderSignInUseCase.FAILED) }
        }
    }

    /**
     * The attempt survives Android ending the app while the browser is in front, in the saved
     * state. Its verifier is only good with the one-time code the server sends to this app, for a
     * minute, so keeping it there is not keeping a password there.
     */
    private fun keepAttempt(attempt: ProviderSignInAttempt) {
        savedStateHandle[KEY_SSO_SERVER] = attempt.serverUrl
        savedStateHandle[KEY_SSO_STATE] = attempt.state
        savedStateHandle[KEY_SSO_VERIFIER] = attempt.codeVerifier
        savedStateHandle[KEY_SSO_REMEMBER] = attempt.rememberSession
        savedStateHandle[KEY_SSO_BROWSER] = attempt.browserUrl
    }

    private fun takeAttempt(): ProviderSignInAttempt? {
        val server = savedStateHandle.remove<String>(KEY_SSO_SERVER)
        val state = savedStateHandle.remove<String>(KEY_SSO_STATE)
        val verifier = savedStateHandle.remove<String>(KEY_SSO_VERIFIER)
        val remember = savedStateHandle.remove<Boolean>(KEY_SSO_REMEMBER)
        val browser = savedStateHandle.remove<String>(KEY_SSO_BROWSER)
        if (server == null || state == null || verifier == null) return null
        return ProviderSignInAttempt(server, state, verifier, remember ?: false, browser.orEmpty())
    }

    fun updateRememberSession(value: Boolean) {
        savedStateHandle[KEY_REMEMBER] = value
        _loginFormState.value = _loginFormState.value.copy(rememberSession = value)

        if (!value) {
            viewModelScope.launch {
                try {
                    rememberMeCredentialsUseCase.clear()
                    logger.d { "Cleared saved credentials because user unchecked remember me" }
                } catch (e: Exception) {
                    logger.e(e) { "Error clearing saved credentials" }
                }
            }
        }
    }

    fun validateForm(): Boolean {
        val currentState = _loginFormState.value
        val isValid = currentState.userName.isNotBlank() &&
                currentState.password.isNotBlank() &&
                urlValidator(currentState.serverUrl)

        _loginFormState.value = currentState.copy(
            userNameError = currentState.userName.isBlank(),
            passwordError = currentState.password.isBlank(),
            urlError = !urlValidator(currentState.serverUrl)
        )

        return isValid
    }

    fun login() {
        if (!validateForm()) return

        val url = loginFormState.value.serverUrl
        val username = loginFormState.value.userName
        val password = loginFormState.value.password
        val rememberSession = loginFormState.value.rememberSession

        logger.d { "Attempting login with URL: $url, Remember Session: $rememberSession" }
        _uiState.update { LoginUiState.Loading }

        viewModelScope.launch {
            try {
                val result = loginUseCase(
                    url = url,
                    username = username,
                    password = password
                )

                when (result) {
                    is Either.Left -> {
                        logger.e { "Login failed: ${result.value}" }
                        _uiState.update { LoginUiState.Error(result.value) }
                    }

                    is Either.Right -> when (val step = result.value) {
                        is LoginResult.SignedIn -> completeSignIn(step.user, url, username, rememberSession)
                        LoginResult.CodeRequired -> {
                            logger.d { "The account asks for a second factor" }
                            pendingSignIn = PendingSignIn(url, username, rememberSession)
                            _uiState.update { LoginUiState.CodeRequired() }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error during login" }
                _uiState.update { LoginUiState.Error("Unexpected error occurred: ${e.message}") }
            }
        }
    }

    /** Digits only, as many as a recovery code has: the field takes nothing else. */
    fun updateLoginCode(newCode: String) {
        val state = _uiState.value as? LoginUiState.CodeRequired ?: return
        _uiState.value = state.copy(code = newCode.filter { it.isDigit() }.take(8), error = null)
    }

    fun verifyLoginCode() {
        val state = _uiState.value as? LoginUiState.CodeRequired ?: return
        val pending = pendingSignIn ?: return
        if (state.verifying) return
        if (!VerifyLoginCodeUseCase.isWellFormed(state.code)) {
            _uiState.value = state.copy(error = VerifyLoginCodeUseCase.ENTER_A_CODE)
            return
        }
        _uiState.value = state.copy(verifying = true, error = null)

        viewModelScope.launch {
            try {
                when (val result = verifyLoginCodeUseCase(pending.url, state.code)) {
                    is Either.Right -> {
                        pendingSignIn = null
                        completeSignIn(result.value, pending.url, pending.username, pending.rememberSession)
                    }
                    is Either.Left -> {
                        val failure = result.value
                        logger.e { "Second factor failed: ${failure.message}" }
                        if (failure.signInAgain) {
                            pendingSignIn = null
                            verifyLoginCodeUseCase.cancel()
                            _uiState.update { LoginUiState.Error(failure.message) }
                        } else {
                            _uiState.update { LoginUiState.CodeRequired(error = failure.message) }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error sending the second factor" }
                _uiState.update { LoginUiState.CodeRequired(error = VerifyLoginCodeUseCase.FAILED) }
            }
        }
    }

    /** Back to the password form; the half-done sign-in is dropped. */
    fun cancelLoginCode() {
        if (_uiState.value !is LoginUiState.CodeRequired) return
        pendingSignIn = null
        verifyLoginCodeUseCase.cancel()
        _uiState.update { LoginUiState.Empty }
    }

    private suspend fun completeSignIn(
        userDetails: UserDetails,
        url: String,
        username: String,
        rememberSession: Boolean
    ) {
        logger.d { "Login successful for user: ${userDetails.username}" }

        // The session must be published on every login - Home reads the user from
        // it for the greeting and to load stats. "Remember me" only decides
        // whether it also survives an app restart.
        // serverUrl is recorded here because the login response does not carry it,
        // and the image loader needs it to tell the user's own server apart from
        // third-party hosts before attaching the session token.
        saveSessionUseCase(
            userDetails.copy(serverUrl = url.trimEnd('/')),
            persist = rememberSession
        )

        if (rememberSession) {
            rememberMeCredentialsUseCase.save(
                url = url,
                username = username
            )
            logger.d { "Saved persistent session, server and username - will auto-login next time" }
        } else {
            rememberMeCredentialsUseCase.clear()
            logger.d { "Remember me not checked - session is active for this run only" }
        }

        _uiState.update { LoginUiState.Success(userDetails) }
    }

    private companion object {
        const val PROVIDERS_DEBOUNCE_MS = 600L
        const val NO_BROWSER = "There's no browser on this phone to sign in with."
        const val KEY_SSO_SERVER = "provider_sign_in_server"
        const val KEY_SSO_STATE = "provider_sign_in_state"
        const val KEY_SSO_VERIFIER = "provider_sign_in_verifier"
        const val KEY_SSO_REMEMBER = "provider_sign_in_remember"
        const val KEY_SSO_BROWSER = "provider_sign_in_browser"
        const val KEY_USER_NAME = "login_user_name"
        const val KEY_SERVER_URL = "login_server_url"
        const val KEY_REMEMBER = "login_remember"
    }

    fun clearErrors() {
        _loginFormState.value = _loginFormState.value.copy(
            userNameError = false,
            passwordError = false,
            urlError = false
        )
    }
}