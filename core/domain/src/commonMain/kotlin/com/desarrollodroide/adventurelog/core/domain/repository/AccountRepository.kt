package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.core.model.MediaUsage
import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * The settings side of the account: the profile the server stores, the password, the addresses on
 * it, and how much media it holds.
 *
 * These return the server's own message on failure rather than an [com.desarrollodroide.adventurelog.core.common.ApiResponse]
 * category, because every one of them can fail for a reason the user has to read to act on -
 * "A user with that username already exists", "Please type your current password" - and a generic
 * "Something went wrong" would leave them guessing.
 */
interface AccountRepository {

    /**
     * Patch the profile. Pass only what changed: the server rejects a username it already holds,
     * even when the username being sent is the caller's own.
     *
     * On success the session is refreshed, so anything observing the user - the greeting, the
     * default currency, the map style - follows immediately.
     */
    suspend fun updateProfile(
        username: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        publicProfile: Boolean? = null,
        measurementSystem: String? = null,
        defaultCurrency: String? = null,
        mapStyle: String? = null
    ): Either<String, UserDetails>

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Either<String, Unit>

    suspend fun getMediaUsage(): Either<String, MediaUsage>

    suspend fun getEmailAddresses(): Either<String, List<EmailAddress>>

    suspend fun addEmailAddress(email: String): Either<String, Unit>

    suspend fun requestEmailVerification(email: String): Either<String, Unit>

    suspend fun setPrimaryEmailAddress(email: String): Either<String, Unit>

    suspend fun removeEmailAddress(email: String): Either<String, Unit>
}
