package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.Serializable

@Serializable
data class WikipediaDescriptionResponse(
    val extract: String? = null
)
