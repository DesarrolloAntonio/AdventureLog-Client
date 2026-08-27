package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.mappers.toUserStats
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("UserRepositoryImpl")

/**
 * Implementation of UserRepository using MultiPlatform-Settings
 */
class UserRepositoryImpl(
    private val settings: Settings,
    private val networkDataSource: AdventureLogNetwork
) : UserRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // StateFlows to observe changes
    private val rememberMeFlow = MutableStateFlow<Account?>(null)
    private val userSessionFlow = MutableStateFlow<UserDetails?>(null)
    private val userStatsFlow = MutableStateFlow<UserStats?>(null)

    // Keys for storing user data
    private object Keys {
        // Remember Me keys
        const val REMEMBER_USER_ID = "remember_user_id"
        const val REMEMBER_USERNAME = "remember_username"
        const val REMEMBER_PASSWORD = "remember_password"
        const val REMEMBER_URL = "remember_url"

        // User Session key (stored as JSON)
        const val USER_SESSION = "user_session"
    }

    init {
        // Initialize state flows with stored data
        loadInitialData()
    }

    private fun loadInitialData() {
        if (hasKey(Keys.REMEMBER_USERNAME)) {
            rememberMeFlow.value = Account(
                id = settings.getInt(Keys.REMEMBER_USER_ID, -1),
                userName = settings.getString(Keys.REMEMBER_USERNAME, ""),
                password = settings.getString(Keys.REMEMBER_PASSWORD, ""),
                serverUrl = settings.getString(Keys.REMEMBER_URL, "")
            )
        }

        // Load user session if it exists
        try {
            val userSessionJson = settings.getStringOrNull(Keys.USER_SESSION)
            if (userSessionJson != null) {
                val userDetails = json.decodeFromString<UserDetails>(userSessionJson)

                if (userDetails.serverUrl?.isBlank() == true) {
                    val rememberUrl = settings.getStringOrNull(Keys.REMEMBER_URL) ?: ""
                    if (rememberUrl.isNotBlank()) {
                        val updatedUserDetails = userDetails.copy(serverUrl = rememberUrl)
                        userSessionFlow.value = updatedUserDetails
                        val updatedJson = json.encodeToString(UserDetails.serializer(), updatedUserDetails)
                        settings.putString(Keys.USER_SESSION, updatedJson)
                    } else {
                        userSessionFlow.value = userDetails
                    }
                } else {
                    userSessionFlow.value = userDetails
                }
            }
        } catch (e: Exception) {
            logger.e { "Error deserializing user session: ${e.message}" }
            settings.remove(Keys.USER_SESSION)
        }
    }

    /**
     * Helper method to check if a key exists in settings
     */
    private fun hasKey(key: String): Boolean {
        return try {
            settings.getString(key, "").isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun saveRememberMeCredentials(
        url: String,
        username: String,
        password: String
    ) {
        val id = settings.getInt(Keys.REMEMBER_USER_ID, 1)

        settings.putInt(Keys.REMEMBER_USER_ID, id)
        settings.putString(Keys.REMEMBER_USERNAME, username)
        settings.putString(Keys.REMEMBER_PASSWORD, password)
        settings.putString(Keys.REMEMBER_URL, url)

        rememberMeFlow.value = Account(
            id = id,
            userName = username,
            password = password,
            serverUrl = url
        )
    }

    override fun getRememberMeCredentials(): Flow<Account?> {
        return rememberMeFlow.asStateFlow()
    }

    override suspend fun clearRememberMeCredentials() {
        settings.remove(Keys.REMEMBER_USER_ID)
        settings.remove(Keys.REMEMBER_USERNAME)
        settings.remove(Keys.REMEMBER_PASSWORD)
        settings.remove(Keys.REMEMBER_URL)

        rememberMeFlow.value = null
    }

    override suspend fun saveUserSession(userDetails: UserDetails) {
        try {
            val userSessionJson = json.encodeToString(UserDetails.serializer(), userDetails)
            settings.putString(Keys.USER_SESSION, userSessionJson)
        } catch (e: Exception) {
            logger.e { "Error saving user session: ${e.message}" }
        }
        // Publish regardless of whether persistence succeeded - the running app still needs a
        // session, and a failed disk write should not log the user out of the current run.
        setActiveSession(userDetails)
    }

    override fun setActiveSession(userDetails: UserDetails) {
        userSessionFlow.value = userDetails
    }

    override fun getUserSession(): Flow<UserDetails?> {
        return userSessionFlow.asStateFlow()
    }

    override suspend fun getUserSessionOnce(): UserDetails? {
        return userSessionFlow.value
    }

    override val activeSession: UserDetails?
        get() = userSessionFlow.value

    override suspend fun clearUserSession() {
        settings.remove(Keys.USER_SESSION)
        userSessionFlow.value = null
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return userSessionFlow.map { it != null }
    }

    override suspend fun clearAllUserData() {
        clearRememberMeCredentials()
        clearUserSession()
    }
    
    override suspend fun getUserStats(username: String): Either<ApiResponse, UserStats> {
        return try {
            val statsDTO = networkDataSource.getUserStats(username)
            val stats = statsDTO.toUserStats()
            // Cache the stats in the flow
            userStatsFlow.value = stats
            Either.Right(stats)
        } catch (e: HttpException) {
            logger.e { "HTTP Error getting user stats: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error getting user stats: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error getting user stats: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override fun getUserStatsFlow(): Flow<UserStats?> {
        return userStatsFlow.asStateFlow()
    }
}