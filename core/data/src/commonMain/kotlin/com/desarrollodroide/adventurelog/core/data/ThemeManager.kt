package com.desarrollodroide.adventurelog.core.data

import androidx.compose.runtime.MutableState
import com.desarrollodroide.adventurelog.core.constants.ThemeMode

interface ThemeManager {
    var themeMode: MutableState<ThemeMode>
    var useDynamicColors: MutableState<Boolean>
}