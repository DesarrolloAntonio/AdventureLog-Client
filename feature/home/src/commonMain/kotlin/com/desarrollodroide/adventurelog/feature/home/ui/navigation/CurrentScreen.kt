package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes

enum class CurrentScreen(val route: String, val index: Int, val title: String) {
    HOME(NavigationRoutes.Home.screen, 0, "Home"),
    ADVENTURES(NavigationRoutes.Locations.route, 1, "Locations"),
    COLLECTIONS(NavigationRoutes.Collections.route, 2, "Collections"),
    TRAVEL(NavigationRoutes.Travel.route, 3, "World"),
    MAP(NavigationRoutes.Map.route, 4, "Map"),
    CALENDAR(NavigationRoutes.Calendar.route, 5, "Calendar"),
    SETTINGS(NavigationRoutes.Settings.route, 6, "Settings");

    companion object {
        fun fromRoute(route: String): CurrentScreen {
            return when {
                route == NavigationRoutes.Home.screen -> HOME
                route == NavigationRoutes.Locations.route -> ADVENTURES
                route.startsWith(NavigationRoutes.Locations.add) -> ADVENTURES
                route.startsWith("adventures/edit") -> ADVENTURES
                route == NavigationRoutes.Collections.route -> COLLECTIONS
                route.startsWith(NavigationRoutes.Collections.add) -> COLLECTIONS
                route.startsWith("edit_collection/") -> COLLECTIONS
                route.startsWith("collection/") -> COLLECTIONS
                route.startsWith(NavigationRoutes.Collections.Transportations.addRoute.substringBefore('?')) -> COLLECTIONS
                route.startsWith("transportations/edit") -> COLLECTIONS
                route == NavigationRoutes.Travel.route -> TRAVEL
                route == NavigationRoutes.Map.route -> MAP
                route == NavigationRoutes.Calendar.route -> CALENDAR
                route == NavigationRoutes.Settings.route -> SETTINGS
                else -> HOME
            }
        }

        fun fromIndex(index: Int): CurrentScreen {
            return entries.find { it.index == index } ?: HOME
        }
    }
    
    fun getTitle(userName: String = ""): String {
        return when (this) {
            HOME -> "Hi, $userName!"
            else -> title
        }
    }
}
