package com.desarrollodroide.adventurelog.feature.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class LocationState {
    data object Loading : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Error(val message: String) : LocationState()
}

class AdventureDetailViewModel(
    private val getLocationUseCase: GetLocationUseCase,
    observeCollectionsUseCase: ObserveCollectionsUseCase
) : ViewModel() {

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Loading)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val allCollections: StateFlow<List<UltraSlimCollection>> = observeCollectionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val collections: StateFlow<List<UltraSlimCollection>> = _locationState.map { state ->
        when (state) {
            is LocationState.Success -> {
                allCollections.value.filter { collection ->
                    state.location.collections.contains(collection.id)
                }
            }
            else -> emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadLocation(locationId: String) {
        viewModelScope.launch {
            _locationState.value = LocationState.Loading
            
            println("📍 [ViewModel] Loading location: $locationId")
            
            when (val result = getLocationUseCase(locationId)) {
                is Either.Right -> {
                    println("✅ [ViewModel] Location loaded: ${result.value.name}")
                    _locationState.value = LocationState.Success(result.value)
                    getLocationUseCase.clearSelectedLocation()
                }
                is Either.Left -> {
                    println("❌ [ViewModel] Error loading location: ${result.value}")
                    _locationState.value = LocationState.Error(result.value)
                }
            }
        }
    }

    fun editAdventure(adventureId: String) {
        println("Edit adventure: $adventureId")
    }

    fun openMap(latitude: String, longitude: String) {
        println("Open map at: $latitude, $longitude")
    }

    fun openLink(url: String) {
        println("Open URL: $url")
    }
}