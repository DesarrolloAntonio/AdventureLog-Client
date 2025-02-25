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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _loginFormState = MutableStateFlow(LoginFormState())
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    init {
        viewModelScope.launch {

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
    }

    fun validateForm(): Boolean {
        val currentState = _loginFormState.value
        val isValid = currentState.userName.isNotBlank() &&
                currentState.password.isNotBlank() &&
                isValidUrl( currentState.serverUrl)

        _loginFormState.value = currentState.copy(
            userNameError = currentState.userName.isBlank(),
            passwordError = currentState.password.isBlank(),
            urlError = !isValidUrl(currentState.serverUrl)
        )

        return isValid
    }

    fun login() {
        if (!validateForm()) return

        viewModelScope.launch {
            val arts = loginUseCase(
                username = loginFormState.value.userName,
                password = loginFormState.value.password
            )
            _uiState.update {
                when (arts) {
                    is Either.Left -> LoginUiState.Error(arts.value)
                    is Either.Right -> LoginUiState.Success(arts.value)
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