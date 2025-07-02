package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodeSearchResultDTO(
    @SerialName("lat")
    val latitude: String,
    @SerialName("lon") 
    val longitude: String,
    @SerialName("name")
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("type")
    val type: String? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("importance")
    val importance: Double? = null,
    @SerialName("addresstype")
    val addressType: String? = null,
    @SerialName("powered_by")
    val poweredBy: String? = null
)
