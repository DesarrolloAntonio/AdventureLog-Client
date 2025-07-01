package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.filter
import com.desarrollodroide.adventurelog.core.domain.GetCollectionsPagingUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
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
class CollectionsViewModel(
    private val getCollectionsPagingUseCase: GetCollectionsPagingUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val collectionsPagingData: Flow<PagingData<Collection>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            getCollectionsPagingUseCase()
                .map { pagingData ->
                    if (query.isEmpty()) {
                        pagingData
                    } else {
                        pagingData.filter { collection ->
                            collection.name.contains(query, ignoreCase = true)
                        }
                    }
                }
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        _isRefreshing.value = true
    }

    fun onRefreshComplete() {
        _isRefreshing.value = false
    }
}
