package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.PublicUser

/**
 * Who a collection can be shared with, and the three things that can be done about it.
 *
 * Results carry the server's own message rather than a code: it says useful things - "Invite
 * already sent to this user", "Cannot share with yourself" - that nothing here could improve on.
 */
interface SharingRepository {
    suspend fun getPublicUsers(): Either<String, List<PublicUser>>

    suspend fun share(collectionId: String, userUuid: String): Either<String, Unit>

    suspend fun unshare(collectionId: String, userUuid: String): Either<String, Unit>

    suspend fun revokeInvite(collectionId: String, userUuid: String): Either<String, Unit>
}
