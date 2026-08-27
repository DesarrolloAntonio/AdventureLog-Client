package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.SharingRepository
import com.desarrollodroide.adventurelog.core.model.PublicUser
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

class SharingRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork,
    private val ioDispatcher: CoroutineDispatcher
) : SharingRepository {

    override suspend fun getPublicUsers(): Either<String, List<PublicUser>> =
        call("load the people on this server") {
            networkDataSource.getPublicUsers().map { dto ->
                PublicUser(
                    uuid = dto.uuid,
                    username = dto.username,
                    firstName = dto.firstName.orEmpty(),
                    lastName = dto.lastName.orEmpty(),
                    profilePic = dto.profilePic
                )
            }
        }

    override suspend fun share(collectionId: String, userUuid: String) =
        call("send the invitation") { networkDataSource.shareCollection(collectionId, userUuid) }

    override suspend fun unshare(collectionId: String, userUuid: String) =
        call("stop sharing") { networkDataSource.unshareCollection(collectionId, userUuid) }

    override suspend fun revokeInvite(collectionId: String, userUuid: String) =
        call("revoke the invitation") { networkDataSource.revokeInvite(collectionId, userUuid) }

    private suspend fun <T> call(action: String, block: suspend () -> T): Either<String, T> =
        withContext(ioDispatcher) {
            try {
                Either.Right(block())
            } catch (e: HttpException) {
                Either.Left(e.message)
            } catch (e: IOException) {
                Either.Left("No connection to the server. Please try again.")
            } catch (e: Exception) {
                Either.Left("Could not $action.")
            }
        }
}
