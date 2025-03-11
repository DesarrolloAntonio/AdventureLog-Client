package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    // Add Use cases classes here
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTempPreviewData()
    }
    
    /**
     * Temporary function to load preview data while network is not implemented
     */
    private fun loadTempPreviewData() {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(800)
            
            // Use the preview data
            _uiState.update {
                HomeUiState.Success(
                    userName = "User",
                    recentAdventures = PreviewData.adventures
                )
            }
        }
    }

}