package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCollectionRequest(
    val name: String,
    val description: String,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
)

@Serializable
data class UpdateCollectionRequest(
    val name: String? = null,
    val description: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    val link: String? = null
)
