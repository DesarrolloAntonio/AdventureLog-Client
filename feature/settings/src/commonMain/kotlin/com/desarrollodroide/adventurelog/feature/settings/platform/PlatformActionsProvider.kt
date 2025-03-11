package com.desarrollodroide.adventurelog.feature.settings.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PlatformActionsProvider {
    private val _platformActions = MutableStateFlow<PlatformActions?>(null)
    val platformActions: StateFlow<PlatformActions?> = _platformActions

    fun setPlatformActions(actions: PlatformActions) {
        _platformActions.value = actions
    }
}
