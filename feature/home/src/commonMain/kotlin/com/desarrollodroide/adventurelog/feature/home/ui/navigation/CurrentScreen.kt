package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes

enum class CurrentScreen(val route: String, val index: Int, val title: String) {
    HOME(NavigationRoutes.Home.screen, 0, "Home"),
    // One word for one thing. The bottom bar said Places, this said Locations, and the
    // cards said "3 places" - the web has the same split, calling them Lugares in its nav and
    // ubicaciones in the heading, which is no reason to inherit it.
    PLACES(NavigationRoutes.Locations.route, 1, "Places"),
    COLLECTIONS(NavigationRoutes.Collections.route, 2, "Collections"),
    TRAVEL(NavigationRoutes.Travel.route, 3, "World"),
    MAP(NavigationRoutes.Map.route, 4, "Map"),
    CALENDAR(NavigationRoutes.Calendar.route, 5, "Calendar"),
    SETTINGS(NavigationRoutes.Settings.route, 6, "Settings");

    companion object {
        fun fromRoute(route: String): CurrentScreen {
            return when {
                route == NavigationRoutes.Home.screen -> HOME
                route == NavigationRoutes.Locations.route -> PLACES
                route.startsWith(NavigationRoutes.Locations.add) -> PLACES
                route.startsWith("adventures/edit") -> PLACES
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
