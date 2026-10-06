package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a CountriesRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class CountriesRepositoryStub : CountriesRepository {

    // A flow of state is something to observe, not a call to make: a use case reads one in its
    // constructor, long before any test could have set it up. Empty, and overridable.
    override val countriesFlow: StateFlow<List<Country>> =
        MutableStateFlow(emptyList())
    override val visitedRegionsFlow: StateFlow<List<VisitedRegion>> =
        MutableStateFlow(emptyList())
    override val visitedCitiesFlow: StateFlow<List<VisitedCity>> =
        MutableStateFlow(emptyList())
    override suspend fun getCountries(): Either<ApiResponse, List<Country>> = unused()
    override suspend fun getRegions(countryCode: String): Either<ApiResponse, List<Region>> = unused()
    override suspend fun getVisitedRegions(): Either<ApiResponse, List<VisitedRegion>> = unused()
    override suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>> = unused()
    override suspend fun refreshVisitedRegions(): Either<ApiResponse, Pair<Int, Int>> = unused()
    override suspend fun markRegionVisited(regionId: String): Either<ApiResponse, VisitedRegion> = unused()
    override suspend fun unmarkRegionVisited(regionId: String): Either<ApiResponse, Unit> = unused()
    override suspend fun refreshCountries(): Either<ApiResponse, List<Country>> = unused()
}
