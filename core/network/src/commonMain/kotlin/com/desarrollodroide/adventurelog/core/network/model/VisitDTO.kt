package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.Visit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisitDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    @SerialName("notes")
    val notes: String? = null
)

fun VisitDTO.toDomainModel() = Visit(
    id = id ?: "",
    startDate = startDate ?: "",
    endDate = endDate ?: "",
    notes = notes ?: ""
)