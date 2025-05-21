package com.desarrollodroide.adventurelog.core.network.ktor

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.AdventureDTO
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
    
    // Create Json format once to avoid redundant creation
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    // Store auth tokens for authenticated requests
    private var sessionToken: String? = null
    private var csrfToken: String? = null
    private var baseUrl: String? = null
    
    companion object {
        private const val LOGIN_ENDPOINT = "auth/browser/v1/auth/login"
        private const val ADVENTURES_ENDPOINT = "api/adventures/"
        private const val USER_DETAILS_ENDPOINT = "api/user/details/"
    }

    override suspend fun sendLogin(url: String, username: String, password: String): UserDetailsDTO {
        // Normalize URL by removing trailing slash
        baseUrl = url.trimEnd('/')
        val loginUrl = "$baseUrl/$LOGIN_ENDPOINT"
        logger.d { "Login URL: $loginUrl" }
        
        val response = adventurelogClient.post(loginUrl) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-Is-Mobile", "true")
                append("Referer", baseUrl?:"")
            }
            setBody(LoginRequest(username, password))
        }
        
        logger.d { "Login response status: ${response.status}" }
        logger.d { "Login response headers: ${response.headers.entries()}" }
        
        if (response.status.isSuccess()) {
            // Extract both sessionid and csrftoken from cookies
            val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
            logger.d { "Cookies from login response: $cookies" }
            
            // Try to find sessionid and csrftoken cookies
            for (cookie in cookies) {
                // Extract sessionid
                if (cookie.contains("sessionid=")) {
                    val sessionidPattern = Regex("sessionid=([^;]+)")
                    val matchResult = sessionidPattern.find(cookie)
                    sessionToken = matchResult?.groupValues?.get(1)
                    logger.d { "Extracted sessionId from cookies: $sessionToken" }
                }
                
                // Extract csrftoken
                if (cookie.contains("csrftoken=")) {
                    val csrfPattern = Regex("csrftoken=([^;]+)")
                    val matchResult = csrfPattern.find(cookie)
                    csrfToken = matchResult?.groupValues?.get(1)
                    logger.d { "Extracted csrfToken from cookies: $csrfToken" }
                }
            }
            
            if (sessionToken == null && csrfToken == null) {
                logger.w { "No authentication tokens found in cookies" }
            }
            
            // Log complete response body for debugging
            val responseBody = response.body<String>()
            logger.d { "Login response body: $responseBody" }
            
            // Parse the response using the shared Json instance
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
            // Log error response if available
            try {
                val errorBody = response.body<String>()
                logger.e { "Login failed with status: ${response.status}. Error body: $errorBody" }
            } catch (e: Exception) {
                logger.e { "Login failed with status: ${response.status}. Could not read error body." }
            }
            
            throw HttpException(response.status.value, "Login failed with status: ${response.status}")
        }
    }

    override suspend fun getUserDetails(csrfTokenParam: String): UserDetailsDTO {
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
                    
                    // Add X-CSRFToken for authentication (from screenshot this is what works)
                    // First check our stored csrfToken
                    if (csrfToken != null) {
                        append("X-CSRFToken", csrfToken!!)
                        logger.d { "Using stored X-CSRFToken for authentication: $csrfToken" }
                    }
                    
                    // Also try X-Session-Token as backup
                    sessionToken?.let { token ->
                        append("X-Session-Token", token)
                        logger.d { "Using X-Session-Token for authentication: $token" }
                    }
                    
                    if (csrfToken == null && csrfTokenParam.isBlank() && sessionToken == null) {
                        logger.w { "No authentication tokens available for request" }
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
                throw HttpException(response.status.value, "Failed to fetch user details with status: ${response.status}")
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

                    // Also try X-Session-Token as backup
                    sessionToken?.let { token ->
                        append("X-Session-Token", token)
                        logger.d { "Using X-Session-Token for authentication: $token" }
                    }
                    
                    if (csrfToken == null && sessionToken == null) {
                        logger.w { "No authentication tokens available for request" }
                    }
                }
            }
            
            logger.d { "Adventures response status: ${response.status}" }
            logger.d { "Adventures response headers: ${response.headers.entries()}" }
            
            if (response.status.isSuccess()) {
                val responseText = response.body<String>()
                logger.d { "Adventures raw response: $responseText" }
                
                try {
                    val adventuresResponse = json.decodeFromString<com.desarrollodroide.adventurelog.core.network.model.AdventuresDTO>(responseText)
                    logger.d { "Parsed adventures: count=${adventuresResponse.count}, results size=${adventuresResponse.results?.size}" }
                    return adventuresResponse.results ?: emptyList()
                } catch (e: Exception) {
                    logger.e(e) { "Error parsing adventures JSON: ${e.message}" }
                    throw e
                }
            } else {
                logger.e { "Failed to fetch adventures with status: ${response.status}" }
                throw HttpException(response.status.value, "Failed to fetch adventures with status: ${response.status}")
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
                    
                    // Add X-CSRFToken for authentication (from screenshot this is what works)
                    csrfToken?.let { token ->
                        append("X-CSRFToken", token)
                        logger.d { "Using X-CSRFToken for authentication: $token" }
                    }
                    
                    // Also try X-Session-Token as backup
                    sessionToken?.let { token ->
                        append("X-Session-Token", token)
                        logger.d { "Using X-Session-Token for authentication: $token" }
                    }
                    
                    if (csrfToken == null && sessionToken == null) {
                        logger.w { "No authentication tokens available for request" }
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
                throw HttpException(response.status.value, "Failed to fetch adventure detail with status: ${response.status}")
            }
        } catch (e: Exception) {
            logger.e(e) { "Exception while fetching adventure detail: ${e.message}" }
            throw e
        }
    }
}

class HttpException(val code: Int, override val message: String) : Exception(message)