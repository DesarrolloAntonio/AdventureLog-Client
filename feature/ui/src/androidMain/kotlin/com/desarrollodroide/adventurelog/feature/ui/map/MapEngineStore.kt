package com.desarrollodroide.adventurelog.feature.ui.map

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The "Map engine" setting. It stays on the phone, like the theme: which engine can draw depends
 * on the device, so it is not an account preference the web could share.
 */
class MapEngineStore(private val settings: Settings) {

    private val _engine = MutableStateFlow(read())
    val engine: StateFlow<MapEngine> = _engine.asStateFlow()

    fun set(engine: MapEngine) {
        settings.putString(KEY, engine.name)
        _engine.value = engine
    }

    private fun read(): MapEngine =
        settings.getStringOrNull(KEY)?.let { stored -> MapEngine.entries.firstOrNull { it.name == stored } }
            ?: MapEngine.AUTO

    private companion object {
        const val KEY = "map_engine"
    }
}
