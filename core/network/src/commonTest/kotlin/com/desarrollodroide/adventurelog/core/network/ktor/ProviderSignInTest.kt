package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Provider sign-in against server 0.14.0, shapes from upstream's `users/views/auth_config.py` and
 * `users/views/mobile_auth.py`: which providers there are, and the code traded for a session.
 */
class ProviderSignInTest {

    private class Harness(
        private val serverInfo: Pair<HttpStatusCode, String> = HttpStatusCode.OK to info(),
        private val authConfig: String = CONFIG,
        private val token: Pair<HttpStatusCode, String> = HttpStatusCode.OK to TOKEN,
        private val account: Pair<HttpStatusCode, String> = HttpStatusCode.OK to ACCOUNT,
    ) {
        val requests = mutableListOf<HttpRequestData>()
        val client = HttpClient(MockEngine { request ->
            requests += request
            val json = headersOf(HttpHeaders.ContentType, "application/json")
            when (request.url.encodedPath) {
                "/api/server-info/" -> respond(serverInfo.second, serverInfo.first, json)
                "/api/auth/config/" -> respond(authConfig, HttpStatusCode.OK, json)
                "/api/auth/mobile/token/" -> respond(token.second, token.first, json)
                "/api/account/" -> respond(account.second, account.first, json)
                else -> respond("""{"detail":"Not found."}""", HttpStatusCode.NotFound, json)
            }
        }) {
            install(ContentNegotiation) { json(defaultJson) }
        }
        val network = KtorAdventureLogNetwork(client, CoroutineScope(SupervisorJob() + Dispatchers.Default))

        fun to(path: String) = requests.filter { it.url.encodedPath == path }
    }

    private fun HttpRequestData.bodyText(): String =
        (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()

    @Test
    fun `a 0_14 server lists its providers and where the browser goes`() = runBlocking {
        val h = Harness()

        val found = h.network.getSignInProviders("https://typed.test/")

        assertEquals("https://public.test", found.browserBaseUrl)
        assertEquals(listOf("github" to "GitHub", "oidc/authelia" to "Authelia"), found.providers.map { it.id to it.name })
        // Sent as they are: nobody is signed in, and the address may still be changing.
        h.requests.forEach { assertNull(it.headers[SESSION_TOKEN_HEADER]) }
    }

    @Test
    fun `without mobile_auth_v1 there are no providers, and auth config is not asked`() = runBlocking {
        val h = Harness(serverInfo = HttpStatusCode.OK to info(capabilities = """["conditional_writes_v1"]"""))

        assertTrue(h.network.getSignInProviders("https://typed.test").providers.isEmpty())
        assertTrue(h.to("/api/auth/config/").isEmpty())
    }

    @Test
    fun `a 0_13 server has no providers`() = runBlocking {
        val h = Harness(serverInfo = HttpStatusCode.NotFound to """{"detail":"Not found."}""")

        val found = h.network.getSignInProviders("https://typed.test")

        assertTrue(found.providers.isEmpty())
        assertEquals("https://typed.test", found.browserBaseUrl)
    }

    @Test
    fun `a loopback public url falls back to the typed address`() {
        assertEquals("https://typed.test", browserBaseUrl("http://localhost:8016", typed = "https://typed.test"))
        assertEquals("https://typed.test", browserBaseUrl(null, typed = "https://typed.test"))
        assertEquals("https://typed.test", browserBaseUrl("ftp://x.test", typed = "https://typed.test"))
        assertEquals("https://public.test:3447", browserBaseUrl("https://public.test:3447/", typed = "https://typed.test"))
    }

    @Test
    fun `the code and verifier become the session, and the account names the user`() = runBlocking {
        val h = Harness()

        val user = h.network.redeemProviderSignIn("https://typed.test/", "the-code", "the-verifier", "adventurelog://auth/callback")

        val exchange = h.to("/api/auth/mobile/token/").single()
        assertEquals(HttpMethod.Post, exchange.method)
        assertEquals(
            """{"code":"the-code","code_verifier":"the-verifier","redirect_uri":"adventurelog://auth/callback"}""",
            exchange.bodyText(),
        )
        assertNull(exchange.headers[SESSION_TOKEN_HEADER])
        assertEquals("sso-token", h.to("/api/account/").single().headers[SESSION_TOKEN_HEADER])
        assertEquals("sso-user", user.username)
        assertEquals("sso-token", user.sessionToken)
    }

    @Test
    fun `a refused code is a 400 and leaves no session`() = runBlocking {
        val h = Harness(token = HttpStatusCode.BadRequest to """{"detail":"The sign-in code is invalid or has expired.","code":"invalid_grant"}""")

        val error = assertFailsWith<HttpException> {
            h.network.redeemProviderSignIn("https://typed.test", "spent", "v", "adventurelog://auth/callback")
        }
        assertEquals(400, error.code)
        assertTrue(h.to("/api/account/").isEmpty())
    }

    private companion object {
        fun info(capabilities: String = """["mobile_auth_v1","conditional_writes_v1","client_ids_v1"]""") =
            """{"public_url":"https://public.test","allow_public_access":true,"version":"v0.14.0","capabilities":$capabilities}"""

        const val CONFIG = """{"registration":{"enabled":true,"message":""},""" +
            """"password_policy":{"min_length":8,"validators_enabled":true},""" +
            """"legal_links":{"terms_of_service_url":null,"privacy_policy_url":null},"social_providers":[""" +
            """{"id":"github","provider":"github","name":"GitHub","url":"https://public.test/accounts/github/login/","usage_required":false},""" +
            """{"id":"oidc/authelia","provider":"openid_connect","name":"Authelia","url":"https://public.test/accounts/oidc/authelia/login/","usage_required":false}]}"""

        const val TOKEN = """{"status":200,"meta":{"is_authenticated":true,"session_token":"sso-token"}}"""

        const val ACCOUNT = """{"user":{"id":9,"uuid":"u9","username":"sso-user","email":"sso@test","first_name":"Sso","last_name":"User"}}"""
    }
}
