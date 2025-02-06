package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String,  // Required (*)

    @SerialName("display_name")
    val displayName: String,  // Required (*)

    @SerialName("icon")
    val icon: String,  // Required (*)

    @SerialName("num_adventures")
    val numAdventures: String? = null
)


fun CategoryDTO.toDomainModel() = Category(
    id = id ?: "",
    name = name,
    displayName = displayName,
    icon = icon,
    numAdventures = numAdventures ?: "0"
)