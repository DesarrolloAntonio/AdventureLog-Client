package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Trail(
    val id: String,
    val user: String,
    val name: String,
    val location: String,
    val createdAt: String,
    val link: String? = null,
    val wandererId: String? = null,
    val provider: String? = null,
    val wandererData: String? = null,
    val wandererLink: String? = null
)
