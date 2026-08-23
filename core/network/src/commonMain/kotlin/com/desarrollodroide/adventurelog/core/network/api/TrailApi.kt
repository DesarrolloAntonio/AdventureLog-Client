package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.network.model.response.TrailDTO

interface TrailApi {

    suspend fun createTrail(locationId: String, trail: TrailFormData): TrailDTO

    suspend fun updateTrail(trailId: String, locationId: String, trail: TrailFormData): TrailDTO

    suspend fun deleteTrail(trailId: String)
}
