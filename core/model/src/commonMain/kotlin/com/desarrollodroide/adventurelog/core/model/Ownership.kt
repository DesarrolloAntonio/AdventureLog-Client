package com.desarrollodroide.adventurelog.core.model

/**
 * Whether a record owned by [ownerId] belongs to the signed-in [currentUserId].
 *
 * Someone a collection is shared with sees the owner's collection and the owner's places in it, and
 * the server refuses them what only an owner may do (QA 09, MC-02). When either id is unknown the
 * answer is yes: the server still guards every write, and hiding someone's own actions on a guess
 * would be worse than offering one it will refuse.
 */
fun ownedBy(ownerId: String?, currentUserId: String?): Boolean =
    ownerId.isNullOrBlank() || currentUserId.isNullOrBlank() || ownerId == currentUserId
