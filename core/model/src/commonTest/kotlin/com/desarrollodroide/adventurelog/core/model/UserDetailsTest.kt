package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserDetailsTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val userDetails = UserDetails(
            id = 123,
            username = "adventurer",
            profilePic = "/path/to/pic.jpg",
            uuid = "550e8400-e29b-41d4-a716-446655440000",
            publicProfile = true,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            dateJoined = "2024-01-15T10:30:00Z",
            isStaff = false,
            hasPassword = "true",
            sessionToken = "abc123token",
            serverUrl = "https://api.adventurelog.com"
        )
        
        // When
        val jsonString = json.encodeToString(UserDetails.serializer(), userDetails)
        
        // Then
        assertTrue(jsonString.contains("\"id\":123"))
        assertTrue(jsonString.contains("\"username\":\"adventurer\""))
        assertTrue(jsonString.contains("\"email\":\"test@example.com\""))
        assertTrue(jsonString.contains("\"profilePic\":\"/path/to/pic.jpg\""))
        assertTrue(jsonString.contains("\"uuid\":\"550e8400-e29b-41d4-a716-446655440000\""))
        assertTrue(jsonString.contains("\"publicProfile\":true"))
        assertTrue(jsonString.contains("\"firstName\":\"John\""))
        assertTrue(jsonString.contains("\"lastName\":\"Doe\""))
        assertTrue(jsonString.contains("\"isStaff\":false"))
        assertTrue(jsonString.contains("\"hasPassword\":\"true\""))
        assertTrue(jsonString.contains("\"serverUrl\":\"https://api.adventurelog.com\""))
    }
    
    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": 456,
                "username": "explorer",
                "profilePic": "/images/profile.png",
                "uuid": "123e4567-e89b-12d3-a456-426614174000",
                "publicProfile": false,
                "email": "explorer@test.com",
                "firstName": "Jane",
                "lastName": "Smith",
                "dateJoined": "2024-02-20T15:45:00Z",
                "isStaff": true,
                "hasPassword": "false",
                "sessionToken": "xyz789token",
                "serverUrl": "https://api.test.com"
            }
        """.trimIndent()
        
        // When
        val userDetails = json.decodeFromString<UserDetails>(jsonString)
        
        // Then
        assertEquals(456, userDetails.id)
        assertEquals("explorer", userDetails.username)
        assertEquals("explorer@test.com", userDetails.email)
        assertEquals("/images/profile.png", userDetails.profilePic)
        assertEquals(false, userDetails.publicProfile)
        assertEquals("Jane", userDetails.firstName)
        assertEquals("Smith", userDetails.lastName)
        assertEquals(true, userDetails.isStaff)
        assertEquals("false", userDetails.hasPassword)
        assertEquals("xyz789token", userDetails.sessionToken)
        assertEquals("https://api.test.com", userDetails.serverUrl)
    }
    
    @Test
    fun `should handle null profilePic`() {
        // Given
        val jsonString = """
            {
                "id": 789,
                "username": "nomad",
                "profilePic": null,
                "uuid": "987e6543-e21b-12d3-a456-426614174000",
                "publicProfile": true,
                "email": "nomad@test.com",
                "firstName": "Bob",
                "lastName": "Wilson",
                "dateJoined": "2024-03-01T08:00:00Z",
                "isStaff": false,
                "hasPassword": "true",
                "sessionToken": "def456token",
                "serverUrl": "https://server.test.com"
            }
        """.trimIndent()
        
        // When
        val userDetails = json.decodeFromString<UserDetails>(jsonString)
        
        // Then
        assertEquals(789, userDetails.id)
        assertEquals("nomad", userDetails.username)
        assertNull(userDetails.profilePic)
        assertEquals("987e6543-e21b-12d3-a456-426614174000", userDetails.uuid)
        assertEquals(true, userDetails.publicProfile)
    }
    
    @Test
    fun `should handle edge cases for boolean fields`() {
        // Given
        val userDetails1 = UserDetails(
            id = 1,
            username = "test1",
            profilePic = null,
            uuid = "test-uuid-1",
            publicProfile = true,
            email = "test1@example.com",
            firstName = "Test",
            lastName = "One",
            dateJoined = "2024-01-01",
            isStaff = true,
            hasPassword = "true",
            sessionToken = "token1",
            serverUrl = "https://server1.com"
        )
        
        val userDetails2 = UserDetails(
            id = 2,
            username = "test2",
            profilePic = null,
            uuid = "test-uuid-2",
            publicProfile = false,
            email = "test2@example.com",
            firstName = "Test",
            lastName = "Two",
            dateJoined = "2024-01-02",
            isStaff = false,
            hasPassword = "false",
            sessionToken = "token2",
            serverUrl = "https://server2.com"
        )
        
        // When & Then
        val json1 = json.encodeToString(UserDetails.serializer(), userDetails1)
        val json2 = json.encodeToString(UserDetails.serializer(), userDetails2)
        
        assertTrue(json1.contains("\"publicProfile\":true"))
        assertTrue(json1.contains("\"isStaff\":true"))
        assertTrue(json2.contains("\"publicProfile\":false"))
        assertTrue(json2.contains("\"isStaff\":false"))
    }
    
    @Test
    fun `should correctly compare UserDetails instances`() {
        // Given
        val user1 = UserDetails(
            id = 100,
            username = "user100",
            profilePic = "/pic100.jpg",
            uuid = "uuid-100",
            publicProfile = true,
            email = "user100@test.com",
            firstName = "First",
            lastName = "Last",
            dateJoined = "2024-01-01",
            isStaff = false,
            hasPassword = "true",
            sessionToken = "token100",
            serverUrl = "https://server.com"
        )
        
        val user2 = user1.copy()
        val user3 = user1.copy(id = 101)
        
        // Then
        assertEquals(user1, user2)
        assertNotEquals(user1, user3)
        assertEquals(user1.hashCode(), user2.hashCode())
    }
}
