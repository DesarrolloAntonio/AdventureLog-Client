package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SignOutTest {

    private class Harness {
        val requests = mutableListOf<HttpRequestData>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        // allauth answers a successful DELETE on the session with 401.
        val network = KtorAdventureLogNetwork(
            HttpClient(MockEngine { request -> requests += request; respond("", HttpStatusCode.Unauthorized) }),
            backgroundScope = scope
        )

        /** Waits for the background request to be sent AND answered, so nothing is left racing the assertions. */
        suspend fun settle() = withTimeout(5_000) { scope.coroutineContext[Job]!!.children.toList().joinAll() }
    }

    @Test
    fun `signing out asks the server to end the session it holds`() = runBlocking {
        val h = Harness()
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")

        h.network.endServerSession()
        h.settle()

        assertEquals(1, h.requests.size, "no DELETE was sent to end the session")
        val request = h.requests.first()
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("https://qa.test/auth/browser/v1/auth/session", request.url.toString())
        assertEquals("session-token", request.headers["X-Session-Token"])
        h.scope.cancel()
    }

    @Test
    fun `the 401 that means signed out is not reported as a rejected session`() = runBlocking {
        val h = Harness()
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = "session-token")
        // Subscribed before the request, and given a second after it is answered to hear anything.
        val rejection = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeoutOrNull(1_000) { h.network.sessionRejections.first() }
        }

        h.network.endServerSession()
        h.settle()

        assertNull(rejection.await(), "the 401 that means signed out was reported as a rejected session")
        h.scope.cancel()
    }

    @Test
    fun `with no session token there is nothing to end`() = runBlocking {
        val h = Harness()
        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = null)

        h.network.endServerSession()
        h.settle()

        assertTrue(h.requests.isEmpty(), "a DELETE went out with no session to end")
        h.scope.cancel()
    }
}
