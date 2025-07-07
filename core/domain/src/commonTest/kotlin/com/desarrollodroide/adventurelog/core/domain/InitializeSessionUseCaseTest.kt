package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InitializeSessionUseCaseTest {

    private open class FakeUserRepository : UserRepository {
        var getUserSessionOnceResult: UserDetails? = null

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
            throw NotImplementedError()
        }

        override fun getUserSession(): Flow<UserDetails?> {
            throw NotImplementedError()
        }

        override suspend fun getUserSessionOnce(): UserDetails? {
            return getUserSessionOnceResult
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

    private open class FakeNetworkDataSource : AdventureLogNetworkDataSource {
        var lastServerUrl: String? = null
        var lastSessionToken: String? = null
        var initializeFromSessionCalled = false

        override fun initializeFromSession(serverUrl: String, sessionToken: String?) {
            initializeFromSessionCalled = true
            lastServerUrl = serverUrl
            lastSessionToken = sessionToken
        }

        override fun clearSession() {
            throw NotImplementedError()
        }

        override suspend fun getAdventures(page: Int, pageSize: Int): List<AdventureDTO> {
            throw NotImplementedError()
        }

        override suspend fun getAdventureDetail(objectId: String): AdventureDTO {
            throw NotImplementedError()
        }

        override suspend fun getCollections(page: Int, pageSize: Int): List<CollectionDTO> {
            throw NotImplementedError()
        }

        override suspend fun getCollectionDetail(collectionId: String): CollectionDTO {
            throw NotImplementedError()
        }

        override suspend fun sendLogin(url: String, username: String, password: String): UserDetailsDTO {
            throw NotImplementedError()
        }

        override suspend fun getUserDetails(): UserDetailsDTO {
            throw NotImplementedError()
        }

        override suspend fun createAdventure(
            name: String,
            description: String,
            category: Category,
            rating: Double,
            link: String,
            location: String,
            latitude: String?,
            longitude: String?,
            isPublic: Boolean,
            visitDates: Visit?
        ): AdventureDTO {
            throw NotImplementedError()
        }

        override suspend fun createCollection(
            name: String,
            description: String,
            isPublic: Boolean,
            startDate: String?,
            endDate: String?
        ): CollectionDTO {
            throw NotImplementedError()
        }

        override suspend fun getCategories(): List<CategoryDTO> {
            throw NotImplementedError()
        }

        override suspend fun generateDescription(name: String): String {
            return "Generated description for $name"
        }

        override suspend fun searchLocations(query: String): List<GeocodeSearchResultDTO> {
            return emptyList()
        }

        override suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResultDTO {
            return ReverseGeocodeResultDTO(
                city = "Test City",
                region = "Test Region",
                country = "Test Country",
                cityId = "city-1",
                regionId = "region-1",
                countryId = "country-1",
                displayName = "Test City, Test Region, Test Country",
                locationName = "Test Location"
            )
        }

        override suspend fun getUserStats(username: String): UserStatsDTO {
            return UserStatsDTO()
        }
    }

    private val fakeRepository = FakeUserRepository()
    private val fakeNetworkDataSource = FakeNetworkDataSource()
    private val useCase = InitializeSessionUseCase(fakeRepository, fakeNetworkDataSource)

    @Test
    fun `invoke returns user details and initializes network when session exists`() = runTest {
        val userDetails = createFakeUserDetails()
        fakeRepository.getUserSessionOnceResult = userDetails

        val result = useCase()

        assertEquals(userDetails, result)
        assertEquals(true, fakeNetworkDataSource.initializeFromSessionCalled)
        assertEquals(userDetails.serverUrl, fakeNetworkDataSource.lastServerUrl)
        assertEquals(userDetails.sessionToken, fakeNetworkDataSource.lastSessionToken)
    }

    @Test
    fun `invoke returns null when no session exists`() = runTest {
        fakeRepository.getUserSessionOnceResult = null

        val result = useCase()

        assertNull(result)
        assertEquals(false, fakeNetworkDataSource.initializeFromSessionCalled)
    }

    @Test
    fun `invoke returns null when repository throws exception`() = runTest {
        val exceptionRepository = object : FakeUserRepository() {
            override suspend fun getUserSessionOnce(): UserDetails? {
                throw RuntimeException("Repository error")
            }
        }
        val useCase = InitializeSessionUseCase(exceptionRepository, fakeNetworkDataSource)

        val result = useCase()

        assertNull(result)
        assertEquals(false, fakeNetworkDataSource.initializeFromSessionCalled)
    }

    @Test
    fun `invoke returns null when network initialization throws exception`() = runTest {
        val userDetails = createFakeUserDetails()
        fakeRepository.getUserSessionOnceResult = userDetails
        
        val exceptionNetworkDataSource = object : FakeNetworkDataSource() {
            override fun initializeFromSession(serverUrl: String, sessionToken: String?) {
                throw RuntimeException("Network initialization error")
            }
        }
        val useCase = InitializeSessionUseCase(fakeRepository, exceptionNetworkDataSource)

        val result = useCase()

        assertNull(result)
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
