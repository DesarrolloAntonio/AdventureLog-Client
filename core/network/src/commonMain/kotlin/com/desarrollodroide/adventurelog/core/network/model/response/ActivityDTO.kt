package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Activity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ActivityDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("user")
    val user: String,
    
    @SerialName("visit")
    val visit: String,
    
    @SerialName("trail")
    val trail: String? = null,
    
    @SerialName("gpx_file")
    val gpxFile: String? = null,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("sport_type")
    val sportType: String = "General",
    
    @SerialName("distance")
    val distance: Double? = null,
    
    @SerialName("moving_time")
    val movingTime: String? = null,
    
    @SerialName("elapsed_time")
    val elapsedTime: String? = null,
    
    @SerialName("rest_time")
    val restTime: String? = null,
    
    @SerialName("elevation_gain")
    val elevationGain: Double? = null,
    
    @SerialName("elevation_loss")
    val elevationLoss: Double? = null,
    
    @SerialName("elev_high")
    val elevHigh: Double? = null,
    
    @SerialName("elev_low")
    val elevLow: Double? = null,
    
    @SerialName("start_date")
    val startDate: String? = null,
    
    @SerialName("start_date_local")
    val startDateLocal: String? = null,
    
    @SerialName("timezone")
    val timezone: String? = null,
    
    @SerialName("average_speed")
    val averageSpeed: Double? = null,
    
    @SerialName("max_speed")
    val maxSpeed: Double? = null,
    
    @SerialName("average_cadence")
    val averageCadence: Double? = null,
    
    @SerialName("calories")
    val calories: Double? = null,
    
    @SerialName("start_lat")
    val startLat: Double? = null,
    
    @SerialName("start_lng")
    val startLng: Double? = null,
    
    @SerialName("end_lat")
    val endLat: Double? = null,
    
    @SerialName("end_lng")
    val endLng: Double? = null,
    
    @SerialName("external_service_id")
    val externalServiceId: String? = null,
    
    // TODO: Backend issue - API spec is incomplete for geojson field
    //       Current spec: "geojson": {"title": "Geojson", "type": "string", "readOnly": true}
    //       Actual response: "geojson": {"type":"FeatureCollection","features":[]}
    //       The spec should properly define the GeoJSON FeatureCollection structure with its fields
    //       Using JsonElement as temporary workaround until backend provides proper OpenAPI schema
    //       Create GitHub issue for backend team
    @SerialName("geojson")
    val geojson: JsonElement? = null
)

fun ActivityDTO.toDomainModel(): Activity = Activity(
    id = id,
    user = user,
    visit = visit,
    trail = trail,
    gpxFile = gpxFile,
    name = name,
    sportType = sportType,
    distance = distance,
    movingTime = movingTime,
    elapsedTime = elapsedTime,
    restTime = restTime,
    elevationGain = elevationGain,
    elevationLoss = elevationLoss,
    elevHigh = elevHigh,
    elevLow = elevLow,
    startDate = startDate,
    startDateLocal = startDateLocal,
    timezone = timezone,
    averageSpeed = averageSpeed,
    maxSpeed = maxSpeed,
    averageCadence = averageCadence,
    calories = calories,
    startLat = startLat,
    startLng = startLng,
    endLat = endLat,
    endLng = endLng,
    externalServiceId = externalServiceId,
    geojson = geojson?.toString()
)
