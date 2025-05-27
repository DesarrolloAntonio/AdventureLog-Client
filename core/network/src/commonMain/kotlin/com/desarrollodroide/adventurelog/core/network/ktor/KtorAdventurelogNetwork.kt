package com.desarrollodroide.adventurelog.core.network.ktor

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.UserDetailsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(
    val status: Int,
    val data: LoginData,
    val meta: LoginMeta
)

@Serializable
data class LoginData(
    val user: LoginUserData,
    val methods: List<LoginMethod>
)

@Serializable
data class LoginUserData(
    val id: Int,
    val display: String,
    val has_usable_password: Boolean,
    val email: String,
    val username: String
)

@Serializable
data class LoginMethod(
    val method: String,
    val at: Double,
    val username: String
)

@Serializable
data class LoginMeta(val is_authenticated: Boolean)

class KtorAdventurelogNetwork(
    private val adventurelogClient: HttpClient
) : AdventureLogNetworkDataSource {

    private val logger = Logger.withTag("KtorAdventurelogNetwork")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var sessionToken: String? = null
    private var baseUrl: String? = null

    companion object {
        private const val LOGIN_ENDPOINT = "auth/browser/v1/auth/login"
        private const val ADVENTURES_ENDPOINT = "api/adventures/"
        private const val COLLECTIONS_ENDPOINT = "api/collections/"
        private const val USER_DETAILS_ENDPOINT = "api/user/details/"
    }

    override fun initializeFromSession(serverUrl: String, sessionToken: String?) {
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
            setBody(LoginRequest(username, password))
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
                hasPassword = if (loginResponse.data.user.has_usable_password) "true" else "false",
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

    override suspend fun getAdventures(page: Int, pageSize: Int): List<AdventureDTO> {
        try {
            if (baseUrl == null) {
                logger.e { "Base URL is not initialized, login must be called first" }
                throw IllegalStateException("Base URL is not initialized, login must be called first")
            }

            val url = "$baseUrl/$ADVENTURES_ENDPOINT"
            logger.d { "Fetching adventures from URL: $url" }

            val response = adventurelogClient.get(url) {
                parameter("page", page)
                parameter("page_size", pageSize)
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

            logger.d { "Adventures response status: ${response.status}" }
            logger.d { "Adventures response headers: ${response.headers.entries()}" }

            if (response.status.isSuccess()) {
                val responseText = response.body<String>()
                logger.d { "Adventures raw response: $responseText" }

                try {
                    val adventuresResponse =
                        json.decodeFromString<com.desarrollodroide.adventurelog.core.network.model.AdventuresDTO>(
                            responseText
                        )
                    logger.d { "Parsed adventures: count=${adventuresResponse.count}, results size=${adventuresResponse.results?.size}" }
                    return adventuresResponse.results ?: emptyList()
                } catch (e: Exception) {
                    logger.e(e) { "Error parsing adventures JSON: ${e.message}" }
                    throw e
                }
            } else {
                logger.e { "Failed to fetch adventures with status: ${response.status}" }
                throw HttpException(
                    response.status.value,
                    "Failed to fetch adventures with status: ${response.status}"
                )
            }
        } catch (e: Exception) {
            logger.e(e) { "Exception while fetching adventures: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAdventureDetail(objectId: String): AdventureDTO {
        try {
            if (baseUrl == null) {
                logger.e { "Base URL is not initialized, login must be called first" }
                throw IllegalStateException("Base URL is not initialized, login must be called first")
            }

            val url = "$baseUrl/$ADVENTURES_ENDPOINT$objectId/"
            logger.d { "Fetching adventure detail from URL: $url" }

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

            logger.d { "Adventure detail response status: ${response.status}" }
            logger.d { "Adventure detail response headers: ${response.headers.entries()}" }

            if (response.status.isSuccess()) {
                val responseText = response.body<String>()
                logger.d { "Adventure detail raw response: $responseText" }

                try {
                    val adventure = json.decodeFromString<AdventureDTO>(responseText)
                    logger.d { "Successfully fetched adventure: ${adventure.name}" }
                    return adventure
                } catch (e: Exception) {
                    logger.e(e) { "Error parsing adventure detail JSON: ${e.message}" }
                    throw e
                }
            } else {
                logger.e { "Failed to fetch adventure detail with status: ${response.status}" }
                throw HttpException(
                    response.status.value,
                    "Failed to fetch adventure detail with status: ${response.status}"
                )
            }
        } catch (e: Exception) {
            logger.e(e) { "Exception while fetching adventure detail: ${e.message}" }
            throw e
        }
    }

    override suspend fun getCollections(page: Int, pageSize: Int): List<CollectionDTO> {
        try {
            if (baseUrl == null) {
                logger.e { "Base URL is not initialized, login must be called first" }
                throw IllegalStateException("Base URL is not initialized, login must be called first")
            }

            val url = "$baseUrl/$COLLECTIONS_ENDPOINT"
            logger.d { "Fetching collections from URL: $url" }

            val response = adventurelogClient.get(url) {
                parameter("page", page)
                parameter("page_size", pageSize)
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

            logger.d { "Collections response status: ${response.status}" }
            logger.d { "Collections response headers: ${response.headers.entries()}" }

            if (response.status.isSuccess()) {
                val responseText = response.body<String>()
                logger.d { "Collections raw response: $responseText" }

                try {
                    val collectionsResponse =
                        json.decodeFromString<com.desarrollodroide.adventurelog.core.network.model.CollectionsDTO>(
                            responseText
                        )
                    logger.d { "Parsed collections: count=${collectionsResponse.count}, results size=${collectionsResponse.results?.size}" }
                    return collectionsResponse.results ?: emptyList()
                } catch (e: Exception) {
                    logger.e(e) { "Error parsing collections JSON: ${e.message}" }
                    throw e
                }
            } else {
                logger.e { "Failed to fetch collections with status: ${response.status}" }
                throw HttpException(
                    response.status.value,
                    "Failed to fetch collections with status: ${response.status}"
                )
            }
        } catch (e: Exception) {
            logger.e(e) { "Exception while fetching collections: ${e.message}" }
            throw e
        }
    }

    override suspend fun getCollectionDetail(collectionId: String): CollectionDTO {
        try {
            if (baseUrl == null) {
                logger.e { "Base URL is not initialized, login must be called first" }
                throw IllegalStateException("Base URL is not initialized, login must be called first")
            }

            val url = "$baseUrl/$COLLECTIONS_ENDPOINT$collectionId/"
            logger.d { "Fetching collection detail from URL: $url" }

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

            logger.d { "Collection detail response status: ${response.status}" }
            logger.d { "Collection detail response headers: ${response.headers.entries()}" }

            if (response.status.isSuccess()) {
                val responseText = response.body<String>()
                logger.d { "Collection detail raw response: $responseText" }

                try {
                    val collection = json.decodeFromString<CollectionDTO>(responseText)
                    logger.d { "Successfully fetched collection: ${collection.name}" }
                    return collection
                } catch (e: Exception) {
                    logger.e(e) { "Error parsing collection detail JSON: ${e.message}" }
                    throw e
                }
            } else {
                logger.e { "Failed to fetch collection detail with status: ${response.status}" }
                throw HttpException(
                    response.status.value,
                    "Failed to fetch collection detail with status: ${response.status}"
                )
            }
        } catch (e: Exception) {
            logger.e(e) { "Exception while fetching collection detail: ${e.message}" }
            throw e
        }
    }
}

class HttpException(val code: Int, override val message: String) : Exception(message)
