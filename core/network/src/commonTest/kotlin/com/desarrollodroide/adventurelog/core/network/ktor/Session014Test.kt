package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Against server 0.14.0 the session is the X-Session-Token header alone: upstream's
 * DisableCSRFForHeaderCredentialsMiddleware skips CSRF for it only when no session cookie comes
 * along. Taken from upstream's code (users/middleware.py, users/authentication.py); no 0.14.0
 * server has been driven yet.
 */
class Session014Test {

    private class Harness(proxied: Boolean = false, status: HttpStatusCode = HttpStatusCode.OK, body: String = "{}") {
        val requests = mutableListOf<HttpRequestData>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val client = HttpClient(MockEngine { request ->
            val path = request.url.encodedPath
            when {
                path == "/api/server-info/" && proxied ->
                    respond("", HttpStatusCode.PermanentRedirect, headersOf(HttpHeaders.Location, "/api/server-info"))
                path.startsWith("/api/server-info") ->
                    respond(
                        """{"version":"v0.14.0","capabilities":["mobile_auth_v1"]}""",
                        HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                else -> {
                    requests += request
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            }
        }) {
            followRedirects = false
            install(ContentNegotiation) { json(defaultJson) }
        }
        val network = KtorAdventureLogNetwork(client, backgroundScope = scope)

        suspend fun settle() = withTimeout(5_000) { scope.coroutineContext[Job]!!.children.toList().joinAll() }
    }

    @Test
    fun `login goes to the app client under api with no cookie and no csrf`() = runBlocking {
        val h = Harness(status = HttpStatusCode.Forbidden)

        runCatching { h.network.sendLogin("https://qa.test", "someone", "secret") }

        val request = h.requests.single()
        assertEquals("/api/auth/app/v1/auth/login", request.url.encodedPath)
        assertNull(request.headers[HttpHeaders.Cookie])
        assertNull(request.headers["X-CSRFToken"])
        h.scope.cancel()
    }

    @Test
    fun `a signed-in write carries only the session token`() = runBlocking {
        val h = Harness()
        h.network.initializeFromSession(serverUrl = "https://qa.test:3447/", sessionToken = "session-token")

        h.client.post("https://qa.test:3447/api/locations/") { headers { commonHeaders("session-token") } }

        val request = h.requests.single()
        assertEquals("/api/locations/", request.url.encodedPath)
        assertEquals("session-token", request.headers[SESSION_TOKEN_HEADER])
        assertNull(request.headers[HttpHeaders.Cookie], "a session cookie turns CSRF back on")
        assertNull(request.headers["X-CSRFToken"])
        h.scope.cancel()
    }

    @Test
    fun `behind the proxy a write loses its trailing slash and names the site as Origin`() = runBlocking {
        val h = Harness(proxied = true)
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")

        h.client.post("https://qa.test/api/locations/") { headers { commonHeaders("session-token") } }

        val request = h.requests.single()
        assertEquals("/api/locations", request.url.encodedPath)
        assertEquals("https://qa.test", request.headers[HttpHeaders.Origin])
        assertNull(request.headers[HttpHeaders.Cookie])
        h.scope.cancel()
    }

    @Test
    fun `signing out ends the app client's session`() = runBlocking {
        val h = Harness(status = HttpStatusCode.Unauthorized)
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")

        h.network.endServerSession()
        h.network.clearSession()
        h.settle()

        val request = h.requests.single()
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/api/auth/app/v1/auth/session", request.url.encodedPath)
        assertEquals("session-token", request.headers[SESSION_TOKEN_HEADER])
        h.scope.cancel()
    }
}
