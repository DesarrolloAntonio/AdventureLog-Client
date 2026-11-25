package com.desarrollodroide.adventurelog.core.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSUserDefaults
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrService

/**
 * The keychain, readable once the phone has been unlocked after a restart and never carried into
 * backups or onto another phone ("ThisDeviceOnly"). Builds before this one kept the session in
 * NSUserDefaults, which iOS backs up; the first run moves it here and deletes it there.
 */
@OptIn(ExperimentalSettingsImplementation::class, ExperimentalSettingsApi::class, ExperimentalForeignApi::class)
internal actual fun platformSettings(): Settings {
    val keychain = KeychainSettings(
        kSecAttrService to CFBridgingRetain("com.desarrollodroide.adventurelog"),
        kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
    )
    val defaults = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    moveSettings(from = defaults, to = keychain, movedFlag = "settings_in_keychain")
    return keychain
}
