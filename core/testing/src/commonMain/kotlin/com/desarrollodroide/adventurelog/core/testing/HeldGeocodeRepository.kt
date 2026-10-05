package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import kotlinx.coroutines.CompletableDeferred

/**
 * A geocoder whose answers the test releases one query at a time, in any order - the way the real
 * server answered "Madrid A" five seconds after "Madrid Atocha" (QA RL-05).
 */
class HeldGeocodeRepository : GeocodeRepositoryStub() {
    /** Every query that reached the server, in the order it was sent. */
    val asked = mutableListOf<String>()
    private val answers = mutableMapOf<String, CompletableDeferred<List<GeocodeSearchResult>>>()

    private fun answerFor(query: String) = answers.getOrPut(query) { CompletableDeferred() }

    override suspend fun searchLocations(query: String): Either<ApiResponse, List<GeocodeSearchResult>> {
        asked += query
        return Either.Right(answerFor(query).await())
    }

    /** Lets the request for [query] answer with one result per name. */
    fun answer(query: String, vararg names: String) {
        answerFor(query).complete(names.map { GeocodeSearchResult(latitude = "0", longitude = "0", name = it, displayName = it) })
    }
}
