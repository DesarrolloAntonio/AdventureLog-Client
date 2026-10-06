package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory

/** Finding places near a point that are not in the account yet. */
interface RecommendationApi {
    /**
     * Either [latitude]/[longitude] or [place] must be given; the server geocodes the latter. A
     * call with neither is a 400, which is a caller bug rather than a state worth modelling.
     */
    suspend fun getRecommendations(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: RecommendationCategory,
        radiusMetres: Int
    ): List<Recommendation>
}
