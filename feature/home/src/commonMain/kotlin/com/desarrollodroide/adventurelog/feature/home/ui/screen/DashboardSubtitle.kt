package com.desarrollodroide.adventurelog.feature.home.ui.screen

import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen

// Split out of MainShell.kt (Compose, androidMain): plain logic, tested on every platform.

/**
 * The line under the screen's name: what the numbers say about you right now.
 *
 * Only home has one for the moment - the other screens print their own count inside the page,
 * and two copies of "22 places" one above the other reads as a mistake.
 */
internal fun dashboardSubtitle(screen: CurrentScreen, state: HomeUiState): String? {
    if (screen != CurrentScreen.HOME) return null
    val dashboard = (state as? HomeUiState.Success)?.dashboard ?: return null
    val visited = dashboard.stats.visitedLocationCount
    // Ahead means not started: the trip under way is on the card below, not ahead of anyone
    // (measured: "3 trips ahead" with one in progress and two to come).
    val trips = dashboard.upcomingTrips.size
    return listOfNotNull(
        "$visited places visited",
        when {
            trips == 0 -> null
            trips == 1 -> "1 trip ahead"
            else -> "$trips trips ahead"
        }
    ).joinToString(" \u00b7 ")
}
