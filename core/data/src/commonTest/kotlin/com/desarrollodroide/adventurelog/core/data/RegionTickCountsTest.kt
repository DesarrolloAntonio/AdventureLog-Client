package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO
import com.desarrollodroide.adventurelog.core.testing.AdventureLogNetworkStub
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A region ticked on a country's page counts in the country the world list shows (QA 05, WO-02).
 * Measured: Aichi ticked, the page read 4/47 and the list still said 3/47 until a pull to refresh.
 */
class RegionTickCountsTest {

    private class Server : AdventureLogNetworkStub() {
        override suspend fun getCountries() = listOf(
            CountryDTO(id = 1, name = "Japan", countryCode = "JP", flagUrl = "", numRegions = "47", numVisits = "3"),
            CountryDTO(id = 2, name = "Spain", countryCode = "ES", flagUrl = "", numRegions = "19", numVisits = "1")
        )
        override suspend fun markRegionVisited(regionId: String) = VisitedRegionDTO(id = 9, region = regionId, name = regionId)
        override suspend fun unmarkRegionVisited(regionId: String) = Unit
    }

    private fun visits(repository: CountriesRepositoryImpl) =
        repository.countriesFlow.value.associate { it.countryCode to it.numVisits }

    @Test
    fun tickingAndUntickingARegionMovesItsCountrysCountAndNoOther() = runTest {
        val repository = CountriesRepositoryImpl(Server())
        repository.getCountries()

        repository.markRegionVisited("JP-23")
        assertEquals(mapOf("JP" to 4, "ES" to 1), visits(repository))

        repository.unmarkRegionVisited("JP-23")
        assertEquals(mapOf("JP" to 3, "ES" to 1), visits(repository))
    }

    @Test
    fun aRegionTickedTwiceCountsOnce() = runTest {
        val repository = CountriesRepositoryImpl(Server())
        repository.getCountries()

        repository.markRegionVisited("JP-23")
        repository.markRegionVisited("JP-23")

        assertEquals(4, visits(repository)["JP"])
    }
}
