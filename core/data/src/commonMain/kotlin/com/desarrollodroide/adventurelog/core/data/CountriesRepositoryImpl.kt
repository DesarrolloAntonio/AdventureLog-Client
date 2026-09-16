package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.domain.repository.AccountDataCache
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.io.IOException
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("CountriesRepositoryImpl")

class CountriesRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : CountriesRepository, AccountDataCache {

    private val _countriesFlow = MutableStateFlow<List<Country>>(emptyList())
    override val countriesFlow: StateFlow<List<Country>> = _countriesFlow.asStateFlow()
    
    private val _visitedRegionsFlow = MutableStateFlow<List<VisitedRegion>>(emptyList())
    override val visitedRegionsFlow: StateFlow<List<VisitedRegion>> = _visitedRegionsFlow.asStateFlow()
    
    private val _visitedCitiesFlow = MutableStateFlow<List<VisitedCity>>(emptyList())
    override val visitedCitiesFlow: StateFlow<List<VisitedCity>> = _visitedCitiesFlow.asStateFlow()
    
    /** Countries carry the account's own visit counts, and the regions and cities are its visits. */
    override fun clearAccountData() {
        _countriesFlow.value = emptyList()
        _visitedRegionsFlow.value = emptyList()
        _visitedCitiesFlow.value = emptyList()
    }

    override suspend fun getCountries(): Either<ApiResponse, List<Country>> {
        // If we already have countries cached, return them
        if (_countriesFlow.value.isNotEmpty()) {
            return Either.Right(_countriesFlow.value)
        }
        
        return try {
            val countries = networkDataSource.getCountries().map { it.toDomainModel() }
            _countriesFlow.value = countries
            Either.Right(countries)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getCountries: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getCountries: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getCountries: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun getRegions(countryCode: String): Either<ApiResponse, List<Region>> {
        return try {
            val regions = networkDataSource.getRegions(countryCode).map { it.toDomainModel() }
            Either.Right(regions)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getRegions: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getRegions: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getRegions: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun getVisitedRegions(): Either<ApiResponse, List<VisitedRegion>> {
        return try {
            logger.d { "Fetching visited regions from network..." }
            val visitedRegionsDTO = networkDataSource.getVisitedRegions()
            logger.d { "Received ${visitedRegionsDTO.size} visited regions DTOs" }
            
            val visitedRegions = visitedRegionsDTO.map { dto ->
                logger.d { "Mapping DTO: id=${dto.id}, userId=${dto.userId}, region=${dto.region}, name=${dto.name}" }
                dto.toDomainModel()
            }
            
            _visitedRegionsFlow.value = visitedRegions
            logger.d { "Successfully mapped ${visitedRegions.size} visited regions" }
            Either.Right(visitedRegions)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getVisitedRegions: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getVisitedRegions: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getVisitedRegions: ${e.message}" }
            e.printStackTrace()
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>> {
        return try {
            val visitedCities = networkDataSource.getVisitedCities().map { it.toDomainModel() }
            _visitedCitiesFlow.value = visitedCities
            Either.Right(visitedCities)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getVisitedCities: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getVisitedCities: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getVisitedCities: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun refreshCountries(): Either<ApiResponse, List<Country>> {
        return try {
            val countries = networkDataSource.getCountries().map { it.toDomainModel() }
            _countriesFlow.value = countries
            
            // Try to refresh visited regions and cities, but don't fail if they return errors
            try {
                getVisitedRegions()
            } catch (e: Exception) {
                logger.e { "Failed to refresh visited regions: ${e.message}" }
            }
            
            try {
                getVisitedCities()
            } catch (e: Exception) {
                logger.e { "Failed to refresh visited cities: ${e.message}" }
            }
            
            Either.Right(countries)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during refreshCountries: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during refreshCountries: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during refreshCountries: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    /**
     * Keeps the cached country's count in step with a tick. The world list is drawn from these
     * counts and was loaded once, so Japan still read 3/47 after its fourth region was ticked
     * (QA 05, WO-02). A region's id starts with its country's code: "JP-23".
     */
    private fun countVisit(regionId: String, delta: Int) {
        val code = regionId.substringBefore('-')
        _countriesFlow.value = _countriesFlow.value.map { country ->
            if (country.countryCode.equals(code, ignoreCase = true)) {
                country.copy(numVisits = (country.numVisits + delta).coerceAtLeast(0))
            } else {
                country
            }
        }
    }

    override suspend fun markRegionVisited(regionId: String): Either<ApiResponse, VisitedRegion> {
        return try {
            val visited = networkDataSource.markRegionVisited(regionId).toDomainModel()
            // Add it to the cached list rather than refetching: the country screen reads this flow
            // and the tick should follow the tap, not a round trip.
            val wasVisited = _visitedRegionsFlow.value.any { it.regionId == regionId }
            _visitedRegionsFlow.value = _visitedRegionsFlow.value
                .filterNot { it.regionId == regionId } + visited
            if (!wasVisited) countVisit(regionId, +1)
            Either.Right(visited)
        } catch (e: HttpException) {
            logger.e { "HTTP Error marking region visited: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error marking region visited: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error marking region visited: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun unmarkRegionVisited(regionId: String): Either<ApiResponse, Unit> {
        return try {
            networkDataSource.unmarkRegionVisited(regionId)
            val wasVisited = _visitedRegionsFlow.value.any { it.regionId == regionId }
            _visitedRegionsFlow.value = _visitedRegionsFlow.value.filterNot { it.regionId == regionId }
            if (wasVisited) countVisit(regionId, -1)
            Either.Right(Unit)
        } catch (e: HttpException) {
            logger.e { "HTTP Error removing visited region: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error removing visited region: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error removing visited region: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun refreshVisitedRegions(): Either<ApiResponse, Pair<Int, Int>> {
        return try {
            val found = networkDataSource.refreshVisitedRegions()
            // The sweep may have added records, so the cached list is stale either way.
            getVisitedRegions()
            Either.Right(found)
        } catch (e: HttpException) {
            logger.e { "HTTP Error refreshing visited regions: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error refreshing visited regions: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error refreshing visited regions: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
}
