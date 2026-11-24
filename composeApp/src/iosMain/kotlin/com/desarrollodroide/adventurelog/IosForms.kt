package com.desarrollodroide.adventurelog

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportDates
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.locationFormOf

/**
 * The form inputs the SwiftUI place and journey forms hand their ViewModels. Swift sees a Kotlin
 * data class's constructor and copy with every argument and no defaults, so the forms build their
 * input here from the few fields they edit, over the form as it was loaded ([base]), which keeps
 * everything they do not show (photos, trails, coordinates, attachments) as it was.
 */
object IosForms {

    fun place(location: Location?, defaultCurrency: String): LocationFormData =
        locationFormOf(location, defaultCurrency)

    /**
     * [visited] adds a visit on [today] to a place that has none; turned off, it drops only that
     * visit again. Visits the server already has are never removed from here.
     */
    fun place(
        base: LocationFormData,
        name: String,
        description: String,
        category: Category?,
        rating: Int,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        visited: Boolean,
        today: String,
        timezone: String,
    ): LocationFormData {
        val visits = when {
            visited && base.visits.isEmpty() -> listOf(VisitFormData(startDate = today, timezone = timezone))
            visited -> base.visits
            else -> base.visits.filter { it.id != null }
        }
        return base.copy(
            name = name,
            description = description,
            category = category,
            rating = rating,
            location = location,
            latitude = latitude,
            longitude = longitude,
            isPublic = isPublic,
            visits = visits,
        )
    }

    /** [base] with one more visit, from [start] to [end] ("yyyy-MM-dd"; empty for a single day). */
    fun addVisit(base: LocationFormData, start: String, end: String, notes: String, timezone: String): LocationFormData =
        base.copy(
            visits = base.visits + VisitFormData(
                startDate = start,
                endDate = end.ifBlank { null },
                timezone = timezone,
                notes = notes,
            ),
        )

    fun transport(transportation: Transportation?): TransportationFormData =
        transportation?.let(TransportationFormData::fromTransportation) ?: TransportationFormData()

    /** What the form shows for a stored journey time: "yyyy-MM-dd", or "yyyy-MM-ddTHH:mm" when timed. */
    fun transportLocal(stored: String, zone: String, allDay: Boolean): String {
        val local = TransportDates.toLocal(stored, zone, allDay)
        return if (local.time == null) local.date else "${local.date}T${local.time}"
    }

    /** [departure] and [arrival] as [transportLocal] gives them; empty for none. */
    fun transport(
        base: TransportationFormData,
        name: String,
        type: String,
        description: String,
        link: String,
        from: String,
        to: String,
        departure: String,
        arrival: String,
        allDay: Boolean,
        keepToTripDates: Boolean,
        flightNumber: String,
        isPublic: Boolean,
    ): TransportationFormData {
        fun stored(local: String, zone: String) =
            if (local.isBlank()) "" else TransportDates.toStored(local.take(10), local.drop(11).ifBlank { null }, zone, allDay)
        return base.copy(
            name = name,
            type = type,
            description = description,
            link = link,
            fromLocation = from,
            toLocation = to,
            departureDate = stored(departure, base.departureTimezone),
            arrivalDate = stored(arrival, base.arrivalTimezone),
            isAllDay = allDay,
            constrainToCollectionDates = keepToTripDates,
            flightNumber = flightNumber,
            isPublic = isPublic,
        )
    }
}
