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

    private class Harness(status: HttpStatusCode = HttpStatusCode.OK) {
        val requests = mutableListOf<HttpRequestData>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val client = HttpClient(MockEngine { request -> requests += request; respond("{}", status) }) {
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
}
