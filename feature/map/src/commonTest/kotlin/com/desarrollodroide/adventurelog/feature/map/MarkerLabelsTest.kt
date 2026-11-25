package com.desarrollodroide.adventurelog.feature.map

import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.feature.map.ui.state.MarkerLabel
import com.desarrollodroide.adventurelog.feature.map.ui.state.markerLabel
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the map says to a screen reader (QA 06). */
class MarkerLabelsTest {

    @Test
    fun aVisitedCityAndRegionAreAnnouncedWithWhatTheyAre() {
        // MP-04: a city read "Ullensvang. " and a region read nothing.
        assertEquals(
            MarkerLabel("Ullensvang", "Visited city"),
            VisitedCity(1, "u", "NO-46-79622", "Ullensvang", 6.72, 60.37).markerLabel()
        )
        assertEquals(
            MarkerLabel("Kyōto", "Visited region"),
            VisitedRegion(1, "u", "JP-26", "Kyōto", 135.7, 35.0).markerLabel()
        )
    }
}
