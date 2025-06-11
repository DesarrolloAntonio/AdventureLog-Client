package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.LoginRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {
    
    private val loginRepository: LoginRepository = mockk()
    private val loginUseCase = LoginUseCase(loginRepository)
    
    @Test
    fun `invoke with valid credentials returns UserDetails`() = runTest {
        // Given
        val url = "https://api.example.com"
        val username = "testuser"
        val password = "testpass"
        val expectedUserDetails = UserDetails(
            id = "123",
            username = username,
            token = "token123",
            isAdmin = false
        )
        
        coEvery {
            loginRepository.sendLogin(url, username, password)
        } returns Either.Right(expectedUserDetails)
        
        // When
        val result = loginUseCase(url, username, password)
        
        // Then
        assertTrue(result is Either.Right)
        assertEquals(expectedUserDetails, result.value)
        coVerify(exactly = 1) { loginRepository.sendLogin(url, username, password) }
    }
    
    @Test
    fun `invoke with invalid credentials returns error message`() = runTest {
        // Given
        val url = "https://api.example.com"
        val username = "wronguser"
        val password = "wrongpass"
        
        coEvery {
            loginRepository.sendLogin(url, username, password)
        } returns Either.Left(ApiResponse.InvalidCredentials)
        
        // When
        val result = loginUseCase(url, username, password)
        
        // Then
        assertTrue(result is Either.Left)
        assertEquals("Invalid username or password", result.value)
        coVerify(exactly = 1) { loginRepository.sendLogin(url, username, password) }
    }
    
    @Test
    fun `invoke with network error returns network unavailable message`() = runTest {
        // Given
        val url = "https://api.example.com"
        val username = "testuser"
        val password = "testpass"
        val ioException = Exception("Network error")
        
        coEvery {
            loginRepository.sendLogin(url, username, password)
        } returns Either.Left(ApiResponse.IOException(ioException))
        
        // When
        val result = loginUseCase(url, username, password)
        
        // Then
        assertTrue(result is Either.Left)
        assertEquals("Network unavailable", result.value)
        coVerify(exactly = 1) { loginRepository.sendLogin(url, username, password) }
    }
    
    @Test
    fun `invoke with http error returns generic error message`() = runTest {
        // Given
        val url = "https://api.example.com"
        val username = "testuser"
        val password = "testpass"
        
        coEvery {
            loginRepository.sendLogin(url, username, password)
        } returns Either.Left(ApiResponse.HttpError("500 Internal Server Error"))
        
        // When
        val result = loginUseCase(url, username, password)
        
        // Then
        assertTrue(result is Either.Left)
        assertEquals("Error getting user credentials, try again later", result.value)
        coVerify(exactly = 1) { loginRepository.sendLogin(url, username, password) }
    }
}