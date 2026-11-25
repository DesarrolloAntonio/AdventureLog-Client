package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The app's real client follows redirects, and the web frontend answers the probe's trailing
 * slash with a 308 on both server versions. The probe must survive the redirect without probing
 * itself again.
 */
class ProbeRedirectTest {

    private fun firstRequest(serverInfo: Boolean): HttpRequestData {
        val requests = mutableListOf<HttpRequestData>()
        val client = HttpClient(MockEngine { request ->
            requests += request
            val path = request.url.encodedPath
            when {
                path.length > 1 && path.endsWith("/") ->
                    respond("", HttpStatusCode.PermanentRedirect, headersOf(HttpHeaders.Location, path.trimEnd('/')))
                path == "/api/server-info" && serverInfo ->
                    respond("""{"capabilities":[]}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                path == "/api/server-info" ->
                    respond("""{"detail":"Not found."}""", HttpStatusCode.NotFound, headersOf(HttpHeaders.ContentType, "application/json"))
                else -> respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }) // followRedirects left on, as in NetworkModule
        val network = KtorAdventureLogNetwork(client)
        network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")
        runBlocking {
            withTimeout(5_000) {
                client.get("https://qa.test/api/collections/") { headers { commonHeaders("session-token") } }
            }
        }
        return requests.last()
    }

    @Test
    fun `a proxied 0_14 server is probed through the redirect`() {
        val request = firstRequest(serverInfo = true)
        assertEquals("/api/collections", request.url.encodedPath)
        assertNull(request.headers[HttpHeaders.Cookie])
    }

    @Test
    fun `a proxied 0_13 server is probed through the redirect`() {
        val request = firstRequest(serverInfo = false)
        assertEquals("/api/collections", request.url.encodedPath)
        assertEquals("sessionid=session-token", request.headers[HttpHeaders.Cookie])
    }
}
