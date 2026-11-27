package com.desarrollodroide.adventurelog.feature.ui.map.maplibre

import android.content.Context
import org.maplibre.compose.map.DefaultMapRuntime
import org.maplibre.compose.resource.MapRequestInterceptor

/**
 * Process-wide MapLibre settings, applied before the first map.
 *
 * Every request names the app. The OpenStreetMap tile policy blocks generic library agents, and
 * the other volunteer-run servers (OpenStreetMap France, OpenTopoMap, OpenFreeMap) ask to know who
 * is calling.
 */
internal object MapLibreSetup {

    @Volatile
    private var configured = false

    fun ensure(context: Context) {
        if (configured) return
        synchronized(this) {
            if (configured) return
            val userAgent = "AdventureLog/${versionName(context)} (Android; " +
                "+https://github.com/DesarrolloAntonio/AdventureLog-Client)"
            // Throws only if a map already started the runtime, which nothing else in the app does.
            runCatching {
                DefaultMapRuntime.configure {
                    requestInterceptor = MapRequestInterceptor(headers = { mapOf("User-Agent" to userAgent) })
                }
            }
            configured = true
        }
    }

    private fun versionName(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "unknown"
}
