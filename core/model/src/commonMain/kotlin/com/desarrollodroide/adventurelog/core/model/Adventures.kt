package com.desarrollodroide.adventurelog.core.model

data class Adventures(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<Adventure>
)