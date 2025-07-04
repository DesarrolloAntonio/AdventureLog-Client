package com.desarrollodroide.adventurelog.feature.home.model

import com.desarrollodroide.adventurelog.core.model.UserStats

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data class Success(val stats: UserStats) : StatsUiState
    data class Error(val message: String) : StatsUiState
}
