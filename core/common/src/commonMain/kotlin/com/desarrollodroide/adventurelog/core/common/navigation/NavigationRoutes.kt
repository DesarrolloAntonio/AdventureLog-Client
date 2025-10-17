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

    object Locations {
        const val route = "adventures"
        const val add = "adventures/add"
        const val editRoute = "adventures/edit?adventureId={adventureId}&adventureJson={adventureJson}"
        
        fun createEditRoute(adventureId: String, adventureJson: String): String {
            return "adventures/edit?adventureId=$adventureId&adventureJson=$adventureJson"
        }
    }

    object Collections {
        const val route = "collections"
        const val add = "add_collection"
        const val editRoute = "edit_collection/{collectionId}"
        const val detailRoute = "collection/{collectionId}/{collectionName}"
        
        fun createEditRoute(collectionId: String): String {
            return "edit_collection/$collectionId"
        }
        
        fun createDetailRoute(collectionId: String, collectionName: String): String {
            return "collection/$collectionId/$collectionName"
        }
        
        object Transportations {
            const val add = "transportations/add"
            const val editRoute = "transportations/edit?transportationId={transportationId}&transportationJson={transportationJson}"
            
            fun createEditRoute(transportationId: String, transportationJson: String): String {
                return "transportations/edit?transportationId=$transportationId&transportationJson=$transportationJson"
            }
        }
    }

    object Settings {
        const val route = "settings"
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
