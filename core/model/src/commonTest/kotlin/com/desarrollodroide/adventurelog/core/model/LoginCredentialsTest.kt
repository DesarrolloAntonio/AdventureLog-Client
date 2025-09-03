package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginCredentialsTest : BaseModelTest<LoginCredentials>() {

    @Test
    fun `should create LoginCredentials with all properties`() {
        val credentials = LoginCredentials(
            username = "test@example.com",
            password = "securePass123!",
            serverUrl = "https://api.adventurelog.com",
            rememberCredentials = true
        )

        assertEquals("test@example.com", credentials.username)
        assertEquals("securePass123!", credentials.password)
        assertEquals("https://api.adventurelog.com", credentials.serverUrl)
        assertTrue(credentials.rememberCredentials)
    }

    @Test
    fun `should correctly implement equals and hashCode`() {
        val credentials1 = LoginCredentials(
            username = "user1",
            password = "pass1",
            serverUrl = "https://server1.com",
            rememberCredentials = false
        )
        
        val credentials2 = credentials1.copy()
        val credentials3 = credentials1.copy(username = "user2")
        val credentials4 = credentials1.copy(serverUrl = "https://server2.com")

        testEquality(
            original = credentials1,
            equal = credentials2,
            different = listOf(credentials3, credentials4)
        )
    }

    @Test
    fun `should handle special characters in credentials`() {
        val specialCredentials = LoginCredentials(
            username = "user@special#chars.com",
            password = "p@\$\$w0rd!@#\$%^&*()",
            serverUrl = "https://server.com/api/v1",
            rememberCredentials = true
        )

        assertEquals("user@special#chars.com", specialCredentials.username)
        assertEquals("p@\$\$w0rd!@#\$%^&*()", specialCredentials.password)
    }

    @Test
    fun `should handle different server URL formats`() {
        val urls = listOf(
            "https://api.example.com",
            "http://localhost:3000",
            "https://server.com/api/v2",
            "https://192.168.1.1:8080"
        )

        urls.forEach { url ->
            val creds = LoginCredentials("user", "pass", url, false)
            assertEquals(url, creds.serverUrl)
        }
    }

    @Test
    fun `should handle empty values`() {
        val emptyCredentials = LoginCredentials(
            username = "",
            password = "",
            serverUrl = "",
            rememberCredentials = false
        )

        assertTrue(emptyCredentials.username.isEmpty())
        assertTrue(emptyCredentials.password.isEmpty())
        assertTrue(emptyCredentials.serverUrl.isEmpty())
        assertTrue(!emptyCredentials.rememberCredentials)
    }

    @Test
    fun `should handle remember credentials flag`() {
        val rememberTrue = LoginCredentials(
            username = "user1",
            password = "pass1",
            serverUrl = "https://server.com",
            rememberCredentials = true
        )
        
        val rememberFalse = LoginCredentials(
            username = "user2",
            password = "pass2",
            serverUrl = "https://server.com",
            rememberCredentials = false
        )
        
        assertTrue(rememberTrue.rememberCredentials)
        assertTrue(!rememberFalse.rememberCredentials)
    }
}
