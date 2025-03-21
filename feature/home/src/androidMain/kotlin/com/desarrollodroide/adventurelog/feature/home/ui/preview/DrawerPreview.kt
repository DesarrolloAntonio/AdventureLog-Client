package com.desarrollodroide.adventurelog.feature.home.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.ui.components.drawer.DrawerContent
import com.desarrollodroide.adventurelog.feature.home.ui.components.drawer.DrawerHeader
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen

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
        // Use a Box with right padding to ensure drawer is visible (shifted right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 320.dp) // Force drawer to be visible by shifting right
        ) {
            DrawerContent(
                userName = "John Doe",
                adventureCount = 5,
                currentScreen = CurrentScreen.HOME,
                onHomeClick = {},
                onAdventuresClick = {},
                onCollectionsClick = {},
                onTravelClick = {},
                onMapClick = {},
                onCalendarClick = {},
                onSettingsClick = {},
                onHelpClick = {},
                drawerOpen = true // Simulating open drawer
            )
        }
    }
}

/**
 * Simplified drawer preview that directly renders drawer sheet without animations
 */
@Preview(showBackground = true, name = "Drawer Sheet - Home")
@Composable
fun SimpleDrawerPreview() {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.padding(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.width(280.dp)
        ) {
            ModalDrawerSheet {
                // First add the drawer header
                DrawerHeader(userName = "John Doe", adventureCount = 5)

                // Rest of drawer content would go here...
                // (We'd need to recreate parts of DrawerContent without animations)
            }
        }
    }
}