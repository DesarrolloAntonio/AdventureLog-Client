package com.desarrollodroide.adventurelog.core.network.ktor

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import io.ktor.client.HttpClient
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.api.AdventureApi
import com.desarrollodroide.adventurelog.core.network.api.CollectionApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorAdventureNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorCollectionApi
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import com.desarrollodroide.adventurelog.core.network.api.CategoryApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorCategoryNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.api.UserApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorUserApi
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO
import com.desarrollodroide.adventurelog.core.network.api.ContentApi
import com.desarrollodroide.adventurelog.core.network.api.CountriesApi
import com.desarrollodroide.adventurelog.core.network.api.GeocodingApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorContentApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorCountriesApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorGeocodingApi
import com.desarrollodroide.adventurelog.core.network.api.AuthApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorAuthApi

class KtorAdventurelogNetwork(
    private val adventurelogClient: HttpClient
) : AdventureLogNetworkDataSource {

    private val logger = Logger.withTag("KtorAdventurelogNetwork")

    private val json = defaultJson

    private var sessionToken: String? = null
    private var baseUrl: String? = null
    
    private val authDataSource: AuthApi by lazy {
        KtorAuthApi(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) },
            onSessionTokenReceived = { token ->
                sessionToken = token
            },
            json = json
        )
    }
    
    private val adventureDataSource: AdventureApi by lazy {
        KtorAdventureNetworkDataSource(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) },
            json = json
        )
    }
    
    private val collectionDataSource: CollectionApi by lazy {
        KtorCollectionApi(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) },
            json = json
        )
    }
    
    private val categoryDataSource: CategoryApi by lazy {
        KtorCategoryNetworkDataSource(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) },
            json = json
        )
    }
    
    private val userDataSource: UserApi by lazy {
        KtorUserApi(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) },
            json = json
        )
    }
    
    private val countriesDataSource: CountriesApi by lazy {
        KtorCountriesApi(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) }
        )
    }
    
    private val geocodingDataSource: GeocodingApi by lazy {
        KtorGeocodingApi(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) }
        )
    }
    
    private val contentDataSource: ContentApi by lazy {
        KtorContentApi(
            httpClient = adventurelogClient,
            sessionProvider = { SessionInfo(baseUrl ?: "", sessionToken) }
        )
    }

    companion object {
        // Constants moved to respective DataSource implementations
    }

    override fun initializeFromSession(
        serverUrl: String,
        sessionToken: String?
    ) {
        this.baseUrl = serverUrl.trimEnd('/')
        this.sessionToken = sessionToken
        logger.d {
            "Network initialized from existing session - BaseURL: ${this.baseUrl}, SessionToken: ${
                sessionToken?.take(
                    10
                )
            }..."
        }
    }

    override fun clearSession() {
        sessionToken = null
        baseUrl = null
        logger.d { "Network session cleared - all tokens and base URL reset" }
    }

    override suspend fun sendLogin(
        url: String,
        username: String,
        password: String
    ): UserDetailsDTO {
        baseUrl = url.trimEnd('/')
        // Initialize the session info before calling login
        // The authDataSource will use the sessionProvider to get the baseUrl
        
        val userDetails = authDataSource.login(username, password)
        
        // The session token is already updated via the onSessionTokenReceived callback
        logger.d { "Login completed successfully for user: ${userDetails.username}" }
        
        return userDetails
    }

    override suspend fun getUserDetails(): UserDetailsDTO {
        ensureInitialized()
        return userDataSource.getUserDetails()
    }

    override suspend fun getAdventures(
        page: Int,
        pageSize: Int
    ): List<AdventureDTO> {
        ensureInitialized()
        return adventureDataSource.getAdventures(page, pageSize)
    }

    override suspend fun getAdventuresFiltered(
        page: Int,
        pageSize: Int,
        categoryIds: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): List<AdventureDTO> {
        ensureInitialized()
        return adventureDataSource.getAdventuresFiltered(
            page = page,
            pageSize = pageSize,
            categoryIds = categoryIds,
            sortBy = sortBy,
            sortOrder = sortOrder,
            isVisited = isVisited,
            searchQuery = searchQuery,
            includeCollections = includeCollections
        )
    }

    override suspend fun getAdventureDetail(
        objectId: String
    ): AdventureDTO {
        ensureInitialized()
        return adventureDataSource.getAdventureDetail(objectId)
    }

    override suspend fun getCollections(
        page: Int,
        pageSize: Int
    ): List<CollectionDTO> {
        ensureInitialized()
        return collectionDataSource.getCollections(page, pageSize)
    }

    override suspend fun getCollectionDetail(
        collectionId: String
    ): CollectionDTO {
        ensureInitialized()
        return collectionDataSource.getCollectionDetail(collectionId)
    }

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
        activityTypes: List<String>
    ): AdventureDTO {
        ensureInitialized()
        return adventureDataSource.createAdventure(
            name = name,
            description = description,
            category = category,
            rating = rating,
            link = link,
            location = location,
            latitude = latitude,
            longitude = longitude,
            isPublic = isPublic,
            visits = visits,
            activityTypes = activityTypes
        )
    }
    
    private fun ensureInitialized() {
        if (baseUrl == null) {
            logger.e { "Base URL is not initialized, login must be called first" }
            throw IllegalStateException("Base URL is not initialized, login must be called first")
        }
    }

    override suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): CollectionDTO {
        ensureInitialized()
        return collectionDataSource.createCollection(
            name = name,
            description = description,
            isPublic = isPublic,
            startDate = startDate,
            endDate = endDate
        )
    }

    override suspend fun getCategories(): List<CategoryDTO> {
        ensureInitialized()
        return categoryDataSource.getCategories()
    }

    override suspend fun generateDescription(
        name: String
    ): String {
        ensureInitialized()
        return contentDataSource.generateDescription(name)
    }
    
    override suspend fun searchLocations(
        query: String
    ): List<GeocodeSearchResultDTO> {
        ensureInitialized()
        return geocodingDataSource.searchLocations(query)
    }
    
    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): ReverseGeocodeResultDTO {
        ensureInitialized()
        return geocodingDataSource.reverseGeocode(latitude, longitude)
    }
    
    override suspend fun getUserStats(
        username: String
    ): UserStatsDTO {
        ensureInitialized()
        return userDataSource.getUserStats(username)
    }
    
    override suspend fun deleteAdventure(adventureId: String) {
        ensureInitialized()
        return adventureDataSource.deleteAdventure(adventureId)
    }
    
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
        tags: List<String>
    ): AdventureDTO {
        ensureInitialized()
        return adventureDataSource.updateAdventure(
            adventureId = adventureId,
            name = name,
            description = description,
            category = category,
            rating = rating,
            link = link,
            location = location,
            latitude = latitude,
            longitude = longitude,
            isPublic = isPublic,
            tags = tags
        )
    }
    
    override suspend fun deleteCollection(collectionId: String) {
        ensureInitialized()
        return collectionDataSource.deleteCollection(collectionId)
    }

    override suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): CollectionDTO {
        ensureInitialized()
        return collectionDataSource.updateCollection(
            collectionId = collectionId,
            name = name,
            description = description,
            isPublic = isPublic,
            startDate = startDate,
            endDate = endDate,
            link = link
        )
    }
    
    override suspend fun getCountries(): List<CountryDTO> {
        ensureInitialized()
        return countriesDataSource.getCountries()
    }
    
    override suspend fun getRegions(countryCode: String): List<RegionDTO> {
        ensureInitialized()
        return countriesDataSource.getRegions(countryCode)
    }
    
    override suspend fun getVisitedRegions(): List<VisitedRegionDTO> {
        ensureInitialized()
        return countriesDataSource.getVisitedRegions()
    }
    
    override suspend fun getVisitedCities(): List<VisitedCityDTO> {
        ensureInitialized()
        return countriesDataSource.getVisitedCities()
    }
}
