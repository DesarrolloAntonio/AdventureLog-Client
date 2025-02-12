package com.desarrollodroide.adventurelog.feature.login.model

sealed interface LoginUiState {
    data object Empty : LoginUiState
    data class Error(val message: String) : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
}