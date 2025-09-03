package com.desarrollodroide.adventurelog.core.model

data class Locations(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<Location>
)