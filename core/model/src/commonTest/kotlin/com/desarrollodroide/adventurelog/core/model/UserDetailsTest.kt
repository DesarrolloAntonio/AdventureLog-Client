package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserDetailsTest : BaseModelTest<UserDetails>() {
    
    @Test
    fun `should create UserDetails with all properties`() {
        val userDetails = TestDataFactory.createUserDetails(
            username = "adventurer",
            profilePic = "/path/to/pic.jpg",
            uuid = "550e8400-e29b-41d4-a716-446655440000",
            publicProfile = true,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            dateJoined = "2024-01-15T10:30:00Z",
            isStaff = false,
            hasPassword = true,
            sessionToken = "abc123token",
            serverUrl = "https://api.adventurelog.com"
        )
        
        assertEquals("adventurer", userDetails.username)
        assertEquals("test@example.com", userDetails.email)
        assertEquals("/path/to/pic.jpg", userDetails.profilePic)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", userDetails.uuid)
        assertTrue(userDetails.publicProfile)
        assertEquals("John", userDetails.firstName)
        assertEquals("Doe", userDetails.lastName)
    }
    
    @Test
    fun `should serialize and deserialize correctly`() {
        val userDetails = TestDataFactory.createUserDetails(
            username = "explorer",
            profilePic = "/images/profile.png",
            uuid = "123e4567-e89b-12d3-a456-426614174000",
            publicProfile = false,
            email = "explorer@test.com",
            firstName = "Jane",
            lastName = "Smith",
            isStaff = true,
            hasPassword = false,
            sessionToken = "xyz789token",
            serverUrl = "https://api.test.com"
        )
        
        testSerialization(userDetails, UserDetails.serializer()) { jsonString ->
            assertTrue(jsonString.contains("\"username\":\"explorer\""))
            assertTrue(jsonString.contains("\"email\":\"explorer@test.com\""))
            assertTrue(jsonString.contains("\"profilePic\":\"/images/profile.png\""))
            assertTrue(jsonString.contains("\"uuid\":\"123e4567-e89b-12d3-a456-426614174000\""))
            assertTrue(jsonString.contains("\"publicProfile\":false"))
            assertTrue(jsonString.contains("\"isStaff\":true"))
            assertTrue(jsonString.contains("\"hasPassword\":false"))
        }
    }
    
    @Test
    fun `should handle null profilePic`() {
        val userDetails = TestDataFactory.createUserDetails(
            username = "nomad",
            profilePic = null,
            uuid = "987e6543-e21b-12d3-a456-426614174000",
            publicProfile = true,
            email = "nomad@test.com"
        )
        
        assertEquals("nomad", userDetails.username)
        assertNull(userDetails.profilePic)
        assertEquals("987e6543-e21b-12d3-a456-426614174000", userDetails.uuid)
        assertTrue(userDetails.publicProfile)
    }
    
    @Test
    fun `should correctly implement equals and hashCode`() {
        val user1 = TestDataFactory.createUserDetails(username = "user100")
        val user2 = user1.copy()
        val user3 = user1.copy(username = "different")
        val user4 = user1.copy(uuid = "different-uuid")
        
        testEquality(
            original = user1,
            equal = user2,
            different = listOf(user3, user4)
        )
    }
    
    @Test
    fun `should handle boolean fields correctly`() {
        val publicUser = TestDataFactory.createUserDetails(
            publicProfile = true,
            isStaff = true,
            hasPassword = true
        )
        
        val privateUser = TestDataFactory.createUserDetails(
            publicProfile = false,
            isStaff = false,
            hasPassword = false
        )
        
        assertTrue(publicUser.publicProfile)
        assertTrue(publicUser.isStaff)
        assertTrue(publicUser.hasPassword)
        
        assertTrue(!privateUser.publicProfile)
        assertTrue(!privateUser.isStaff)
        assertTrue(!privateUser.hasPassword)
    }
}
