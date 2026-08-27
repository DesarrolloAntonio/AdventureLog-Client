package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
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
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionInviteDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.EmailAddressDTO
import com.desarrollodroide.adventurelog.core.network.model.response.MediaUsageDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import com.desarrollodroide.adventurelog.core.network.model.response.CalendarEventsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.SearchResultsDTO

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

        var activeSessionSet: UserDetails? = null

        override fun setActiveSession(userDetails: UserDetails) {
            activeSessionSet = userDetails
        }

        override fun getUserSession(): Flow<UserDetails?> {
            throw NotImplementedError()
        }

        override val activeSession: UserDetails?
            get() = null

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

        override suspend fun getUserStats(username: String): Either<ApiResponse, UserStats> {
            return Either.Right(UserStats())
        }

        override fun getUserStatsFlow(): Flow<UserStats?> {
            throw NotImplementedError()
        }
    }

    private open class FakeNetworkDataSource : AdventureLogNetwork {
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
            return UserDetailsDTO(
                id = 1,
                username = "testuser",
                firstName = "Test",
                lastName = "User",
                email = "test@example.com",
                profilePic = null,
                isStaff = false,
                dateJoined = "2024-01-01T00:00:00Z",
                uuid = "user123",
                publicProfile = true,
                measurementSystem = "metric",
                disablePassword = false,
                hasPassword = true
            )
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

        override suspend fun getCalendarEvents(start: String?, end: String?): CalendarEventsDTO =
            throw NotImplementedError()

        override suspend fun globalSearch(query: String, limit: Int): SearchResultsDTO =
            throw NotImplementedError()

        override suspend fun getPublicUsers(): List<UserDetailsDTO> =
            throw NotImplementedError()

        override suspend fun shareCollection(collectionId: String, userUuid: String) =
            throw NotImplementedError()

        override suspend fun unshareCollection(collectionId: String, userUuid: String) =
            throw NotImplementedError()

        override suspend fun revokeInvite(collectionId: String, userUuid: String) =
            throw NotImplementedError()


        override suspend fun updateUserProfile(
            username: String?,
            firstName: String?,
            lastName: String?,
            publicProfile: Boolean?,
            measurementSystem: String?,
            defaultCurrency: String?,
            mapStyle: String?
        ): UserDetailsDTO {
            throw NotImplementedError()
        }

        override suspend fun changePassword(currentPassword: String, newPassword: String): Boolean {
            throw NotImplementedError()
        }

        override suspend fun getMediaUsage(): MediaUsageDTO {
            throw NotImplementedError()
        }

        override suspend fun getEmailAddresses(): List<EmailAddressDTO> {
            throw NotImplementedError()
        }

        override suspend fun addEmailAddress(email: String) {
            throw NotImplementedError()
        }

        override suspend fun requestEmailVerification(email: String) {
            throw NotImplementedError()
        }

        override suspend fun setPrimaryEmailAddress(email: String) {
            throw NotImplementedError()
        }

        override suspend fun removeEmailAddress(email: String) {
            throw NotImplementedError()
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

        override suspend fun duplicateCollection(collectionId: String): CollectionDTO {
            throw NotImplementedError()
        }

        override suspend fun setCollectionArchived(
            collectionId: String,
            archived: Boolean
        ): CollectionDTO {
            throw NotImplementedError()
        }

        override suspend fun getCollectionShareImage(
            collectionId: String,
            aspect: String
        ): ByteArray {
            throw NotImplementedError()
        }

        override suspend fun exportCollectionPdf(collectionId: String): ByteArray {
            throw NotImplementedError()
        }

        override suspend fun exportCollectionZip(collectionId: String): ByteArray {
            throw NotImplementedError()
        }

        override suspend fun getArchivedCollections(): List<com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO> {
            throw NotImplementedError()
        }

        override suspend fun getSharedCollections(): List<com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO> {
            throw NotImplementedError()
        }

        override suspend fun getCollectionInvites(): List<CollectionInviteDTO> {
            throw NotImplementedError()
        }

        override suspend fun acceptCollectionInvite(collectionId: String) {
            throw NotImplementedError()
        }

        override suspend fun declineCollectionInvite(collectionId: String) {
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

    private val fakeRepository = FakeUserRepository()
    private val fakeNetworkDataSource = FakeNetworkDataSource()
    private val useCase = InitializeSessionUseCase(fakeRepository, fakeNetworkDataSource)

    @Test
    fun `invoke initializes the network from the stored session`() = runTest {
        val userDetails = createFakeUserDetails()
        fakeRepository.getUserSessionOnceResult = userDetails

        val result = useCase()

        assertNotNull(result)
        assertEquals(true, fakeNetworkDataSource.initializeFromSessionCalled)
        assertEquals(userDetails.serverUrl, fakeNetworkDataSource.lastServerUrl)
        assertEquals(userDetails.sessionToken, fakeNetworkDataSource.lastSessionToken)
    }

    @Test
    fun `invoke returns the freshly fetched profile rather than the stored copy`() = runTest {
        // The stored session is written from the login response, which carries no name, so the
        // profile fetched here is the only place the display name can come from.
        fakeRepository.getUserSessionOnceResult = createFakeUserDetails().copy(
            firstName = "",
            lastName = "",
            email = ""
        )

        val result = useCase()

        assertEquals("Test", result?.firstName)
        assertEquals("User", result?.lastName)
        assertEquals("test@example.com", result?.email)
        // Returning it is not enough - the greeting and drawer read the session from the
        // repository, so the refreshed profile has to be published there too.
        assertEquals("Test", fakeRepository.activeSessionSet?.firstName)
    }

    @Test
    fun `invoke keeps the session token and server url from the stored session`() = runTest {
        // Neither is part of the profile response, so both have to survive the merge.
        fakeRepository.getUserSessionOnceResult = createFakeUserDetails()

        val result = useCase()

        assertEquals("test-session-token", result?.sessionToken)
        assertEquals("https://test.com", result?.serverUrl)
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
        uuid = "user123",
        username = "testuser",
        firstName = "Test",
        lastName = "User",
        profilePic = "",
        publicProfile = true,
        measurementSystem = "metric",
        dateJoined = "2024-01-01T00:00:00Z",
        isStaff = false,
        disablePassword = false,
        hasPassword = true,
        serverUrl = "https://test.com",
        sessionToken = "test-session-token"
    )
}
