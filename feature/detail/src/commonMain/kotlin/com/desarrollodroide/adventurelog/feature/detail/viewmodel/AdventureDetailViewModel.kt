package com.desarrollodroide.adventurelog.feature.detail.viewmodel

import androidx.lifecycle.ViewModel
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Adventure Detail screen
 */
class AdventureDetailViewModel (
    // Add Use cases classes here
): ViewModel() {

    // In a real app, this would be loaded asynchronously from a repository
    private val _adventure = MutableStateFlow<Adventure?>(null)
    val adventure: StateFlow<Adventure?> = _adventure.asStateFlow()

    /**
     * Get adventure by ID
     * This is a simplified implementation that uses preview data
     */
    fun getAdventureById(adventureId: String): Adventure {
        // Find adventure by ID or return first one as fallback
        val foundAdventure = PreviewData.adventures.find { it.id == adventureId } 
                         ?: PreviewData.adventures.first()
        
        // Update the state flow
        _adventure.value = foundAdventure
        
        return foundAdventure
    }
    
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