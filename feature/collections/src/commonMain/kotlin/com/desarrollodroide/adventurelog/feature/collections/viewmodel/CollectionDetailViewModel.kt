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

data class CollectionDetailUiState(
    val collection: Collection? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CollectionDetailViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionDetailUiState(isLoading = true))
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()
    
    fun loadCollection(collectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // In a real implementation, this would come from a repository
                // For now, we'll simulate by finding the collection in our preview data
                val collection = PreviewData.collections.find { it.id == collectionId }
                
                if (collection != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            collection = collection,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "Collection not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load collection"
                    )
                }
            }
        }
    }
}
