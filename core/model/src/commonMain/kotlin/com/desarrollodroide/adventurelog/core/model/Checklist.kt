package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Checklist(
    val id: String,
    val user: Int,
    val name: String,
    val date: String? = null,
    val isPublic: Boolean = false,
    val collection: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val items: List<ChecklistItem> = emptyList()
)

@Serializable
data class ChecklistItem(
    val id: String,
    val user: Int,
    val name: String,
    val isChecked: Boolean = false,
    val checklist: String,
    val createdAt: String,
    val updatedAt: String
)
