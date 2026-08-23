package com.desarrollodroide.adventurelog.core.model

/**
 * A trail as the form holds it while being edited.
 *
 * Trails are their own resource, so - like visits - they are saved against the location once it
 * has an id. [id] is null for one the user has just added and the server has never seen.
 */
data class TrailFormData(
    val id: String? = null,
    val name: String = "",
    val link: String = ""
)
