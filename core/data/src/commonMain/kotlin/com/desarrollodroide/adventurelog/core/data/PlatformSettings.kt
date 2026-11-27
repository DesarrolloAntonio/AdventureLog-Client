package com.desarrollodroide.adventurelog.core.data

import com.russhwolf.settings.Settings

/**
 * Where the app keeps its session and preferences: SharedPreferences on Android (left out of backups
 * by the manifest), the keychain on iOS for this device only.
 */
internal expect fun platformSettings(): Settings

/** Every key the app writes, by type: what [moveSettings] carries from an older store. */
internal object StoredKeys {
    val strings = listOf("user_session", "user_details", "login_credentials", "remember_username", "remember_url")
    val ints = listOf("theme_mode", "remember_user_id")
    val booleans = listOf("use_dynamic_colors")

    /** Written by older builds and read by none: dropped, not carried. */
    val dropped = listOf("remember_password", "compact_view")
}

/**
 * Moves what an older build stored in [from] into [to], once: [movedFlag] in [from] marks it done.
 * On the first run [to] is emptied first - the keychain outlives an uninstall, so without this a
 * reinstalled app would come back signed in to whatever account it last had.
 */
internal fun moveSettings(from: Settings, to: Settings, movedFlag: String) {
    if (from.getBoolean(movedFlag, false)) return
    to.clear()
    StoredKeys.strings.forEach { key -> from.getStringOrNull(key)?.let { to.putString(key, it) } }
    StoredKeys.ints.forEach { key -> from.getIntOrNull(key)?.let { to.putInt(key, it) } }
    StoredKeys.booleans.forEach { key -> from.getBooleanOrNull(key)?.let { to.putBoolean(key, it) } }
    (StoredKeys.strings + StoredKeys.ints + StoredKeys.booleans + StoredKeys.dropped).forEach(from::remove)
    from.putBoolean(movedFlag, true)
}
