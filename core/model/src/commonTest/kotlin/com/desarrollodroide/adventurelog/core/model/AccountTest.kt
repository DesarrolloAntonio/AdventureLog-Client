package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AccountTest : BaseModelTest<Account>() {
    
    @Test
    fun `should create Account with all properties`() {
        val account = Account(
            id = 123,
            userName = "testuser",
            password = "testpass123",
            serverUrl = "https://api.test.com"
        )
        
        assertEquals(123, account.id)
        assertEquals("testuser", account.userName)
        assertEquals("testpass123", account.password)
        assertEquals("https://api.test.com", account.serverUrl)
    }
    
    @Test
    fun `should create Account with default constructor`() {
        val account = Account()
        
        assertEquals(-1, account.id)
        assertEquals("", account.userName)
        assertEquals("", account.password)
        assertEquals("", account.serverUrl)
    }
    
    @Test
    fun `should use mock Account correctly`() {
        val mockAccount = Account.mock
        
        assertEquals(1, mockAccount.id)
        assertEquals("user@example.com", mockAccount.userName)
        assertEquals("securePassword123", mockAccount.password)
        assertEquals("https://api.example.com", mockAccount.serverUrl)
    }
    
    @Test
    fun `should serialize and deserialize correctly`() {
        val account = Account(
            id = 456,
            userName = "john.doe@test.com",
            password = "p@ssw0rd!",
            serverUrl = "https://backend.adventurelog.com"
        )
        
        val jsonString = json.encodeToString(Account.serializer(), account)
        assertTrue(jsonString.contains("\"id\":456"))
        assertTrue(jsonString.contains("\"userName\":\"john.doe@test.com\""))
        assertTrue(jsonString.contains("\"password\":\"p@ssw0rd!\""))
        assertTrue(jsonString.contains("\"serverUrl\":\"https://backend.adventurelog.com\""))
        
        // Deserialize and check properties individually since Account is not a data class
        val deserialized = json.decodeFromString(Account.serializer(), jsonString)
        assertEquals(account.id, deserialized.id)
        assertEquals(account.userName, deserialized.userName)
        assertEquals(account.password, deserialized.password)
        assertEquals(account.serverUrl, deserialized.serverUrl)
    }
    
    @Test
    fun `should handle edge cases`() {
        // Negative ID
        val negativeIdAccount = Account(id = -999, userName = "test", password = "test", serverUrl = "https://test.com")
        assertEquals(-999, negativeIdAccount.id)
        
        // Empty values
        val emptyAccount = Account(0, "", "", "")
        assertEquals(0, emptyAccount.id)
        assertTrue(emptyAccount.userName.isEmpty())
        assertTrue(emptyAccount.password.isEmpty())
        assertTrue(emptyAccount.serverUrl.isEmpty())
        
        // Special characters
        val specialAccount = Account(
            id = 100,
            userName = "user+test@company-name.co.uk",
            password = "P@$\$w0rd!#%^&*()",
            serverUrl = "https://api.test.com:8443/v2"
        )
        assertEquals("user+test@company-name.co.uk", specialAccount.userName)
        assertEquals("P@$\$w0rd!#%^&*()", specialAccount.password)
    }
    
    @Test
    fun `should handle various server URL formats`() {
        val urls = listOf(
            "https://api.example.com",
            "http://localhost:8080",
            "https://api.example.com/v1/",
            "https://subdomain.example.com:443/api",
            "http://192.168.1.1:3000"
        )
        
        urls.forEachIndexed { index, url ->
            val account = Account(index, "user$index", "pass$index", url)
            assertEquals(url, account.serverUrl)
        }
    }
    
    @Test
    fun `should create different instances with different values`() {
        val account1 = Account(1, "user1", "pass1", "https://server1.com")
        val account2 = Account(2, "user2", "pass2", "https://server2.com")
        
        assertNotEquals(account1.id, account2.id)
        assertNotEquals(account1.userName, account2.userName)
        assertNotEquals(account1.password, account2.password)
        assertNotEquals(account1.serverUrl, account2.serverUrl)
    }
}
