package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.Adventures
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventuresDTO(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("next")
    val next: String? = null,
    @SerialName("previous")
    val previous: String? = null,
    @SerialName("results")
    val results: List<AdventureDTO>? = null
)

fun AdventuresDTO.toDomainModel(): Adventures = Adventures(
    count = count ?: 0,
    next = next.orEmpty(),
    previous = previous.orEmpty(),
    results = results?.map { it.toDomainModel() } ?: emptyList()
)



