package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.domain.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.UpdateCollectionUseCase
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data.CollectionFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditCollectionUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val initialData: CollectionFormData? = null
)

class AddEditCollectionViewModel(
    private val collectionId: String?,
    private val collectionsRepository: CollectionsRepository,
    private val getCollectionDetailUseCase: GetCollectionDetailUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditCollectionUiState())
    val uiState: StateFlow<AddEditCollectionUiState> = _uiState.asStateFlow()
    
    init {
        if (collectionId != null) {
            loadCollection()
        }
    }
    
    private fun loadCollection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getCollectionDetailUseCase(collectionId!!)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.value.toString()
                    )
                }
                is Either.Right -> {
                    val collection = result.value
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        initialData = CollectionFormData(
                            name = collection.name,
                            description = collection.description,
                            isPublic = collection.isPublic,
                            startDate = collection.startDate ?: "",
                            endDate = collection.endDate ?: "",
                            link = collection.link ?: ""
                        )
                    )
                }
            }
        }
    }
    
    fun saveCollection(formData: CollectionFormData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = if (collectionId != null) {
                    // Update existing collection
                    updateCollectionUseCase(
                        collectionId = collectionId,
                        name = formData.name,
                        description = formData.description,
                        isPublic = formData.isPublic,
                        startDate = formData.startDate.ifEmpty { null },
                        endDate = formData.endDate.ifEmpty { null },
                        link = formData.link.ifEmpty { null }
                    )
                } else {
                    // Create new collection
                    collectionsRepository.createCollection(
                        name = formData.name,
                        description = formData.description,
                        isPublic = formData.isPublic,
                        startDate = formData.startDate.ifEmpty { null },
                        endDate = formData.endDate.ifEmpty { null }
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to save collection"
                )
            }
        }
    }
    
    fun clearSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}