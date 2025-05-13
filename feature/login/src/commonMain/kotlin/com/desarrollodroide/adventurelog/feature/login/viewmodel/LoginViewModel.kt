package com.desarrollodroide.adventurelog.feature.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.LoginUseCase
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import isValidUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val logger = co.touchlab.kermit.Logger.withTag("LoginViewModel")
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

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
        
        logger.d { "Attempting login with URL: $url" }

        viewModelScope.launch {
            val result = loginUseCase(
                url = url,
                username = username,
                password = password
            )
            _uiState.update {
                when (result) {
                    is Either.Left -> LoginUiState.Error(result.value)
                    is Either.Right -> LoginUiState.Success(result.value)
                }
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