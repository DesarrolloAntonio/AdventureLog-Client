package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.core.constants.ThemeMode

/**
 * The two settings on this screen the server knows nothing about. They stay on the phone, and the
 * caption says so - otherwise it is not obvious why changing the theme here leaves the web alone.
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

    SettingsGroup(
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
