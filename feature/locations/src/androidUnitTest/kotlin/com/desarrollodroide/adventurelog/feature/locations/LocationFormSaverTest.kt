package com.desarrollodroide.adventurelog.feature.locations

import androidx.compose.runtime.saveable.SaverScope
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.testing.testCategory
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormSaver
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import kotlin.test.Test
import kotlin.test.assertEquals

/** The place form across a process death. */
class LocationFormSaverTest {

    private val anywhere = object : SaverScope {
        override fun canBeSaved(value: Any) = true
    }

    @Test
    fun `a half filled form comes back as it was`() {
        // Typed into Add place, then the app was killed in the background: the form came back empty.
        val typed = LocationFormData(
            name = "QA_R9_typed",
            description = "half written",
            category = testCategory("Nature"),
            rating = 4,
            price = "12.5",
            priceCurrency = "EUR",
            tags = listOf("qa_tag"),
            visits = listOf(VisitFormData(id = "v1", startDate = "2025-07-01", startTime = "10:30", endTime = "18:45", timezone = "America/New_York", isAllDay = false)),
            trails = listOf(TrailFormData(name = "QA trail", link = "https://example.org/trail")),
            images = listOf(
                ImageFormData("https://qa.test/media/i1.webp", ImageType.URL, isPrimary = true, serverId = "i1"),
                ImageFormData("content://picked/new.jpg", ImageType.LOCAL_FILE)
            )
        )

        val saved = with(LocationFormSaver) { anywhere.save(typed) }!!

        assertEquals(typed, LocationFormSaver.restore(saved))
    }
}
