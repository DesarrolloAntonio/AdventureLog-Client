package com.desarrollodroide.adventurelog.core.model

data class VisitFormData(
    val startDate: String = "",
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val timezone: String = "Europe/Madrid",
    val notes: String = "",
    val isAllDay: Boolean = true
)
