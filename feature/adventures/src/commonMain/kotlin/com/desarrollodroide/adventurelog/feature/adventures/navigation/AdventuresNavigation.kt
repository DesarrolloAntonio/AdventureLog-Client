package com.desarrollodroide.adventurelog.feature.adventures.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.adventures.adventures.AdventureListScreen

/**
 * Navigation graph for adventures
 */
fun NavGraphBuilder.adventuresGraph(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    navigation<AdventuresRoute>(
        startDestination = AdventuresScreen
    ) {
        // Adventure list screen
        composable<AdventuresScreen> {
            AdventureListScreen(
                adventureItems = PreviewData.adventures
            )
        }

        // Screen with details of a specific adventure (for future implementation)
        composable<AdventureDetails> { backStackEntry ->
            val adventureId = backStackEntry.arguments?.getString(AdventureDetails.adventureIdArg) ?: ""
            
            // For now we show the complete list
            // In the future: AdventureDetailsScreen(adventureId = adventureId)
            AdventureListScreen(
                adventureItems = PreviewData.adventures
            )
        }
    }
}