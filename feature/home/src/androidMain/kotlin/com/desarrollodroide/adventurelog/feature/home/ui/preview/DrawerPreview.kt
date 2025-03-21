package com.desarrollodroide.adventurelog.feature.home.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.home.ui.components.drawer.DrawerContentBody

/**
 * Preview for drawer content using translationX offset to make it visible
 */
@Preview(showBackground = true, name = "Drawer Content - Open")
@Composable
fun DrawerContentPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        DrawerContentBody(
            userName = "Antonio",
            adventureCount = 42,
            selectedItem = 0,
            onSelectionChanged = {},
            visible = true,
            drawerOpen = true,
            onHomeClick = {},
            onAdventuresClick = {},
            onCollectionsClick = {},
            onTravelClick = {},
            onMapClick = {},
            onCalendarClick = {},
            onSettingsClick = {},
            onHelpClick = {}
        )
    }
}
