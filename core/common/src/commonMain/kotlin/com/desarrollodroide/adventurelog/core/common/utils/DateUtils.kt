package com.desarrollodroide.adventurelog.core.common.utils

/**
 * Formats an ISO date string (yyyy-MM-dd) to display format (dd/mm/yyyy)
 */
fun formatDateForDisplay(dateString: String): String {
    return try {
        if (dateString.isEmpty()) return ""

        val parts = dateString.split("-")
        if (parts.size == 3) {
            "${parts[2].padStart(2, '0')}/${parts[1].padStart(2, '0')}/${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}