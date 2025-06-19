package com.desarrollodroide.adventurelog.core.network.ktor.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCollectionRequest(
    val name: String,
    val description: String,
    val is_public: Boolean,
    val start_date: String? = null,
    val end_date: String? = null
)

@Serializable
data class UpdateCollectionRequest(
    val name: String? = null,
    val description: String? = null,
    val is_public: Boolean? = null,
    val start_date: String? = null,
    val end_date: String? = null
)
