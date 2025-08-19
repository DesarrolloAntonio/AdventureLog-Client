package com.desarrollodroide.adventurelog.core.data

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.paging.CollectionsPagingSource
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.io.IOException

class CollectionsRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : CollectionsRepository {

    private val _collectionsFlow = MutableStateFlow<List<Collection>>(emptyList())
    override val collectionsFlow: StateFlow<List<Collection>> = _collectionsFlow.asStateFlow()

    // Version counter to force paging invalidation
    private val _version = MutableStateFlow(0)

    override fun getCollectionsPagingData(
        sortField: String?,
        sortDirection: String?
    ): Flow<PagingData<Collection>> {
        return _version.flatMapLatest { _ ->
            Pager(
                config = PagingConfig(
                    pageSize = 30,
                    enablePlaceholders = false,
                    initialLoadSize = 30,
                    prefetchDistance = 10
                ),
                pagingSourceFactory = { 
                    CollectionsPagingSource(
                        networkDataSource = networkDataSource, 
                        pageSize = 30,
                        sortField = sortField,
                        sortDirection = sortDirection
                    ) 
                }
            ).flow
        }
    }

    override suspend fun getCollections(
        page: Int,
        pageSize: Int
    ): Either<ApiResponse, List<Collection>> {
        // If we already have collections cached and it's the first page, return from cache
        if (page == 1 && _collectionsFlow.value.isNotEmpty()) {
            val cachedCollections = _collectionsFlow.value
            val requestedCollections = if (pageSize >= cachedCollections.size) {
                cachedCollections
            } else {
                cachedCollections.take(pageSize)
            }
            return Either.Right(requestedCollections)
        }

        return try {
            val collections =
                networkDataSource.getCollections(page, pageSize).map { it.toDomainModel() }

            // Update the flow with the new collections (only for first page)
            if (page == 1) {
                _collectionsFlow.value = collections
            }

            Either.Right(collections)
        } catch (e: HttpException) {
            println("HTTP Error during getCollections: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getCollections: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getCollections: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> {
        return try {
            val collection = networkDataSource.getCollectionDetail(collectionId).toDomainModel()
            Either.Right(collection)
        } catch (e: HttpException) {
            println("HTTP Error during getCollection: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getCollection: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getCollection: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): Either<ApiResponse, Collection> {
        return try {
            val collection = networkDataSource.createCollection(
                name = name,
                description = description,
                isPublic = isPublic,
                startDate = startDate,
                endDate = endDate
            ).toDomainModel()

            // Add the new collection to the flow
            _collectionsFlow.value = _collectionsFlow.value + collection

            // Increment version to invalidate paging
            _version.value++

            Either.Right(collection)
        } catch (e: HttpException) {
            println("HTTP Error during createCollection: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during createCollection: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during createCollection: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun refreshCollections(): Either<ApiResponse, List<Collection>> {
        return try {
            val collections = networkDataSource.getCollections(1, 1000).map { it.toDomainModel() }
            _collectionsFlow.value = collections
            Either.Right(collections)
        } catch (e: HttpException) {
            println("HTTP Error during refreshCollections: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during refreshCollections: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during refreshCollections: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun deleteCollection(collectionId: String): Either<ApiResponse, Unit> {
        return try {
            networkDataSource.deleteCollection(collectionId)
            
            // Remove the deleted collection from the flow
            _collectionsFlow.value = _collectionsFlow.value.filter { it.id != collectionId }
            
            // Increment version to invalidate paging
            _version.value++
            
            Either.Right(Unit)
        } catch (e: HttpException) {
            println("HTTP Error during deleteCollection: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during deleteCollection: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during deleteCollection: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
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
        return try {
            val collection = networkDataSource.updateCollection(
                collectionId = collectionId,
                name = name,
                description = description,
                isPublic = isPublic,
                startDate = startDate,
                endDate = endDate,
                link = link
            ).toDomainModel()

            // Update the collection in the flow
            _collectionsFlow.value = _collectionsFlow.value.map { 
                if (it.id == collectionId) collection else it
            }

            // Increment version to invalidate paging
            _version.value++

            Either.Right(collection)
        } catch (e: HttpException) {
            println("HTTP Error during updateCollection: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during updateCollection: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during updateCollection: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
}
