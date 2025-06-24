package com.desarrollodroide.adventurelog.core.model

data class Collection(
    val id: String,
    val description: String,
    val userId: String,
    val name: String,
    val isPublic: Boolean,
    val adventures: List<Adventure>,
    val createdAt: String,
    val startDate: String?,
    val endDate: String?,
    val transportations: List<String>,
    val notes: List<String>,
    val updatedAt: String,
    val checklists: List<String>,
    val isArchived: Boolean,
    val sharedWith: List<String>,
    val link: String,
    val lodging: List<String>
)