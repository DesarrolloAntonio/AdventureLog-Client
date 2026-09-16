package com.desarrollodroide.adventurelog.core.domain.repository

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import com.desarrollodroide.adventurelog.core.model.CollectionExport
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import com.desarrollodroide.adventurelog.core.model.Lodging

interface CollectionsRepository {

    val collectionsFlow: StateFlow<List<UltraSlimCollection>>

    fun getCollectionsPagingData(
        sortField: String? = null,
        sortDirection: String? = null
    ): Flow<PagingData<UltraSlimCollection>>

    suspend fun getCollections(page: Int, pageSize: Int): Either<ApiResponse, List<UltraSlimCollection>>
    suspend fun getAllCollections(forceRefresh: Boolean = false): Either<ApiResponse, List<UltraSlimCollection>>
    suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection>
    suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): Either<ApiResponse, Collection>

    /** Notes belong to a collection, so they live on this repository rather than one of their own. */
    suspend fun createNote(
        collectionId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Note>

    suspend fun updateNote(
        noteId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Note>

    suspend fun deleteNote(noteId: String): Either<ApiResponse, Unit>

    suspend fun createChecklist(
        collectionId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Checklist>

    suspend fun updateChecklist(
        checklistId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Either<ApiResponse, Checklist>

    suspend fun deleteChecklist(checklistId: String): Either<ApiResponse, Unit>

    suspend fun createLodging(
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
    ): Either<ApiResponse, Lodging>

    suspend fun updateLodging(
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
    ): Either<ApiResponse, Lodging>

    suspend fun deleteLodging(lodgingId: String): Either<ApiResponse, Unit>

    /**
     * Build a whole itinerary from the dates already on the collection's records. The server only
     * allows it while the collection's itinerary is empty.
     */
    suspend fun autoGenerateItinerary(
        collectionId: String
    ): Either<ApiResponse, List<ItineraryEntry>>

    /** Place one of the collection's items on a day, or in the trip-context bucket when null. */
    suspend fun addItineraryEntry(
        collectionId: String,
        kind: ItineraryItemKind,
        itemId: String,
        date: String?,
        order: Int
    ): Either<ApiResponse, ItineraryEntry>

    /** Take an entry off its day. The item itself stays in the collection. */
    suspend fun deleteItineraryEntry(entryId: String): Either<ApiResponse, Unit>

    suspend fun refreshCollections(): Either<ApiResponse, List<UltraSlimCollection>>

    suspend fun deleteCollection(collectionId: String): Either<ApiResponse, Unit>

    suspend fun duplicateCollection(collectionId: String): Either<ApiResponse, Collection>

    suspend fun setArchived(collectionId: String, archived: Boolean): Either<ApiResponse, Collection>

    /** [what] picks the file: a share card, a printable itinerary, or a full export. */
    suspend fun exportCollection(
        collectionId: String,
        what: CollectionExport
    ): Either<ApiResponse, ByteArray>

    suspend fun getArchivedCollections(): Either<ApiResponse, List<UltraSlimCollection>>

    suspend fun getSharedCollections(): Either<ApiResponse, List<UltraSlimCollection>>

    suspend fun getInvites(): Either<ApiResponse, List<CollectionInvite>>

    suspend fun respondToInvite(
        collectionId: String,
        accept: Boolean
    ): Either<ApiResponse, Unit>

    suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): Either<ApiResponse, Collection>

}
