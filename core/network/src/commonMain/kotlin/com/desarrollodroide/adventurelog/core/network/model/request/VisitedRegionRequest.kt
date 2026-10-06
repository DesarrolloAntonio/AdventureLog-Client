package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The body /api/visitedregion/ expects: the region's id and nothing else. */
@Serializable
data class VisitedRegionRequest(
    @SerialName("region")
    val region: String
)
