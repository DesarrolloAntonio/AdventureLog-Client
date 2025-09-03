package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val id: String,
    val file: String,
    val extension: String,
    val name: String? = null,
    val user: String,  // UUID String
    val geojson: String? = null
)
