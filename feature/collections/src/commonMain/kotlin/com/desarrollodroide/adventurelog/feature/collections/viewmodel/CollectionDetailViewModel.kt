package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
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

class CollectionDetailViewModel(
    private val getCollectionDetailUseCase: GetCollectionDetailUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionDetailUiState(isLoading = true))
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()
    
    fun loadCollection(collectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = getCollectionDetailUseCase(collectionId)) {
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
                            collection = result.value,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
}
