package com.desarrollodroide.adventurelog.feature.collections.ui.state

import com.desarrollodroide.adventurelog.core.model.Transportation

private val SHORT_MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/**
 * The line under a transport's name on its card: when it leaves and where it goes,
 * "2 Nov 2026 · Madrid → Barcelona". The card showed only the name and the type, so two legs of a
 * trip looked the same until each was opened (QA CO-19). Null when there is nothing to say.
 */
internal fun Transportation.summary(): String? {
    val day = dayOf(date)?.let { "${it.day} ${SHORT_MONTHS[it.month.ordinal]} ${it.year}" }
    val from = fromLocation?.takeIf { it.isNotBlank() }
    val to = toLocation?.takeIf { it.isNotBlank() }
    val route = when {
        from != null && to != null -> "$from → $to"
        from != null -> "From $from"
        to != null -> "To $to"
        else -> null
    }
    return listOfNotNull(day, route).joinToString(" · ").ifEmpty { null }
}
