package com.desarrollodroide.adventurelog.feature.collections.model

import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection

/**
 * The four lists the collections screen can show, matching the web's tabs. Only [MINE] is paged;
 * the others come back whole from the server.
 */
enum class CollectionsTab(val label: String) {
    MINE("Mine"),
    SHARED("Shared"),
    ARCHIVED("Archived"),
    INVITES("Invites")
}

data class CollectionsTabContent(
    val collections: List<UltraSlimCollection> = emptyList(),
    val invites: List<CollectionInvite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * The line above the search bar. It counts what the tab lists - on Invites that is invitations, not
 * collections: the header said "0 collections · Invites" above two of them (QA RL-08).
 */
internal fun collectionsHeader(tab: CollectionsTab, collectionCount: Int, content: CollectionsTabContent): String =
    when (tab) {
        CollectionsTab.INVITES -> content.invites.size.let { if (it == 1) "1 invitation" else "$it invitations" }
        CollectionsTab.MINE -> if (collectionCount == 1) "1 collection" else "$collectionCount collections"
        else -> content.collections.size.let { "${if (it == 1) "1 collection" else "$it collections"} · ${tab.label}" }
    }
