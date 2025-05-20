package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AdventureImage(
    val id: String,
    val image: String,
    val adventure: String,
    val isPrimary: Boolean,
    val userId: String
)