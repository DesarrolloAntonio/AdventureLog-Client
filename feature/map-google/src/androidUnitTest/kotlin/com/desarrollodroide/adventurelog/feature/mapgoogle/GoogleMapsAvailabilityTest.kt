package com.desarrollodroide.adventurelog.feature.mapgoogle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoogleMapsAvailabilityTest {

    @Test
    fun `asks the Maps SDK once and keeps the answer`() {
        var calls = 0
        val availability = GoogleMapsAvailability()
        repeat(3) { assertTrue(availability.isAvailable { calls++; SUCCESS }) }
        assertEquals(1, calls)
    }

    // MapsInitializer.initialize returns SERVICE_MISSING (1) on a phone without Play Services.
    @Test
    fun `any other status means Google cannot draw here`() {
        assertFalse(GoogleMapsAvailability().isAvailable { SERVICE_MISSING })
    }

    @Test
    fun `an initializer that throws means Google cannot draw here`() {
        assertFalse(GoogleMapsAvailability().isAvailable { error("no Play Services") })
    }

    private companion object {
        const val SUCCESS = 0
        const val SERVICE_MISSING = 1
    }
}
