package com.desarrollodroide.adventurelog.core.data

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.AccountRepository
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.core.model.MediaUsage
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

class AccountRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork,
    private val userRepository: UserRepository,
    private val ioDispatcher: CoroutineDispatcher
) : AccountRepository {

    private val logger = Logger.withTag("AccountRepositoryImpl")

    override suspend fun updateProfile(
        username: String?,
        firstName: String?,
        lastName: String?,
        publicProfile: Boolean?,
        measurementSystem: String?,
        defaultCurrency: String?,
        mapStyle: String?
    ): Either<String, UserDetails> = call("update your profile") {
        val session = userRepository.activeSession
        val updated = networkDataSource.updateUserProfile(
            username = username,
            firstName = firstName,
            lastName = lastName,
            publicProfile = publicProfile,
            measurementSystem = measurementSystem,
            defaultCurrency = defaultCurrency,
            mapStyle = mapStyle
        ).toDomainModel(serverUrl = session?.serverUrl ?: "")
            // The PATCH response is the profile alone - it carries neither the session token nor
            // the server URL, and losing either would sign the user out on the next request.
            .copy(sessionToken = session?.sessionToken)

        // Published, not persisted: writing to disk here would create an auto-login for someone
        // who never asked to be remembered. A remembered session is refreshed from the server on
        // the next start by InitializeSessionUseCase anyway.
        userRepository.setActiveSession(updated)
        updated
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Either<String, Unit> = call("change your password") {
        val changed = networkDataSource.changePassword(currentPassword, newPassword)
        if (!changed) {
            // allauth answers 400 here rather than throwing, and the only thing it can be is the
            // current password or a new one the validators rejected.
            throw HttpException(400, "Could not change the password. Check your current password.")
        }
    }

    override suspend fun getMediaUsage(): Either<String, MediaUsage> = call("load storage usage") {
        networkDataSource.getMediaUsage().toDomainModel()
    }

    override suspend fun getEmailAddresses(): Either<String, List<EmailAddress>> =
        call("load your email addresses") {
            networkDataSource.getEmailAddresses().map { it.toDomainModel() }
        }

    override suspend fun addEmailAddress(email: String): Either<String, Unit> =
        call("add that address") { networkDataSource.addEmailAddress(email) }

    override suspend fun requestEmailVerification(email: String): Either<String, Unit> =
        call("send the verification email") { networkDataSource.requestEmailVerification(email) }

    override suspend fun setPrimaryEmailAddress(email: String): Either<String, Unit> =
        call("set that address as primary") { networkDataSource.setPrimaryEmailAddress(email) }

    override suspend fun removeEmailAddress(email: String): Either<String, Unit> =
        call("remove that address") { networkDataSource.removeEmailAddress(email) }

    /**
     * Runs [block] off the main thread and turns anything it throws into a message the user can
     * act on. [HttpException] already carries the server's own wording, so it is passed through
     * untouched; everything else falls back to "Could not <action>".
     */
    private suspend fun <T> call(
        action: String,
        block: suspend () -> T
    ): Either<String, T> = withContext(ioDispatcher) {
        try {
            Either.Right(block())
        } catch (e: HttpException) {
            logger.e { "HTTP ${e.code} while trying to $action" }
            Either.Left(e.message)
        } catch (e: IOException) {
            logger.e(e) { "IO error while trying to $action" }
            Either.Left("No connection to the server. Please try again.")
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error while trying to $action" }
            Either.Left("Could not $action.")
        }
    }
}
