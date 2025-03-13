package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun VisualSection(
    themeMode: StateFlow<ThemeMode>,
    dynamicColors: StateFlow<Boolean>,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    onDynamicColorsChanged: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 5.dp)
    ) {
        Text(text = "Visual", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(5.dp))
        ThemeOption(
            item = Item("Theme", Icons.Filled.Palette, onClick = {}),
            themeMode = themeMode,
            onThemeModeChanged = onThemeModeChanged
        )
        
        val currentDynamicColors by dynamicColors.collectAsState()
        SwitchOption(
            title = "Use dynamic colors",
            icon = Icons.Filled.FormatColorFill,
            checked = currentDynamicColors,
            onCheckedChange = onDynamicColorsChanged
        )
    }
}

@Composable
fun ThemeOption(
    item: Item,
    themeMode: StateFlow<ThemeMode>,
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    val currentThemeMode by themeMode.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val newMode = when (currentThemeMode) {
                    ThemeMode.DARK -> ThemeMode.LIGHT
                    ThemeMode.LIGHT -> ThemeMode.AUTO
                    ThemeMode.AUTO -> ThemeMode.DARK
                }
                onThemeModeChanged(newMode)
                item.onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(item.icon, contentDescription = "Change theme")
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.title, modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        )

        val themeIcon = when (currentThemeMode) {
            ThemeMode.DARK -> Icons.Filled.DarkMode
            ThemeMode.LIGHT -> Icons.Filled.LightMode
            ThemeMode.AUTO -> Icons.Filled.HdrAuto
        }
        Icon(themeIcon, contentDescription = "Current theme icon")
    }
}
