package com.desarrollodroide.adventurelog.core.network.ktor

import kotlinx.serialization.json.JsonNull
import com.desarrollodroide.adventurelog.core.network.di.apiJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** What an update of a place puts on the wire, written by the same Json the app's client uses. */
class LocationUpdateBodyTest {

    private var sent = ""

    private fun network() = KtorAdventureLogNetwork(
        HttpClient(MockEngine { request ->
            sent = request.body.toByteArray().decodeToString()
            respond(
                content = """{"id":"p1","name":"QA_Place","created_at":"2026-09-15T00:00:00Z",""" +
                    """"updated_at":"2026-09-15T00:00:00Z","user":{"uuid":"u1","username":"claude"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }) { install(ContentNegotiation) { json(apiJson) } }
    ).apply { initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token") }

    private suspend fun bodySentFor(collections: List<String>?, price: Double? = 25.0): JsonObject {
        network().updateAdventure(
            adventureId = "p1", name = "QA_Place", description = "", category = null, rating = 4.0,
            link = "", location = "", latitude = null, longitude = null, isPublic = false,
            tags = emptyList(), collections = collections, visits = emptyList(), price = price,
            priceCurrency = "EUR"
        )
        return apiJson.parseToJsonElement(sent).jsonObject
    }

    @Test
    fun `an edit that does not name collections leaves them out of the body`() = runTest {
        // The server replaces a place's collections whenever the key is present: `[]` took every
        // edited place out of all its collections (measured).
        val body = bodySentFor(collections = null)

        assertFalse("collections" in body, "collections were sent: ${body["collections"]}")
        assertEquals("QA_Place", body.getValue("name").jsonPrimitive.content)
    }

    @Test
    fun `manage collections sends the collections and nothing else`() = runTest {
        network().updateLocationCollections(locationId = "p1", collections = listOf("c1"))

        assertEquals(setOf("collections"), apiJson.parseToJsonElement(sent).jsonObject.keys)
    }

    @Test
    fun `a price cleared in the form is sent as null`() = runTest {
        val body = bodySentFor(collections = null, price = null)

        assertEquals(JsonNull, body["price"], "price was not cleared: ${body["price"]}")
        assertEquals(JsonNull, body["price_currency"])
    }

    @Test
    fun `a price kept in the form is sent with its currency`() = runTest {
        val body = bodySentFor(collections = null, price = 25.0)

        assertEquals("25.0", body["price"]?.jsonPrimitive?.content)
        assertEquals("EUR", body["price_currency"]?.jsonPrimitive?.content)
    }

    @Test
    fun `collections chosen for the place are sent`() = runTest {
        val body = bodySentFor(collections = listOf("c1", "c2"))

        assertEquals(listOf("c1", "c2"), (body["collections"] as? JsonArray)?.map { it.jsonPrimitive.content })
    }
}
