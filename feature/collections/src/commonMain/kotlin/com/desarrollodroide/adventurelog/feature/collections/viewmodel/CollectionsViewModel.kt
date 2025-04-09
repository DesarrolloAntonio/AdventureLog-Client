package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionsUiState(
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CollectionsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionsUiState(isLoading = true))
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()
    
    init {
        loadCollections()
    }
    
    private fun loadCollections() {
        viewModelScope.launch {
            try {
                // In a real implementation, this would come from a repository
                val collections = PreviewData.collections
                
                _uiState.update { currentState ->
                    currentState.copy(
                        collections = collections,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load collections"
                    )
                }
            }
        }
    }
}
