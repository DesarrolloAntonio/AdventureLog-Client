package com.desarrollodroide.adventurelog.feature.mapgoogle

import com.google.android.gms.common.ConnectionResult

/**
 * Whether the Maps SDK can start on this phone, asked once and remembered.
 *
 * Without Google Play Services (GrapheneOS, a de-Googled ROM) the SDK never initialises, and the
 * first camera move throws "CameraUpdateFactory is not initialized". `MapsInitializer.initialize`
 * reports that as a status code instead of throwing, so it is the question to ask before a Google
 * map is ever composed.
 */
class GoogleMapsAvailability {

    @Volatile
    private var available: Boolean? = null

    /** [initialize] returns a `ConnectionResult` code; it runs on the first call only. */
    fun isAvailable(initialize: () -> Int): Boolean = available ?: synchronized(this) {
        available ?: (runCatching(initialize).getOrNull() == ConnectionResult.SUCCESS).also { available = it }
    }
}
