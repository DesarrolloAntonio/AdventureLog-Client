package com.desarrollodroide.adventurelog.feature.settings.viewmodel

import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.core.model.MapStyles
import com.desarrollodroide.adventurelog.core.model.MediaUsage
import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * The editable profile, held apart from the session so a half-typed name never leaks into the
 * rest of the app before the user presses Update.
 */
data class ProfileForm(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val publicProfile: Boolean = false,
    val imperialUnits: Boolean = false,
    val currency: String = Currencies.DEFAULT,
    val mapStyle: String = MapStyles.DEFAULT
) {
    companion object {
        fun from(user: UserDetails) = ProfileForm(
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            publicProfile = user.publicProfile,
            imperialUnits = user.measurementSystem == "imperial",
            currency = user.defaultCurrency,
            mapStyle = user.mapStyle
        )
    }
}

data class ProfileSectionState(
    val form: ProfileForm = ProfileForm(),
    val saved: ProfileForm = ProfileForm(),
    val isSaving: Boolean = false
) {
    val hasChanges: Boolean get() = form != saved
}

data class EmailsSectionState(
    val addresses: List<EmailAddress> = emptyList(),
    val isLoading: Boolean = true,
    val newAddress: String = "",
    val isBusy: Boolean = false,
    val error: String? = null
)

data class StorageSectionState(
    val usage: MediaUsage? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
