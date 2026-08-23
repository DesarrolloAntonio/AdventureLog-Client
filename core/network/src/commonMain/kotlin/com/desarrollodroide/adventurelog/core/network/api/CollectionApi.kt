package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO

interface CollectionApi {
    /**
     * Get paginated list of collections (returns slim version)
     */
    suspend fun getCollections(page: Int, pageSize: Int): List<UltraSlimCollectionDTO>
    
    /**
     * Get all collections without pagination (returns slim version)
     */
    suspend fun getAllCollections(): List<UltraSlimCollectionDTO>

    /**
     * Get collection details by ID
     */
    suspend fun getCollectionDetail(collectionId: String): CollectionDTO
    
    /**
     * Create a new collection
     */
    suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): CollectionDTO
    
    /**
     * Update an existing collection
     */
    suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String? = null,
        endDate: String? = null,
        link: String? = null
    ): CollectionDTO
    
    /**
     * Delete a collection
     */
    suspend fun deleteCollection(collectionId: String)

    /**
     * Server-side copy. Metadata plus the linked locations, transport, notes, checklists,
     * lodging and itinerary; shared users are not carried over and the copy is private.
     */
    suspend fun duplicateCollection(collectionId: String): CollectionDTO

    /** Moves a collection in or out of the archive. */
    suspend fun setArchived(collectionId: String, archived: Boolean): CollectionDTO

    /** A rendered PNG share card. [aspect] is one of square, story or landscape. */
    suspend fun getShareImage(collectionId: String, aspect: String): ByteArray

    /** The day-by-day itinerary as a printable PDF. */
    suspend fun exportPdf(collectionId: String): ByteArray

    /** The collection and everything in it, as a ZIP. */
    suspend fun exportZip(collectionId: String): ByteArray
    
    
}
