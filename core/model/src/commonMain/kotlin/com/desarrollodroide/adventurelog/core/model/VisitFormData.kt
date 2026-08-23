package com.desarrollodroide.adventurelog.core.model

data class VisitFormData(
    /**
     * Null for a visit the user has just added and that the server has never seen. Carrying the
     * id lets an edit be matched back to the record it came from, so saving updates that visit
     * rather than replacing it - which would discard any activities hanging off it.
     */
    val id: String? = null,
    val startDate: String = "",
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val timezone: String = "Europe/Madrid",
    val notes: String = "",
    val isAllDay: Boolean = true
)
