package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a VisitsRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class VisitsRepositoryStub : VisitsRepository {
    override suspend fun createVisit(locationId: String, visit: VisitFormData): Either<ApiResponse, Visit> = unused()
    override suspend fun updateVisit(
        visitId: String,
        locationId: String,
        visit: VisitFormData
    ): Either<ApiResponse, Visit> = unused()
    override suspend fun deleteVisit(visitId: String): Either<ApiResponse, Unit> = unused()
}
