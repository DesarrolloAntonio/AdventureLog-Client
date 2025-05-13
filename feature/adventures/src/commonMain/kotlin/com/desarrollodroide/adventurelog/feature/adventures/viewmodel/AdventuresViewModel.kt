package com.desarrollodroide.adventurelog.feature.adventures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AdventuresUiState {
    data object Loading : AdventuresUiState()
    data class Error(val message: String) : AdventuresUiState()
    data class Success(val adventures: List<Adventure>) : AdventuresUiState()
}

class AdventuresViewModel(
    private val getAdventuresUseCase: GetAdventuresUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<AdventuresUiState>(AdventuresUiState.Loading)
    val uiState: StateFlow<AdventuresUiState> = _uiState.asStateFlow()

    init {
        loadAdventures()
    }

    fun loadAdventures(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { AdventuresUiState.Loading }
            
            when (val result = getAdventuresUseCase(page)) {
                is Either.Left -> {
                    val errorMessage = result.value
                    _uiState.update { AdventuresUiState.Error(errorMessage) }
                    println("AdventuresViewModel: Error loading adventures - $errorMessage")
                }
                is Either.Right -> {
                    val adventures = result.value
                    _uiState.update { AdventuresUiState.Success(adventures) }
                    println("AdventuresViewModel: Successfully loaded ${adventures.size} adventures")
                }
            }
        }
    }
}