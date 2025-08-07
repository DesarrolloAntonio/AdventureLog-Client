package com.desarrollodroide.adventurelog.core.domain

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCollectionDetailUseCaseTest {

    private class FakeCollectionsRepository : CollectionsRepository {
        var getCollectionResult: Either<ApiResponse, Collection> = Either.Right(createFakeCollection())
        var lastCollectionIdParam: String? = null
        
        private val _collectionsFlow = MutableStateFlow<List<Collection>>(emptyList())
        override val collectionsFlow: StateFlow<List<Collection>> = _collectionsFlow

        override fun getCollectionsPagingData(): Flow<PagingData<Collection>> {
            return flowOf(PagingData.empty())
        }

        override suspend fun getCollections(page: Int, pageSize: Int): Either<ApiResponse, List<Collection>> {
            throw NotImplementedError()
        }

        override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> {
            lastCollectionIdParam = collectionId
            return getCollectionResult
        }

        override suspend fun createCollection(
            name: String,
            description: String,
            isPublic: Boolean,
            startDate: String?,
            endDate: String?
        ): Either<String, Collection> {
            throw NotImplementedError()
        }

        override suspend fun refreshCollections(): Either<ApiResponse, List<Collection>> {
            return Either.Right(emptyList())
        }
    }

    private val fakeRepository = FakeCollectionsRepository()
    private val useCase = GetCollectionDetailUseCase(fakeRepository)

    @Test
    fun `invoke returns success when repository returns collection`() = runTest {
        val expectedCollection = createFakeCollection()
        fakeRepository.getCollectionResult = Either.Right(expectedCollection)

        val result = useCase("collection123")

        assertTrue(result is Either.Right)
        assertEquals(expectedCollection, result.value)
        assertEquals("collection123", fakeRepository.lastCollectionIdParam)
    }

    @Test
    fun `invoke returns network error message when repository returns IOException`() = runTest {
        fakeRepository.getCollectionResult = Either.Left(ApiResponse.IOException)

        val result = useCase("collection123")

        assertTrue(result is Either.Left)
        assertEquals("Network unavailable", result.value)
    }

    @Test
    fun `invoke returns http error message when repository returns HttpError`() = runTest {
        fakeRepository.getCollectionResult = Either.Left(ApiResponse.HttpError)

        val result = useCase("collection123")

        assertTrue(result is Either.Left)
        assertEquals("Error getting collection details, try again later", result.value)
    }

    @Test
    fun `invoke returns invalid credentials message when repository returns InvalidCredentials`() = runTest {
        fakeRepository.getCollectionResult = Either.Left(ApiResponse.InvalidCredentials)

        val result = useCase("collection123")

        assertTrue(result is Either.Left)
        assertEquals("Session expired, please log in again", result.value)
    }

    @Test
    fun `invoke passes correct collection id to repository`() = runTest {
        val collectionId = "test-collection-456"

        useCase(collectionId)

        assertEquals(collectionId, fakeRepository.lastCollectionIdParam)
    }

    companion object {
        private fun createFakeCollection() = Collection(
            id = "collection123",
            name = "Test Collection",
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
            link = "https://test.com/collection/collection123",
            lodging = emptyList()
        )
    }
}
