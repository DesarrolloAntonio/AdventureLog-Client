package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.model.response.VisitDTO

/**
 * Visits are their own resource rather than a nested field of a location: the server rejects a
 * location payload carrying visits, because each visit needs the id of a location that does not
 * exist yet at creation time. The web client has the same constraint - its Visits step only
 * unlocks once the location has been saved.
 */
interface VisitApi {

    suspend fun createVisit(locationId: String, visit: VisitFormData): VisitDTO

    suspend fun updateVisit(visitId: String, locationId: String, visit: VisitFormData): VisitDTO

    suspend fun deleteVisit(visitId: String)
}
