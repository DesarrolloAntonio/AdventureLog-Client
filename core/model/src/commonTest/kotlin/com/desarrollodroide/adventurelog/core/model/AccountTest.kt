package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AccountTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    @Test
    fun `should create Account with all properties`() {
        // Given
        val account = Account(
            id = 123,
            userName = "testuser",
            password = "testpass123",
            serverUrl = "https://api.test.com"
        )
        
        // Then
        assertEquals(123, account.id)
        assertEquals("testuser", account.userName)
        assertEquals("testpass123", account.password)
        assertEquals("https://api.test.com", account.serverUrl)
    }
    
    @Test
    fun `should create Account with default constructor`() {
        // Given
        val account = Account()
        
        // Then
        assertEquals(-1, account.id)
        assertEquals("", account.userName)
        assertEquals("", account.password)
        assertEquals("", account.serverUrl)
    }
    
    @Test
    fun `should use mock Account correctly`() {
        // Given
        val mockAccount = Account.mock
        
        // Then
        assertEquals(1, mockAccount.id)
        assertEquals("user@example.com", mockAccount.userName)
        assertEquals("securePassword123", mockAccount.password)
        assertEquals("https://api.example.com", mockAccount.serverUrl)
    }
    
    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val account = Account(
            id = 456,
            userName = "john.doe@test.com",
            password = "p@ssw0rd!",
            serverUrl = "https://backend.adventurelog.com"
        )
        
        // When
        val jsonString = json.encodeToString(Account.serializer(), account)
        
        // Then
        assertTrue(jsonString.contains("\"id\":456"))
        assertTrue(jsonString.contains("\"userName\":\"john.doe@test.com\""))
        assertTrue(jsonString.contains("\"password\":\"p@ssw0rd!\""))
        assertTrue(jsonString.contains("\"serverUrl\":\"https://backend.adventurelog.com\""))
    }
    
    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": 789,
                "userName": "jane.smith@example.org",
                "password": "SuperSecure123!",
                "serverUrl": "https://api.adventurelog.io"
            }
        """.trimIndent()
        
        // When
        val account = json.decodeFromString<Account>(jsonString)
        
        // Then
        assertEquals(789, account.id)
        assertEquals("jane.smith@example.org", account.userName)
        assertEquals("SuperSecure123!", account.password)
        assertEquals("https://api.adventurelog.io", account.serverUrl)
    }
    
    @Test
    fun `should handle negative id values`() {
        // Given
        val accountWithNegativeId = Account(
            id = -999,
            userName = "test",
            password = "test",
            serverUrl = "https://test.com"
        )
        
        // Then
        assertEquals(-999, accountWithNegativeId.id)
    }
    
    @Test
    fun `should handle empty values`() {
        // Given
        val emptyAccount = Account(
            id = 0,
            userName = "",
            password = "",
            serverUrl = ""
        )
        
        // Then
        assertEquals(0, emptyAccount.id)
        assertEquals("", emptyAccount.userName)
        assertEquals("", emptyAccount.password)
        assertEquals("", emptyAccount.serverUrl)
    }
    
    @Test
    fun `should handle special characters in userName and password`() {
        // Given
        val specialAccount = Account(
            id = 100,
            userName = "user+test@company-name.co.uk",
            password = "P@$\$w0rd!#%^&*()",
            serverUrl = "https://api.test.com:8443/v2"
        )
        
        // Then
        assertEquals("user+test@company-name.co.uk", specialAccount.userName)
        assertEquals("P@$\$w0rd!#%^&*()", specialAccount.password)
        assertEquals("https://api.test.com:8443/v2", specialAccount.serverUrl)
    }
    
    @Test
    fun `should create different instances with different values`() {
        // Given
        val account1 = Account(
            id = 1,
            userName = "user1",
            password = "pass1",
            serverUrl = "https://server1.com"
        )
        
        val account2 = Account(
            id = 2,
            userName = "user2",
            password = "pass2",
            serverUrl = "https://server2.com"
        )
        
        // Then
        assertNotEquals(account1.id, account2.id)
        assertNotEquals(account1.userName, account2.userName)
        assertNotEquals(account1.password, account2.password)
        assertNotEquals(account1.serverUrl, account2.serverUrl)
    }
    
    @Test
    fun `should handle various server URL formats`() {
        // Given
        val accounts = listOf(
            Account(1, "user1", "pass1", "https://api.example.com"),
            Account(2, "user2", "pass2", "http://localhost:8080"),
            Account(3, "user3", "pass3", "https://api.example.com/v1/"),
            Account(4, "user4", "pass4", "https://subdomain.example.com:443/api"),
            Account(5, "user5", "pass5", "http://192.168.1.1:3000")
        )
        
        // Then
        assertEquals("https://api.example.com", accounts[0].serverUrl)
        assertEquals("http://localhost:8080", accounts[1].serverUrl)
        assertEquals("https://api.example.com/v1/", accounts[2].serverUrl)
        assertEquals("https://subdomain.example.com:443/api", accounts[3].serverUrl)
        assertEquals("http://192.168.1.1:3000", accounts[4].serverUrl)
    }
    
    @Test
    fun `should serialize and deserialize empty account`() {
        // Given
        val emptyAccount = Account()
        
        // When
        val jsonString = json.encodeToString(Account.serializer(), emptyAccount)
        val deserializedAccount = json.decodeFromString<Account>(jsonString)
        
        // Then
        assertEquals(emptyAccount.id, deserializedAccount.id)
        assertEquals(emptyAccount.userName, deserializedAccount.userName)
        assertEquals(emptyAccount.password, deserializedAccount.password)
        assertEquals(emptyAccount.serverUrl, deserializedAccount.serverUrl)
    }
}
