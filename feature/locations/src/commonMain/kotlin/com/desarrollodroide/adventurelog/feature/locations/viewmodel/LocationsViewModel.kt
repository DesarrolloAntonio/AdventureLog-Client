package com.desarrollodroide.adventurelog.feature.locations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DuplicateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetShareImageUseCase
import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles
import com.desarrollodroide.adventurelog.core.domain.usecase.GetUserStatsUseCase
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.toSafeFileName
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.locations.model.LocationFilters
import com.desarrollodroide.adventurelog.feature.locations.model.LocationSortField
import com.desarrollodroide.adventurelog.feature.locations.model.VisitedFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LocationsViewModel(
    private val getLocationsPagingUseCase: GetLocationsPagingUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAllCollectionsUseCase: GetAllCollectionsUseCase,
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val deleteLocationUseCase: DeleteLocationUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val updateLocationCollectionsUseCase: UpdateLocationCollectionsUseCase,
    private val duplicateLocationUseCase: DuplicateLocationUseCase,
    private val getShareImageUseCase: GetShareImageUseCase,
    private val platformFiles: PlatformFiles,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _busyLocationId = MutableStateFlow<String?>(null)
    val busyLocationId: StateFlow<String?> = _busyLocationId.asStateFlow()

    /**
     * Totals for the whole library, not for the page on screen. The web header counts only the
     * locations it has loaded and labels them as if they were everything, which is why it reads
     * "0 visited" for an account that has two.
     */
    private val _libraryCounts = MutableStateFlow<UserStats?>(null)
    val libraryCounts: StateFlow<UserStats?> = _libraryCounts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _actualSearchQuery = MutableStateFlow("")
    val actualSearchQuery: StateFlow<String> = _actualSearchQuery.asStateFlow()

    private val _filters = MutableStateFlow(LocationFilters())
    val filters: StateFlow<LocationFilters> = _filters.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    private val _categoriesState = MutableStateFlow<CategoriesState>(CategoriesState.Loading)
    val categoriesState: StateFlow<CategoriesState> = _categoriesState.asStateFlow()

    // Directly observe collections from repository
    val collections: StateFlow<List<UltraSlimCollection>> = observeCollectionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _collectionsLoading = MutableStateFlow(false)
    val collectionsLoading: StateFlow<Boolean> = _collectionsLoading.asStateFlow()

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
    
    private val _updateCollectionsState = MutableStateFlow<UpdateCollectionsState>(UpdateCollectionsState.Idle)
    val updateCollectionsState: StateFlow<UpdateCollectionsState> = _updateCollectionsState.asStateFlow()
    
    private val _categoryOperationState = MutableStateFlow<CategoryOperationState>(CategoryOperationState.Idle)
    val categoryOperationState: StateFlow<CategoryOperationState> = _categoryOperationState.asStateFlow()

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
    
    sealed class UpdateCollectionsState {
        data object Idle : UpdateCollectionsState()
        data object Loading : UpdateCollectionsState()
        data object Success : UpdateCollectionsState()
        data class Error(val message: String) : UpdateCollectionsState()
    }
    
    sealed class CategoryOperationState {
        data object Idle : CategoryOperationState()
        data object Loading : CategoryOperationState()
        data object Success : CategoryOperationState()
        data class Error(val message: String) : CategoryOperationState()
    }

        init {
            loadCategories()
            loadLibraryCounts()
            viewModelScope.launch {
                if (observeCollectionsUseCase().value.isEmpty()) {
                    _collectionsLoading.value = true
                    try {
                        getAllCollectionsUseCase(forceRefresh = false)
                    } finally {
                        _collectionsLoading.value = false
                    }
                }
            }
        }

    val adventuresPagingData: Flow<PagingData<Location>> = combine(
        _actualSearchQuery,
        _filters
    ) { query, filters ->
        Pair(query, filters)
    }.flatMapLatest { (query, filters) ->
        // Only pass non-default values to avoid using the filtered endpoint unnecessarily
        val hasActiveFilters = filters.categoryNames.isNotEmpty() ||
                filters.sortField != LocationSortField.UPDATED_AT ||
                filters.sortDirection != SortDirection.DESCENDING ||
                filters.visitedFilter != VisitedFilter.ALL ||
                query.isNotEmpty() ||
                filters.includeCollections
        
        if (hasActiveFilters) {
            getLocationsPagingUseCase(
                categoryNames = filters.categoryNames.ifEmpty { null },
                sortBy = if (filters.sortField != LocationSortField.UPDATED_AT) filters.sortField.apiValue else null,
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
            getLocationsPagingUseCase()
        }
    }.cachedIn(viewModelScope)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    fun executeSearch() {
        _actualSearchQuery.value = _searchQuery.value
        _searchQuery.value = ""
    }

    fun onFiltersChanged(filters: LocationFilters) {
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
        _filters.value = LocationFilters()
    }

    fun hasActiveFilters(): Boolean {
        val currentFilters = _filters.value
        return currentFilters.categoryNames.isNotEmpty() ||
                currentFilters.sortField != LocationSortField.UPDATED_AT ||
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
            when (val result = deleteLocationUseCase(adventureId)) {
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

    private fun loadLibraryCounts() {
        viewModelScope.launch {
            val username = userRepository.getUserSessionOnce()?.username ?: return@launch
            val result = getUserStatsUseCase(username)
            if (result is Either.Right) _libraryCounts.value = result.value
        }
    }

    fun duplicateLocation(location: Location) {
        if (_busyLocationId.value != null) return

        viewModelScope.launch {
            _busyLocationId.value = location.id
            _actionMessage.value = when (val result = duplicateLocationUseCase(location.id)) {
                is Either.Left -> result.value
                // The copy lands at the top of the list once the page reloads, named "Copy of ...".
                is Either.Right -> "Duplicated as \"${result.value.name}\""
            }
            _busyLocationId.value = null
            refresh()
        }
    }

    /**
     * Shares the card the server renders for a location, rather than a link: the server is on a
     * private network, so a URL would be useless to whoever receives it.
     */
    fun shareLocation(location: Location) {
        if (_busyLocationId.value != null) return

        viewModelScope.launch {
            _busyLocationId.value = location.id
            _actionMessage.value = when (val result = getShareImageUseCase(location.id)) {
                is Either.Left -> result.value
                is Either.Right -> {
                    val fileName = location.name.toSafeFileName(extension = "png")
                    if (platformFiles.share(result.value, fileName)) {
                        null
                    } else {
                        "Nothing on this device can share an image."
                    }
                }
            }
            _busyLocationId.value = null
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun clearDeleteState() {
        _deleteState.value = DeleteState.Idle
    }

    fun createCategory(name: String, icon: String) {
        viewModelScope.launch {
            _categoryOperationState.value = CategoryOperationState.Loading
            
            val internalName = name.lowercase().replace(" ", "_")
            
            when (val result = createCategoryUseCase(
                name = internalName,
                displayName = name,
                icon = icon
            )) {
                is Either.Left -> {
                    _categoryOperationState.value = CategoryOperationState.Error(result.value)
                }
                is Either.Right -> {
                    _categoryOperationState.value = CategoryOperationState.Success
                    loadCategories()
                }
            }
        }
    }

    fun updateCategory(categoryId: String, name: String, icon: String) {
        viewModelScope.launch {
            _categoryOperationState.value = CategoryOperationState.Loading
            
            val internalName = name.lowercase().replace(" ", "_")
            
            when (val result = updateCategoryUseCase(
                categoryId = categoryId,
                name = internalName,
                displayName = name,
                icon = icon
            )) {
                is Either.Left -> {
                    _categoryOperationState.value = CategoryOperationState.Error(result.value)
                }
                is Either.Right -> {
                    _categoryOperationState.value = CategoryOperationState.Success
                    loadCategories()
                }
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _categoryOperationState.value = CategoryOperationState.Loading
            
            when (val result = deleteCategoryUseCase(categoryId)) {
                is Either.Left -> {
                    _categoryOperationState.value = CategoryOperationState.Error(result.value)
                }
                is Either.Right -> {
                    _categoryOperationState.value = CategoryOperationState.Success
                    loadCategories()
                }
            }
        }
    }
    
    fun clearCategoryOperationState() {
        _categoryOperationState.value = CategoryOperationState.Idle
    }

    fun updateAdventureCollections(adventureId: String, collectionIds: List<String>) {
        viewModelScope.launch {
            _updateCollectionsState.value = UpdateCollectionsState.Loading
            
            when (val result = updateLocationCollectionsUseCase(adventureId, collectionIds)) {
                is Either.Left -> {
                    _updateCollectionsState.value = UpdateCollectionsState.Error(result.value)
                }
                is Either.Right -> {
                    _updateCollectionsState.value = UpdateCollectionsState.Success
                }
            }
        }
    }
    
    fun clearUpdateCollectionsState() {
        _updateCollectionsState.value = UpdateCollectionsState.Idle
    }
    
    fun refreshCollections() {
        viewModelScope.launch {
            _collectionsLoading.value = true
            try {
                getAllCollectionsUseCase(forceRefresh = true)
            } finally {
                _collectionsLoading.value = false
            }
        }
    }
    
    fun selectLocation(location: Location) {
        println("🔵 [LocationsViewModel] Selecting location: ${location.id} - ${location.name}")
        getLocationsPagingUseCase.selectLocation(location)
    }
}
