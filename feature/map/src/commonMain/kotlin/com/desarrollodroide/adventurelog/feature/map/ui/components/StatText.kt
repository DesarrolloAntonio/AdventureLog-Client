package com.desarrollodroide.adventurelog.feature.map.ui.components

// Split out of ClearStatsSection.kt (Compose, androidMain): tested on every platform.

/** A count as the card shows it: a dash while it is not known yet, never a zero that is not true. */
internal fun statText(value: Int?): String = value?.toString() ?: "–"
