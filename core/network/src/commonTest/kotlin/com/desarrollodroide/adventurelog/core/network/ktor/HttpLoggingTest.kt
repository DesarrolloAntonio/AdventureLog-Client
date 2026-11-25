package com.desarrollodroide.adventurelog.core.network.ktor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpLoggingTest {

    @Test
    fun `redacts password from a login request body`() {
        val body = """
            {
                "username": "memnoch",
                "password": "hunter2-super-secret"
            }
        """.trimIndent()

        val redacted = redactSecrets(body)

        assertFalse(redacted.contains("hunter2-super-secret"), "password leaked: $redacted")
        assertTrue(redacted.contains("\"password\": \"***\""), redacted)
        assertTrue(redacted.contains("memnoch"), "username should survive: $redacted")
    }

    @Test
    fun `redacts session and csrf cookie values`() {
        val header = "set-cookie=[csrftoken=nb8meujHx1AaIguo0dNa; Path=/, " +
            "sessionid=6t3jl998602iah6angnxm0b9xgvkykhy; HttpOnly]"

        val redacted = redactSecrets(header)

        assertFalse(redacted.contains("6t3jl998602iah6angnxm0b9xgvkykhy"), redacted)
        assertFalse(redacted.contains("nb8meujHx1AaIguo0dNa"), redacted)
    }

    @Test
    fun `redacts the session token header`() {
        val redacted = redactSecrets("X-Session-Token: 6t3jl998602iah6angnxm0b9xgvkykhy")

        assertFalse(redacted.contains("6t3jl998602iah6angnxm0b9xgvkykhy"), redacted)
    }

    @Test
    fun `redacts password change payloads`() {
        val body = """{"current_password":"old-one","new_password":"brand-new"}"""

        val redacted = redactSecrets(body)

        assertFalse(redacted.contains("old-one"), redacted)
        assertFalse(redacted.contains("brand-new"), redacted)
    }

    @Test
    fun `leaves ordinary payloads untouched`() {
        val body = """{"name":"Sima de Alcorón","rating":null,"is_public":false}"""

        assertEquals(body, redactSecrets(body))
    }

    @Test
    fun `logger never emits a raw password`() {
        val emitted = mutableListOf<String>()
        val logger = RedactingHttpLogger(sink = { emitted += it })

        logger.log("""BODY START {"username":"memnoch","password":"hunter2"} BODY END""")

        assertEquals(1, emitted.size)
        assertFalse(emitted.single().contains("hunter2"), emitted.single())
    }

    @Test
    fun `redacts the session in an app-client login answer pending or final`() {
        val body = """{"status":401,"data":{"flows":[{"id":"mfa_authenticate","is_pending":true}]},""" +
            """"meta":{"is_authenticated":false,"session_token":"6t3jl998602iah6angnxm0b9xgvkykhy"}}"""

        val redacted = redactSecrets(body)

        assertFalse(redacted.contains("6t3jl998602iah6angnxm0b9xgvkykhy"), redacted)
        assertTrue(redacted.contains("mfa_authenticate"), redacted)
    }

    @Test
    fun `redacts a second-factor code but not allauth's error codes`() {
        assertFalse(redactSecrets("""{"code":"482913"}""").contains("482913"))
        val error = """{"errors":[{"message":"Incorrect code.","code":"incorrect_code","param":"code"}]}"""
        assertEquals(error, redactSecrets(error))
    }

    @Test
    fun `redacts a provider sign-in's code and verifier but not error codes`() {
        val body = """{"code":"Xk3_tR9vQm2LpZ8wYc4nBf7hJd1sAe6uGo5iKy0qWlM","code_verifier":"dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk","redirect_uri":"adventurelog://auth/callback"}"""
        val redacted = redactSecrets(body)
        assertFalse(redacted.contains("Xk3_tR9v"), redacted)
        assertFalse(redacted.contains("dBjftJeZ"), redacted)
        assertTrue(redacted.contains("adventurelog://auth/callback"), redacted)
        val error = """{"detail":"Unable to log in.","code":"username_password_mismatch_for_this_user"}"""
        assertEquals(error, redactSecrets(error))
    }
}
