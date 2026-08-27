package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.DashboardDTO
import com.desarrollodroide.adventurelog.core.network.model.response.EmailAddressDTO
import com.desarrollodroide.adventurelog.core.network.model.response.MediaUsageDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CalendarEventsDTO

interface UserApi {
    /**
     * Get current user details
     */
    suspend fun getUserDetails(): UserDetailsDTO
    
    /**
     * Update the writable half of the user's profile.
     *
     * Only the fields `CustomUserDetailsSerializer` accepts are sent; `email` is read-only there
     * and is changed through the email endpoints instead. Every argument is optional because the
     * server patches partially, and re-sending an unchanged username makes it answer
     * "already taken".
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
    
    /**
     * Change user password
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Boolean
    
    /**
     * Upload user avatar
     */
    suspend fun uploadAvatar(imageData: ByteArray): String
    
    /**
     * Media storage this account is using on the server.
     */
    suspend fun getMediaUsage(): MediaUsageDTO

    /**
     * Every address on the account, with its verified and primary flags.
     */
    suspend fun getEmailAddresses(): List<EmailAddressDTO>

    /**
     * Add an address. The server sends it a verification mail; it stays unverified until then.
     */
    suspend fun addEmailAddress(email: String)

    /**
     * Send the verification mail for an address again.
     */
    suspend fun requestEmailVerification(email: String)

    /**
     * Make an address the primary one. The server refuses if it is not verified.
     */
    suspend fun setPrimaryEmailAddress(email: String)

    /**
     * Remove an address. The primary address cannot be removed.
     */
    suspend fun removeEmailAddress(email: String)

    /**
     * Get user statistics
     */
    suspend fun getUserStats(username: String): UserStatsDTO

    /**
     * Get everything the home screen shows in a single request.
     */
    suspend fun getDashboard(): DashboardDTO

    /**
     * Every dated entry between [start] and [end], as `YYYY-MM-DD`. Both are optional; without
     * them the server answers with the whole calendar.
     */
    suspend fun getCalendarEvents(start: String? = null, end: String? = null): CalendarEventsDTO
}
