package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionsUiState(
    val collections: List<Collection> = emptyList(),
    val filteredCollections: List<Collection> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CollectionsViewModel(
    private val getCollectionsUseCase: GetCollectionsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionsUiState(isLoading = true))
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()
    
    private val searchDebounceTime = 300L
    
    // Cache for all collections
    private var allCollections: List<Collection> = emptyList()
    
    init {
        loadCollections()
    }
    
    private fun loadCollections() {
        viewModelScope.launch {
            // TODO: Implement pagination
            when (val result = getCollectionsUseCase(page = 1, pageSize = 1000)) {
                is Either.Left -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = result.value
                        )
                    }
                }
                is Either.Right -> {
                    allCollections = result.value
                    _uiState.update { currentState ->
                        currentState.copy(
                            collections = result.value,
                            filteredCollections = result.value,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _uiState.update { currentState ->
            currentState.copy(searchQuery = query)
        }
        
        // Debounce search
        viewModelScope.launch {
            delay(searchDebounceTime)
            performSearch(query)
        }
    }
    
    fun onSearchSubmit() {
        performSearch(_uiState.value.searchQuery)
    }
    
    private fun performSearch(query: String) {
        val trimmedQuery = query.trim().lowercase()
        
        val filteredCollections = if (trimmedQuery.isEmpty()) {
            allCollections
        } else {
            allCollections.filter { collection ->
                collection.name.lowercase().contains(trimmedQuery)
            }
        }
        
        _uiState.update { currentState ->
            currentState.copy(filteredCollections = filteredCollections)
        }
    }
}
