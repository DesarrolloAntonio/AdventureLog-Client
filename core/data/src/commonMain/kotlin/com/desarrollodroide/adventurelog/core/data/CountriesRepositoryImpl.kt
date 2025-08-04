package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CountriesRepositoryImpl(
    private val networkDataSource: AdventureLogNetworkDataSource
) : CountriesRepository {

    private val _countriesFlow = MutableStateFlow<List<Country>>(emptyList())
    override val countriesFlow: StateFlow<List<Country>> = _countriesFlow.asStateFlow()
    
    private val _visitedRegionsFlow = MutableStateFlow<List<VisitedRegion>>(emptyList())
    override val visitedRegionsFlow: StateFlow<List<VisitedRegion>> = _visitedRegionsFlow.asStateFlow()
    
    private val _visitedCitiesFlow = MutableStateFlow<List<VisitedCity>>(emptyList())
    override val visitedCitiesFlow: StateFlow<List<VisitedCity>> = _visitedCitiesFlow.asStateFlow()
    
    override suspend fun getCountries(): Either<String, List<Country>> {
        // If we already have countries cached, return them
        if (_countriesFlow.value.isNotEmpty()) {
            return Either.Right(_countriesFlow.value)
        }
        
        return try {
            val countries = networkDataSource.getCountries().map { it.toDomainModel() }
            _countriesFlow.value = countries
            Either.Right(countries)
        } catch (e: ResponseException) {
            println("HTTP Error during getCountries: ${e.response.status}")
            Either.Left("Failed to load countries: HTTP ${e.response.status}")
        } catch (e: HttpRequestTimeoutException) {
            println("Timeout during getCountries: ${e.message}")
            Either.Left("Request timeout: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error during getCountries: ${e.message}")
            Either.Left("Unexpected error: ${e.message}")
        }
    }
    
    override suspend fun getRegions(countryCode: String): Either<String, List<Region>> {
        return try {
            val regions = networkDataSource.getRegions(countryCode).map { it.toDomainModel() }
            Either.Right(regions)
        } catch (e: ResponseException) {
            println("HTTP Error during getRegions: ${e.response.status}")
            Either.Left("Failed to load regions: HTTP ${e.response.status}")
        } catch (e: HttpRequestTimeoutException) {
            println("Timeout during getRegions: ${e.message}")
            Either.Left("Request timeout: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error during getRegions: ${e.message}")
            Either.Left("Unexpected error: ${e.message}")
        }
    }
    
    override suspend fun getVisitedRegions(): Either<String, List<VisitedRegion>> {
        return try {
            val visitedRegions = networkDataSource.getVisitedRegions().map { it.toDomainModel() }
            _visitedRegionsFlow.value = visitedRegions
            Either.Right(visitedRegions)
        } catch (e: ResponseException) {
            println("HTTP Error during getVisitedRegions: ${e.response.status}")
            Either.Left("Failed to load visited regions: HTTP ${e.response.status}")
        } catch (e: HttpRequestTimeoutException) {
            println("Timeout during getVisitedRegions: ${e.message}")
            Either.Left("Request timeout: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error during getVisitedRegions: ${e.message}")
            Either.Left("Unexpected error: ${e.message}")
        }
    }
    
    override suspend fun getVisitedCities(): Either<String, List<VisitedCity>> {
        return try {
            val visitedCities = networkDataSource.getVisitedCities().map { it.toDomainModel() }
            _visitedCitiesFlow.value = visitedCities
            Either.Right(visitedCities)
        } catch (e: ResponseException) {
            println("HTTP Error during getVisitedCities: ${e.response.status}")
            Either.Left("Failed to load visited cities: HTTP ${e.response.status}")
        } catch (e: HttpRequestTimeoutException) {
            println("Timeout during getVisitedCities: ${e.message}")
            Either.Left("Request timeout: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error during getVisitedCities: ${e.message}")
            Either.Left("Unexpected error: ${e.message}")
        }
    }
    
    override suspend fun refreshCountries(): Either<String, List<Country>> {
        return try {
            val countries = networkDataSource.getCountries().map { it.toDomainModel() }
            _countriesFlow.value = countries
            
            // Try to refresh visited regions and cities, but don't fail if they return errors
            try {
                getVisitedRegions()
            } catch (e: Exception) {
                println("Failed to refresh visited regions: ${e.message}")
            }
            
            try {
                getVisitedCities()
            } catch (e: Exception) {
                println("Failed to refresh visited cities: ${e.message}")
            }
            
            Either.Right(countries)
        } catch (e: ResponseException) {
            println("HTTP Error during refreshCountries: ${e.response.status}")
            Either.Left("Failed to refresh countries: HTTP ${e.response.status}")
        } catch (e: HttpRequestTimeoutException) {
            println("Timeout during refreshCountries: ${e.message}")
            Either.Left("Request timeout: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error during refreshCountries: ${e.message}")
            Either.Left("Unexpected error: ${e.message}")
        }
    }
}