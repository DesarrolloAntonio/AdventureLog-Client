package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.LocationDTO
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.model.response.SearchResultsDTO

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

    /** Server-side copy. Everything but collections and visits, named "Copy of ...". */
    suspend fun duplicateLocation(locationId: String): LocationDTO

    /** A rendered PNG share card. [aspect] is one of square, story or landscape. */
    suspend fun getShareImage(locationId: String, aspect: String): ByteArray
    
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
    /**
     * Search everything the account can see. The server rejects a term under two characters, so
     * callers should not send one.
     */
    suspend fun globalSearch(query: String, limit: Int = 20): SearchResultsDTO

    suspend fun deleteLocation(adventureId: String)
}
