package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes

/**
 * Enumeration of screen types that can be displayed in the HomeScreen
 * Enhanced with navigation route mapping and utility methods
 */
enum class CurrentScreen(val route: String, val index: Int, val title: String) {
    HOME(NavigationRoutes.Home.screen, 0, "Home"), // Title will be customized in getTitle()
    ADVENTURES(NavigationRoutes.Adventures.route, 1, "Adventures"),
    COLLECTIONS(NavigationRoutes.Collections.route, 2, "Collections"),
    TRAVEL(NavigationRoutes.Travel.route, 3, "Travel"),
    MAP(NavigationRoutes.Map.route, 4, "Map"),
    CALENDAR(NavigationRoutes.Calendar.route, 5, "Calendar"),
    SETTINGS(NavigationRoutes.Settings.route, 6, "Settings"),
    ADD_ADVENTURE("add_adventure", 7, "New Adventure"),
    ADD_COLLECTION("add_collection", 8, "New Collection"),
    EDIT_ADVENTURE("edit_adventure", 9, "Edit Adventure"),
    EDIT_COLLECTION("edit_collection", 10, "Edit Collection");

    companion object {
        /**
         * Find the CurrentScreen that corresponds to a route
         * @param route The navigation route
         * @return The corresponding CurrentScreen, or HOME if not found
         */
        fun fromRoute(route: String): CurrentScreen {
            // Handle collection detail routes specially
            if (route.startsWith("collection/")) {
                return COLLECTIONS
            }
            return entries.find { it.route == route } ?: HOME
        }

        /**
         * Find the CurrentScreen that corresponds to a selection index
         * @param index The selection index (0-based)
         * @return The corresponding CurrentScreen, or HOME if index is invalid
         */
        fun fromIndex(index: Int): CurrentScreen {
            return entries.find { it.index == index } ?: HOME
        }
    }
    
    /**
     * Get the formatted title for this screen
     * @param userName The user's name to use for HOME screen
     * @return The formatted title
     */
    fun getTitle(userName: String = ""): String {
        return when (this) {
            HOME -> "Hi, $userName!"
            else -> title
        }
    }
}