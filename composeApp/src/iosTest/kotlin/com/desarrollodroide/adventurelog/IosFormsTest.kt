package com.desarrollodroide.adventurelog

import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The inputs the SwiftUI place and visit forms build for the shared ViewModel. */
class IosFormsTest {

    private val timed = VisitFormData(id = "v1", startDate = "2025-08-30", startTime = "10:00", timezone = "Europe/Oslo")
    private val other = VisitFormData(id = "v2", startDate = "2025-09-01")
    private val base = LocationFormData(name = "Bulnes", visits = listOf(timed, other))

    @Test
    fun editingAVisitKeepsItsTimesAndZoneAndLeavesTheOthers() {
        val edited = IosForms.editVisit(base, "v1", start = "2025-08-31", end = "", notes = "rain",
                                        startTime = "10:00", endTime = null, allDay = false, timezone = "Europe/Oslo")

        val visit = edited.visits.first { it.id == "v1" }
        assertEquals("2025-08-31", visit.startDate)
        assertNull(visit.endDate)
        assertEquals("rain", visit.notes)
        assertEquals("10:00", visit.startTime)
        assertEquals("Europe/Oslo", visit.timezone)
        assertEquals(other, edited.visits.first { it.id == "v2" })
    }

    @Test
    fun removingAVisitDropsOnlyThatOne() {
        assertEquals(listOf(other), IosForms.removeVisit(base, "v1").visits)
    }

    @Test
    fun blankTrailRowsAreNotSaved() {
        val trails = IosForms.withTrails(base, listOf(TrailFormData(name = "Ruta del Cares"), TrailFormData())).trails
        assertEquals(listOf("Ruta del Cares"), trails.map { it.name })
    }

    @Test
    fun removingTheCoverMakesTheNextServerPhotoTheCover() {
        val left = ImageFormData(uri = "https://s/b.jpg", type = ImageType.URL, serverId = "b")
        val added = IosForms.localPhoto("file:///tmp/c.jpg")

        val photos = IosForms.withPhotos(base, listOf(left, added)).images

        assertTrue(photos[0].isPrimary)
        assertEquals(ImageType.LOCAL_FILE, photos[1].type)
        assertNull(photos[1].serverId)
    }

    @Test
    fun tagsAreTrimmedAndKeptOnce() {
        val form = IosForms.withDetails(base, link = " https://x.y ", price = "12,5", currency = "EUR",
                                        tags = listOf(" hike", "hike", "", "food "))
        assertEquals(listOf("hike", "food"), form.tags)
        assertEquals("https://x.y", form.link)
        assertEquals("12.5", form.price)
        assertEquals("EUR", form.priceCurrency)
    }

    @Test
    fun aBareWebsiteGetsHttpsAndABlankOneStaysBlank() {
        assertEquals("https://x.com", IosForms.withDetails(base, "x.com", "", "USD", emptyList()).link)
        assertEquals("http://x.com", IosForms.withDetails(base, "http://x.com", "", "USD", emptyList()).link)
        assertEquals("", IosForms.withDetails(base, "  ", "", "USD", emptyList()).link)
    }

    @Test
    fun aPhotoFromALinkIsDownloadedOnSaveAndAWikipediaOneSaysWhereItCameFrom() {
        val link = IosForms.webPhoto(" https://x.y/a.jpg ", wikipedia = false)
        assertEquals("https://x.y/a.jpg", link.uri)
        assertEquals(ImageType.URL, link.type)
        assertNull(link.serverId)
        assertEquals(ImageType.WIKIPEDIA, IosForms.webPhoto("https://w.org/b.jpg", wikipedia = true).type)
    }

    @Test
    fun aTimedVisitKeepsItsHoursAndZoneAndAnAllDayOneDropsThem() {
        val timedNew = IosForms.addVisit(base, start = "2025-09-02", end = "2025-09-02", notes = "",
                                         startTime = "09:30", endTime = "18:00", allDay = false, timezone = "Asia/Tokyo")
            .visits.last()
        assertEquals("09:30", timedNew.startTime)
        assertEquals("18:00", timedNew.endTime)
        assertEquals("Asia/Tokyo", timedNew.timezone)
        assertEquals(false, timedNew.isAllDay)

        val allDay = IosForms.editVisit(base, "v1", start = "2025-08-30", end = "", notes = "",
                                        startTime = "10:00", endTime = "12:00", allDay = true, timezone = "Europe/Oslo")
            .visits.first { it.id == "v1" }
        assertNull(allDay.startTime)
        assertNull(allDay.endTime)
        assertEquals(true, allDay.isAllDay)
    }
}
