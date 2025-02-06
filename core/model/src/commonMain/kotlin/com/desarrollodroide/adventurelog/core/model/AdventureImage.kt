package com.desarrollodroide.adventurelog.core.model

data class AdventureImage(
    val id: String,
    val image: String,
    val adventure: String,
    val isPrimary: Boolean,
    val userId: Int
)