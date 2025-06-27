package com.desarrollodroide.adventurelog.feature.adventures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.filter
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresPagingUseCase
import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AdventuresViewModel(
    private val getAdventuresPagingUseCase: GetAdventuresPagingUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val adventuresPagingData: Flow<PagingData<Adventure>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            getAdventuresPagingUseCase()
                .map { pagingData ->
                    if (query.isEmpty()) {
                        pagingData
                    } else {
                        pagingData.filter { adventure ->
                            adventure.name.contains(query, ignoreCase = true)
                        }
                    }
                }
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
