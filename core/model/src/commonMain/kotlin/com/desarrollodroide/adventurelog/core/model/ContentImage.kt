package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentImage(
    val id: String,
    val image: String,
    val isPrimary: Boolean = false,
    val user: String,  // UUID String
    val immichId: String? = null
)
