package com.desarrollodroide.adventurelog.feature.world.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CountryDetailScreen(
    countryCode: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement country detail screen
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Country Detail: $countryCode")
    }
}