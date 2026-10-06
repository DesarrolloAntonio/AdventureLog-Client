package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionRejectionTest {

    private fun networkAnswering(status: HttpStatusCode) = KtorAdventureLogNetwork(
        HttpClient(MockEngine {
            respond(
                content = """{"detail":"Authentication credentials were not provided."}""",
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
    )

    private fun rejectionsSeen(status: HttpStatusCode, token: String?) = runTest {
        val network = networkAnswering(status)
        network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = token)
        val seen = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            network.sessionRejections.toList(seen)
        }

        runCatching { network.getUserDetails() }
        testScheduler.advanceUntilIdle()

        seenCount = seen.size
    }

    private var seenCount = -1

    @Test
    fun `a 401 to a request carrying the session reports it rejected`() {
        rejectionsSeen(HttpStatusCode.Unauthorized, token = "session-token")
        assertEquals(1, seenCount)
    }

    @Test
    fun `a 403 refuses one object and not the session`() {
        rejectionsSeen(HttpStatusCode.Forbidden, token = "session-token")
        assertEquals(0, seenCount)
    }

    @Test
    fun `a 401 to a request with no session is not a rejected session`() {
        rejectionsSeen(HttpStatusCode.Unauthorized, token = null)
        assertEquals(0, seenCount)
    }
}
