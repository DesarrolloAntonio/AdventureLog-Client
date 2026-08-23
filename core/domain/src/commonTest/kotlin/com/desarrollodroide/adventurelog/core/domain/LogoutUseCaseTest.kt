package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.model.response.DashboardDTO
import com.desarrollodroide.adventurelog.core.network.model.response.TrailDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitDTO
import com.desarrollodroide.adventurelog.core.network.model.response.LocationDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO
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

        override fun setActiveSession(userDetails: UserDetails) {
            throw NotImplementedError()
        }

        override fun getUserSession(): Flow<UserDetails?> {
            throw NotImplementedError()
        }

        override val activeSession: UserDetails?
            get() = null

        override suspend fun getUserSessionOnce(): UserDetails? {
            throw NotImplementedError()
        }

        override fun isLoggedIn(): Flow<Boolean> {
            throw NotImplementedError()
        }

        override suspend fun clearAllUserData() {
            throw NotImplementedError()
        }

        override suspend fun getUserStats(username: String): Either<ApiResponse, UserStats> {
            return Either.Right(UserStats())
        }

        override fun getUserStatsFlow(): Flow<UserStats?> {
            throw NotImplementedError()
        }
    }

    private open class FakeNetworkDataSource : AdventureLogNetwork {
        var clearSessionCalled = false

        override fun clearSession() {
            clearSessionCalled = true
        }

        override fun initializeFromSession(serverUrl: String, sessionToken: String?) {
            throw NotImplementedError()
        }

        override suspend fun getAdventures(page: Int, pageSize: Int): List<LocationDTO> {
            throw NotImplementedError()
        }

        override suspend fun getAdventureDetail(objectId: String): LocationDTO {
            throw NotImplementedError()
        }

        override suspend fun getCollections(page: Int, pageSize: Int): List<com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO> {
            throw NotImplementedError()
        }
        
        override suspend fun getAllCollections(): List<com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO> {
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
            price: Double?,
            priceCurrency: String?,
            activityTypes: List<String>
        ): LocationDTO {
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

        override suspend fun getDashboard(): DashboardDTO {
            return DashboardDTO()
        }

        override suspend fun createVisit(locationId: String, visit: VisitFormData): VisitDTO {
            throw NotImplementedError()
        }

        override suspend fun updateVisit(
            visitId: String,
            locationId: String,
            visit: VisitFormData
        ): VisitDTO {
            throw NotImplementedError()
        }

        override suspend fun deleteVisit(visitId: String) {
            throw NotImplementedError()
        }

        override suspend fun createTrail(locationId: String, trail: TrailFormData): TrailDTO {
            throw NotImplementedError()
        }

        override suspend fun updateTrail(
            trailId: String,
            locationId: String,
            trail: TrailFormData
        ): TrailDTO {
            throw NotImplementedError()
        }

        override suspend fun deleteTrail(trailId: String) {
            throw NotImplementedError()
        }

        override suspend fun duplicateLocation(locationId: String): LocationDTO {
            throw NotImplementedError()
        }

        override suspend fun getShareImage(locationId: String, aspect: String): ByteArray {
            throw NotImplementedError()
        }

        override suspend fun getAdventuresFiltered(
            page: Int,
            pageSize: Int,
            categoryIds: List<String>?,
            sortBy: String?,
            sortOrder: String?,
            isVisited: Boolean?,
            searchQuery: String?,
            includeCollections: Boolean
        ): List<LocationDTO> {
            throw NotImplementedError()
        }

        override suspend fun getCategoryById(categoryId: String): CategoryDTO {
            throw NotImplementedError()
        }

        override suspend fun createCategory(
            name: String,
            displayName: String,
            icon: String?
        ): CategoryDTO {
            throw NotImplementedError()
        }

        override suspend fun updateCategory(
            categoryId: String,
            name: String,
            displayName: String,
            icon: String?
        ): CategoryDTO {
            throw NotImplementedError()
        }

        override suspend fun deleteCategory(categoryId: String) {
            throw NotImplementedError()
        }

        override suspend fun deleteAdventure(adventureId: String) {
            throw NotImplementedError()
        }

        override suspend fun updateAdventure(
            adventureId: String,
            name: String,
            description: String,
            category: Category?,
            rating: Double,
            link: String,
            location: String,
            latitude: String?,
            longitude: String?,
            isPublic: Boolean,
            tags: List<String>,
            collections: List<String>,
            visits: List<VisitFormData>,
            price: Double?,
            priceCurrency: String?
        ): LocationDTO {
            throw NotImplementedError()
        }

        override suspend fun deleteCollection(collectionId: String) {
            throw NotImplementedError()
        }

        override suspend fun updateCollection(
            collectionId: String,
            name: String,
            description: String,
            isPublic: Boolean,
            startDate: String?,
            endDate: String?,
            link: String?
        ): CollectionDTO {
            throw NotImplementedError()
        }

        override suspend fun getCountries(): List<CountryDTO> {
            throw NotImplementedError()
        }

        override suspend fun getRegions(countryCode: String): List<RegionDTO> {
            throw NotImplementedError()
        }

        override suspend fun getVisitedRegions(): List<VisitedRegionDTO> {
            throw NotImplementedError()
        }

        override suspend fun getVisitedCities(): List<VisitedCityDTO> {
            throw NotImplementedError()
        }
        
        override suspend fun createTransportation(
            name: String,
            type: String,
            description: String,
            rating: Double,
            link: String,
            fromLocation: String,
            toLocation: String,
            departureDate: String,
            arrivalDate: String,
            departureTimezone: String,
            arrivalTimezone: String,
            flightNumber: String,
            distance: String,
            originLatitude: String?,
            originLongitude: String?,
            destinationLatitude: String?,
            destinationLongitude: String?,
            isPublic: Boolean,
            images: List<String>,
            attachments: List<String>,
            collectionId: String?
        ): com.desarrollodroide.adventurelog.core.model.Transportation {
            throw NotImplementedError()
        }
        
        override suspend fun updateTransportation(
            transportationId: String,
            name: String,
            type: String,
            description: String,
            rating: Double,
            link: String,
            fromLocation: String,
            toLocation: String,
            departureDate: String,
            arrivalDate: String,
            departureTimezone: String,
            arrivalTimezone: String,
            flightNumber: String,
            distance: String,
            originLatitude: String?,
            originLongitude: String?,
            destinationLatitude: String?,
            destinationLongitude: String?,
            isPublic: Boolean,
            images: List<String>,
            attachments: List<String>,
            collectionId: String?
        ): com.desarrollodroide.adventurelog.core.model.Transportation {
            throw NotImplementedError()
        }
        
        override suspend fun getTransportation(transportationId: String): com.desarrollodroide.adventurelog.core.model.Transportation {
            throw NotImplementedError()
        }
        
        override suspend fun deleteTransportation(transportationId: String) {
            throw NotImplementedError()
        }

        override suspend fun uploadImage(
            contentType: String,
            objectId: String,
            imageBytes: ByteArray,
            fileName: String
        ) {
            throw NotImplementedError()
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
