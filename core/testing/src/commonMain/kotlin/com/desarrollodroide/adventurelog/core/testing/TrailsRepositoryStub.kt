package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Trail
import com.desarrollodroide.adventurelog.core.model.TrailFormData
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a TrailsRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class TrailsRepositoryStub : TrailsRepository {
    override suspend fun createTrail(locationId: String, trail: TrailFormData): Either<ApiResponse, Trail> = unused()
    override suspend fun updateTrail(
        trailId: String,
        locationId: String,
        trail: TrailFormData
    ): Either<ApiResponse, Trail> = unused()
    override suspend fun deleteTrail(trailId: String): Either<ApiResponse, Unit> = unused()
}
