package com.desarrollodroide.adventurelog.core.testing

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import com.desarrollodroide.adventurelog.core.model.CollectionExport
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.Lodging
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a CollectionsRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class CollectionsRepositoryStub : CollectionsRepository {

    // A flow of state is something to observe, not a call to make: a use case reads one in its
    // constructor, long before any test could have set it up. Empty, and overridable.
    override val collectionsFlow: StateFlow<List<UltraSlimCollection>> =
        MutableStateFlow(emptyList())
    override fun getCollectionsPagingData(sortField: String?, sortDirection: String?): Flow<PagingData<UltraSlimCollection>> = unused()
    override suspend fun getCollections(page: Int, pageSize: Int): Either<ApiResponse, List<UltraSlimCollection>> = unused()
    override suspend fun getAllCollections(forceRefresh: Boolean): Either<ApiResponse, List<UltraSlimCollection>> = unused()
    override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> = unused()
    override suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): Either<ApiResponse, Collection> = unused()
    override suspend fun createNote(
        collectionId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Note> = unused()
    override suspend fun updateNote(
        noteId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Note> = unused()
    override suspend fun deleteNote(noteId: String): Either<ApiResponse, Unit> = unused()
    override suspend fun createChecklist(
        collectionId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Checklist> = unused()
    override suspend fun updateChecklist(
        checklistId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Checklist> = unused()
    override suspend fun deleteChecklist(checklistId: String): Either<ApiResponse, Unit> = unused()
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
        link: String,
        location: String,
        isPublic: Boolean
    ): Either<ApiResponse, Lodging> = unused()
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
        link: String,
        location: String,
        isPublic: Boolean
    ): Either<ApiResponse, Lodging> = unused()
    override suspend fun deleteLodging(lodgingId: String): Either<ApiResponse, Unit> = unused()
    override suspend fun refreshCollections(): Either<ApiResponse, List<UltraSlimCollection>> = unused()
    override suspend fun deleteCollection(collectionId: String): Either<ApiResponse, Unit> = unused()
    override suspend fun duplicateCollection(collectionId: String): Either<ApiResponse, Collection> = unused()
    override suspend fun setArchived(collectionId: String, archived: Boolean): Either<ApiResponse, Collection> = unused()
    override suspend fun exportCollection(collectionId: String, what: CollectionExport): Either<ApiResponse, ByteArray> = unused()
    override suspend fun getArchivedCollections(): Either<ApiResponse, List<UltraSlimCollection>> = unused()
    override suspend fun getSharedCollections(): Either<ApiResponse, List<UltraSlimCollection>> = unused()
    override suspend fun getInvites(): Either<ApiResponse, List<CollectionInvite>> = unused()
    override suspend fun respondToInvite(collectionId: String, accept: Boolean): Either<ApiResponse, Unit> = unused()
    override suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): Either<ApiResponse, Collection> = unused()
}
