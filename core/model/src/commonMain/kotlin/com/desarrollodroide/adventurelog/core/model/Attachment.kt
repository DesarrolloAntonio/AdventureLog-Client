package com.desarrollodroide.adventurelog.core.model

data class Attachment(
    val id: String,
    val file: String,
    val adventure: String,
    val extension: String,
    val name: String,
    val userId: Int
)