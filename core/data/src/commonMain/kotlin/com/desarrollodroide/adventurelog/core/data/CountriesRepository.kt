package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import kotlinx.coroutines.flow.StateFlow

interface CountriesRepository {
    
    val countriesFlow: StateFlow<List<Country>>
    val visitedRegionsFlow: StateFlow<List<VisitedRegion>>
    val visitedCitiesFlow: StateFlow<List<VisitedCity>>
    
    suspend fun getCountries(): Either<ApiResponse, List<Country>>
    
    suspend fun getRegions(countryCode: String): Either<ApiResponse, List<Region>>
    
    suspend fun getVisitedRegions(): Either<ApiResponse, List<VisitedRegion>>
    
    suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>>
    
    suspend fun refreshCountries(): Either<ApiResponse, List<Country>>
}
