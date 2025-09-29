package com.desarrollodroide.adventurelog.feature.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Adventure Detail screen
 */
class AdventureDetailViewModel (
    observeCollectionsUseCase: ObserveCollectionsUseCase
): ViewModel() {

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()
    
    // Observe all collections from repository
    private val allCollections: StateFlow<List<UltraSlimCollection>> = observeCollectionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Filter collections for the current location
    fun getCollectionsForLocation(location: Location): StateFlow<List<UltraSlimCollection>> {
        return allCollections.map { collections ->
            location.collections.mapNotNull { collectionId ->
                collections.find { it.id == collectionId }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
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