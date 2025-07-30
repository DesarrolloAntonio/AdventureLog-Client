package com.desarrollodroide.adventurelog.core.common.navigation

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
        const val add = "adventures/add"
        const val editRoute = "adventures/edit?adventureId={adventureId}&adventureJson={adventureJson}"
        
        fun createEditRoute(adventureId: String, adventureJson: String): String {
            return "adventures/edit?adventureId=$adventureId&adventureJson=$adventureJson"
        }
    }

    object Settings {
        const val route = "settings"
    }

    object Collections {
        const val route = "collections"
        
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
    }
}