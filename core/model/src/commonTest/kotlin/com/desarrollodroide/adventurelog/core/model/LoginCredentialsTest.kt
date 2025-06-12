package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LoginCredentialsTest {
    
    @Test
    fun `should create LoginCredentials with all properties`() {
        // Given
        val credentials = LoginCredentials(
            username = "testuser",
            password = "securePassword123",
            serverUrl = "https://api.adventurelog.com",
            rememberCredentials = true
        )
        
        // Then
        assertEquals("testuser", credentials.username)
        assertEquals("securePassword123", credentials.password)
        assertEquals("https://api.adventurelog.com", credentials.serverUrl)
        assertTrue(credentials.rememberCredentials)
    }
    
    @Test
    fun `should handle different server URL formats`() {
        // Given
        val credentials1 = LoginCredentials(
            username = "user1",
            password = "pass1",
            serverUrl = "https://api.example.com",
            rememberCredentials = false
        )
        
        val credentials2 = LoginCredentials(
            username = "user2",
            password = "pass2",
            serverUrl = "http://localhost:8080",
            rememberCredentials = true
        )
        
        val credentials3 = LoginCredentials(
            username = "user3",
            password = "pass3",
            serverUrl = "https://api.example.com/v1/",
            rememberCredentials = false
        )
        
        // Then
        assertEquals("https://api.example.com", credentials1.serverUrl)
        assertEquals("http://localhost:8080", credentials2.serverUrl)
        assertEquals("https://api.example.com/v1/", credentials3.serverUrl)
    }
    
    @Test
    fun `should correctly compare LoginCredentials instances`() {
        // Given
        val credentials1 = LoginCredentials(
            username = "user",
            password = "pass",
            serverUrl = "https://api.com",
            rememberCredentials = true
        )
        
        val credentials2 = credentials1.copy()
        val credentials3 = credentials1.copy(username = "different")
        val credentials4 = credentials1.copy(rememberCredentials = false)
        
        // Then
        assertEquals(credentials1, credentials2)
        assertNotEquals(credentials1, credentials3)
        assertNotEquals(credentials1, credentials4)
        assertEquals(credentials1.hashCode(), credentials2.hashCode())
    }
    
    @Test
    fun `should handle empty values`() {
        // Given
        val emptyCredentials = LoginCredentials(
            username = "",
            password = "",
            serverUrl = "",
            rememberCredentials = false
        )
        
        // Then
        assertEquals("", emptyCredentials.username)
        assertEquals("", emptyCredentials.password)
        assertEquals("", emptyCredentials.serverUrl)
        assertFalse(emptyCredentials.rememberCredentials)
    }
    
    @Test
    fun `should handle special characters in credentials`() {
        // Given
        val specialCredentials = LoginCredentials(
            username = "user@domain.com",
            password = "p@ssw0rd!#$%",
            serverUrl = "https://api.test.com:443/v2",
            rememberCredentials = true
        )
        
        // Then
        assertEquals("user@domain.com", specialCredentials.username)
        assertEquals("p@ssw0rd!#$%", specialCredentials.password)
        assertEquals("https://api.test.com:443/v2", specialCredentials.serverUrl)
    }
    
    @Test
    fun `should handle remember credentials flag`() {
        // Given
        val rememberTrue = LoginCredentials(
            username = "user1",
            password = "pass1",
            serverUrl = "https://api.com",
            rememberCredentials = true
        )
        
        val rememberFalse = rememberTrue.copy(rememberCredentials = false)
        
        // Then
        assertTrue(rememberTrue.rememberCredentials)
        assertFalse(rememberFalse.rememberCredentials)
        assertNotEquals(rememberTrue, rememberFalse)
    }
}
