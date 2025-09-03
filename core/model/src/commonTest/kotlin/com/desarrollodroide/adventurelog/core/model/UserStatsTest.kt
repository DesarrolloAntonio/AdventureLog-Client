package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserStatsTest : BaseModelTest<UserStats>() {

    @Test
    fun `should create UserStats with default values`() {
        val stats = UserStats()
        
        assertEquals(0, stats.adventureCount)
        assertEquals(0, stats.tripsCount)
        assertEquals(0, stats.visitedCityCount)
        assertEquals(0, stats.totalCities)
        assertEquals(0, stats.visitedRegionCount)
        assertEquals(0, stats.totalRegions)
        assertEquals(0, stats.visitedCountryCount)
        assertEquals(0, stats.totalCountries)
    }

    @Test
    fun `should create UserStats with custom values`() {
        val stats = UserStats(
            adventureCount = 25,
            tripsCount = 10,
            visitedCityCount = 50,
            totalCities = 60,
            visitedRegionCount = 30,
            totalRegions = 38,
            visitedCountryCount = 15,
            totalCountries = 20
        )

        assertEquals(25, stats.adventureCount)
        assertEquals(10, stats.tripsCount)
        assertEquals(50, stats.visitedCityCount)
        assertEquals(60, stats.totalCities)
        assertEquals(30, stats.visitedRegionCount)
        assertEquals(38, stats.totalRegions)
        assertEquals(15, stats.visitedCountryCount)
        assertEquals(20, stats.totalCountries)
    }

    @Test
    fun `should correctly implement equals and hashCode`() {
        val stats1 = UserStats(
            adventureCount = 10,
            visitedCountryCount = 5,
            totalCountries = 15
        )
        
        val stats2 = stats1.copy()
        val stats3 = stats1.copy(adventureCount = 11)
        val stats4 = stats1.copy(visitedCityCount = 20)

        testEquality(
            original = stats1,
            equal = stats2,
            different = listOf(stats3, stats4)
        )
    }

    @Test
    fun `should handle zero total values`() {
        val emptyStats = UserStats(
            adventureCount = 0,
            tripsCount = 0,
            visitedCountryCount = 0,
            totalCountries = 0
        )

        assertEquals(0, emptyStats.adventureCount)
        assertEquals(0, emptyStats.tripsCount)
        assertEquals(0, emptyStats.visitedCountryCount)
        assertEquals(0, emptyStats.totalCountries)
    }

    @Test
    fun `should calculate percentage of visited locations`() {
        val stats = UserStats(
            visitedCountryCount = 25,
            totalCountries = 50,
            visitedCityCount = 100,
            totalCities = 150,
            visitedRegionCount = 40,
            totalRegions = 80
        )

        val countriesPercentage = (stats.visitedCountryCount.toDouble() / stats.totalCountries) * 100
        val citiesPercentage = (stats.visitedCityCount.toDouble() / stats.totalCities) * 100
        val regionsPercentage = (stats.visitedRegionCount.toDouble() / stats.totalRegions) * 100
        
        assertEquals(50.0, countriesPercentage)
        assertEquals(66.67, citiesPercentage, 0.01)
        assertEquals(50.0, regionsPercentage)
    }

    @Test
    fun `should update individual stats`() {
        val initialStats = UserStats(adventureCount = 10)
        
        val updatedStats = initialStats.copy(
            adventureCount = 11,
            visitedCityCount = initialStats.visitedCityCount + 2
        )

        assertEquals(11, updatedStats.adventureCount)
        assertEquals(2, updatedStats.visitedCityCount)
        assertEquals(0, updatedStats.visitedRegionCount)
    }

    @Test
    fun `should handle maximum values`() {
        val maxStats = UserStats(
            adventureCount = 10000,
            tripsCount = 5000,
            visitedCountryCount = 195,
            totalCountries = 195,
            visitedCityCount = 10000,
            totalCities = 10500,
            visitedRegionCount = 5000,
            totalRegions = 5100
        )

        assertEquals(10000, maxStats.adventureCount)
        assertEquals(195, maxStats.visitedCountryCount)
        assertEquals(10000, maxStats.visitedCityCount)
        assertEquals(5000, maxStats.visitedRegionCount)
    }

    @Test
    fun `should represent realistic user journey`() {
        val beginnerStats = UserStats(
            adventureCount = 5,
            tripsCount = 2,
            visitedCountryCount = 1,
            totalCountries = 195,
            visitedCityCount = 5,
            totalCities = 10000,
            visitedRegionCount = 3,
            totalRegions = 5000
        )

        val experiencedStats = UserStats(
            adventureCount = 150,
            tripsCount = 50,
            visitedCountryCount = 30,
            totalCountries = 195,
            visitedCityCount = 150,
            totalCities = 10000,
            visitedRegionCount = 100,
            totalRegions = 5000
        )

        assertEquals(5, beginnerStats.adventureCount)
        assertEquals(150, experiencedStats.adventureCount)
        assertTrue(experiencedStats.visitedCountryCount > beginnerStats.visitedCountryCount)
        assertTrue(experiencedStats.visitedCityCount > beginnerStats.visitedCityCount)
    }
}
