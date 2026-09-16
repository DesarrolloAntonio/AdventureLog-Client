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
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionInviteDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.EmailAddressDTO
import com.desarrollodroide.adventurelog.core.network.model.response.MediaUsageDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CalendarEventsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.SearchResultsDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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
     * Asks the server to end the current session, in the background: a sign-out must not wait on a
     * server that may not answer. Sign-out used to leave the token valid on the server (measured:
     * it still answered 200 after the app had signed out).
     */
    fun endServerSession()

    /**
     * Fires every time the server answers **401** to a request that carried the session token:
     * the session is over, whatever screen asked.
     *
     * A 403 is not in it - that is a refusal of one object (a collection shared read-only), not of
     * the session - and neither is a 401 to a request with no token, such as a failed login.
     */
    val sessionRejections: Flow<Unit>
        get() = emptyFlow()

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
        activityTypes: List<String> = emptyList(),
        /** Collections the new place joins straight away, so no second call is needed. */
        collectionIds: List<String> = emptyList()
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
     * Update the writable half of the user's profile - see
     * [com.desarrollodroide.adventurelog.core.network.api.UserApi.updateUserProfile].
     */
    suspend fun updateUserProfile(
        username: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        publicProfile: Boolean? = null,
        measurementSystem: String? = null,
        defaultCurrency: String? = null,
        mapStyle: String? = null
    ): UserDetailsDTO

    suspend fun changePassword(currentPassword: String, newPassword: String): Boolean

    suspend fun getMediaUsage(): MediaUsageDTO

    suspend fun getEmailAddresses(): List<EmailAddressDTO>

    suspend fun addEmailAddress(email: String)

    suspend fun requestEmailVerification(email: String)

    suspend fun setPrimaryEmailAddress(email: String)

    suspend fun removeEmailAddress(email: String)

    /**
     * Get everything the home screen shows in a single request.
     */
    suspend fun getPublicUsers(): List<UserDetailsDTO>

    suspend fun getDashboard(): DashboardDTO

    suspend fun getCalendarEvents(start: String? = null, end: String? = null): CalendarEventsDTO

    suspend fun globalSearch(query: String, limit: Int = 20): SearchResultsDTO

    suspend fun shareCollection(collectionId: String, userUuid: String)

    suspend fun unshareCollection(collectionId: String, userUuid: String)

    suspend fun revokeInvite(collectionId: String, userUuid: String)

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

    suspend fun getArchivedCollections(): List<UltraSlimCollectionDTO>

    suspend fun getSharedCollections(): List<UltraSlimCollectionDTO>

    suspend fun getCollectionInvites(): List<CollectionInviteDTO>

    suspend fun acceptCollectionInvite(collectionId: String)

    suspend fun declineCollectionInvite(collectionId: String)

    /**
     * Delete an adventure
     */
    suspend fun deleteAdventure(adventureId: String)

    /** PATCH with `collections` alone. */
    suspend fun updateLocationCollections(locationId: String, collections: List<String>): LocationDTO
    
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
        collections: List<String>? = null,
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
     * Create a note in a collection
     */
    suspend fun createNote(
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): com.desarrollodroide.adventurelog.core.model.Note

    /**
     * Update a note
     */
    suspend fun updateNote(
        noteId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): com.desarrollodroide.adventurelog.core.model.Note

    /**
     * Delete a note
     */
    suspend fun deleteNote(noteId: String)

    /**
     * Create lodging in a collection
     */
    suspend fun createLodging(
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean,
        collectionId: String
    ): com.desarrollodroide.adventurelog.core.model.Lodging

    /**
     * Update lodging
     */
    suspend fun updateLodging(
        lodgingId: String,
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean
    ): com.desarrollodroide.adventurelog.core.model.Lodging

    /**
     * Delete lodging
     */
    suspend fun deleteLodging(lodgingId: String)

    /**
     * Build a whole itinerary from the dates already on a collection's records. The server only
     * allows it on a collection whose itinerary is empty.
     */
    suspend fun autoGenerateItinerary(
        collectionId: String
    ): List<com.desarrollodroide.adventurelog.core.model.ItineraryEntry>

    /** Place one of a collection's items on a day, or in the trip-context bucket when null. */
    suspend fun addItineraryEntry(
        collectionId: String,
        kind: com.desarrollodroide.adventurelog.core.model.ItineraryItemKind,
        itemId: String,
        date: String?,
        order: Int
    ): com.desarrollodroide.adventurelog.core.model.ItineraryEntry

    /** Take an entry off its day, leaving the item itself alone. */
    suspend fun deleteItineraryEntry(entryId: String)

    /**
     * Places near a point that are not in the account yet. Either the coordinates or [place] must
     * be given; the server geocodes the latter.
     */
    suspend fun getRecommendations(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: com.desarrollodroide.adventurelog.core.model.RecommendationCategory,
        radiusMetres: Int
    ): List<com.desarrollodroide.adventurelog.core.model.Recommendation>

    /**
     * Create a checklist in a collection
     */
    suspend fun createChecklist(
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): com.desarrollodroide.adventurelog.core.model.Checklist

    /**
     * Update a checklist, items included
     */
    suspend fun updateChecklist(
        checklistId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): com.desarrollodroide.adventurelog.core.model.Checklist

    /**
     * Delete a checklist
     */
    suspend fun deleteChecklist(checklistId: String)

    /**
     * Sweep every location and mark the regions and cities they fall in
     */
    suspend fun refreshVisitedRegions(): Pair<Int, Int>

    /**
     * Mark a region as visited
     */
    suspend fun markRegionVisited(regionId: String): VisitedRegionDTO

    /**
     * Remove a visited-region record, keyed by the region's code
     */
    suspend fun unmarkRegionVisited(regionId: String)
    
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

    suspend fun deleteImage(imageId: String)

    suspend fun setPrimaryImage(imageId: String)
}
