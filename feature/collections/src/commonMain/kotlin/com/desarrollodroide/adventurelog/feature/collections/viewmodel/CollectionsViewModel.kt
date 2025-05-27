package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
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

class CollectionsViewModel(
    private val getCollectionsUseCase: GetCollectionsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionsUiState(isLoading = true))
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()
    
    init {
        loadCollections()
    }
    
    private fun loadCollections() {
        viewModelScope.launch {
            // TODO: Implement pagination
            when (val result = getCollectionsUseCase(page = 1, pageSize = 20)) {
                is Either.Left -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = result.value
                        )
                    }
                }
                is Either.Right -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            collections = result.value,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}
