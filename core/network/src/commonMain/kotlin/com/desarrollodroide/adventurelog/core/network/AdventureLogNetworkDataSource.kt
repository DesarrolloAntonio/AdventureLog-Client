package com.desarrollodroide.adventurelog.core.network

import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO

interface AdventureLogNetworkDataSource {

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
        visitDates: Visit?
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
}