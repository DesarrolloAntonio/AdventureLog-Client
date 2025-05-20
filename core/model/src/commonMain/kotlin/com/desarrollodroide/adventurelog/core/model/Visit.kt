package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Visit(
    val id: String,
    val startDate: String,
    val endDate: String,
    val notes: String
)