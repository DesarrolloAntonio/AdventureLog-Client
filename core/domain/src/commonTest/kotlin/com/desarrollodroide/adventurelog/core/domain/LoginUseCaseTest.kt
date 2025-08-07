package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.LoginRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginUseCase
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private class FakeLoginRepository : LoginRepository {
        var sendLoginResult: Either<ApiResponse, UserDetails> = Either.Right(createFakeUserDetails())
        var lastUrl: String? = null
        var lastUsername: String? = null
        var lastPassword: String? = null

        override suspend fun sendLogin(url: String, username: String, password: String): Either<ApiResponse, UserDetails> {
            lastUrl = url
            lastUsername = username
            lastPassword = password
            return sendLoginResult
        }
    }

    private val fakeRepository = FakeLoginRepository()
    private val useCase = LoginUseCase(fakeRepository)

    @Test
    fun `invoke returns success when repository returns user details`() = runTest {
        val expectedUserDetails = createFakeUserDetails()
        fakeRepository.sendLoginResult = Either.Right(expectedUserDetails)

        val result = useCase("https://test.com", "testuser", "testpass")

        assertTrue(result is Either.Right)
        assertEquals(expectedUserDetails, result.value)
        assertEquals("https://test.com", fakeRepository.lastUrl)
        assertEquals("testuser", fakeRepository.lastUsername)
        assertEquals("testpass", fakeRepository.lastPassword)
    }

    @Test
    fun `invoke returns network error message when repository returns IOException`() = runTest {
        fakeRepository.sendLoginResult = Either.Left(ApiResponse.IOException)

        val result = useCase("https://test.com", "testuser", "testpass")

        assertTrue(result is Either.Left)
        assertEquals("Network unavailable", result.value)
    }

    @Test
    fun `invoke returns http error message when repository returns HttpError`() = runTest {
        fakeRepository.sendLoginResult = Either.Left(ApiResponse.HttpError)

        val result = useCase("https://test.com", "testuser", "testpass")

        assertTrue(result is Either.Left)
        assertEquals("Error getting user credentials, try again later", result.value)
    }

    @Test
    fun `invoke returns invalid credentials message when repository returns InvalidCredentials`() = runTest {
        fakeRepository.sendLoginResult = Either.Left(ApiResponse.InvalidCredentials)

        val result = useCase("https://test.com", "testuser", "testpass")

        assertTrue(result is Either.Left)
        assertEquals("Invalid username or password", result.value)
    }

    @Test
    fun `invoke passes correct parameters to repository`() = runTest {
        val url = "https://example.com"
        val username = "john.doe"
        val password = "securePassword123"

        useCase(url, username, password)

        assertEquals(url, fakeRepository.lastUrl)
        assertEquals(username, fakeRepository.lastUsername)
        assertEquals(password, fakeRepository.lastPassword)
    }

    companion object {
        private fun createFakeUserDetails() = UserDetails(
            id = 1,
            profilePic = null,
            uuid = "test-uuid",
            publicProfile = true,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            dateJoined = "2024-01-01",
            isStaff = false,
            hasPassword = "true",
            sessionToken = "test-session-token",
            serverUrl = "https://test.com"
        )
    }
}
