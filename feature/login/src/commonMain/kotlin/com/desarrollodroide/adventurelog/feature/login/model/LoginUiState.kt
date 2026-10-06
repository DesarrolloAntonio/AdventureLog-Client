package com.desarrollodroide.adventurelog.feature.login.model

import com.desarrollodroide.adventurelog.core.model.UserDetails

sealed interface LoginUiState {
    data object Empty : LoginUiState
    data class Error(val message: String) : LoginUiState
    /** At launch, while a stored session is checked with the server - there is no form yet. */
    data object CheckingSession : LoginUiState
    /** A login the user started: the form stays on screen under a progress indicator. */
    data object Loading : LoginUiState
    data class Success(val userDetails: UserDetails) : LoginUiState
}