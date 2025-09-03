package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.LocationDTO
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData

interface AdventureApi {
    /**
     * Get paginated list of adventures
     */
    suspend fun getAdventures(
        page: Int,
        pageSize: Int
    ): List<LocationDTO>

    /**
     * Get filtered and paginated list of adventures
     * Uses the /api/adventures/filtered/ endpoint
     */
    suspend fun getAdventuresFiltered(
        page: Int,
        pageSize: Int,
        categoryIds: List<String>? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        isVisited: Boolean? = null,
        searchQuery: String? = null,
        includeCollections: Boolean = false
    ): List<LocationDTO>

    /**
     * Get adventure details by ID
     */
    suspend fun getAdventureDetail(
        objectId: String
    ): LocationDTO
    
    /**
     * Create a new adventure
     */
    suspend fun createAdventure(
        name: String,
        description: String,
        category: Category,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        visits: List<VisitFormData>,
        activityTypes: List<String> = emptyList()
    ): LocationDTO
    
    /**
     * Update an existing adventure
     */
    suspend fun updateAdventure(
        adventureId: String,
        name: String,
        description: String,
        category: Category?,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        tags: List<String>,
        collections: List<String> = emptyList()
    ): LocationDTO
    
    /**
     * Delete an adventure
     */
    suspend fun deleteAdventure(adventureId: String)
}
