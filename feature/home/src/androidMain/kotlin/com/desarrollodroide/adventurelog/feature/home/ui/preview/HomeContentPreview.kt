package com.desarrollodroide.adventurelog.feature.home.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.ui.components.home.HomeContent

/**
 * Preview for HomeContent in different states
 */
@Preview(
    name = "Home Content - Empty State",
    showBackground = true,
    backgroundColor = 0xFFF3F3F3
)
@Composable
fun HomeContentEmptyPreview() {
    HomeContent(
        homeUiState = HomeUiState.Empty
    )
}

@Preview(
    name = "Home Content - Loading State",
    showBackground = true,
    backgroundColor = 0xFFF3F3F3
)
@Composable
fun HomeContentLoadingPreview() {
    HomeContent(
        homeUiState = HomeUiState.Loading
    )
}

@Preview(
    name = "Home Content - Success State",
    showBackground = true,
    backgroundColor = 0xFFF3F3F3
)
@Composable
fun HomeContentSuccessPreview() {
    // Create sample user stats based on the JSON response
    val sampleStats = UserStats(
        adventureCount = 12,
        tripsCount = 5,
        visitedCityCount = 0,
        totalCities = 15020,
        visitedRegionCount = 1,
        totalRegions = 5062,
        visitedCountryCount = 1,
        totalCountries = 250
    )
    
    // Create some sample adventure items
    val sampleAdventures = emptyList<Adventure>()
    
    HomeContent(
        homeUiState = HomeUiState.Success(
            userName = "Antonio",
            recentAdventures = sampleAdventures,
            userStats = sampleStats
        )
    )
}

@Preview(
    name = "Home Content - Error State",
    showBackground = true,
    backgroundColor = 0xFFF3F3F3
)
@Composable
fun HomeContentErrorPreview() {
    HomeContent(
        homeUiState = HomeUiState.Error("Failed to load adventures")
    )
}
