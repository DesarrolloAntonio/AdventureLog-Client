package com.desarrollodroide.adventurelog.core.domain

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCollectionsUseCaseTest {

    private class FakeCollectionsRepository : CollectionsRepository {
        var getCollectionsResult: Either<ApiResponse, List<Collection>> = Either.Right(emptyList())
        var getCollectionsCallCount = 0
        var lastPageParam: Int? = null
        var lastPageSizeParam: Int? = null
        
        private val _collectionsFlow = MutableStateFlow<List<Collection>>(emptyList())
        override val collectionsFlow: StateFlow<List<Collection>> = _collectionsFlow

        override fun getCollectionsPagingData(): Flow<PagingData<Collection>> {
            return flowOf(PagingData.empty())
        }

        override suspend fun getCollections(page: Int, pageSize: Int): Either<ApiResponse, List<Collection>> {
            getCollectionsCallCount++
            lastPageParam = page
            lastPageSizeParam = pageSize
            return getCollectionsResult
        }

        override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> {
            throw NotImplementedError()
        }

        override suspend fun createCollection(
            name: String,
            description: String,
            isPublic: Boolean,
            startDate: String?,
            endDate: String?
        ): Either<ApiResponse, Collection> {
            throw NotImplementedError()
        }

        override suspend fun refreshCollections(): Either<ApiResponse, List<Collection>> {
            return getCollectionsResult
        }

        override suspend fun deleteCollection(collectionId: String): Either<ApiResponse, Unit> {
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
        ): Either<ApiResponse, Collection> {
            throw NotImplementedError()
        }
    }

    private val fakeRepository = FakeCollectionsRepository()
    private val useCase = GetCollectionsUseCase(fakeRepository)

    @Test
    fun `invoke returns success when repository returns collections`() = runTest {
        val expectedCollections = listOf(
            createFakeCollection("1", "Collection 1"),
            createFakeCollection("2", "Collection 2")
        )
        fakeRepository.getCollectionsResult = Either.Right(expectedCollections)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Right)
        assertEquals(expectedCollections, result.value)
        assertEquals(1, fakeRepository.getCollectionsCallCount)
        assertEquals(1, fakeRepository.lastPageParam)
        assertEquals(10, fakeRepository.lastPageSizeParam)
    }

    @Test
    fun `invoke returns network error message when repository returns IOException`() = runTest {
        fakeRepository.getCollectionsResult = Either.Left(ApiResponse.IOException)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Left)
        assertEquals("Network unavailable", result.value)
    }

    @Test
    fun `invoke returns http error message when repository returns HttpError`() = runTest {
        fakeRepository.getCollectionsResult = Either.Left(ApiResponse.HttpError)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Left)
        assertEquals("Error getting collections, try again later", result.value)
    }

    @Test
    fun `invoke returns invalid credentials message when repository returns InvalidCredentials`() = runTest {
        fakeRepository.getCollectionsResult = Either.Left(ApiResponse.InvalidCredentials)

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Left)
        assertEquals("Session expired, please log in again", result.value)
    }

    @Test
    fun `invoke passes correct pagination parameters to repository`() = runTest {
        val page = 3
        val pageSize = 25
        fakeRepository.getCollectionsResult = Either.Right(emptyList())

        useCase(page = page, pageSize = pageSize)

        assertEquals(page, fakeRepository.lastPageParam)
        assertEquals(pageSize, fakeRepository.lastPageSizeParam)
    }

    @Test
    fun `invoke returns empty list when repository returns empty list`() = runTest {
        fakeRepository.getCollectionsResult = Either.Right(emptyList())

        val result = useCase(page = 1, pageSize = 10)

        assertTrue(result is Either.Right)
        assertTrue(result.value.isEmpty())
    }

    private fun createFakeCollection(id: String, name: String): Collection {
        return Collection(
            id = id,
            name = name,
            description = "Test collection description",
            userId = "user123",
            isPublic = true,
            adventures = emptyList(),
            createdAt = "2024-01-01T00:00:00Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2024-01-01T00:00:00Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "https://test.com/collection/$id",
            lodging = emptyList()
        )
    }
}
