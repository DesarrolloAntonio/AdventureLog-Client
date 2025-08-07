package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource
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
import kotlin.test.assertTrue

class LogoutUseCaseTest {

    private open class FakeUserRepository : UserRepository {
        var clearUserSessionCalled = false

        override suspend fun clearUserSession() {
            clearUserSessionCalled = true
        }

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
        var clearSessionCalled = false

        override fun clearSession() {
            clearSessionCalled = true
        }

        override fun initializeFromSession(serverUrl: String, sessionToken: String?) {
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
            visits: List<VisitFormData>,
            activityTypes: List<String>
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

    private val fakeUserRepository = FakeUserRepository()
    private val fakeNetworkDataSource = FakeNetworkDataSource()
    private val useCase = LogoutUseCase(fakeUserRepository, fakeNetworkDataSource)

    @Test
    fun `invoke clears user session and network session`() = runTest {
        useCase()

        assertTrue(fakeUserRepository.clearUserSessionCalled)
        assertTrue(fakeNetworkDataSource.clearSessionCalled)
    }

    @Test
    fun `invoke continues even if user repository throws exception`() = runTest {
        val exceptionRepository = object : FakeUserRepository() {
            override suspend fun clearUserSession() {
                throw RuntimeException("Repository error")
            }
        }
        val freshNetworkDataSource = FakeNetworkDataSource()
        val useCase = LogoutUseCase(exceptionRepository, freshNetworkDataSource)

        useCase()

        assertTrue(freshNetworkDataSource.clearSessionCalled)
    }

    @Test
    fun `invoke continues even if network data source throws exception`() = runTest {
        val exceptionNetworkDataSource = object : FakeNetworkDataSource() {
            override fun clearSession() {
                throw RuntimeException("Network error")
            }
        }
        val useCase = LogoutUseCase(fakeUserRepository, exceptionNetworkDataSource)

        useCase()

        assertTrue(fakeUserRepository.clearUserSessionCalled)
    }

    @Test
    fun `invoke handles both repositories throwing exceptions`() = runTest {
        val exceptionRepository = object : FakeUserRepository() {
            override suspend fun clearUserSession() {
                throw RuntimeException("Repository error")
            }
        }
        val exceptionNetworkDataSource = object : FakeNetworkDataSource() {
            override fun clearSession() {
                throw RuntimeException("Network error")
            }
        }
        val useCase = LogoutUseCase(exceptionRepository, exceptionNetworkDataSource)

        useCase()

        // Test passes if no exception is thrown
    }
}
