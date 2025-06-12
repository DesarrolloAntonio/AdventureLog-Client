package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UserStatsTest {
    
    @Test
    fun `should create UserStats with default values`() {
        // Given
        val stats = UserStats()
        
        // Then
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
        // Given
        val stats = UserStats(
            adventureCount = 25,
            tripsCount = 10,
            visitedCityCount = 15,
            totalCities = 100,
            visitedRegionCount = 8,
            totalRegions = 50,
            visitedCountryCount = 5,
            totalCountries = 195
        )
        
        // Then
        assertEquals(25, stats.adventureCount)
        assertEquals(10, stats.tripsCount)
        assertEquals(15, stats.visitedCityCount)
        assertEquals(100, stats.totalCities)
        assertEquals(8, stats.visitedRegionCount)
        assertEquals(50, stats.totalRegions)
        assertEquals(5, stats.visitedCountryCount)
        assertEquals(195, stats.totalCountries)
    }
    
    @Test
    fun `should correctly compare UserStats instances`() {
        // Given
        val stats1 = UserStats(
            adventureCount = 10,
            tripsCount = 5,
            visitedCityCount = 8,
            totalCities = 50,
            visitedRegionCount = 4,
            totalRegions = 20,
            visitedCountryCount = 2,
            totalCountries = 100
        )
        
        val stats2 = stats1.copy()
        val stats3 = stats1.copy(adventureCount = 11)
        val stats4 = stats1.copy(visitedCountryCount = 3)
        
        // Then
        assertEquals(stats1, stats2)
        assertNotEquals(stats1, stats3)
        assertNotEquals(stats1, stats4)
        assertEquals(stats1.hashCode(), stats2.hashCode())
    }
    
    @Test
    fun `should calculate percentage of visited locations`() {
        // Given
        val stats = UserStats(
            adventureCount = 50,
            tripsCount = 20,
            visitedCityCount = 25,
            totalCities = 100,
            visitedRegionCount = 10,
            totalRegions = 50,
            visitedCountryCount = 5,
            totalCountries = 200
        )
        
        // When calculating percentages
        val cityPercentage = (stats.visitedCityCount.toDouble() / stats.totalCities) * 100
        val regionPercentage = (stats.visitedRegionCount.toDouble() / stats.totalRegions) * 100
        val countryPercentage = (stats.visitedCountryCount.toDouble() / stats.totalCountries) * 100
        
        // Then
        assertEquals(25.0, cityPercentage)
        assertEquals(20.0, regionPercentage)
        assertEquals(2.5, countryPercentage)
    }
    
    @Test
    fun `should handle zero total values`() {
        // Given
        val stats = UserStats(
            adventureCount = 10,
            tripsCount = 5,
            visitedCityCount = 3,
            totalCities = 0,  // Zero total
            visitedRegionCount = 2,
            totalRegions = 0,  // Zero total
            visitedCountryCount = 1,
            totalCountries = 0  // Zero total
        )
        
        // Then - Stats should still be valid even with zero totals
        assertEquals(3, stats.visitedCityCount)
        assertEquals(0, stats.totalCities)
        assertEquals(2, stats.visitedRegionCount)
        assertEquals(0, stats.totalRegions)
        assertEquals(1, stats.visitedCountryCount)
        assertEquals(0, stats.totalCountries)
    }
    
    @Test
    fun `should handle maximum values`() {
        // Given
        val maxStats = UserStats(
            adventureCount = Int.MAX_VALUE,
            tripsCount = Int.MAX_VALUE,
            visitedCityCount = Int.MAX_VALUE,
            totalCities = Int.MAX_VALUE,
            visitedRegionCount = Int.MAX_VALUE,
            totalRegions = Int.MAX_VALUE,
            visitedCountryCount = Int.MAX_VALUE,
            totalCountries = Int.MAX_VALUE
        )
        
        // Then
        assertEquals(Int.MAX_VALUE, maxStats.adventureCount)
        assertEquals(Int.MAX_VALUE, maxStats.tripsCount)
        assertEquals(Int.MAX_VALUE, maxStats.visitedCityCount)
        assertEquals(Int.MAX_VALUE, maxStats.totalCities)
    }
    
    @Test
    fun `should update individual stats`() {
        // Given
        val initialStats = UserStats()
        
        // When
        val updatedStats = initialStats.copy(
            adventureCount = 1,
            tripsCount = 1
        )
        
        val furtherUpdatedStats = updatedStats.copy(
            visitedCityCount = 1,
            totalCities = 10
        )
        
        // Then
        assertEquals(0, initialStats.adventureCount)
        assertEquals(1, updatedStats.adventureCount)
        assertEquals(1, updatedStats.tripsCount)
        assertEquals(1, furtherUpdatedStats.visitedCityCount)
        assertEquals(10, furtherUpdatedStats.totalCities)
    }
    
    @Test
    fun `should represent realistic user journey`() {
        // Given - A user's progression over time
        val newUser = UserStats()
        
        val afterFirstTrip = UserStats(
            adventureCount = 3,
            tripsCount = 1,
            visitedCityCount = 2,
            totalCities = 100,
            visitedRegionCount = 1,
            totalRegions = 50,
            visitedCountryCount = 1,
            totalCountries = 195
        )
        
        val experiencedTraveler = UserStats(
            adventureCount = 150,
            tripsCount = 45,
            visitedCityCount = 80,
            totalCities = 500,
            visitedRegionCount = 35,
            totalRegions = 100,
            visitedCountryCount = 25,
            totalCountries = 195
        )
        
        // Then
        assertEquals(0, newUser.adventureCount)
        assertEquals(3, afterFirstTrip.adventureCount)
        assertEquals(150, experiencedTraveler.adventureCount)
        
        // Experienced traveler has visited more places
        assertEquals(80, experiencedTraveler.visitedCityCount)
        assertEquals(25, experiencedTraveler.visitedCountryCount)
    }
}
