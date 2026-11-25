package com.desarrollodroide.adventurelog.core.network.ktor

import com.desarrollodroide.adventurelog.core.network.model.response.LoginOutcomeDTO
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Signing in to an account with two-step verification, as allauth 0.63.6's headless app client
 * does it (the version both 0.13.1 and 0.14.0 pin): the password answers 401 with
 * `mfa_authenticate` pending and the half-done session in `meta.session_token`; the code goes to
 * `auth/2fa/authenticate` with that session as X-Session-Token. Shapes taken from allauth's own
 * source and tests (headless/base/response.py, headless/mfa/tests).
 */
class SecondFactorLoginTest {

    /** [answers] by path suffix; [v014] makes the server one that answers /api/server-info/. */
    private class Harness(private val v014: Boolean, private val answers: Map<String, Pair<HttpStatusCode, String>>) {
        val requests = mutableListOf<HttpRequestData>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val client = HttpClient(MockEngine { request ->
            val path = request.url.encodedPath
            val json = headersOf(HttpHeaders.ContentType, "application/json")
            when {
                path.startsWith("/api/server-info") ->
                    if (v014) respond("""{"version":"v0.14.0","capabilities":["mobile_auth_v1"]}""", HttpStatusCode.OK, json)
                    else respond("<!doctype html>", HttpStatusCode.NotFound, headersOf(HttpHeaders.ContentType, "text/html"))
                path == "/csrf/" -> respond("""{"csrfToken":"x"}""", HttpStatusCode.OK, json)
                else -> {
                    requests += request
                    val (status, body) = answers.entries.firstOrNull { path.endsWith(it.key) }?.value
                        ?: (HttpStatusCode.OK to USER_DETAILS)
                    respond(body, status, json)
                }
            }
        }) {
            install(ContentNegotiation) { json(defaultJson) }
        }
        val network = KtorAdventureLogNetwork(client, backgroundScope = scope)
    }

    private fun HttpRequestData.bodyText(): String =
        (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()

    @Test
    fun `on 0_13 the password step hands back a pending sign-in and the code finishes it`() = runBlocking {
        val h = Harness(
            v014 = false,
            answers = mapOf(
                "/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING),
                "/2fa/authenticate" to (HttpStatusCode.OK to signedIn(token = "final-token")),
            ),
        )

        val outcome = h.network.sendLogin("https://qa.test", "claude", "secret")
        assertEquals(LoginOutcomeDTO.SecondFactorRequired, outcome)

        val user = h.network.sendSecondFactorCode("123456")
        assertEquals("claude", user.username)
        assertEquals("final-token", user.sessionToken)

        val code = h.requests.single { it.url.encodedPath.endsWith("/2fa/authenticate") }
        assertEquals("/auth/app/v1/auth/2fa/authenticate", code.url.encodedPath)
        assertEquals("pending-token", code.headers[SESSION_TOKEN_HEADER])
        assertTrue(code.headers["X-CSRFToken"] != null, "0.13.1 checks CSRF on writes")
        assertEquals("""{"code":"123456"}""", code.bodyText())

        // The signed-in session is the final one: it is what the next request carries.
        h.network.getUserDetails()
        assertEquals("final-token", h.requests.last().headers[SESSION_TOKEN_HEADER])
        h.scope.cancel()
    }

    @Test
    fun `on 0_14 the code goes under api with the pending token and no cookie`() = runBlocking {
        val h = Harness(
            v014 = true,
            answers = mapOf(
                "/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING),
                "/2fa/authenticate" to (HttpStatusCode.OK to signedIn(token = "final-token")),
            ),
        )

        h.network.sendLogin("https://qa.test", "claude", "secret")
        h.network.sendSecondFactorCode("12345678")

        val code = h.requests.single { it.url.encodedPath.endsWith("/2fa/authenticate") }
        assertEquals("/api/auth/app/v1/auth/2fa/authenticate", code.url.encodedPath)
        assertEquals("pending-token", code.headers[SESSION_TOKEN_HEADER])
        assertNull(code.headers[HttpHeaders.Cookie])
        h.scope.cancel()
    }

    @Test
    fun `when the answer names no new session the pending one became the session`() = runBlocking {
        // allauth only exposes session_token when it differs from the one the request sent.
        val h = Harness(
            v014 = true,
            answers = mapOf(
                "/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING),
                "/2fa/authenticate" to (HttpStatusCode.OK to signedIn(token = null)),
            ),
        )

        h.network.sendLogin("https://qa.test", "claude", "secret")
        val user = h.network.sendSecondFactorCode("123456")

        assertEquals("pending-token", user.sessionToken)
        h.scope.cancel()
    }

    @Test
    fun `a password login without two-step verification is unchanged`() = runBlocking {
        val h = Harness(v014 = true, answers = mapOf("/auth/login" to (HttpStatusCode.OK to signedIn(token = "token"))))

        val outcome = h.network.sendLogin("https://qa.test", "claude", "secret")

        assertIs<LoginOutcomeDTO.SignedIn>(outcome)
        assertEquals("token", outcome.user.sessionToken)
        h.scope.cancel()
    }

    @Test
    fun `a 401 to the password that is not waiting for a code is still a failure`() = runBlocking {
        val h = Harness(v014 = true, answers = mapOf("/auth/login" to (HttpStatusCode.Unauthorized to NOT_PENDING)))

        val failure = assertFailsWith<HttpException> { h.network.sendLogin("https://qa.test", "claude", "secret") }

        assertEquals(401, failure.code)
        h.scope.cancel()
    }

    @Test
    fun `a wrong code is a 400 and leaves the sign-in waiting`() = runBlocking {
        val h = Harness(
            v014 = true,
            answers = mapOf(
                "/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING),
                "/2fa/authenticate" to (HttpStatusCode.BadRequest to INCORRECT_CODE),
            ),
        )
        h.network.sendLogin("https://qa.test", "claude", "secret")

        repeat(2) {
            val failure = assertFailsWith<HttpException> { h.network.sendSecondFactorCode("000000") }
            assertEquals(400, failure.code)
        }
        // Both attempts carried the same pending sign-in.
        assertEquals(2, h.requests.count { it.headers[SESSION_TOKEN_HEADER] == "pending-token" })
        h.scope.cancel()
    }

    @Test
    fun `a sign-in that timed out answers 401 or 410`() = runBlocking {
        for (status in listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Gone)) {
            val h = Harness(
                v014 = true,
                answers = mapOf(
                    "/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING),
                    "/2fa/authenticate" to (status to NOT_PENDING),
                ),
            )
            h.network.sendLogin("https://qa.test", "claude", "secret")

            val failure = assertFailsWith<HttpException> { h.network.sendSecondFactorCode("123456") }

            assertEquals(status.value, failure.code)
            h.scope.cancel()
        }
    }

    @Test
    fun `with no sign-in waiting a code is refused without asking the server`() = runBlocking {
        val h = Harness(v014 = true, answers = emptyMap())

        h.network.initializeFromSession(serverUrl = "https://qa.test", sessionToken = null)
        val failure = assertFailsWith<HttpException> { h.network.sendSecondFactorCode("123456") }

        assertEquals(410, failure.code)
        assertTrue(h.requests.isEmpty())
        h.scope.cancel()
    }

    @Test
    fun `cancelling drops the pending sign-in`() = runBlocking {
        val h = Harness(v014 = true, answers = mapOf("/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING)))
        h.network.sendLogin("https://qa.test", "claude", "secret")

        h.network.cancelPendingLogin()

        assertEquals(410, assertFailsWith<HttpException> { h.network.sendSecondFactorCode("123456") }.code)
        h.scope.cancel()
    }

    @Test
    fun `the pending sign-in is never the session other requests carry`() = runBlocking {
        val h = Harness(v014 = true, answers = mapOf("/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING)))
        h.network.sendLogin("https://qa.test", "claude", "secret")

        runCatching { h.network.getUserDetails() }

        assertNull(h.requests.last().headers[SESSION_TOKEN_HEADER])
        h.scope.cancel()
    }

    @Test
    fun `a timed-out code is not reported as a signed-in session ending`() = runTest {
        val h = Harness(
            v014 = true,
            answers = mapOf(
                "/auth/login" to (HttpStatusCode.Unauthorized to MFA_PENDING),
                "/2fa/authenticate" to (HttpStatusCode.Unauthorized to NOT_PENDING),
            ),
        )
        val seen = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { h.network.sessionRejections.toList(seen) }

        h.network.sendLogin("https://qa.test", "claude", "secret")
        runCatching { h.network.sendSecondFactorCode("123456") }

        assertEquals(0, seen.size)
        h.scope.cancel()
    }

    private companion object {
        /** allauth's answer to a right password on an account with TOTP. */
        const val MFA_PENDING = """{"status":401,"data":{"flows":[{"id":"login"},{"id":"signup"},""" +
            """{"id":"mfa_authenticate","is_pending":true}]},""" +
            """"meta":{"is_authenticated":false,"session_token":"pending-token"}}"""

        const val NOT_PENDING = """{"status":401,"data":{"flows":[{"id":"login"},{"id":"signup"}]},""" +
            """"meta":{"is_authenticated":false}}"""

        const val INCORRECT_CODE = """{"status":400,"errors":[{"message":"Incorrect code.",""" +
            """"code":"incorrect_code","param":"code"}]}"""

        const val USER_DETAILS = """{"id":7,"uuid":"u","username":"claude","first_name":"John","last_name":"Doe"}"""

        /** After the code, `methods` also lists the second factor, with a type and no username. */
        fun signedIn(token: String?): String =
            """{"status":200,"data":{"user":{"id":7,"display":"claude","username":"claude"},""" +
                """"methods":[{"method":"password","at":1.0,"username":"claude"},""" +
                """{"method":"mfa","at":2.0,"id":3,"type":"totp"}]},""" +
                """"meta":{"is_authenticated":true""" +
                (token?.let { ""","session_token":"$it"""" } ?: "") + "}}"
    }
}
