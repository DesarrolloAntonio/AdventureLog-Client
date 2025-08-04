package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.GeocodeSearchResultDTO
import com.desarrollodroide.adventurelog.core.network.model.response.ReverseGeocodeResultDTO

interface GeocodingApi {
    suspend fun searchLocations(query: String): List<GeocodeSearchResultDTO>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResultDTO
}
