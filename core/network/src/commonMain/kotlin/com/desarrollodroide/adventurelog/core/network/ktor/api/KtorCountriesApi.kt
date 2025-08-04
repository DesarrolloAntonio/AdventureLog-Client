package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.CountriesApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.RegionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedCityDTO
import com.desarrollodroide.adventurelog.core.network.model.response.VisitedRegionDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class KtorCountriesApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : CountriesApi {
    
    private val logger = Logger.withTag("KtorCountriesApi")
    
    override suspend fun getCountries(): List<CountryDTO> {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/countries/"
        logger.d { "Fetching countries from: $url" }
        
        val response = httpClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
        }
        
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            logger.e { "Failed to fetch countries with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to fetch countries with status: ${response.status}"
            )
        }
    }
    
    override suspend fun getRegions(countryCode: String): List<RegionDTO> {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/$countryCode/regions/"
        logger.d { "Fetching regions for country: $countryCode from: $url" }
        
        val response = httpClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
        }
        
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            logger.e { "Failed to fetch regions with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to fetch regions with status: ${response.status}"
            )
        }
    }
    
    override suspend fun getVisitedRegions(): List<VisitedRegionDTO> {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/visitedregion/"
        logger.d { "Fetching visited regions from: $url" }
        
        val response = httpClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
        }
        
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            logger.e { "Failed to fetch visited regions with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to fetch visited regions with status: ${response.status}"
            )
        }
    }
    
    override suspend fun getVisitedCities(): List<VisitedCityDTO> {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/visitedcity/"
        logger.d { "Fetching visited cities from: $url" }
        
        val response = httpClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
        }
        
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            logger.e { "Failed to fetch visited cities with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to fetch visited cities with status: ${response.status}"
            )
        }
    }
}
