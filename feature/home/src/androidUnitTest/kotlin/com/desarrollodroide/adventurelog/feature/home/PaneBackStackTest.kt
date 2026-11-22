package com.desarrollodroide.adventurelog.feature.home

import androidx.navigation3.runtime.NavKey
import com.desarrollodroide.adventurelog.feature.home.ui.screen.CollectionDetail
import com.desarrollodroide.adventurelog.feature.home.ui.screen.PlaceDetail
import com.desarrollodroide.adventurelog.feature.home.ui.screen.backStackWithoutDeleted
import com.desarrollodroide.adventurelog.feature.home.ui.screen.collectionsBackStackWithoutDeleted
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the two panes keep after something they are showing is deleted. */
class PaneBackStackTest {

    @Serializable
    private data object ListPane : NavKey

    @Test
    fun `the place being shown goes with it`() {
        // On a tablet the pane kept the deleted place - with its Edit and Share - until another
        // one was chosen (measured).
        val stack = listOf(ListPane, PlaceDetail("p1"))

        assertEquals(listOf(ListPane), backStackWithoutDeleted(stack, "p1"))
    }

    @Test
    fun `another place stays where it is`() {
        val stack = listOf(ListPane, PlaceDetail("p2"))

        assertEquals(stack, backStackWithoutDeleted(stack, "p1"))
    }

    @Test
    fun `a collection being shown goes with it and another stays`() {
        val stack = listOf(ListPane, CollectionDetail("c1"))

        assertEquals(listOf(ListPane), collectionsBackStackWithoutDeleted(stack, "c1"))
        assertEquals(stack, collectionsBackStackWithoutDeleted(stack, "c2"))
    }
}
