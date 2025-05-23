package com.desarrollodroide.adventurelog.feature.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.ClearSessionUseCase
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import isValidUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val initializeSessionUseCase: InitializeSessionUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val clearSessionUseCase: ClearSessionUseCase
) : ViewModel() {

    private val logger = co.touchlab.kermit.Logger.withTag("LoginViewModel")
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        viewModelScope.launch {
            try {
                // Use the UseCase to check for existing session and initialize network
                val existingSession = initializeSessionUseCase()

                if (existingSession != null) {
                    logger.d { "Found existing user session for: ${existingSession.username}" }
                    _uiState.update { LoginUiState.Success(existingSession) }
                    return@launch
                }

                // No valid session found - show clean login form
                _uiState.update { LoginUiState.Empty }

            } catch (e: Exception) {
                logger.e(e) { "Error during initialization" }
                _uiState.update { LoginUiState.Empty }
            }
        }
    }

    fun updateUserName(newUserName: String) {
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
        _loginFormState.value = _loginFormState.value.copy(
            serverUrl = newUrl,
            urlError = !isValidUrl(newUrl)
        )
    }

    fun updateRememberSession(value: Boolean) {
        _loginFormState.value = _loginFormState.value.copy(rememberSession = value)

        // If user unchecks "Remember me", clear any saved session for next app restart
        if (!value) {
            viewModelScope.launch {
                try {
                    clearSessionUseCase()
                    logger.d { "Cleared saved session because user unchecked remember me" }
                } catch (e: Exception) {
                    logger.e(e) { "Error clearing saved session" }
                }
            }
        }
    }

    fun validateForm(): Boolean {
        val currentState = _loginFormState.value
        val isValid = currentState.userName.isNotBlank() &&
                currentState.password.isNotBlank() &&
                isValidUrl(currentState.serverUrl)

        _loginFormState.value = currentState.copy(
            userNameError = currentState.userName.isBlank(),
            passwordError = currentState.password.isBlank(),
            urlError = !isValidUrl(currentState.serverUrl)
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

                    is Either.Right -> {
                        val userDetails = result.value
                        logger.d { "Login successful for user: ${userDetails.username}" }

                        // Save user session - this controls token persistence
                        if (rememberSession) {
                            // Save session persistently - will auto-login on next app start
                            saveSessionUseCase(userDetails)
                            logger.d { "Saved persistent session - will auto-login next time" }
                        } else {
                            // Don't save session persistently - user will need to login again
                            logger.d { "Remember me not checked - won't auto-login next time" }
                        }

                        _uiState.update { LoginUiState.Success(userDetails) }
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error during login" }
                _uiState.update { LoginUiState.Error("Unexpected error occurred: ${e.message}") }
            }
        }
    }

    fun clearErrors() {
        _loginFormState.value = _loginFormState.value.copy(
            userNameError = false,
            passwordError = false,
            urlError = false
        )
    }
}