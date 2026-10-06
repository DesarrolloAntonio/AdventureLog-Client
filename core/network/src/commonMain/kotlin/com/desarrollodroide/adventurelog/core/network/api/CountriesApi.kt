package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO

interface CountriesApi {
    suspend fun getCountries(): List<CountryDTO>
    suspend fun getRegions(countryCode: String): List<RegionDTO>
    suspend fun getVisitedRegions(): List<VisitedRegionDTO>
    suspend fun getVisitedCities(): List<VisitedCityDTO>

    /** Marks a region visited. Returns the record the server created, which carries its own id. */
    suspend fun markRegionVisited(regionId: String): VisitedRegionDTO

    /**
     * Asks the server to sweep every location and mark the regions and cities they fall in.
     * Returns how many of each were new.
     */
    suspend fun refreshVisitedRegions(): Pair<Int, Int>

    /** Removes a visit. Keyed by the region's code - the record's numeric id gives a 404. */
    suspend fun unmarkRegionVisited(regionId: String)
}
