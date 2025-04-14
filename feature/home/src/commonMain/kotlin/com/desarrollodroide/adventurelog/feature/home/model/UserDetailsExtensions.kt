package com.desarrollodroide.adventurelog.feature.home.model

import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * Extensions for the UserDetails class to facilitate its use in the UI.
 */
val UserDetails.fullName: String
    get() = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
        "$firstName $lastName"
    } else if (firstName.isNotEmpty()) {
        firstName
    } else if (lastName.isNotEmpty()) {
        lastName
    } else {
        username
    }