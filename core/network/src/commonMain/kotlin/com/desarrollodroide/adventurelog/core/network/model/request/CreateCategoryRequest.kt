package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val icon: String? = null
)
