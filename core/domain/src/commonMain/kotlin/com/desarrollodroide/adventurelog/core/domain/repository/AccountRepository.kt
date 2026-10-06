package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.core.model.MediaUsage
import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * Why an account call failed.
 *
 * The [message] is the server's own words wherever there are any, because every one of these can
 * fail for a reason the user has to read to act on - "A user with that username already exists",
 * "Please type your current password" - and a generic "Something went wrong" would leave them
 * guessing. That is the whole reason this is not an
 * [com.desarrollodroide.adventurelog.core.common.ApiResponse].
 *
 * [serverRefused] carries the one distinction a message cannot: whether the server answered at
 * all. A refusal is the server's verdict and the screen should fall back to what it holds; being
 * unable to reach it says nothing about what the user typed, and discarding their work over it is
 * just losing it for them.
 */
data class AccountError(
    val message: String,
    val serverRefused: Boolean
)

/**
 * The settings side of the account: the profile the server stores, the password, the addresses on
 * it, and how much media it holds.
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
    ): Either<AccountError, UserDetails>

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Either<AccountError, Unit>

    suspend fun getMediaUsage(): Either<AccountError, MediaUsage>

    suspend fun getEmailAddresses(): Either<AccountError, List<EmailAddress>>

    suspend fun addEmailAddress(email: String): Either<AccountError, Unit>

    suspend fun requestEmailVerification(email: String): Either<AccountError, Unit>

    suspend fun setPrimaryEmailAddress(email: String): Either<AccountError, Unit>

    suspend fun removeEmailAddress(email: String): Either<AccountError, Unit>
}
