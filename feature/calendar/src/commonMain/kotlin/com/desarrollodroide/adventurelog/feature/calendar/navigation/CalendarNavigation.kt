package com.desarrollodroide.adventurelog.feature.calendar.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.calendar.ui.screen.CalendarScreenRoute
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations

fun NavGraphBuilder.calendarScreen(
    onOpenPlace: (String) -> Unit,
    onOpenCollection: (id: String, name: String) -> Unit
) {
    composable(
        route = NavigationRoutes.Calendar.route,
        enterTransition = NavigationAnimations.enterTransitionVertical,
        exitTransition = NavigationAnimations.exitTransitionVertical
    ) {
        CalendarScreenRoute(onOpenPlace = onOpenPlace, onOpenCollection = onOpenCollection)
    }
}
