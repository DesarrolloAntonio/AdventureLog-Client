package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.PublicUser
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a SharingRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class SharingRepositoryStub : SharingRepository {
    override suspend fun getPublicUsers(): Either<String, List<PublicUser>> = unused()
    override suspend fun share(collectionId: String, userUuid: String): Either<String, Unit> = unused()
    override suspend fun unshare(collectionId: String, userUuid: String): Either<String, Unit> = unused()
    override suspend fun revokeInvite(collectionId: String, userUuid: String): Either<String, Unit> = unused()
}
