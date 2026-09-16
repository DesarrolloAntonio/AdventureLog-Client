package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.domain.repository.AccountDataCache
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.paging.CollectionsPagingSource
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.CollectionInvite
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
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import com.desarrollodroide.adventurelog.core.model.Lodging

private val logger = Logger.withTag("CollectionsRepositoryImpl")

class CollectionsRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : CollectionsRepository, AccountDataCache {

    private val _collectionsFlow = MutableStateFlow<List<UltraSlimCollection>>(emptyList())
    override val collectionsFlow: StateFlow<List<UltraSlimCollection>> = _collectionsFlow.asStateFlow()

    // Version counter to force paging invalidation
    private val _version = MutableStateFlow(0)

    override fun clearAccountData() {
        _collectionsFlow.value = emptyList()
        _version.value++
    }

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
            logger.e { "HTTP Error during getCollections: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getCollections: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getCollections: ${e.message}" }
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
            logger.e { "HTTP Error during getAllCollections: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getAllCollections: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getAllCollections: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> {
        return try {
            val collection = networkDataSource.getCollectionDetail(collectionId).toDomainModel()
            Either.Right(collection)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getCollection: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getCollection: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getCollection: ${e.message}" }
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
            logger.e { "HTTP Error during createCollection: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during createCollection: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during createCollection: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun refreshCollections(): Either<ApiResponse, List<UltraSlimCollection>> {
        return try {
            val collections = networkDataSource.getCollections(1, 1000).map { it.toDomainModel() }
            _collectionsFlow.value = collections

            // An explicit refresh is a statement that what is on screen may be out of date, and
            // the paged list is on screen too - it just reads from a different source than the
            // cached flow above. Without this, anything that only calls refresh() (duplicating a
            // collection, accepting an invitation) moved the header count and left the list
            // alone: six collections above five cards.
            _version.value++

            Either.Right(collections)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during refreshCollections: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during refreshCollections: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during refreshCollections: ${e.message}" }
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
            logger.e { "HTTP Error during deleteCollection: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during deleteCollection: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during deleteCollection: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun duplicateCollection(collectionId: String): Either<ApiResponse, Collection> =
        guard { networkDataSource.duplicateCollection(collectionId).toDomainModel() }

    override suspend fun setArchived(
        collectionId: String,
        archived: Boolean
    ): Either<ApiResponse, Collection> = guard {
        val collection = networkDataSource.setCollectionArchived(collectionId, archived)
            .toDomainModel()

        // Archiving changes which collections the paged list should show, so it has to invalidate
        // paging and update the cached list the same way create and delete do. Without this the
        // archived collection stayed on screen until the process died.
        _collectionsFlow.value = _collectionsFlow.value.map {
            if (it.id == collectionId) it.copy(isArchived = archived) else it
        }
        _version.value++

        collection
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

    override suspend fun getArchivedCollections(): Either<ApiResponse, List<UltraSlimCollection>> =
        guard { networkDataSource.getArchivedCollections().map { it.toDomainModel() } }

    override suspend fun getSharedCollections(): Either<ApiResponse, List<UltraSlimCollection>> =
        guard { networkDataSource.getSharedCollections().map { it.toDomainModel() } }

    override suspend fun getInvites(): Either<ApiResponse, List<CollectionInvite>> =
        guard { networkDataSource.getCollectionInvites().map { it.toDomainModel() } }

    override suspend fun respondToInvite(
        collectionId: String,
        accept: Boolean
    ): Either<ApiResponse, Unit> = guard {
        if (accept) {
            networkDataSource.acceptCollectionInvite(collectionId)
        } else {
            networkDataSource.declineCollectionInvite(collectionId)
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
            logger.e { "HTTP Error during updateCollection: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during updateCollection: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during updateCollection: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun createNote(
        collectionId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Note> = noteCall {
        networkDataSource.createNote(name, content, date, isPublic, collectionId)
    }

    override suspend fun updateNote(
        noteId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Note> = noteCall {
        networkDataSource.updateNote(noteId, name, content, date, isPublic)
    }

    override suspend fun deleteNote(noteId: String): Either<ApiResponse, Unit> = noteCall {
        networkDataSource.deleteNote(noteId)
    }

    override suspend fun createChecklist(
        collectionId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Checklist> = noteCall {
        networkDataSource.createChecklist(name, items, date, isPublic, collectionId)
    }

    override suspend fun updateChecklist(
        checklistId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Checklist> = noteCall {
        networkDataSource.updateChecklist(checklistId, name, items, date, isPublic)
    }

    override suspend fun deleteChecklist(checklistId: String): Either<ApiResponse, Unit> = noteCall {
        networkDataSource.deleteChecklist(checklistId)
    }

    override suspend fun createLodging(
        collectionId: String,
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean
    ): Either<ApiResponse, Lodging> = noteCall {
        networkDataSource.createLodging(
            name, type, description, checkIn, checkOut, timezone,
            reservationNumber, price, priceCurrency, link, location, isPublic, collectionId
        )
    }

    override suspend fun updateLodging(
        lodgingId: String,
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean
    ): Either<ApiResponse, Lodging> = noteCall {
        networkDataSource.updateLodging(
            lodgingId, name, type, description, checkIn, checkOut, timezone,
            reservationNumber, price, priceCurrency, link, location, isPublic
        )
    }

    override suspend fun deleteLodging(lodgingId: String): Either<ApiResponse, Unit> = noteCall {
        networkDataSource.deleteLodging(lodgingId)
    }

    override suspend fun autoGenerateItinerary(
        collectionId: String
    ): Either<ApiResponse, List<ItineraryEntry>> = noteCall {
        networkDataSource.autoGenerateItinerary(collectionId)
    }

    override suspend fun addItineraryEntry(
        collectionId: String,
        kind: ItineraryItemKind,
        itemId: String,
        date: String?,
        order: Int
    ): Either<ApiResponse, ItineraryEntry> = noteCall {
        networkDataSource.addItineraryEntry(collectionId, kind, itemId, date, order)
    }

    override suspend fun deleteItineraryEntry(entryId: String): Either<ApiResponse, Unit> =
        noteCall {
            networkDataSource.deleteItineraryEntry(entryId)
        }

    /**
     * The note, checklist and lodging calls differ only in the line that talks to the network; the twenty lines of
     * error mapping around them are identical, and three copies of it is three places to fix the
     * next time the shape changes.
     *
     * The version counter is bumped either way: a collection's note list is part of the collection
     * the detail screen is showing, so it has to be re-read.
     */
    private inline fun <T> noteCall(block: () -> T): Either<ApiResponse, T> {
        return try {
            val result = block()
            _version.value++
            Either.Right(result)
        } catch (e: HttpException) {
            logger.e { "HTTP Error on a note call: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error on a note call: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error on a note call: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
}
