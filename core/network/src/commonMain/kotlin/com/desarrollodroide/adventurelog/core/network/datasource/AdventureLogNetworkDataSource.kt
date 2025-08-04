package com.desarrollodroide.adventurelog.core.network.datasource

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO

interface AdventureLogNetworkDataSource {

    /**
     * Get paginated list of adventures
     */
    suspend fun getAdventures(
        page: Int,
        pageSize: Int
    ): List<AdventureDTO>

    /**
     * Get filtered and paginated list of adventures
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
    ): List<AdventureDTO>

    /**
     * Get adventure details by ID
     */
    suspend fun getAdventureDetail(
        objectId: String
    ): AdventureDTO

    /**
     * Get paginated list of collections
     */
    suspend fun getCollections(
        page: Int,
        pageSize: Int
    ): List<CollectionDTO>

    /**
     * Get collection details by ID
     */
    suspend fun getCollectionDetail(
        collectionId: String
    ): CollectionDTO

    /**
     * Send login request and return user details
     */
    suspend fun sendLogin(
        url: String,
        username: String,
        password: String
    ): UserDetailsDTO

    /**
     * Get current user details
     */
    suspend fun getUserDetails(): UserDetailsDTO

    /**
     * Initialize network client with server URL and tokens from existing session
     */
    fun initializeFromSession(
        serverUrl: String,
        sessionToken: String?
    )

    /**
     * Clear session data from network client (tokens, base URL)
     * Used during logout to reset network state
     */
    fun clearSession()

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
    ): AdventureDTO

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
     * Get all available categories
     */
    suspend fun getCategories(): List<CategoryDTO>

    /**
     * Generate description from Wikipedia
     */
    suspend fun generateDescription(
        name: String
    ): String

    /**
     * Search for locations by query
     */
    suspend fun searchLocations(
        query: String
    ): List<GeocodeSearchResultDTO>

    /**
     * Reverse geocode coordinates to get location details
     */
    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): ReverseGeocodeResultDTO

    /**
     * Get user statistics
     */
    suspend fun getUserStats(
        username: String
    ): UserStatsDTO

    /**
     * Delete an adventure
     */
    suspend fun deleteAdventure(adventureId: String)
    
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
        tags: List<String>
    ): AdventureDTO

    /**
     * Delete a collection
     */
    suspend fun deleteCollection(collectionId: String)

    /**
     * Update an existing collection
     */
    suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): CollectionDTO
    
    /**
     * Get all countries
     */
    suspend fun getCountries(): List<CountryDTO>
    
    /**
     * Get regions for a specific country
     */
    suspend fun getRegions(countryCode: String): List<RegionDTO>
    
    /**
     * Get visited regions for the current user
     */
    suspend fun getVisitedRegions(): List<VisitedRegionDTO>
    
    /**
     * Get visited cities for the current user
     */
    suspend fun getVisitedCities(): List<VisitedCityDTO>
}