package com.desarrollodroide.adventurelog.core.data

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
) : CountriesRepository {

    private val _countriesFlow = MutableStateFlow<List<Country>>(emptyList())
    override val countriesFlow: StateFlow<List<Country>> = _countriesFlow.asStateFlow()
    
    private val _visitedRegionsFlow = MutableStateFlow<List<VisitedRegion>>(emptyList())
    override val visitedRegionsFlow: StateFlow<List<VisitedRegion>> = _visitedRegionsFlow.asStateFlow()
    
    private val _visitedCitiesFlow = MutableStateFlow<List<VisitedCity>>(emptyList())
    override val visitedCitiesFlow: StateFlow<List<VisitedCity>> = _visitedCitiesFlow.asStateFlow()
    
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
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
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
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
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
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
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
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
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
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
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

    override suspend fun markRegionVisited(regionId: String): Either<ApiResponse, VisitedRegion> {
        return try {
            val visited = networkDataSource.markRegionVisited(regionId).toDomainModel()
            // Add it to the cached list rather than refetching: the country screen reads this flow
            // and the tick should follow the tap, not a round trip.
            _visitedRegionsFlow.value = _visitedRegionsFlow.value
                .filterNot { it.regionId == regionId } + visited
            Either.Right(visited)
        } catch (e: HttpException) {
            logger.e { "HTTP Error marking region visited: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
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
            _visitedRegionsFlow.value = _visitedRegionsFlow.value.filterNot { it.regionId == regionId }
            Either.Right(Unit)
        } catch (e: HttpException) {
            logger.e { "HTTP Error removing visited region: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
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
}
