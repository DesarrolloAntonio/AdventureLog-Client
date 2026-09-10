package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.core.model.MediaUsage
import com.desarrollodroide.adventurelog.core.model.UserDetails
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a AccountRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class AccountRepositoryStub : AccountRepository {
    override suspend fun updateProfile(
        username: String?,
        firstName: String?,
        lastName: String?,
        publicProfile: Boolean?,
        measurementSystem: String?,
        defaultCurrency: String?,
        mapStyle: String?
    ): Either<AccountError, UserDetails> = unused()
    override suspend fun changePassword(currentPassword: String, newPassword: String): Either<AccountError, Unit> = unused()
    override suspend fun getMediaUsage(): Either<AccountError, MediaUsage> = unused()
    override suspend fun getEmailAddresses(): Either<AccountError, List<EmailAddress>> = unused()
    override suspend fun addEmailAddress(email: String): Either<AccountError, Unit> = unused()
    override suspend fun requestEmailVerification(email: String): Either<AccountError, Unit> = unused()
    override suspend fun setPrimaryEmailAddress(email: String): Either<AccountError, Unit> = unused()
    override suspend fun removeEmailAddress(email: String): Either<AccountError, Unit> = unused()
}
