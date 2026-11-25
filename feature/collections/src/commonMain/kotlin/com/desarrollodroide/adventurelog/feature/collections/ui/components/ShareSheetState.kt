package com.desarrollodroide.adventurelog.feature.collections.ui.components

import com.desarrollodroide.adventurelog.core.model.PublicUser

/** What the sheet knows. [invitedThisVisit] is deliberately not persisted - see the sheet's note. */
data class ShareSheetState(
    val people: List<PublicUser> = emptyList(),
    val sharedWith: Set<String> = emptySet(),
    val invitedThisVisit: Set<String> = emptySet(),
    val busyUuid: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    /** The outcome of the last invite, revoke or remove - said here, not in a snackbar behind the sheet. */
    val message: SheetMessage? = null
)

data class SheetMessage(val text: String, val isError: Boolean)
