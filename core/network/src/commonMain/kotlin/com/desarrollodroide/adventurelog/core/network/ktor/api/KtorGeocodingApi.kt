package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.GeocodingApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class KtorGeocodingApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : GeocodingApi {
    
    private val logger = Logger.withTag("KtorGeocodingApi")
    
    override suspend fun searchLocations(query: String): List<GeocodeSearchResultDTO> {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/reverse-geocode/search/"
        logger.d { "Searching locations for query: $query" }
        
        val response = httpClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
            parameter("query", query)
        }
        
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            logger.e { "Failed to search locations with status: ${response.status}" }
            // Return empty list for search failures instead of throwing
            return emptyList()
        }
    }
    
    override suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResultDTO {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/reverse-geocode/reverse_geocode/"
        logger.d { "Reverse geocoding for lat: $latitude, lon: $longitude" }
        
        val response = httpClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
            parameter("lat", latitude.toString())
            parameter("lon", longitude.toString())
        }
        
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            logger.e { "Failed to reverse geocode with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to reverse geocode with status: ${response.status}"
            )
        }
    }
}
