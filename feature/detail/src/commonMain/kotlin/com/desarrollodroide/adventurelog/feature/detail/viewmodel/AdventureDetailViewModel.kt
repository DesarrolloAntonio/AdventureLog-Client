package com.desarrollodroide.adventurelog.feature.detail.viewmodel

import androidx.lifecycle.ViewModel
import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Adventure Detail screen
 */
class AdventureDetailViewModel (
    // Add Use cases classes here
): ViewModel() {

    private val _adventure = MutableStateFlow<Adventure?>(null)
    val adventure: StateFlow<Adventure?> = _adventure.asStateFlow()
    
    /**
     * Handle edit adventure action
     */
    fun editAdventure(adventureId: String) {
        // Implementation would navigate to edit screen or show edit UI
        println("Edit adventure: $adventureId")
    }
    
    /**
     * Handle opening map
     */
    fun openMap(latitude: String, longitude: String) {
        // Implementation would open map app with coordinates
        println("Open map at: $latitude, $longitude")
    }
    
    /**
     * Handle opening links
     */
    fun openLink(url: String) {
        // Implementation would open URL in browser
        println("Open URL: $url")
    }
}