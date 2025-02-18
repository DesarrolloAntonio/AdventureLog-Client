package com.desarrollodroide.adventurelog.feature.login

import androidx.lifecycle.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(

) : ViewModel() {

    private var _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Success)
    val uiState: StateFlow<LoginUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginUiState.Loading
        )

    var rememberSession = mutableStateOf(false)

    var serverUrl = mutableStateOf("")
    var userName = mutableStateOf("")
    var password = mutableStateOf("")

    val userNameError = mutableStateOf(false)
    val passwordError = mutableStateOf(false)
    val urlError = mutableStateOf(false)

    init {
        viewModelScope.launch {

        }
    }

}