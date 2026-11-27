package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.feature.ui.map.GoogleMapsStatus
import com.desarrollodroide.adventurelog.feature.ui.map.MapEngine
import com.desarrollodroide.adventurelog.feature.ui.map.MapEngineChoice
import com.desarrollodroide.adventurelog.feature.ui.map.MapRenderer
import com.desarrollodroide.adventurelog.feature.ui.map.rememberMapEngineChoice
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsSwitchRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRowDivider
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsGroup

/**
 * The settings on this screen the server knows nothing about. They stay on the phone, and the
 * caption says so - otherwise it is not obvious why changing the theme here leaves the web alone.
 * The map engine is one of them: which engines can draw depends on the phone, not the account.
 */
@Composable
fun AppearanceGroup(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    dynamicColors: Boolean,
    onDynamicColorsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var themeSheetOpen by remember { mutableStateOf(false) }
    var engineSheetOpen by remember { mutableStateOf(false) }
    val mapEngine = rememberMapEngineChoice()
    // The fdroid build has no Google Maps, so there is nothing to choose.
    val canChooseEngine = mapEngine.googleStatus != GoogleMapsStatus.NOT_INSTALLED

    SettingsGroup(
        segmented = true,
        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
        title = "Appearance",
        modifier = modifier,
        caption = "Kept on this phone only."
    ) {
        SettingsRow(
            title = "Theme",
            icon = when (themeMode) {
                ThemeMode.DARK -> Icons.Outlined.DarkMode
                ThemeMode.LIGHT -> Icons.Outlined.LightMode
                ThemeMode.AUTO -> Icons.Outlined.Brightness4
            },
            supporting = themeLabel(themeMode),
            onClick = { themeSheetOpen = true }
        )
        SettingsRowDivider()
        SettingsSwitchRow(
            title = "Dynamic colours",
            icon = Icons.Outlined.FormatColorFill,
            supporting = "Take the palette from your wallpaper",
            checked = dynamicColors,
            onCheckedChange = onDynamicColorsChanged
        )
        if (canChooseEngine) {
            SettingsRowDivider()
            SettingsRow(
                title = "Map engine",
                icon = Icons.Outlined.Map,
                supporting = engineLabel(mapEngine),
                onClick = { engineSheetOpen = true }
            )
        }
    }

    if (engineSheetOpen) {
        val googleReady = mapEngine.googleStatus == GoogleMapsStatus.AVAILABLE
        SettingsChoiceSheet(
            title = "Map engine",
            note = "Which engine draws the maps in the app.",
            selected = mapEngine.preference.name,
            entries = listOf(
                ChoiceEntry(
                    value = MapEngine.AUTO.name,
                    label = "Automatic",
                    supporting = "Google Maps where it can run, MapLibre otherwise",
                    icon = Icons.Outlined.PhoneAndroid
                ),
                ChoiceEntry(
                    value = MapEngine.GOOGLE.name,
                    label = "Google Maps",
                    supporting = if (googleReady) "Google's maps and satellite imagery"
                    else "Needs Google Play services, which this phone does not have",
                    icon = Icons.Outlined.Map,
                    enabled = googleReady
                ),
                ChoiceEntry(
                    value = MapEngine.MAPLIBRE.name,
                    label = "MapLibre",
                    supporting = "Open-source maps that draw your account's map style",
                    icon = Icons.Outlined.Layers
                )
            ),
            onSelect = { value ->
                engineSheetOpen = false
                mapEngine.set(MapEngine.valueOf(value))
            },
            onDismiss = { engineSheetOpen = false }
        )
    }

    if (themeSheetOpen) {
        SettingsChoiceSheet(
            title = "Theme",
            selected = themeMode.name,
            entries = listOf(
                ChoiceEntry(
                    value = ThemeMode.AUTO.name,
                    label = "Follow the system",
                    supporting = "Light or dark, whichever the phone is using",
                    icon = Icons.Outlined.PhoneAndroid
                ),
                ChoiceEntry(
                    value = ThemeMode.LIGHT.name,
                    label = "Light",
                    icon = Icons.Outlined.LightMode
                ),
                ChoiceEntry(
                    value = ThemeMode.DARK.name,
                    label = "Dark",
                    icon = Icons.Outlined.DarkMode
                )
            ),
            onSelect = { value ->
                themeSheetOpen = false
                onThemeModeChanged(ThemeMode.valueOf(value))
            },
            onDismiss = { themeSheetOpen = false }
        )
    }
}

private fun themeLabel(mode: ThemeMode) = when (mode) {
    ThemeMode.AUTO -> "Follow the system"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

private fun engineLabel(choice: MapEngineChoice): String {
    val drawnBy = if (choice.renderer == MapRenderer.GOOGLE) "Google Maps" else "MapLibre"
    return when (choice.preference) {
        MapEngine.AUTO -> "Automatic - $drawnBy"
        MapEngine.GOOGLE -> if (choice.renderer == MapRenderer.GOOGLE) "Google Maps" else "Google Maps unavailable - using MapLibre"
        MapEngine.MAPLIBRE -> "MapLibre"
    }
}
