package com.desarrollodroide.adventurelog.core.common.navigation

/**
 * Centralized navigation routes definition to avoid circular dependencies between modules
 */
object NavigationRoutes {
    // Routes for main features
    object Login {
        const val route = "login"
    }
    
    object Home {
        const val route = "home"
    }
    
    object Adventures {
        const val route = "adventures"
    }
    
    object Settings {
        const val route = "settings"
    }
    
    object Detail {
        const val route = "detail" 
        
        // Method to create route with parameters
        fun createDetailRoute(adventureId: String): String {
            return "detail/$adventureId"
        }
    }
}