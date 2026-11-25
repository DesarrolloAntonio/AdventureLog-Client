package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The calls whose answers or bodies changed shape in 0.14.0, against answers shaped like
 * upstream's v0.14.0 OpenAPI schema (HostedAccount, PaginatedUltraSlimCollectionList,
 * PlaceSearchResponse, Country). Not measured on a real 0.14.0 server.
 */
class Network014Test {

    private class Harness(private val answers: (HttpRequestData) -> String) {
        val requests = mutableListOf<Pair<HttpRequestData, String>>()
        val network = KtorAdventureLogNetwork(
            HttpClient(MockEngine { request ->
                if (request.url.encodedPath == "/api/server-info/") {
                    respond(
                        """{"version":"v0.14.0","capabilities":["mobile_auth_v1"]}""",
                        HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } else {
                    requests += request to request.body.toByteArray().decodeToString()
                    respond(answers(request), HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            }) {
                install(ContentNegotiation) { json(defaultJson) }
            }
        ).apply { initializeFromSession(serverUrl = "https://qa.test:3447", sessionToken = "session-token") }
    }

    private fun HttpRequestData.query(): Map<String, String> =
        url.parameters.entries().associate { (k, v) -> k to v.single() }

    @Test
    fun `the account's profile is read from under user`() = runBlocking {
        val h = Harness {
            """{"user":{"uuid":"u1","username":"john","first_name":"John","profile_visibility":"private"},""" +
                """"pending_invite_count":0,"unread_comment_count":0,"has_access":true,"cloud_mode":false,"subscription":{}}"""
        }

        val user = h.network.getUserDetails()

        assertEquals("/api/account/", h.requests.single().first.url.encodedPath)
        assertEquals("john", user.username)
        assertEquals("u1", user.uuid)
    }

    @Test
    fun `all collections are the active page plus the archived page`() = runBlocking {
        val h = Harness { request ->
            val id = if (request.url.parameters["archived"] == "true") "archived" else "active"
            """{"count":1,"next":null,"previous":null,"results":[""" +
                """{"id":"$id","name":"$id","created_at":"","updated_at":"","locations":[]}]}"""
        }

        val collections = h.network.getAllCollections()

        assertEquals(listOf("active", "archived"), collections.map { it.id })
        val (all, archived) = h.requests.map { it.first }
        assertEquals("/api/collections/", all.url.encodedPath)
        assertEquals(mapOf("page_size" to "1000"), all.query())
        assertEquals(mapOf("archived" to "true", "page_size" to "1000"), archived.query())
    }

    @Test
    fun `sharing names the user in the body of the invite`() = runBlocking {
        val h = Harness { "{}" }

        h.network.shareCollection(collectionId = "c1", userUuid = "u1")

        val (request, body) = h.requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/collections/c1/invites/", request.url.encodedPath)
        assertTrue(body.contains("\"user\":\"u1\""), body)
    }

    @Test
    fun `revoking an invite is a DELETE on it`() = runBlocking {
        val h = Harness { "" }

        h.network.revokeInvite(collectionId = "c1", userUuid = "u1")

        val request = h.requests.single().first
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/api/collections/c1/invites/u1/", request.url.encodedPath)
    }

    @Test
    fun `an itinerary entry is deleted under its collection`() = runBlocking {
        val h = Harness { "" }

        h.network.deleteItineraryEntry(collectionId = "c1", entryId = "e1")

        val request = h.requests.single().first
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/api/collections/c1/itinerary-items/e1/", request.url.encodedPath)
    }

    @Test
    fun `place search reads the results object and skips places with no coordinates`() = runBlocking {
        val h = Harness {
            """{"count":2,"provider_used":"nominatim","providers_attempted":["nominatim"],"results":[""" +
                """{"lat":-12.04,"lon":-77.04,"name":"Lima","display_name":"Lima, Peru"},""" +
                """{"lat":null,"lon":null,"name":null,"display_name":"Somewhere"}]}"""
        }

        val results = h.network.searchLocations("Lima")

        assertEquals("/api/places/search/", h.requests.single().first.url.encodedPath)
        assertEquals(mapOf("q" to "Lima"), h.requests.single().first.query())
        assertEquals(listOf("Lima"), results.map { it.name })
    }

    @Test
    fun `a country with no flag still decodes`() = runBlocking {
        val h = Harness { """[{"id":1,"name":"Peru","country_code":"PE","flag_url":null,"num_regions":25,"num_visits":3}]""" }

        val countries = h.network.getCountries()

        assertEquals("", countries.single().flagUrl)
    }
}
