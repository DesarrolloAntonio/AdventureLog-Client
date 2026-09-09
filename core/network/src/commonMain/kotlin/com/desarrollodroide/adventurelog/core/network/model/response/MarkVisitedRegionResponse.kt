package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What the sweep found: how many regions and cities were newly marked. */
@Serializable
data class MarkVisitedRegionResponse(
    @SerialName("new_regions")
    val newRegions: Int = 0,
    @SerialName("new_cities")
    val newCities: Int = 0
)
