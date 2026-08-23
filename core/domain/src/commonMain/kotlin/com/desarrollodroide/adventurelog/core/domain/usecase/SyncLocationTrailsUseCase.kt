package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.TrailsRepository
import com.desarrollodroide.adventurelog.core.model.Trail
import com.desarrollodroide.adventurelog.core.model.TrailFormData

/**
 * Brings a location's trails in line with what the form holds, after the location itself has been
 * saved - trails carry a location id, so they cannot be created alongside it.
 */
class SyncLocationTrailsUseCase(
    private val trailsRepository: TrailsRepository
) {

    suspend operator fun invoke(
        locationId: String,
        existing: List<Trail>,
        edited: List<TrailFormData>
    ): Either<String, Unit> {
        val keptIds = edited.mapNotNull { it.id }.toSet()

        for (trail in existing) {
            if (trail.id !in keptIds) {
                trailsRepository.deleteTrail(trail.id).onFailure {
                    return failure("removed", it)
                }
            }
        }

        for (trail in edited) {
            // The server requires a link when no Wanderer id is given, and a nameless trail is
            // not worth sending either.
            if (trail.name.isBlank() || trail.link.isBlank()) continue

            val trailId = trail.id
            val result = if (trailId == null) {
                trailsRepository.createTrail(locationId, trail)
            } else {
                trailsRepository.updateTrail(trailId, locationId, trail)
            }
            result.onFailure {
                return failure(if (trailId == null) "added" else "updated", it)
            }
        }

        return Either.Right(Unit)
    }

    private fun failure(action: String, reason: Any) =
        Either.Left("The location was saved, but a trail could not be $action ($reason).")

    private inline fun <L, R> Either<L, R>.onFailure(block: (L) -> Unit) {
        if (this is Either.Left) block(value)
    }
}
