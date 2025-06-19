package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.CreateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.UpdateCollectionUseCase
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditCollectionUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddEditCollectionViewModel(
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase,
    private val collectionId: String? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditCollectionUiState())
    val uiState: StateFlow<AddEditCollectionUiState> = _uiState.asStateFlow()
    
    fun saveCollection(formData: CollectionFormData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = if (collectionId != null) {
                // Update existing collection
                updateCollectionUseCase(
                    collectionId = collectionId,
                    name = formData.name,
                    description = formData.description,
                    isPublic = formData.isPublic,
                    startDate = formData.startDate.takeIf { it.isNotBlank() },
                    endDate = formData.endDate.takeIf { it.isNotBlank() }
                )
            } else {
                // Create new collection
                createCollectionUseCase(
                    name = formData.name,
                    description = formData.description,
                    isPublic = formData.isPublic,
                    startDate = formData.startDate.takeIf { it.isNotBlank() },
                    endDate = formData.endDate.takeIf { it.isNotBlank() }
                )
            }
            
            when (result) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.value
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
            }
        }
    }
    
    fun clearSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
