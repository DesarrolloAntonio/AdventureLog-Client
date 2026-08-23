package com.desarrollodroide.adventurelog.feature.home.model

import com.desarrollodroide.adventurelog.core.model.Dashboard
import kotlinx.datetime.LocalDate

/**
 * The whole screen now resolves from one request, so stats no longer need a state of their own -
 * they either arrived with everything else or the screen is still loading.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Success(
        val userName: String = "",
        val dashboard: Dashboard = Dashboard(),
        /**
         * Captured when the dashboard was fetched. Held here so the screen can tell an event that
         * is already running from one still ahead without reading a clock during composition.
         */
        val today: LocalDate? = null
    ) : HomeUiState
}
