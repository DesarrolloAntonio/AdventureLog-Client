package com.desarrollodroide.adventurelog.core.common.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class NavigationRoutesTest {

    @Test
    fun `a collection name with a slash stays one path segment`() {
        val route = NavigationRoutes.Collections.createDetailRoute("c56ae212", "Madrid/Barcelona")

        // "collection/{collectionId}/{collectionName}" has exactly three segments; a fourth is a
        // route no destination matches, and navigate() threw on it.
        assertEquals(listOf("collection", "c56ae212", "Madrid%2FBarcelona"), route.split("/"))
    }

    @Test
    fun `json in a query argument cannot end the argument or the query`() {
        val json = """{"content":"Tapas & wine #1? 100%"}"""

        val route = NavigationRoutes.Collections.Notes.createEditRoute("c1", "n1", json)
        val query = route.substringAfter("?")

        assertFalse('#' in route, "a # would cut the route into a fragment")
        assertEquals(listOf("collectionId", "noteId", "noteJson"), query.split("&").map { it.substringBefore("=") })
    }

    @Test
    fun `plain values are left as they are`() {
        assertEquals(
            "collection/c56ae212-8bd3-4d36-95a9-223ef47fdca1/Peru_2026",
            NavigationRoutes.Collections.createDetailRoute("c56ae212-8bd3-4d36-95a9-223ef47fdca1", "Peru_2026")
        )
    }

    @Test
    fun `non-ascii text is encoded as utf-8 bytes`() {
        assertEquals("Sacsayhuam%C3%A1n%20%C2%B7%20Cusco", routeArg("Sacsayhuamán · Cusco"))
    }
}
