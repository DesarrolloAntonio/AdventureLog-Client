package com.desarrollodroide.adventurelog.feature.adventures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AdventuresUiState {
    data object Loading : AdventuresUiState()
    data class Error(val message: String) : AdventuresUiState()
    data class Success(
        val adventures: List<Adventure>,
        val filteredAdventures: List<Adventure> = adventures,
        val searchQuery: String = ""
    ) : AdventuresUiState()
}

class AdventuresViewModel(
    private val getAdventuresUseCase: GetAdventuresUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdventuresUiState>(AdventuresUiState.Loading)
    val uiState: StateFlow<AdventuresUiState> = _uiState.asStateFlow()

    private val searchDebounceTime = 300L

    // Cache for all adventures
    private var allAdventures: List<Adventure> = emptyList()

    init {
        loadAdventures(
            page = 1,
            pageSize = 1000 // TODO - Implement pagination
        )
    }

    fun loadAdventures(page: Int = 1, pageSize: Int) {
        viewModelScope.launch {
            _uiState.update { AdventuresUiState.Loading }
            
            when (val result = getAdventuresUseCase(page, pageSize)) {
                is Either.Left -> {
                    val errorMessage = result.value
                    _uiState.update { AdventuresUiState.Error(errorMessage) }
                    println("AdventuresViewModel: Error loading adventures - $errorMessage")
                }
                is Either.Right -> {
                    val adventures = result.value
                    allAdventures = adventures
                    
                    _uiState.update { 
                        AdventuresUiState.Success(
                            adventures = adventures,
                            filteredAdventures = adventures
                        )
                    }
                    println("AdventuresViewModel: Successfully loaded ${adventures.size} adventures")
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        val currentState = _uiState.value as? AdventuresUiState.Success ?: return
        
        // Update query immediately
        _uiState.update {
            currentState.copy(searchQuery = query)
        }

        // Debounce search
        viewModelScope.launch {
            delay(searchDebounceTime)
            performSearch(query)
        }
    }

    fun onSearchSubmit() {
        val currentState = _uiState.value as? AdventuresUiState.Success ?: return
        performSearch(currentState.searchQuery)
    }

    private fun performSearch(query: String) {
        val trimmedQuery = query.trim().lowercase()
        
        val filteredAdventures = if (trimmedQuery.isEmpty()) {
            allAdventures
        } else {
            allAdventures.filter { adventure ->
                adventure.name.lowercase().contains(trimmedQuery)
            }
        }
        
        val currentState = _uiState.value as? AdventuresUiState.Success ?: return
        _uiState.update {
            currentState.copy(filteredAdventures = filteredAdventures)
        }
    }
}
