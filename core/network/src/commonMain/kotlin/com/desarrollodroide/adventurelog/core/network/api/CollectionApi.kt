package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionInviteDTO
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

    /** Collections the user has archived. Returned whole, not paged. */
    suspend fun getArchivedCollections(): List<UltraSlimCollectionDTO>

    /** Collections other people have shared with the user. Returned whole, not paged. */
    suspend fun getSharedCollections(): List<UltraSlimCollectionDTO>

    /** Invitations waiting for the user to accept or decline. */
    suspend fun getInvites(): List<CollectionInviteDTO>

    suspend fun acceptInvite(collectionId: String)

    suspend fun declineInvite(collectionId: String)

    /**
     * Invite someone to a collection by their uuid. The server creates an invitation rather than
     * sharing outright, and refuses anyone whose profile is not public.
     */
    suspend fun shareCollection(collectionId: String, userUuid: String)

    /** Take away access somebody already has. */
    suspend fun unshareCollection(collectionId: String, userUuid: String)

    /** Withdraw an invitation that has not been answered. */
    suspend fun revokeInvite(collectionId: String, userUuid: String)
    
    
}
