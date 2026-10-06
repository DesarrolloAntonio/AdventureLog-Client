package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
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
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.network.datasource.*

/**
 * Every call the network makes available, refusing by default.
 *
 * Seventy-nine of them: spelling them all out to say a test cares about two is what made these
 * fakes break every time the interface grew.
 */
abstract class AdventureLogNetworkStub : AdventureLogNetwork {
    override suspend fun getAdventures(page: Int, pageSize: Int): List<LocationDTO> = unused()
    override suspend fun getAdventuresFiltered(
        page: Int,
        pageSize: Int,
        categoryIds: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): List<LocationDTO> = unused()
    override suspend fun getAdventureDetail(objectId: String): LocationDTO = unused()
    override suspend fun getCollections(page: Int, pageSize: Int): List<UltraSlimCollectionDTO> = unused()
    override suspend fun getAllCollections(): List<UltraSlimCollectionDTO> = unused()
    override suspend fun getCollectionDetail(collectionId: String): CollectionDTO = unused()
    override suspend fun sendLogin(
        url: String,
        username: String,
        password: String
    ): UserDetailsDTO = unused()
    override suspend fun getUserDetails(): UserDetailsDTO = unused()
    override fun initializeFromSession(serverUrl: String, sessionToken: String?): Unit = unused()
    override fun clearSession(): Unit = unused()
    override fun endServerSession(): Unit = unused()
    override suspend fun createAdventure(
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
        activityTypes: List<String>,
        collectionIds: List<String>
    ): LocationDTO = unused()
    override suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): CollectionDTO = unused()
    override suspend fun getCategories(): List<CategoryDTO> = unused()
    override suspend fun getCategoryById(categoryId: String): CategoryDTO = unused()
    override suspend fun createCategory(
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO = unused()
    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO = unused()
    override suspend fun deleteCategory(categoryId: String): Unit = unused()
    override suspend fun generateDescription(name: String): String = unused()
    override suspend fun searchLocations(query: String): List<GeocodeSearchResultDTO> = unused()
    override suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResultDTO = unused()
    override suspend fun getUserStats(username: String): UserStatsDTO = unused()
    override suspend fun updateUserProfile(
        username: String?,
        firstName: String?,
        lastName: String?,
        publicProfile: Boolean?,
        measurementSystem: String?,
        defaultCurrency: String?,
        mapStyle: String?
    ): UserDetailsDTO = unused()
    override suspend fun changePassword(currentPassword: String, newPassword: String): Boolean = unused()
    override suspend fun getMediaUsage(): MediaUsageDTO = unused()
    override suspend fun getEmailAddresses(): List<EmailAddressDTO> = unused()
    override suspend fun addEmailAddress(email: String): Unit = unused()
    override suspend fun requestEmailVerification(email: String): Unit = unused()
    override suspend fun setPrimaryEmailAddress(email: String): Unit = unused()
    override suspend fun removeEmailAddress(email: String): Unit = unused()
    override suspend fun getPublicUsers(): List<UserDetailsDTO> = unused()
    override suspend fun getDashboard(): DashboardDTO = unused()
    override suspend fun getCalendarEvents(start: String?, end: String?): CalendarEventsDTO = unused()
    override suspend fun globalSearch(query: String, limit: Int): SearchResultsDTO = unused()
    override suspend fun shareCollection(collectionId: String, userUuid: String): Unit = unused()
    override suspend fun unshareCollection(collectionId: String, userUuid: String): Unit = unused()
    override suspend fun revokeInvite(collectionId: String, userUuid: String): Unit = unused()
    override suspend fun createVisit(locationId: String, visit: VisitFormData): VisitDTO = unused()
    override suspend fun updateVisit(
        visitId: String,
        locationId: String,
        visit: VisitFormData
    ): VisitDTO = unused()
    override suspend fun deleteVisit(visitId: String): Unit = unused()
    override suspend fun createTrail(locationId: String, trail: TrailFormData): TrailDTO = unused()
    override suspend fun updateTrail(
        trailId: String,
        locationId: String,
        trail: TrailFormData
    ): TrailDTO = unused()
    override suspend fun deleteTrail(trailId: String): Unit = unused()
    override suspend fun duplicateLocation(locationId: String): LocationDTO = unused()
    override suspend fun getShareImage(locationId: String, aspect: String): ByteArray = unused()
    override suspend fun duplicateCollection(collectionId: String): CollectionDTO = unused()
    override suspend fun setCollectionArchived(collectionId: String, archived: Boolean): CollectionDTO = unused()
    override suspend fun getCollectionShareImage(collectionId: String, aspect: String): ByteArray = unused()
    override suspend fun exportCollectionPdf(collectionId: String): ByteArray = unused()
    override suspend fun exportCollectionZip(collectionId: String): ByteArray = unused()
    override suspend fun getArchivedCollections(): List<UltraSlimCollectionDTO> = unused()
    override suspend fun getSharedCollections(): List<UltraSlimCollectionDTO> = unused()
    override suspend fun getCollectionInvites(): List<CollectionInviteDTO> = unused()
    override suspend fun acceptCollectionInvite(collectionId: String): Unit = unused()
    override suspend fun declineCollectionInvite(collectionId: String): Unit = unused()
    override suspend fun deleteAdventure(adventureId: String): Unit = unused()
    override suspend fun updateAdventure(
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
        collections: List<String>?,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?
    ): LocationDTO = unused()
    override suspend fun deleteCollection(collectionId: String): Unit = unused()
    override suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): CollectionDTO = unused()
    override suspend fun getCountries(): List<CountryDTO> = unused()
    override suspend fun getRegions(countryCode: String): List<RegionDTO> = unused()
    override suspend fun getVisitedRegions(): List<VisitedRegionDTO> = unused()
    override suspend fun getVisitedCities(): List<VisitedCityDTO> = unused()
    override suspend fun createNote(
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): com.desarrollodroide.adventurelog.core.model.Note = unused()
    override suspend fun updateNote(
        noteId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): com.desarrollodroide.adventurelog.core.model.Note = unused()
    override suspend fun deleteNote(noteId: String): Unit = unused()
    override suspend fun createLodging(
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
    ): com.desarrollodroide.adventurelog.core.model.Lodging = unused()
    override suspend fun updateLodging(
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
    ): com.desarrollodroide.adventurelog.core.model.Lodging = unused()
    override suspend fun deleteLodging(lodgingId: String): Unit = unused()
    override suspend fun autoGenerateItinerary(collectionId: String): List<ItineraryEntry> = unused()
    override suspend fun addItineraryEntry(
        collectionId: String,
        kind: ItineraryItemKind,
        itemId: String,
        date: String?,
        order: Int
    ): ItineraryEntry = unused()
    override suspend fun deleteItineraryEntry(entryId: String): Unit = unused()
    override suspend fun getRecommendations(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: RecommendationCategory,
        radiusMetres: Int
    ): List<Recommendation> = unused()
    override suspend fun createChecklist(
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): com.desarrollodroide.adventurelog.core.model.Checklist = unused()
    override suspend fun updateChecklist(
        checklistId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): com.desarrollodroide.adventurelog.core.model.Checklist = unused()
    override suspend fun deleteChecklist(checklistId: String): Unit = unused()
    override suspend fun refreshVisitedRegions(): Pair<Int, Int> = unused()
    override suspend fun markRegionVisited(regionId: String): VisitedRegionDTO = unused()
    override suspend fun unmarkRegionVisited(regionId: String): Unit = unused()
    override suspend fun createTransportation(
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
        collectionId: String?
    ): Transportation = unused()
    override suspend fun updateTransportation(
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
        collectionId: String?
    ): Transportation = unused()
    override suspend fun getTransportation(transportationId: String): Transportation = unused()
    override suspend fun deleteTransportation(transportationId: String): Unit = unused()
    override suspend fun uploadImage(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    ): Unit = unused()
    override suspend fun deleteImage(imageId: String): Unit = unused()
    override suspend fun setPrimaryImage(imageId: String): Unit = unused()
    override suspend fun updateLocationCollections(locationId: String, collections: List<String>): LocationDTO = unused()
}
