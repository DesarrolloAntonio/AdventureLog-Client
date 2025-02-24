package com.desarrollodroide.adventurelog.feature.login.model

import com.desarrollodroide.adventurelog.core.model.UserDetails

sealed interface LoginUiState {
    data object Empty : LoginUiState
    data class Error(val message: String) : LoginUiState
    data object Loading : LoginUiState
    data class Success(val userDetails: UserDetails) : LoginUiState
}