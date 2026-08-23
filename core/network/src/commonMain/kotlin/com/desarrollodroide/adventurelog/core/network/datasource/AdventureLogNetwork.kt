package com.desarrollodroide.adventurelog.core.network.datasource

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.model.response.VisitDTO
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.network.model.response.TrailDTO
import com.desarrollodroide.adventurelog.core.network.model.response.DashboardDTO
import com.desarrollodroide.adventurelog.core.network.model.response.LocationDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO

interface AdventureLogNetwork {

    /**
     * Get paginated list of adventures
     */
    suspend fun getAdventures(
        page: Int,
        pageSize: Int
    ): List<LocationDTO>

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
    ): List<LocationDTO>

    /**
     * Get adventure details by ID
     */
    suspend fun getAdventureDetail(
        objectId: String
    ): LocationDTO

    /**
     * Get paginated list of collections (returns slim version)
     */
    suspend fun getCollections(
        page: Int,
        pageSize: Int
    ): List<UltraSlimCollectionDTO>
    
    /**
     * Get all collections without pagination (returns slim version)
     */
    suspend fun getAllCollections(): List<UltraSlimCollectionDTO>

    /**
     * Get collection details by ID (returns full version)
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
        price: Double?,
        priceCurrency: String?,
        activityTypes: List<String> = emptyList()
    ): LocationDTO

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
     * Get a single category by ID
     */
    suspend fun getCategoryById(categoryId: String): CategoryDTO
    
    /**
     * Create a new category
     */
    suspend fun createCategory(
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO
    
    /**
     * Update an existing category
     */
    suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO
    
    /**
     * Delete a category
     */
    suspend fun deleteCategory(categoryId: String)

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
     * Get everything the home screen shows in a single request.
     */
    suspend fun getDashboard(): DashboardDTO

    /**
     * Visits are a resource of their own - see [com.desarrollodroide.adventurelog.core.network.api.VisitApi].
     */
    suspend fun createVisit(locationId: String, visit: VisitFormData): VisitDTO

    suspend fun updateVisit(visitId: String, locationId: String, visit: VisitFormData): VisitDTO

    suspend fun deleteVisit(visitId: String)

    suspend fun createTrail(locationId: String, trail: TrailFormData): TrailDTO

    suspend fun updateTrail(trailId: String, locationId: String, trail: TrailFormData): TrailDTO

    suspend fun deleteTrail(trailId: String)

    suspend fun duplicateLocation(locationId: String): LocationDTO

    suspend fun getShareImage(locationId: String, aspect: String): ByteArray

    suspend fun duplicateCollection(collectionId: String): CollectionDTO

    suspend fun setCollectionArchived(collectionId: String, archived: Boolean): CollectionDTO

    suspend fun getCollectionShareImage(collectionId: String, aspect: String): ByteArray

    suspend fun exportCollectionPdf(collectionId: String): ByteArray

    suspend fun exportCollectionZip(collectionId: String): ByteArray

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
        tags: List<String>,
        collections: List<String> = emptyList(),
        visits: List<VisitFormData> = emptyList(),
        price: Double? = null,
        priceCurrency: String? = null
    ): LocationDTO

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
    
    /**
     * Create a new transportation
     */
    suspend fun createTransportation(
        name: String,
        type: String,
        description: String,
        rating: Double,
        link: String,
        fromLocation: String,
        toLocation: String,
        departureDate: String,
        arrivalDate: String,
        departureTimezone: String,
        arrivalTimezone: String,
        flightNumber: String,
        distance: String,
        originLatitude: String?,
        originLongitude: String?,
        destinationLatitude: String?,
        destinationLongitude: String?,
        isPublic: Boolean,
        images: List<String>,
        attachments: List<String>,
        collectionId: String? = null
    ): Transportation
    
    /**
     * Update an existing transportation
     */
    suspend fun updateTransportation(
        transportationId: String,
        name: String,
        type: String,
        description: String,
        rating: Double,
        link: String,
        fromLocation: String,
        toLocation: String,
        departureDate: String,
        arrivalDate: String,
        departureTimezone: String,
        arrivalTimezone: String,
        flightNumber: String,
        distance: String,
        originLatitude: String?,
        originLongitude: String?,
        destinationLatitude: String?,
        destinationLongitude: String?,
        isPublic: Boolean,
        images: List<String>,
        attachments: List<String>,
        collectionId: String? = null
    ): Transportation
    
    /**
     * Get a transportation by ID
     */
    suspend fun getTransportation(transportationId: String): Transportation
    
    /**
     * Delete a transportation
     */
    suspend fun deleteTransportation(transportationId: String)
    
    /**
     * Upload an image for a specific object
     */
    suspend fun uploadImage(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    )
}
