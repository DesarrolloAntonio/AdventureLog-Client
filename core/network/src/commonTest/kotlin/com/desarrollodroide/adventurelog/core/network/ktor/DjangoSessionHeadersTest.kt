package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Server 0.13.1 dropped the middleware that read X-Session-Token and checks CSRF on login, so a
 * request only counts as signed in when it carries the session as a cookie, a matching csrftoken
 * pair and a same-origin Referer. Measured on the NAS: without these, login answers 403 "CSRF
 * cookie not set" and every other call is anonymous.
 */
class DjangoSessionHeadersTest {

    /**
     * `proxied` answers the /csrf/ probe the way upstream's all-in-one install does (the web
     * frontend's HTML 404); otherwise Django's JSON. Probes are counted, not recorded as requests.
     */
    private class Harness(
        status: HttpStatusCode = HttpStatusCode.OK,
        proxied: Boolean = false,
        body: String = "{}",
    ) {
        val requests = mutableListOf<HttpRequestData>()
        var probes = 0
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val client = HttpClient(MockEngine { request ->
            if (request.url.encodedPath == "/csrf/") {
                probes++
                if (proxied) respond("<!doctype html>", HttpStatusCode.NotFound, headersOf(HttpHeaders.ContentType, "text/html"))
                else respond("""{"csrfToken":"x"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else {
                requests += request
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }) {
            install(ContentNegotiation) { json(defaultJson) }
        }
        val network = KtorAdventureLogNetwork(client, backgroundScope = scope)

        suspend fun settle() = withTimeout(5_000) { scope.coroutineContext[Job]!!.children.toList().joinAll() }
    }

    private fun HttpRequestData.cookies(): Map<String, String> =
        headers[HttpHeaders.Cookie].orEmpty().split(';')
            .map { it.trim() }
            .filter { it.contains('=') }
            .associate { it.substringBefore('=') to it.substringAfter('=') }

    private fun assertCsrfPair(request: HttpRequestData) {
        val csrf = request.headers["X-CSRFToken"]
        assertNotNull(csrf, "no X-CSRFToken header")
        assertTrue(Regex("[A-Za-z0-9]{32}").matches(csrf), "Django rejects a token shaped like '$csrf'")
        assertEquals(csrf, request.cookies()["csrftoken"], "the csrftoken cookie does not match the header")
    }

    @Test
    fun `a signed-in write carries the session cookie and a csrf pair and the server as Referer`() = runBlocking {
        val h = Harness()
        h.network.initializeFromSession(serverUrl = "https://qa.test:3447/", sessionToken = "session-token")

        h.client.post("https://qa.test:3447/api/locations/") { headers { commonHeaders("session-token") } }

        val request = h.requests.single()
        assertEquals("session-token", request.cookies()["sessionid"])
        assertCsrfPair(request)
        assertEquals("https://qa.test:3447", request.headers[HttpHeaders.Referrer])
        h.scope.cancel()
    }

    @Test
    fun `login carries a csrf pair and a Referer but no session`() = runBlocking {
        val h = Harness(HttpStatusCode.Forbidden)

        val failure = runCatching { h.network.sendLogin("https://qa.test", "someone", "secret") }.exceptionOrNull()

        val request = h.requests.singleOrNull() ?: error("no login request was sent: $failure")
        assertEquals("/auth/app/v1/auth/login", request.url.encodedPath)
        assertCsrfPair(request)
        assertNull(request.cookies()["sessionid"])
        assertEquals("https://qa.test", request.headers[HttpHeaders.Referrer])
        h.scope.cancel()
    }

    @Test
    fun `sign-out still sends the session after the local session is cleared`() = runBlocking {
        val h = Harness(HttpStatusCode.Unauthorized)
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")

        h.network.endServerSession()
        h.network.clearSession()
        h.settle()

        val request = h.requests.single()
        assertEquals("session-token", request.cookies()["sessionid"])
        assertCsrfPair(request)
        h.scope.cancel()
    }

    @Test
    fun `nothing of the session goes to another server`() = runBlocking {
        val h = Harness()
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")

        h.client.get("https://en.wikipedia.org/api/rest_v1/page/summary/Lima")

        val request = h.requests.single()
        assertNull(request.headers[HttpHeaders.Cookie])
        assertNull(request.headers["X-CSRFToken"])
        h.scope.cancel()
    }

    @Test
    fun `login keeps the session the app client returns in the body`() = runBlocking {
        // What allauth's app client answers: no Set-Cookie (the proxy would strip it anyway).
        val login = """{"status":200,"data":{"user":{"id":7,"display":"playreview","username":"playreview"},""" +
            """"methods":[{"method":"password","at":1.0,"username":"playreview"}]},""" +
            """"meta":{"is_authenticated":true,"session_token":"token-from-body"}}"""
        val h = Harness(body = login)

        h.network.sendLogin("https://qa.test", "playreview", "secret")
        h.network.endServerSession()
        h.settle()

        val signOut = h.requests.last()
        assertEquals("token-from-body", signOut.cookies()["sessionid"])
        h.scope.cancel()
    }

    @Test
    fun `behind the web frontend proxy a write sends only the session and no trailing slash`() = runBlocking {
        val h = Harness(proxied = true)
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")

        h.client.post("https://qa.test/api/locations/") { headers { commonHeaders("session-token") } }

        val request = h.requests.single()
        assertEquals("/api/locations", request.url.encodedPath, "the frontend answers a trailing slash with a 308 a POST does not survive")
        assertEquals(mapOf("sessionid" to "session-token"), request.cookies(), "the proxy adds its own csrftoken")
        assertNull(request.headers["X-CSRFToken"], "a second X-CSRFToken beside the proxy's makes Django refuse the request")
        assertEquals("https://qa.test", request.headers[HttpHeaders.Origin], "SvelteKit refuses a form post from another origin")
        h.scope.cancel()
    }

    @Test
    fun `the server is probed once and not on every request`() = runBlocking {
        val h = Harness(proxied = true)
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")

        repeat(3) { h.client.get("https://qa.test/api/collections/") { headers { commonHeaders("session-token") } } }

        assertEquals(1, h.probes)
        assertEquals(3, h.requests.size)
        h.scope.cancel()
    }
}
