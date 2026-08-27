package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchEverythingUseCase
import com.desarrollodroide.adventurelog.core.model.SearchHit
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GlobalSearchState(
    val query: String = "",
    val hits: List<SearchHit> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
) {
    val tooShort: Boolean get() = query.trim().length in 1 until SearchEverythingUseCase.MIN_LENGTH
    val hasSearched: Boolean get() = query.trim().length >= SearchEverythingUseCase.MIN_LENGTH
}

class GlobalSearchViewModel(
    private val searchEverythingUseCase: SearchEverythingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GlobalSearchState())
    val state: StateFlow<GlobalSearchState> = _state.asStateFlow()

    private val typed = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val watcher = viewModelScope.launch {
        // Searching on every keystroke would ask the server a question the user has not finished
        // asking. A short pause is enough to tell typing from a query.
        typed.debounce(300).distinctUntilChanged().collect { term -> run(term) }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, error = null) }
        typed.value = query
    }

    fun clear() {
        _state.value = GlobalSearchState()
        typed.value = ""
    }

    private suspend fun run(term: String) {
        if (term.trim().length < SearchEverythingUseCase.MIN_LENGTH) {
            _state.update { it.copy(hits = emptyList(), isSearching = false) }
            return
        }
        _state.update { it.copy(isSearching = true) }
        when (val result = searchEverythingUseCase(term)) {
            is Either.Left -> _state.update {
                it.copy(isSearching = false, error = result.value, hits = emptyList())
            }
            is Either.Right -> _state.update {
                it.copy(isSearching = false, hits = result.value, error = null)
            }
        }
    }

    override fun onCleared() {
        watcher.cancel()
        super.onCleared()
    }
}
