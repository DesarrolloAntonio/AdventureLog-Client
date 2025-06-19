package com.desarrollodroide.adventurelog.core.network.datasource

import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO

interface CollectionNetworkDataSource {
    /**
     * Get paginated list of collections
     */
    suspend fun getCollections(page: Int, pageSize: Int): List<CollectionDTO>

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
        name: String? = null,
        description: String? = null,
        isPublic: Boolean? = null,
        startDate: String? = null,
        endDate: String? = null
    ): CollectionDTO
    
    /**
     * Delete a collection
     */
    suspend fun deleteCollection(collectionId: String)
    
    /**
     * Add an adventure to a collection
     */
    suspend fun addAdventureToCollection(collectionId: String, adventureId: String)
    
    /**
     * Remove an adventure from a collection
     */
    suspend fun removeAdventureFromCollection(collectionId: String, adventureId: String)
}
