package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDTO(
    val id: String,
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val icon: String? = null,
    @SerialName("num_adventures")
    val numAdventures: String? = null
)


fun CategoryDTO.toDomainModel() = Category(
    id = id,
    name = name,
    displayName = displayName,
    icon = icon?: "",
    numAdventures = numAdventures ?: ""
)