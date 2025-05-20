package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val id: String,
    val file: String,
    val adventure: String,
    val extension: String,
    val name: String,
    val userId: Int
)