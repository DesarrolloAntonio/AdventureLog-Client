package com.desarrollodroide.adventurelog.feature.adventures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.filter
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.AdventureFilters
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.core.model.SortField
import com.desarrollodroide.adventurelog.core.model.VisitedFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AdventuresViewModel(
    private val getAdventuresPagingUseCase: GetAdventuresPagingUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow(AdventureFilters())
    val filters: StateFlow<AdventureFilters> = _filters.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    val adventuresPagingData: Flow<PagingData<Adventure>> = combine(
        _searchQuery.debounce(300).distinctUntilChanged(),
        _filters
    ) { query, filters ->
        Pair(query, filters)
    }.flatMapLatest { (query, filters) ->
        // Only pass non-default values to avoid using the filtered endpoint unnecessarily
        val hasActiveFilters = filters.categoryNames.isNotEmpty() ||
                filters.sortField != SortField.UPDATED_AT ||
                filters.sortDirection != SortDirection.DESCENDING ||
                filters.visitedFilter != VisitedFilter.ALL ||
                query.isNotEmpty() ||
                filters.includeCollections
        
        if (hasActiveFilters) {
            getAdventuresPagingUseCase(
                categoryNames = filters.categoryNames.ifEmpty { null },
                sortBy = if (filters.sortField != SortField.UPDATED_AT) filters.sortField.apiValue else null,
                sortOrder = if (filters.sortDirection != SortDirection.DESCENDING) filters.sortDirection.apiValue else null,
                isVisited = when (filters.visitedFilter) {
                    VisitedFilter.ALL -> null
                    VisitedFilter.VISITED -> true
                    VisitedFilter.NOT_VISITED -> false
                },
                searchQuery = query.ifEmpty { null },
                includeCollections = filters.includeCollections
            )
        } else {
            // Use the regular endpoint when no filters are applied
            getAdventuresPagingUseCase()
        }
    }.cachedIn(viewModelScope)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFiltersChanged(filters: AdventureFilters) {
        _filters.value = filters
    }

    fun showFilters() {
        _showFilters.value = true
    }

    fun hideFilters() {
        _showFilters.value = false
    }

    fun refresh() {
        _isRefreshing.value = true
    }

    fun onRefreshComplete() {
        _isRefreshing.value = false
    }

    fun clearFilters() {
        _filters.value = AdventureFilters()
    }

    fun hasActiveFilters(): Boolean {
        val currentFilters = _filters.value
        return currentFilters.categoryNames.isNotEmpty() ||
                currentFilters.sortField != SortField.UPDATED_AT ||
                currentFilters.sortDirection != SortDirection.DESCENDING ||
                currentFilters.visitedFilter != VisitedFilter.ALL ||
                currentFilters.includeCollections
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is Either.Left -> {
                    // Handle error if needed
                    _categories.value = emptyList()
                }
                is Either.Right -> {
                    _categories.value = result.value
                }
            }
        }
    }
}
