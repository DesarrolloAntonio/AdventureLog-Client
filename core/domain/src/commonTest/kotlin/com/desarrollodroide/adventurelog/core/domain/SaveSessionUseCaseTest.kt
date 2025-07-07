package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveSessionUseCaseTest {

    private class FakeUserRepository : UserRepository {
        var savedUserDetails: UserDetails? = null

        override suspend fun saveRememberMeCredentials(url: String, username: String, password: String) {
            throw NotImplementedError()
        }

        override fun getRememberMeCredentials(): Flow<Account?> {
            throw NotImplementedError()
        }

        override suspend fun clearRememberMeCredentials() {
            throw NotImplementedError()
        }

        override suspend fun saveUserSession(userDetails: UserDetails) {
            savedUserDetails = userDetails
        }

        override fun getUserSession(): Flow<UserDetails?> {
            throw NotImplementedError()
        }

        override suspend fun getUserSessionOnce(): UserDetails? {
            throw NotImplementedError()
        }

        override suspend fun clearUserSession() {
            throw NotImplementedError()
        }

        override fun isLoggedIn(): Flow<Boolean> {
            throw NotImplementedError()
        }

        override suspend fun clearAllUserData() {
            throw NotImplementedError()
        }

        override suspend fun getUserStats(username: String): UserStats {
            return UserStats()
        }
    }

    private val fakeRepository = FakeUserRepository()
    private val useCase = SaveSessionUseCase(fakeRepository)

    @Test
    fun `invoke saves user details to repository`() = runTest {
        val userDetails = createFakeUserDetails()

        useCase(userDetails)

        assertEquals(userDetails, fakeRepository.savedUserDetails)
    }

    @Test
    fun `invoke handles different user details correctly`() = runTest {
        val userDetails = UserDetails(
            id = 2,
            profilePic = "https://example.com/pic.jpg",
            uuid = "12345",
            publicProfile = false,
            username = "john.doe",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            dateJoined = "2023-01-01",
            isStaff = true,
            hasPassword = "true",
            sessionToken = "session-123",
            serverUrl = "https://example.com"
        )

        useCase(userDetails)

        assertEquals(userDetails, fakeRepository.savedUserDetails)
        assertEquals("john.doe", fakeRepository.savedUserDetails?.username)
        assertEquals("12345", fakeRepository.savedUserDetails?.uuid)
        assertEquals("john@example.com", fakeRepository.savedUserDetails?.email)
        assertEquals("John", fakeRepository.savedUserDetails?.firstName)
        assertEquals("Doe", fakeRepository.savedUserDetails?.lastName)
    }

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
