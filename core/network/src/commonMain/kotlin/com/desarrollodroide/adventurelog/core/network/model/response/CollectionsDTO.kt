package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionsDTO(
    @SerialName("count")
    val count: Int? = null,
    
    @SerialName("next")
    val next: String? = null,
    
    @SerialName("previous")
    val previous: String? = null,
    
    @SerialName("results")
    val results: List<CollectionDTO>? = null
)
