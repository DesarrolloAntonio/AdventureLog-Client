package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.data

import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageType

data class TransportationFormData(
    val name: String = "",
    val type: String = "car",
    val description: String = "",
    val rating: Int = 0,
    val link: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val departureDate: String = "",
    val arrivalDate: String = "",
    val departureTimezone: String = "UTC",
    val arrivalTimezone: String = "UTC",
    val isAllDay: Boolean = true,
    val constrainToCollectionDates: Boolean = false,
    val flightNumber: String = "",
    val distance: String = "",
    val originLatitude: String? = null,
    val originLongitude: String? = null,
    val destinationLatitude: String? = null,
    val destinationLongitude: String? = null,
    val isPublic: Boolean = false,
    val images: List<ImageFormData> = emptyList(),
    val attachments: List<String> = emptyList()
) {
    companion object {
        fun fromTransportation(transportation: Transportation): TransportationFormData {
            return TransportationFormData(
                name = transportation.name,
                type = transportation.type,
                description = transportation.description ?: "",
                rating = transportation.rating?.toInt() ?: 0,
                link = transportation.link ?: "",
                fromLocation = transportation.fromLocation ?: "",
                toLocation = transportation.toLocation ?: "",
                departureDate = transportation.date ?: "",
                arrivalDate = transportation.endDate ?: transportation.date ?: "",
                departureTimezone = transportation.startTimezone ?: "UTC",
                arrivalTimezone = transportation.endTimezone ?: "UTC",
                isAllDay = true,
                constrainToCollectionDates = false,
                flightNumber = transportation.flightNumber ?: "",
                distance = transportation.distance ?: "",
                originLatitude = transportation.originLatitude,
                originLongitude = transportation.originLongitude,
                destinationLatitude = transportation.destinationLatitude,
                destinationLongitude = transportation.destinationLongitude,
                isPublic = transportation.isPublic,
                images = transportation.images?.map { image ->
                    ImageFormData(
                        uri = image.image,
                        type = ImageType.URL,
                        isPrimary = false
                    )
                } ?: emptyList(),
                attachments = transportation.attachments?.map { it.file } ?: emptyList()
            )
        }
        
        fun getDefaultTransportationTypes(): List<String> = listOf(
            "car",
            "plane",
            "train",
            "bus",
            "boat",
            "bike",
            "walking",
            "other"
        )
    }
}
