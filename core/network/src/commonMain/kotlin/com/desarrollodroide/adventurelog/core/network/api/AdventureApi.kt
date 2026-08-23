package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.LocationDTO
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData

interface AdventureApi {
    /**
     * Get paginated list of locations
     */
    suspend fun getLocations(
        page: Int,
        pageSize: Int
    ): List<LocationDTO>

    /**
     * Get filtered and paginated list of locations
     */
    suspend fun getLocationsFiltered(
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
     * Get location details by ID
     */
    suspend fun getAdventureDetail(
        objectId: String
    ): LocationDTO
    
    /**
     * Create a new location
     */
    suspend fun createLocation(
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
        price: Double?,
        priceCurrency: String?,
        activityTypes: List<String> = emptyList()
    ): LocationDTO
    
    /**
     * Update an existing location
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
        collections: List<String> = emptyList(),
        visits: List<VisitFormData> = emptyList(),
        price: Double? = null,
        priceCurrency: String? = null
    ): LocationDTO
    
    /**
     * Delete a location
     */
    suspend fun deleteLocation(adventureId: String)
}
