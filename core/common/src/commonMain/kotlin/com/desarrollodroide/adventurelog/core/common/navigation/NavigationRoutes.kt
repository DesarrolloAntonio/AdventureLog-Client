package com.desarrollodroide.adventurelog.core.common.navigation

/**
 * Centralized navigation routes definition to avoid circular dependencies between modules
 */
object NavigationRoutes {

    object Login {
        const val graph = "login_graph"
        const val screen = "login"
    }

    object Home {
        const val graph = "home_graph"
        const val screen = "home"
    }

    object Adventures {
        const val route = "adventures"
    }

    object Settings {
        const val route = "settings"
    }

    object Collections {
        const val route = "collections"
        
        // Method to create route for a specific collection
        fun createCollectionDetailRoute(collectionId: String): String {
            return "collection/$collectionId"
        }
    }

    object Travel {
        const val route = "travel"
    }

    object Map {
        const val route = "map"
    }

    object Calendar {
        const val route = "calendar"
    }

    object Detail {
        const val route = "detail"
        
        // Method to create route with parameters
        fun createDetailRoute(adventureId: String): String {
            return "detail/$adventureId"
        }
    }
}
