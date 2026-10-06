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
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.request.VisitedRegionRequest
import io.ktor.client.request.post
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.desarrollodroide.adventurelog.core.network.model.response.MarkVisitedRegionResponse

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

    override suspend fun markRegionVisited(regionId: String): VisitedRegionDTO {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/visitedregion/"
        logger.d { "Marking region $regionId visited at: $url" }

        val response = httpClient.post(url) {
            headers { commonHeaders(sessionInfo.sessionToken) }
            contentType(ContentType.Application.Json)
            setBody(VisitedRegionRequest(region = regionId))
        }

        if (response.status.isSuccess()) {
            return response.body()
        } else {
            logger.e { "Failed to mark region visited: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to mark the region visited: ${response.status}"
            )
        }
    }

    override suspend fun unmarkRegionVisited(regionId: String) {
        val sessionInfo = sessionProvider()
        // Keyed by the region's own code, not by the record's numeric id. The record id is what
        // the list returns and what a REST reflex reaches for; /api/visitedregion/24/ answers 404.
        val url = "${sessionInfo.baseUrl}/api/visitedregion/$regionId/"
        logger.d { "Removing visited region $regionId at: $url" }

        val response = httpClient.delete(url) {
            headers { commonHeaders(sessionInfo.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            logger.e { "Failed to remove visited region: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to remove the visit: ${response.status}"
            )
        }
    }

    override suspend fun refreshVisitedRegions(): Pair<Int, Int> {
        val sessionInfo = sessionProvider()
        // The name says one region, but posting an empty body sweeps every location the account
        // has and reports what it found. That is what the web's "update visited regions" does.
        val url = "${sessionInfo.baseUrl}/api/reverse-geocode/mark_visited_region/"
        logger.d { "Refreshing visited regions at: $url" }

        val response = httpClient.post(url) {
            headers { commonHeaders(sessionInfo.sessionToken) }
            contentType(ContentType.Application.Json)
            setBody("{}")
        }

        if (response.status.isSuccess()) {
            val body = response.body<MarkVisitedRegionResponse>()
            return body.newRegions to body.newCities
        }
        logger.e { "Failed to refresh visited regions: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to update the visited regions: ${response.status}"
        )
    }
}
