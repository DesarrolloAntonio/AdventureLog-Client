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
        
        object Notes {
            const val addRoute = "notes/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String = "notes/add?collectionId=$collectionId"

            const val editRoute = "notes/edit?collectionId={collectionId}&noteId={noteId}&noteJson={noteJson}"

            fun createEditRoute(collectionId: String, noteId: String, noteJson: String): String =
                "notes/edit?collectionId=$collectionId&noteId=$noteId&noteJson=$noteJson"
        }

        object Checklists {
            const val addRoute = "checklists/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String =
                "checklists/add?collectionId=$collectionId"

            const val editRoute =
                "checklists/edit?collectionId={collectionId}&checklistId={checklistId}&checklistJson={checklistJson}"

            fun createEditRoute(
                collectionId: String,
                checklistId: String,
                checklistJson: String
            ): String =
                "checklists/edit?collectionId=$collectionId&checklistId=$checklistId&checklistJson=$checklistJson"
        }

        object Lodgings {
            const val addRoute = "lodging/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String =
                "lodging/add?collectionId=$collectionId"

            const val editRoute =
                "lodging/edit?collectionId={collectionId}&lodgingId={lodgingId}&lodgingJson={lodgingJson}"

            fun createEditRoute(
                collectionId: String,
                lodgingId: String,
                lodgingJson: String
            ): String =
                "lodging/edit?collectionId=$collectionId&lodgingId=$lodgingId&lodgingJson=$lodgingJson"
        }

        object Transportations {
            const val addRoute = "transportations/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String {
                return "transportations/add?collectionId=$collectionId"
            }
            const val editRoute = "transportations/edit?transportationId={transportationId}&transportationJson={transportationJson}"
            
            fun createEditRoute(transportationId: String, transportationJson: String): String {
                return "transportations/edit?transportationId=$transportationId&transportationJson=$transportationJson"
            }
        }
    }

    object Users {
        const val route = "users"
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
