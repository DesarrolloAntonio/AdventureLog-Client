package com.desarrollodroide.adventurelog.core.network.utils

import kotlin.math.pow
import kotlin.math.round

/**
 * Rounds a Double to a specified number of decimal places
 */
fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return round(this * factor) / factor
}

/**
 * Formats a coordinate string to have maximum 6 decimal places
 * Returns null if the string cannot be parsed to Double
 */
fun String?.toCoordinateString(): String? {
    return this?.toDoubleOrNull()?.roundTo(6)?.toString()
}
