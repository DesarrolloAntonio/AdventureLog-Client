package com.desarrollodroide.adventurelog.core.data

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.paging.CollectionsPagingSource
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.CollectionExport
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.toUltraSlimCollection
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.io.IOException

class CollectionsRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : CollectionsRepository {

    private val _collectionsFlow = MutableStateFlow<List<UltraSlimCollection>>(emptyList())
    override val collectionsFlow: StateFlow<List<UltraSlimCollection>> = _collectionsFlow.asStateFlow()

    // Version counter to force paging invalidation
    private val _version = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCollectionsPagingData(
        sortField: String?,
        sortDirection: String?
    ): Flow<PagingData<UltraSlimCollection>> {
        return _version.flatMapLatest { _ ->
            Pager(
                config = PagingConfig(
                    // Large page size to load all collections at once since there's no search endpoint in the backend
                    // Collections are filtered in memory in the ViewModel
                    pageSize = 10000,
                    enablePlaceholders = false,
                    initialLoadSize = 10000,
                    prefetchDistance = 10
                ),
                pagingSourceFactory = { 
                    CollectionsPagingSource(
                        networkDataSource = networkDataSource, 
                        pageSize = 10000,
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
    ): Either<ApiResponse, List<UltraSlimCollection>> {
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
    
    override suspend fun getAllCollections(forceRefresh: Boolean): Either<ApiResponse, List<UltraSlimCollection>> {
        if (!forceRefresh && _collectionsFlow.value.isNotEmpty()) {
            return Either.Right(_collectionsFlow.value)
        }
        
        return try {
            val collections = networkDataSource.getAllCollections().map { it.toDomainModel() }
            
            _collectionsFlow.value = collections
            
            Either.Right(collections)
        } catch (e: HttpException) {
            println("HTTP Error during getAllCollections: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getAllCollections: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getAllCollections: ${e.message}")
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

            // Convert created collection to UltraSlimCollection for the list
            val slimCollection = collection.toUltraSlimCollection()

            _collectionsFlow.value = _collectionsFlow.value + slimCollection

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

    override suspend fun refreshCollections(): Either<ApiResponse, List<UltraSlimCollection>> {
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
            
            _collectionsFlow.value = _collectionsFlow.value.filter { it.id != collectionId }
            
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

    override suspend fun duplicateCollection(collectionId: String): Either<ApiResponse, Collection> =
        guard { networkDataSource.duplicateCollection(collectionId).toDomainModel() }

    override suspend fun setArchived(
        collectionId: String,
        archived: Boolean
    ): Either<ApiResponse, Collection> = guard {
        networkDataSource.setCollectionArchived(collectionId, archived).toDomainModel()
    }

    override suspend fun exportCollection(
        collectionId: String,
        what: CollectionExport
    ): Either<ApiResponse, ByteArray> = guard {
        when (what) {
            CollectionExport.SHARE_CARD ->
                networkDataSource.getCollectionShareImage(collectionId, "square")
            CollectionExport.PDF -> networkDataSource.exportCollectionPdf(collectionId)
            CollectionExport.ZIP -> networkDataSource.exportCollectionZip(collectionId)
        }
    }

    private inline fun <T> guard(block: () -> T): Either<ApiResponse, T> = try {
        Either.Right(block())
    } catch (e: HttpException) {
        when (e.code) {
            401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
            else -> Either.Left(ApiResponse.HttpError)
        }
    } catch (e: IOException) {
        Either.Left(ApiResponse.IOException)
    } catch (e: Exception) {
        Either.Left(ApiResponse.HttpError)
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

            // Update the slim collection in the list
            val slimCollection = collection.toUltraSlimCollection()

            _collectionsFlow.value = _collectionsFlow.value.map { 
                if (it.id == collectionId) slimCollection else it
            }

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
