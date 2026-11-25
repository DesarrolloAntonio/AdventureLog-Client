package com.desarrollodroide.adventurelog.feature.home

import com.desarrollodroide.adventurelog.feature.home.ui.navigation.isSectionSwitch
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Which moves fade through (changing section) and which slide (going deeper inside one). */
class SectionSwitchTest {

    @Test
    fun movingBetweenTabsFadesThrough() {
        assertTrue(isSectionSwitch("home", "adventures"))
        assertTrue(isSectionSwitch("adventures", "map"))
        assertTrue(isSectionSwitch("travel", "collections"))
    }

    @Test
    fun theAccountMenusScreensFadeThroughToo() {
        assertTrue(isSectionSwitch("home", "calendar"))
        assertTrue(isSectionSwitch("adventures", "settings"))
        assertTrue(isSectionSwitch("settings", "users"))
    }

    @Test
    fun leavingADeepPageForAnotherTabFadesThrough() {
        assertTrue(isSectionSwitch("collection/{collectionId}/{collectionName}", "map"))
        assertTrue(isSectionSwitch("travel/{countryCode}", "home"))
    }

    @Test
    fun goingDeeperInsideASectionSlides() {
        assertFalse(isSectionSwitch("collections", "collection/{collectionId}/{collectionName}"))
        assertFalse(isSectionSwitch("travel", "travel/{countryCode}"))
        assertFalse(isSectionSwitch("adventures", "adventures/add?collectionId={collectionId}"))
        // A form opened from another section is still a step forward, not a section of its own.
        assertFalse(isSectionSwitch("home", "adventures/add?collectionId={collectionId}"))
    }

    @Test
    fun backFromADeepPageToItsOwnTabSlides() {
        assertFalse(isSectionSwitch("collection/{collectionId}/{collectionName}", "collections"))
    }

    @Test
    fun nothingToCompareIsNotASwitch() {
        assertFalse(isSectionSwitch(null, "home"))
        assertFalse(isSectionSwitch("home", null))
        assertFalse(isSectionSwitch("home", "home"))
    }
}
