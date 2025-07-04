package com.desarrollodroide.adventurelog.core.network.ktor

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.request.LoginRequest
import com.desarrollodroide.adventurelog.core.network.model.request.LoginResponse
import com.desarrollodroide.adventurelog.core.network.api.AdventureApi
import com.desarrollodroide.adventurelog.core.network.api.CollectionApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorAdventureNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorCollectionApi
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import com.desarrollodroide.adventurelog.core.network.api.CategoryApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorCategoryNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.response.WikipediaDescriptionResponse
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.api.UserApi
import com.desarrollodroide.adventurelog.core.network.ktor.api.KtorUserApi
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO

class KtorAdventurelogNetwork(
    private val adventurelogClient: HttpClient
) : AdventureLogNetworkDataSource {

    private val logger = Logger.withTag("KtorAdventurelogNetwork")

    private val json = defaultJson

    private var sessionToken: String? = null
    private var baseUrl: String? = null
    
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

    companion object {
        private const val LOGIN_ENDPOINT = "auth/browser/v1/auth/login"
        private const val USER_DETAILS_ENDPOINT = "api/user/details/"
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
        val loginUrl = "$baseUrl/$LOGIN_ENDPOINT"
        logger.d { "Login URL: $loginUrl" }

        val response = adventurelogClient.post(loginUrl) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-Is-Mobile", "true")
                append("Referer", baseUrl ?: "")
            }
            setBody(LoginRequest(
                username = username,
                password = password
            ))
        }

        logger.d { "Login response status: ${response.status}" }
        logger.d { "Login response headers: ${response.headers.entries()}" }

        if (response.status.isSuccess()) {
            val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
            logger.d { "Cookies from login response: $cookies" }

            for (cookie in cookies) {
                if (cookie.contains("sessionid=")) {
                    val sessionidPattern = Regex("sessionid=([^;]+)")
                    val matchResult = sessionidPattern.find(cookie)
                    sessionToken = matchResult?.groupValues?.get(1)
                    logger.d { "Extracted sessionId from cookies: $sessionToken" }
                }
            }

            if (sessionToken == null) {
                logger.w { "No session token found in cookies" }
            }

            val responseBody = response.body<String>()
            logger.d { "Login response body: $responseBody" }

            val loginResponse = json.decodeFromString<LoginResponse>(responseBody)

            logger.d { "Login successful for user: ${loginResponse.data.user.username}" }

            return UserDetailsDTO(
                id = loginResponse.data.user.id,
                username = loginResponse.data.user.username,
                email = loginResponse.data.user.email,
                hasPassword = if (loginResponse.data.user.hasUsablePassword) "true" else "false",
                sessionToken = sessionToken
            )
        } else {
            try {
                val errorBody = response.body<String>()
                logger.e { "Login failed with status: ${response.status}. Error body: $errorBody" }
            } catch (e: Exception) {
                logger.e { "Login failed with status: ${response.status}. Could not read error body." }
            }

            throw HttpException(
                response.status.value,
                "Login failed with status: ${response.status}"
            )
        }
    }

    override suspend fun getUserDetails(): UserDetailsDTO {
        try {
            if (baseUrl == null) {
                logger.e { "Base URL is not initialized, login must be called first" }
                throw IllegalStateException("Base URL is not initialized, login must be called first")
            }

            val url = "$baseUrl/$USER_DETAILS_ENDPOINT"
            logger.d { "Fetching user details from URL: $url" }

            val response = adventurelogClient.get(url) {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                    append("X-Is-Mobile", "true")

                    sessionToken?.let { token ->
                        append("X-Session-Token", token)
                        logger.d { "Using X-Session-Token for authentication: $token" }
                    }

                    if (sessionToken == null) {
                        logger.w { "No authentication token available for request" }
                    }
                }
            }

            logger.d { "User details response status: ${response.status}" }
            logger.d { "User details response headers: ${response.headers.entries()}" }

            if (response.status.isSuccess()) {
                val userDetails = response.body<UserDetailsDTO>()
                logger.d { "Successfully fetched user details: ${userDetails.username}" }
                return userDetails
            } else {
                logger.e { "Failed to fetch user details with status: ${response.status}" }
                throw HttpException(
                    response.status.value,
                    "Failed to fetch user details with status: ${response.status}"
                )
            }
        } catch (e: Exception) {
            logger.e(e) { "Exception while fetching user details: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAdventures(
        page: Int,
        pageSize: Int
    ): List<AdventureDTO> {
        ensureInitialized()
        return adventureDataSource.getAdventures(page, pageSize)
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
        visitDates: Visit?
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
            visitDates = visitDates
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
        
        val url = "$baseUrl/api/generate/desc/"
        logger.d { "Generating description for: $name" }
        
        val response = adventurelogClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
            url {
                parameters.append("name", name)
            }
        }
        
        val wikipediaResponse = response.body<WikipediaDescriptionResponse>()
        return wikipediaResponse.extract ?: throw Exception("No description found")
    }
    
    override suspend fun searchLocations(
        query: String
    ): List<GeocodeSearchResultDTO> {
        ensureInitialized()
        
        val url = "$baseUrl/api/reverse-geocode/search/"
        logger.d { "Searching locations for query: $query" }
        
        val response = adventurelogClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
            url {
                parameters.append("query", query)
            }
        }
        
        if (response.status.isSuccess()) {
            return response.body<List<GeocodeSearchResultDTO>>()
        } else {
            logger.e { "Failed to search locations with status: ${response.status}" }
            return emptyList()
        }
    }
    
    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): ReverseGeocodeResultDTO {
        ensureInitialized()
        
        val url = "$baseUrl/api/reverse-geocode/reverse_geocode/"
        logger.d { "Reverse geocoding for lat: $latitude, lon: $longitude" }
        
        val response = adventurelogClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
            url {
                parameters.append("lat", latitude.toString())
                parameters.append("lon", longitude.toString())
            }
        }
        
        if (response.status.isSuccess()) {
            return response.body<ReverseGeocodeResultDTO>()
        } else {
            logger.e { "Failed to reverse geocode with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to reverse geocode with status: ${response.status}"
            )
        }
    }
    
    override suspend fun getUserStats(
        username: String
    ): UserStatsDTO {
        ensureInitialized()
        return userDataSource.getUserStats(username)
    }
}
