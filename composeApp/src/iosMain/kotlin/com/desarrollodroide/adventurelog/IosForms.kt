package com.desarrollodroide.adventurelog

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportDates
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.locationFormOf
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType

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
    fun addVisit(
        base: LocationFormData, start: String, end: String, notes: String,
        startTime: String?, endTime: String?, allDay: Boolean, timezone: String,
    ): LocationFormData =
        base.copy(visits = base.visits + timed(VisitFormData(startDate = start, notes = notes), end, startTime, endTime, allDay, timezone))

    /**
     * [base] with visit [id] moved to [start]-[end], at [startTime]-[endTime] in [timezone] unless it
     * is [allDay], and given [notes]. The other visits stay as they were.
     */
    fun editVisit(
        base: LocationFormData, id: String, start: String, end: String, notes: String,
        startTime: String?, endTime: String?, allDay: Boolean, timezone: String,
    ): LocationFormData =
        base.copy(
            visits = base.visits.map {
                if (it.id == id) timed(it.copy(startDate = start, notes = notes), end, startTime, endTime, allDay, timezone) else it
            },
        )

    /** A visit all day has no hours: the server reads a visit with none as the whole day, as Android sends it. */
    private fun timed(
        visit: VisitFormData, end: String, startTime: String?, endTime: String?, allDay: Boolean, timezone: String,
    ): VisitFormData = visit.copy(
        endDate = end.ifBlank { null },
        startTime = startTime.takeUnless { allDay },
        endTime = endTime.takeUnless { allDay },
        isAllDay = allDay,
        timezone = timezone,
    )

    /** [base] without visit [id]; saving deletes it on the server. */
    fun removeVisit(base: LocationFormData, id: String): LocationFormData =
        base.copy(visits = base.visits.filterNot { it.id == id })

    /**
     * [base] with its website (https:// added when it has no scheme, as the server takes only full URLs), price as typed with a decimal comma made a dot (kept as text so a half-typed amount is not rounded),
     * the price's currency, and tags trimmed, without blanks or repeats.
     */
    fun withDetails(base: LocationFormData, link: String, price: String, currency: String, tags: List<String>): LocationFormData =
        base.copy(
            link = link.trim().let { if (it.isEmpty() || "://" in it) it else "https://$it" },
            price = price.trim().replace(',', '.'),
            priceCurrency = currency,
            tags = tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
        )

    /** A trail as the form holds it; [id] is null for one the server has not seen. */
    fun trail(id: String?, name: String, link: String): TrailFormData = TrailFormData(id = id, name = name, link = link)

    /** [base] with these trails; saving adds, changes and deletes them to match. Blank rows are dropped. */
    fun withTrails(base: LocationFormData, trails: List<TrailFormData>): LocationFormData =
        base.copy(trails = trails.filter { it.name.isNotBlank() || it.link.isNotBlank() })

    /** A photo picked on the phone, written to [fileUrl] ("file://..."); saving uploads it. */
    fun localPhoto(fileUrl: String): ImageFormData = ImageFormData(uri = fileUrl, type = ImageType.LOCAL_FILE)

    /** A photo on the web, a link pasted or Wikipedia's for the place; the save downloads and uploads it. */
    fun webPhoto(url: String, wikipedia: Boolean): ImageFormData =
        ImageFormData(uri = url.trim(), type = if (wikipedia) ImageType.WIKIPEDIA else ImageType.URL)

    /**
     * [base] with these photos: the server's that are left (a removed one is deleted on save) and
     * new ones to upload. The first stays the cover if the cover was removed.
     */
    fun withPhotos(base: LocationFormData, photos: List<ImageFormData>): LocationFormData {
        val cover = photos.any { it.isPrimary }
        return base.copy(
            images = photos.mapIndexed { i, photo ->
                if (!cover && i == 0 && photo.serverId != null) photo.copy(isPrimary = true) else photo
            },
        )
    }

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
