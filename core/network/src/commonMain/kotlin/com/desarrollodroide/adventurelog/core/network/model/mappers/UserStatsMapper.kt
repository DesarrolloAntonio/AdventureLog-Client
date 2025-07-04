package com.desarrollodroide.adventurelog.core.network.model.mappers

import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO

fun UserStatsDTO.toUserStats(): UserStats {
    return UserStats(
        adventureCount = adventureCount ?: 0,
        tripsCount = tripsCount ?: 0,
        visitedCityCount = visitedCityCount ?: 0,
        totalCities = totalCities ?: 0,
        visitedRegionCount = visitedRegionCount ?: 0,
        totalRegions = totalRegions ?: 0,
        visitedCountryCount = visitedCountryCount ?: 0,
        totalCountries = totalCountries ?: 0
    )
}
