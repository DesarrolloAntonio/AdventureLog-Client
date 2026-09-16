package com.desarrollodroide.adventurelog.feature.map.ui.state

import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion

/**
 * The title and snippet a map marker is announced with ("title. snippet").
 *
 * A visited city had a title and no snippet, so TalkBack read "Ullensvang. " with an empty second
 * sentence; a visited region had neither and read nothing at all (QA 06, MP-04).
 */
data class MarkerLabel(val title: String, val snippet: String)

fun VisitedCity.markerLabel() = MarkerLabel(title = name, snippet = "Visited city")

fun VisitedRegion.markerLabel() = MarkerLabel(title = name, snippet = "Visited region")
