package com.desarrollodroide.adventurelog.feature.adventures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.DeleteAdventureUseCase
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AdventuresViewModel(
    private val getAdventuresPagingUseCase: GetAdventuresPagingUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteAdventureUseCase: DeleteAdventureUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filters = MutableStateFlow(AdventureFilters())
    val filters: StateFlow<AdventureFilters> = _filters.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    private val _categoriesState = MutableStateFlow<CategoriesState>(CategoriesState.Loading)
    val categoriesState: StateFlow<CategoriesState> = _categoriesState.asStateFlow()

    // Convenience property for backward compatibility
    @Suppress("unused")
    val categories: StateFlow<List<Category>> = _categoriesState.map { state ->
        when (state) {
            is CategoriesState.Success -> state.categories
            else -> emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    sealed class CategoriesState {
        data object Loading : CategoriesState()
        data class Success(val categories: List<Category>) : CategoriesState()
        data class Error(val message: String) : CategoriesState()
    }

    sealed class DeleteState {
        data object Idle : DeleteState()
        data object Loading : DeleteState()
        data object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }

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
            _categoriesState.value = CategoriesState.Loading
            when (val result = getCategoriesUseCase()) {
                is Either.Left -> {
                    _categoriesState.value = CategoriesState.Error(result.value)
                }
                is Either.Right -> {
                    _categoriesState.value = CategoriesState.Success(result.value)
                }
            }
        }
    }

    fun retryLoadCategories() {
        loadCategories()
    }

    fun deleteAdventure(adventureId: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading
            when (val result = deleteAdventureUseCase(adventureId)) {
                is Either.Left -> {
                    _deleteState.value = DeleteState.Error(result.value)
                }
                is Either.Right -> {
                    _deleteState.value = DeleteState.Success
                    // The paging data will automatically refresh due to the repository updating the flow
                }
            }
        }
    }

    fun clearDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
}
