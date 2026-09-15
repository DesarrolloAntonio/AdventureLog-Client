package com.desarrollodroide.adventurelog.core.common.navigation

/**
 * A value made safe to sit inside a route: every byte outside the URI's unreserved set is
 * percent-encoded, and Navigation decodes it again before the destination reads the argument.
 *
 * Routes carry user text - a collection's name, a note's JSON - and a `/` in a name split the path
 * into a route no destination matched, which crashed on `navigate` (measured: "Madrid/Barcelona").
 * `&`, `#`, `?` and `%` break the query-string arguments the same way.
 */
fun routeArg(value: String): String = buildString {
    for (byte in value.encodeToByteArray()) {
        val c = byte.toInt() and 0xFF
        if (c in 'A'.code..'Z'.code || c in 'a'.code..'z'.code || c in '0'.code..'9'.code ||
            c == '-'.code || c == '.'.code || c == '_'.code || c == '~'.code
        ) {
            append(c.toChar())
        } else {
            append('%')
            append(HEX[c shr 4])
            append(HEX[c and 0x0F])
        }
    }
}

private const val HEX = "0123456789ABCDEF"

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
            return "adventures/edit?adventureId=${routeArg(adventureId)}&adventureJson=${routeArg(adventureJson)}"
        }
    }

    object Collections {
        const val route = "collections"
        const val add = "add_collection"
        const val editRoute = "edit_collection/{collectionId}"
        const val detailRoute = "collection/{collectionId}/{collectionName}"
        
        fun createEditRoute(collectionId: String): String {
            return "edit_collection/${routeArg(collectionId)}"
        }
        
        fun createDetailRoute(collectionId: String, collectionName: String): String {
            return "collection/${routeArg(collectionId)}/${routeArg(collectionName)}"
        }
        
        object Notes {
            const val addRoute = "notes/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String = "notes/add?collectionId=${routeArg(collectionId)}"

            const val editRoute = "notes/edit?collectionId={collectionId}&noteId={noteId}&noteJson={noteJson}"

            fun createEditRoute(collectionId: String, noteId: String, noteJson: String): String =
                "notes/edit?collectionId=${routeArg(collectionId)}&noteId=${routeArg(noteId)}&noteJson=${routeArg(noteJson)}"
        }

        object Checklists {
            const val addRoute = "checklists/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String =
                "checklists/add?collectionId=${routeArg(collectionId)}"

            const val editRoute =
                "checklists/edit?collectionId={collectionId}&checklistId={checklistId}&checklistJson={checklistJson}"

            fun createEditRoute(
                collectionId: String,
                checklistId: String,
                checklistJson: String
            ): String =
                "checklists/edit?collectionId=${routeArg(collectionId)}&checklistId=${routeArg(checklistId)}&checklistJson=${routeArg(checklistJson)}"
        }

        object Lodgings {
            const val addRoute = "lodging/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String =
                "lodging/add?collectionId=${routeArg(collectionId)}"

            const val editRoute =
                "lodging/edit?collectionId={collectionId}&lodgingId={lodgingId}&lodgingJson={lodgingJson}"

            fun createEditRoute(
                collectionId: String,
                lodgingId: String,
                lodgingJson: String
            ): String =
                "lodging/edit?collectionId=${routeArg(collectionId)}&lodgingId=${routeArg(lodgingId)}&lodgingJson=${routeArg(lodgingJson)}"
        }

        object Transportations {
            const val addRoute = "transportations/add?collectionId={collectionId}"

            fun createAddRoute(collectionId: String): String {
                return "transportations/add?collectionId=${routeArg(collectionId)}"
            }
            const val editRoute = "transportations/edit?transportationId={transportationId}&transportationJson={transportationJson}"
            
            fun createEditRoute(transportationId: String, transportationJson: String): String {
                return "transportations/edit?transportationId=${routeArg(transportationId)}&transportationJson=${routeArg(transportationJson)}"
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
