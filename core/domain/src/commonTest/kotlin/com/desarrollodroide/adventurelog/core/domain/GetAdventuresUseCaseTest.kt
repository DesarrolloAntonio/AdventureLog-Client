package com.desarrollodroide.adventurelog.core.domain

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAdventuresUseCaseTest {

    private class FakeAdventuresRepository : LocationsRepository {
        var getAdventuresResult: Either<ApiResponse, List<Location>> = Either.Right(emptyList())
        var getAdventuresCallCount = 0
        var lastPageParam: Int? = null
        var lastPageSizeParam: Int? = null

        private val _adventuresFlow = MutableStateFlow<List<Location>>(emptyList())
        override val locationsFlow: StateFlow<List<Location>> = _adventuresFlow

        override fun getAdventuresPagingData(): Flow<PagingData<Location>> {
            return flowOf(PagingData.empty())
        }

        override fun getLocationsPagingDataFiltered(
            categoryNames: List<String>?,
            sortBy: String?,
            sortOrder: String?,
            isVisited: Boolean?,
            searchQuery: String?,
            includeCollections: Boolean
        ): Flow<PagingData<Location>> {
            return flowOf(PagingData.empty())
        }

        override suspend fun getAllLocations(): Either<ApiResponse, List<Location>> {
            return getAdventuresResult
        }

        override suspend fun getLocations(page: Int, pageSize: Int): Either<ApiResponse, List<Location>> {
            getAdventuresCallCount++
            lastPageParam = page
            lastPageSizeParam = pageSize
            return getAdventuresResult
        }

        override suspend fun getLocation(objectId: String): Either<ApiResponse, Location> {
            throw NotImplementedError()
        }

        override suspend fun createLocation(
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
        ): Either<ApiResponse, Location> {
            throw NotImplementedError()
        }

        override suspend fun refreshLocations(): Either<ApiResponse, List<Location>> {
            return getAdventuresResult
        }

        override suspend fun generateDescription(name: String): Either<ApiResponse, String> {
            return Either.Right("Generated description for $name")
        }

        override suspend fun deleteLocation(adventureId: String): Either<ApiResponse, Unit> {
            throw NotImplementedError()
        }

        override suspend fun updateLocation(
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
            visits: List<VisitFormData>
        ): Either<ApiResponse, Location> {
            throw NotImplementedError()
        }
    }

    private val fakeRepository = FakeAdventuresRepository()
    private val useCase = GetAdventuresUseCase(fakeRepository)

    @Test
    fun `invoke returns success when repository returns adventures`() = runTest {
        val expectedAdventures = listOf(
            createFakeAdventure("1", "Adventure 1"),
            createFakeAdventure("2", "Adventure 2")
        )
        fakeRepository.getAdventuresResult = Either.Right(expectedAdventures)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Right)
        assertEquals(expectedAdventures, result.value)
        assertEquals(1, fakeRepository.getAdventuresCallCount)
        assertEquals(1, fakeRepository.lastPageParam)
        assertEquals(10, fakeRepository.lastPageSizeParam)
    }

    @Test
    fun `invoke returns network error message when repository returns IOException`() = runTest {
        fakeRepository.getAdventuresResult = Either.Left(ApiResponse.IOException)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Left)
        assertEquals("Network unavailable", result.value)
    }

    @Test
    fun `invoke returns http error message when repository returns HttpError`() = runTest {
        fakeRepository.getAdventuresResult = Either.Left(ApiResponse.HttpError)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Left)
        assertEquals("Error getting adventures, try again later", result.value)
    }

    @Test
    fun `invoke returns invalid credentials message when repository returns InvalidCredentials`() = runTest {
        fakeRepository.getAdventuresResult = Either.Left(ApiResponse.InvalidCredentials)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Left)
        assertEquals("Session expired, please log in again", result.value)
    }

    @Test
    fun `invoke passes correct pagination parameters to repository`() = runTest {
        val page = 5
        val pageSize = 20
        fakeRepository.getAdventuresResult = Either.Right(emptyList())

        useCase(page = page, pageSize = pageSize)

        assertEquals(page, fakeRepository.lastPageParam)
        assertEquals(pageSize, fakeRepository.lastPageSizeParam)
    }

    @Test
    fun `invoke returns empty list when repository returns empty list`() = runTest {
        fakeRepository.getAdventuresResult = Either.Right(emptyList())

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Right)
        assertTrue(result.value.isEmpty())
    }

    private fun createFakeAdventure(id: String, name: String): Location {
        return Location(
            id = id,
            name = name,
            description = "Test adventure description",
            rating = 4.5,
            tags = listOf("Hiking", "Outdoor"), // CORRECCIÓN: 'activityTypes' renombrado a 'tags'
            location = "Test Location",
            isPublic = true,
            collections = listOf("test-collection"),
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
            images = emptyList(),
            link = "https://test.com/adventure/$id",
            longitude = "-74.0060",
            latitude = "40.7128",
            visits = emptyList(),
            isVisited = false,
            category = null,
            attachments = emptyList(),
            user = createFakeUser(),
            city = null,
            country = null,
            region = null,
            trails = emptyList()
        )
    }

    private fun createFakeUser(): UserDetails {
        return UserDetails(
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
            hasPassword = true
        )
    }
}
