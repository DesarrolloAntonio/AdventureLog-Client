package com.desarrollodroide.adventurelog.core.network.ktor

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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What a save of a collection's note, stay or transport puts on the wire.
 *
 * The client leaves out any field equal to its default, so a `false` or an emptied text used to
 * vanish from the body and the server kept what it had (measured).
 */
class CollectionItemBodyTest {

    private var sent = ""

    private fun network(answer: String) = KtorAdventureLogNetwork(
        HttpClient(MockEngine { request ->
            sent = request.body.toByteArray().decodeToString()
            respond(content = answer, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }) { install(ContentNegotiation) { json(apiJson) } }
    ).apply { initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token") }

    private fun body(): JsonObject = apiJson.parseToJsonElement(sent) as JsonObject

    private val noteAnswer = """{"id":"n1","user":"u1","name":"QA_Note","created_at":"","updated_at":""}"""
    private val lodgingAnswer = """{"id":"l1","user":"u1","name":"QA_Lodging","created_at":"","updated_at":""}"""
    private val transportAnswer = """{"id":"t1","user":"u1","type":"plane","name":"QA_Transport","created_at":"","updated_at":""}"""

    @Test
    fun `a note turned private says so in the body`() = runTest {
        network(noteAnswer).updateNote(noteId = "n1", name = "QA_Note", content = "", date = null, isPublic = false)

        assertEquals(false, body()["is_public"]?.jsonPrimitive?.boolean, "is_public was left out: ${body().keys}")
    }

    @Test
    fun `a stay sends its emptied text and the currency of its price`() = runTest {
        network(lodgingAnswer).updateLodging(
            lodgingId = "l1", name = "QA_Lodging", type = "hotel", description = "", checkIn = null,
            checkOut = null, timezone = null, reservationNumber = "", price = "80.00", priceCurrency = "EUR",
            link = "", location = "", isPublic = false
        )

        val b = body()
        assertEquals("", b["link"]?.jsonPrimitive?.content, "link was left out: ${b.keys}")
        assertEquals("", b["reservation_number"]?.jsonPrimitive?.content)
        assertEquals("", b["description"]?.jsonPrimitive?.content)
        assertEquals("EUR", b["price_currency"]?.jsonPrimitive?.content)
        assertEquals(false, b["is_public"]?.jsonPrimitive?.boolean)
    }

    @Test
    fun `a transport sends its emptied text and its public flag`() = runTest {
        network(transportAnswer).updateTransportation(
            transportationId = "t1", name = "QA_Transport", type = "plane", description = "", rating = 0.0,
            link = "", fromLocation = "", toLocation = "", departureDate = "", arrivalDate = "",
            departureTimezone = "", arrivalTimezone = "", flightNumber = "", distance = "",
            originLatitude = null, originLongitude = null, destinationLatitude = null,
            destinationLongitude = null, isPublic = false, images = emptyList(), attachments = emptyList()
        )

        val b = body()
        assertEquals("", b["description"]?.jsonPrimitive?.content, "description was left out: ${b.keys}")
        assertEquals("", b["link"]?.jsonPrimitive?.content)
        assertEquals("", b["flight_number"]?.jsonPrimitive?.content)
        assertEquals(false, b["is_public"]?.jsonPrimitive?.boolean)
    }
}
