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
