package com.desarrollodroide.adventurelog.feature.home.model

import com.desarrollodroide.adventurelog.core.model.Location

sealed interface HomeUiState {
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Loading : HomeUiState
    data class Success(
        val userName: String = "",
        val recentLocations: List<Location> = emptyList()
    ) : HomeUiState
}