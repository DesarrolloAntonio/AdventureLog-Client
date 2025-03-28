package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes

/**
 * Enumeration of screen types that can be displayed in the HomeScreen
 * Enhanced with navigation route mapping and utility methods
 */
enum class CurrentScreen(val route: String, val index: Int) {
    HOME(NavigationRoutes.Home.screen, 0),
    ADVENTURES(NavigationRoutes.Adventures.route, 1),
    COLLECTIONS(NavigationRoutes.Collections.route, 2),
    TRAVEL(NavigationRoutes.Travel.route, 3),
    MAP(NavigationRoutes.Map.route, 4),
    CALENDAR(NavigationRoutes.Calendar.route, 5),
    SETTINGS(NavigationRoutes.Settings.route, 6);

    companion object {
        /**
         * Find the CurrentScreen that corresponds to a route
         * @param route The navigation route
         * @return The corresponding CurrentScreen, or HOME if not found
         */
        fun fromRoute(route: String): CurrentScreen {
            return values().find { it.route == route } ?: HOME
        }

        /**
         * Find the CurrentScreen that corresponds to a selection index
         * @param index The selection index (0-based)
         * @return The corresponding CurrentScreen, or HOME if index is invalid
         */
        fun fromIndex(index: Int): CurrentScreen {
            return values().find { it.index == index } ?: HOME
        }
    }
}
