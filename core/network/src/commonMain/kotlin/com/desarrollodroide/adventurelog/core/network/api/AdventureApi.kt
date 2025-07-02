package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit

interface AdventureApi {
    /**
     * Get paginated list of adventures
     */
    suspend fun getAdventures(
        page: Int,
        pageSize: Int
    ): List<AdventureDTO>

    /**
     * Get adventure details by ID
     */
    suspend fun getAdventureDetail(
        objectId: String
    ): AdventureDTO
    
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
        visitDates: Visit?
    ): AdventureDTO
    
    /**
     * Update an existing adventure
     */
    suspend fun updateAdventure(
        adventureId: String,
        name: String? = null,
        description: String? = null,
        categoryId: String? = null,
        rating: Double? = null,
        link: String? = null,
        location: String? = null,
        latitude: String? = null,
        longitude: String? = null,
        isPublic: Boolean? = null,
        visitDates: Visit? = null
    ): AdventureDTO
    
    /**
     * Delete an adventure
     */
    suspend fun deleteAdventure(adventureId: String)
}
