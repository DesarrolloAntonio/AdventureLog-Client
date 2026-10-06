package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a GeocodeRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class GeocodeRepositoryStub : GeocodeRepository {
    override suspend fun searchLocations(query: String): Either<ApiResponse, List<GeocodeSearchResult>> = unused()
    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Either<ApiResponse, ReverseGeocodeResult> = unused()
}
