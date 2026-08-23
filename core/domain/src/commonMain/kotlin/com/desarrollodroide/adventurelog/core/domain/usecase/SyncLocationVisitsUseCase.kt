package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.VisitsRepository
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData

/**
 * Brings a location's visits in line with what the form holds.
 *
 * A location cannot be saved with its visits inline - the server rejects that, because a visit
 * needs the id of a location that does not exist yet while it is being created. So the location
 * is saved first and its visits reconciled here, exactly as the web client does.
 *
 * Existing visits are matched by id and updated in place rather than deleted and recreated:
 * activities are attached to a visit, and replacing the visit would take them with it.
 */
class SyncLocationVisitsUseCase(
    private val visitsRepository: VisitsRepository
) {

    /**
     * @param existing what the server currently holds for this location, empty when creating.
     * @return the first failure, or Unit when every change went through.
     */
    suspend operator fun invoke(
        locationId: String,
        existing: List<Visit>,
        edited: List<VisitFormData>
    ): Either<String, Unit> {
        val keptIds = edited.mapNotNull { it.id }.toSet()

        for (visit in existing) {
            if (visit.id !in keptIds) {
                visitsRepository.deleteVisit(visit.id).onFailure { return failure("removed", it) }
            }
        }

        for (visit in edited) {
            if (visit.startDate.isBlank()) continue

            val visitId = visit.id
            val result = if (visitId == null) {
                visitsRepository.createVisit(locationId, visit)
            } else {
                visitsRepository.updateVisit(visitId, locationId, visit)
            }
            result.onFailure {
                return failure(if (visitId == null) "added" else "updated", it)
            }
        }

        return Either.Right(Unit)
    }

    private fun failure(action: String, reason: Any) =
        Either.Left("The location was saved, but a visit could not be $action ($reason).")

    private inline fun <L, R> Either<L, R>.onFailure(block: (L) -> Unit) {
        if (this is Either.Left) block(value)
    }
}
