package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.domain.usecase.CANT_REACH_SERVER
import com.desarrollodroide.adventurelog.feature.collections.ui.state.collectionsLoadErrorMessage
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

/** Offline the collections list printed the raw "Unable to resolve host …", server address included. */
class CollectionsLoadErrorTest {

    @Test
    fun `no connection reads like every other screen`() {
        assertEquals(
            CANT_REACH_SERVER,
            collectionsLoadErrorMessage(IOException("Unable to resolve host \"qa.test\": No address associated with hostname"))
        )
    }

    @Test
    fun `any other failure says what could not be loaded`() {
        assertEquals("Your collections could not be loaded. Please try again.", collectionsLoadErrorMessage(IllegalStateException("status: 500")))
    }
}
