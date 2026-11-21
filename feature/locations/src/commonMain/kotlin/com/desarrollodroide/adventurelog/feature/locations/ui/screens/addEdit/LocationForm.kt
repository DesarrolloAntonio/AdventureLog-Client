package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit

import com.desarrollodroide.adventurelog.core.model.ContentImage
import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType

// Split out of AddEditLocationScreen.kt (Compose, androidMain): the form's starting state is plain
// logic, tested on every platform.

/** A photo the place already has, as the form holds it: known by its server id, never uploaded again. */
internal fun formImageOf(image: ContentImage) = ImageFormData(
    uri = image.image,
    type = ImageType.URL,
    isPrimary = image.isPrimary,
    serverId = image.id
)

/**
 * The form as it starts: from the place being edited, or empty for a new one. Kept apart from the
 * screen so the unchanged state can be told from an edited one, and restored after a process death.
 */
internal fun locationFormOf(existingLocation: Location?, defaultCurrency: String): LocationFormData =
    if (existingLocation != null) {
        LocationFormData(
            name = existingLocation.name,
            description = existingLocation.description ?: "",
            category = existingLocation.category,
            rating = existingLocation.rating?.toInt() ?: 0,
            price = existingLocation.price?.let(Currencies::formatAmount) ?: "",
            priceCurrency = existingLocation.priceCurrency ?: Currencies.DEFAULT,
            link = existingLocation.link ?: "",
            location = existingLocation.location ?: "",
            latitude = existingLocation.latitude,
            longitude = existingLocation.longitude,
            isPublic = existingLocation.isPublic,
            tags = existingLocation.tags,
            visits = existingLocation.visits.map(::visitFormOf),
            trails = existingLocation.trails.map { trail ->
                TrailFormData(
                    id = trail.id,
                    name = trail.name,
                    link = trail.link ?: trail.wandererLink.orEmpty()
                )
            },
            images = existingLocation.images.map(::formImageOf)
        )
    } else {
        LocationFormData(
            // No category is chosen for a new place, as on the web: the one this used to pick was
            // read before the categories had loaded, so it was always none anyway.
            category = null,
            // The web pre-fills money fields with the account's preferred currency; a new
            // location that always said USD would make every European price wrong by
            // default.
            priceCurrency = defaultCurrency
        )
    }
