package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Who may be offered an owner's actions (QA 09, MC-02). */
class OwnershipTest {

    @Test
    fun `a record owned by someone else is not yours`() {
        assertFalse(ownedBy(ownerId = "owner-uuid", currentUserId = "guest-uuid"))
    }

    @Test
    fun `a record you own is yours`() {
        assertTrue(ownedBy(ownerId = "owner-uuid", currentUserId = "owner-uuid"))
    }

    @Test
    fun `an owner nobody knows yet does not hide anything`() {
        assertTrue(ownedBy(ownerId = null, currentUserId = "owner-uuid"))
        assertTrue(ownedBy(ownerId = "owner-uuid", currentUserId = null))
    }
}
