package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.model.Lodging

/** Somewhere to sleep on a trip: the dates, the booking, and where it is. */
interface LodgingApi {
    suspend fun createLodging(
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean,
        collectionId: String
    ): Lodging

    suspend fun updateLodging(
        lodgingId: String,
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean
    ): Lodging

    suspend fun deleteLodging(lodgingId: String)
}
