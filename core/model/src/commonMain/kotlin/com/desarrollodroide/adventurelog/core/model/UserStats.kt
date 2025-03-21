package com.desarrollodroide.adventurelog.core.model

/**
 * Represents user statistics such as adventures count, visited countries, regions, and cities.
 */
data class UserStats(
    val adventureCount: Int = 0,
    val tripsCount: Int = 0,
    val visitedCityCount: Int = 0,
    val totalCities: Int = 0,
    val visitedRegionCount: Int = 0,
    val totalRegions: Int = 0,
    val visitedCountryCount: Int = 0,
    val totalCountries: Int = 0
)
