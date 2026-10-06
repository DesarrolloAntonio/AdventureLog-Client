package com.desarrollodroide.adventurelog.feature.locations

import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.placesLoadErrorMessage
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.refreshHasEnded
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlacesLoadStateTest {

    @Test
    fun `a refresh that failed has ended`() {
        // With no network the spinner kept turning: only success ended it (measured at 90 s).
        assertTrue(refreshHasEnded(LoadStateError(IOException("Unable to resolve host"))))
        assertTrue(refreshHasEnded(LoadStateNotLoading(endOfPaginationReached = false)))
    }

    @Test
    fun `a refresh still loading has not ended`() {
        assertFalse(refreshHasEnded(LoadStateLoading))
    }

    @Test
    fun `no network is said in words and not as the exception`() {
        assertEquals(
            "Can't reach the server. Check your connection.",
            placesLoadErrorMessage(IOException("Unable to resolve host \"ds224.boga-aeolian.ts.net\": No address associated with hostname"))
        )
        assertEquals("Your places could not be loaded. Please try again.", placesLoadErrorMessage(IllegalStateException("status: 500")))
    }
}
