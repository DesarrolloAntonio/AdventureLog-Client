package com.desarrollodroide.adventurelog.core.data

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

class GeocodeRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork,
    private val ioDispatcher: CoroutineDispatcher
) : GeocodeRepository {
    
    private val logger = Logger.withTag("GeocodeRepository")
    
    override suspend fun searchLocations(query: String): Either<ApiResponse, List<GeocodeSearchResult>> =
        withContext(ioDispatcher) {
            try {
                logger.d { "Searching locations for query: $query" }
                val results = networkDataSource.searchLocations(query)
                val domainResults = results.map { dto ->
                    GeocodeSearchResult(
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        name = dto.name,
                        displayName = dto.displayName,
                        type = dto.type,
                        category = dto.category,
                        importance = dto.importance,
                        addressType = dto.addressType,
                        poweredBy = dto.poweredBy
                    )
                }
                Either.Right(domainResults)
            } catch (e: HttpException) {
                logger.e { "HTTP Error searching locations: ${e.code}" }
                when (e.code) {
                    401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                    else -> Either.Left(ApiResponse.HttpError)
                }
            } catch (e: IOException) {
                logger.e { "IO Error searching locations: ${e.message}" }
                Either.Left(ApiResponse.IOException)
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error searching locations: ${e.message}" }
                Either.Left(ApiResponse.HttpError)
            }
        }
    
    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Either<ApiResponse, ReverseGeocodeResult> = withContext(ioDispatcher) {
        try {
            logger.d { "Reverse geocoding for lat: $latitude, lon: $longitude" }
            val result = networkDataSource.reverseGeocode(latitude, longitude)
            val domainResult = ReverseGeocodeResult(
                regionId = result.regionId,
                region = result.region,
                country = result.country,
                countryId = result.countryId,
                regionVisited = result.regionVisited,
                displayName = result.displayName,
                city = result.city,
                cityId = result.cityId,
                cityVisited = result.cityVisited,
                locationName = result.locationName,
                error = result.error
            )
            Either.Right(domainResult)
        } catch (e: HttpException) {
            logger.e { "HTTP Error reverse geocoding: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error reverse geocoding: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error reverse geocoding: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
}
