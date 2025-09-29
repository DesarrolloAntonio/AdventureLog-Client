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
     * Add an adventure to a collection
     */
    suspend fun addAdventureToCollection(collectionId: String, adventureId: String)
    
    /**
     * Remove an adventure from a collection
     */
    suspend fun removeAdventureFromCollection(collectionId: String, adventureId: String)
}
