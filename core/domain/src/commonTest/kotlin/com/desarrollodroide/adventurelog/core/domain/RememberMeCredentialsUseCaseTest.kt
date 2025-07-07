package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RememberMeCredentialsUseCaseTest {

    private class FakeUserRepository : UserRepository {
        var savedUrl: String? = null
        var savedUsername: String? = null
        var savedPassword: String? = null
        var clearRememberMeCalled = false
        var rememberMeCredentialsFlow: Flow<Account?> = flowOf(null)

        override suspend fun saveRememberMeCredentials(url: String, username: String, password: String) {
            savedUrl = url
            savedUsername = username
            savedPassword = password
        }

        override fun getRememberMeCredentials(): Flow<Account?> {
            return rememberMeCredentialsFlow
        }

        override suspend fun clearRememberMeCredentials() {
            clearRememberMeCalled = true
        }

        override suspend fun saveUserSession(userDetails: UserDetails) {
            throw NotImplementedError()
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
    private val useCase = RememberMeCredentialsUseCase(fakeRepository)

    @Test
    fun `save stores credentials in repository`() = runTest {
        val url = "https://test.com"
        val username = "testuser"
        val password = "testpass"

        useCase.save(url, username, password)

        assertEquals(url, fakeRepository.savedUrl)
        assertEquals(username, fakeRepository.savedUsername)
        assertEquals(password, fakeRepository.savedPassword)
    }

    @Test
    fun `clear calls repository clear method`() = runTest {
        useCase.clear()

        assertTrue(fakeRepository.clearRememberMeCalled)
    }

    @Test
    fun `get returns flow from repository when credentials exist`() = runTest {
        val expectedAccount = Account(
            userName = "testuser",
            password = "testpass",
            serverUrl = "https://test.com"
        )
        fakeRepository.rememberMeCredentialsFlow = flowOf(expectedAccount)

        val result = useCase.get().first()

        assertEquals(expectedAccount, result)
    }

    @Test
    fun `get returns flow with null when no credentials exist`() = runTest {
        fakeRepository.rememberMeCredentialsFlow = flowOf(null)

        val result = useCase.get().first()

        assertNull(result)
    }
}
