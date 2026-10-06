package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTab
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionsTabContent
import com.desarrollodroide.adventurelog.feature.collections.model.collectionsHeader
import kotlin.test.Test
import kotlin.test.assertEquals

/** QA RL-08: the Invites tab said "0 collections · Invites" above two invitations. */
class CollectionsHeaderTest {

    private fun invite(id: String) = CollectionInvite(
        id = id, collectionId = "c$id", collectionName = "QA_$id", ownerUsername = "claude2", createdAt = ""
    )

    private fun trip(id: String) = UltraSlimCollection(
        id = id, name = "QA_$id", description = "", isPublic = false, isArchived = true,
        createdAt = "", updatedAt = "", startDate = null, endDate = null, adventureCount = 0,
        featuredImage = null, link = null
    )

    @Test
    fun `the invites tab counts its invitations`() {
        val content = CollectionsTabContent(invites = listOf(invite("1"), invite("2")))

        assertEquals("2 invitations", collectionsHeader(CollectionsTab.INVITES, collectionCount = 0, content))
        assertEquals("1 invitation", collectionsHeader(CollectionsTab.INVITES, 0, CollectionsTabContent(invites = listOf(invite("1")))))
    }

    @Test
    fun `the other tabs still count collections`() {
        val archived = CollectionsTabContent(collections = listOf(trip("1"), trip("2"), trip("3")))

        assertEquals("6 collections", collectionsHeader(CollectionsTab.MINE, collectionCount = 6, CollectionsTabContent()))
        assertEquals("3 collections · Archived", collectionsHeader(CollectionsTab.ARCHIVED, collectionCount = 6, archived))
        assertEquals("1 collection · Shared", collectionsHeader(CollectionsTab.SHARED, 6, CollectionsTabContent(collections = listOf(trip("1")))))
    }
}
