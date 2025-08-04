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
}
