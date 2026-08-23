package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.filter
import app.cash.paging.map
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteCollectionUseCase
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.SortDirection
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionSortOptions
import com.desarrollodroide.adventurelog.feature.collections.model.CollectionSortField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.desarrollodroide.adventurelog.core.domain.usecase.ArchiveCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DuplicateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ExportCollectionUseCase
import com.desarrollodroide.adventurelog.core.model.CollectionExport
import com.desarrollodroide.adventurelog.core.model.toSafeFileName
import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class CollectionsViewModel(
    private val getCollectionsPagingUseCase: GetCollectionsPagingUseCase,
    private val getAllCollectionsUseCase: GetAllCollectionsUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val duplicateCollectionUseCase: DuplicateCollectionUseCase,
    private val archiveCollectionUseCase: ArchiveCollectionUseCase,
    private val exportCollectionUseCase: ExportCollectionUseCase,
    private val platformFiles: PlatformFiles
) : ViewModel() {

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    /** What the sheet is waiting for, so a slow server-side render does not look like nothing. */
    private val _busyLabel = MutableStateFlow<String?>(null)
    val busyLabel: StateFlow<String?> = _busyLabel.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOptions = MutableStateFlow(CollectionSortOptions())
    val sortOptions: StateFlow<CollectionSortOptions> = _sortOptions.asStateFlow()

    /**
     * Null means every collection. The server computes `status` but does not accept it as a
     * query, so this filters what has been loaded - the same way search here already works, and
     * the same way the web does it.
     */
    private val _statusFilter = MutableStateFlow<TripStatus?>(null)
    val statusFilter: StateFlow<TripStatus?> = _statusFilter.asStateFlow()

    /** How many collections the account holds, for the header. */
    val collectionCount: StateFlow<Int> = observeCollectionsUseCase()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _showSortSheet = MutableStateFlow(false)
    val showSortSheet: StateFlow<Boolean> = _showSortSheet.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    sealed class DeleteState {
        data object Idle : DeleteState()
        data object Loading : DeleteState()
        data object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }

    val collectionsPagingData: Flow<PagingData<UltraSlimCollection>> = combine(
        _searchQuery.debounce(300).distinctUntilChanged(),
        _sortOptions,
        _statusFilter
    ) { query, sortOptions, status ->
        Triple(query, sortOptions, status)
    }.flatMapLatest { (query, sortOptions, status) ->
        getCollectionsPagingUseCase(
            sortField = sortOptions.sortField.name,
            sortDirection = sortOptions.sortDirection.name
        )
            .map { pagingData ->
                pagingData
                    .filter { query.isEmpty() || it.name.contains(query, ignoreCase = true) }
                    .filter { status == null || it.status == status }
            }
    }.cachedIn(viewModelScope)

    init {
        // The count comes from the shared collections flow, which nothing has filled if this is
        // the first screen opened. Paging only ever loads a page at a time, so it cannot answer
        // "how many are there" on its own.
        viewModelScope.launch {
            if (observeCollectionsUseCase().value.isEmpty()) {
                getAllCollectionsUseCase(forceRefresh = false)
            }
        }
    }

    fun duplicateCollection(collection: UltraSlimCollection) {
        run("Duplicating…") {
            when (val result = duplicateCollectionUseCase(collection.id)) {
                is Either.Left -> result.value
                is Either.Right -> {
                    refresh()
                    "Duplicated as \"${result.value.name}\""
                }
            }
        }
    }

    fun setArchived(collection: UltraSlimCollection, archived: Boolean) {
        run(if (archived) "Archiving…" else "Restoring…") {
            when (val result = archiveCollectionUseCase(collection.id, archived)) {
                is Either.Left -> result.value
                is Either.Right -> {
                    refresh()
                    if (archived) "Moved to the archive" else "Restored from the archive"
                }
            }
        }
    }

    /**
     * Shares or saves one of the files the server renders. All three go through the platform
     * share sheet: the phone has nowhere useful to put a file otherwise, and the server is on a
     * private network so a link would be no use to whoever receives it.
     */
    fun exportCollection(collection: UltraSlimCollection, what: CollectionExport) {
        val label = when (what) {
            CollectionExport.SHARE_CARD -> "Building the share image…"
            CollectionExport.PDF -> "Building the PDF…"
            CollectionExport.ZIP -> "Building the export…"
        }

        run(label) {
            when (val result = exportCollectionUseCase(collection.id, what)) {
                is Either.Left -> result.value
                is Either.Right -> {
                    val fileName = collection.name.toSafeFileName(extension = what.fileExtension)
                    if (platformFiles.share(result.value, fileName)) {
                        null
                    } else {
                        "Nothing on this device can open a .${what.fileExtension} file."
                    }
                }
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    private fun run(busy: String, block: suspend () -> String?) {
        if (_busyLabel.value != null) return

        viewModelScope.launch {
            _busyLabel.value = busy
            _actionMessage.value = block()
            _busyLabel.value = null
        }
    }

    fun onStatusFilterChanged(status: TripStatus?) {
        _statusFilter.value = status
    }


    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOptionsChanged(options: CollectionSortOptions) {
        _sortOptions.value = options
    }

    fun showSortSheet() {
        _showSortSheet.value = true
    }

    fun hideSortSheet() {
        _showSortSheet.value = false
    }

    fun hasActiveSorting(): Boolean {
        val options = _sortOptions.value
        return options.sortField != CollectionSortField.UPDATED_AT || 
               options.sortDirection != SortDirection.DESCENDING
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getAllCollectionsUseCase(forceRefresh = true)
            _isRefreshing.value = false
        }
    }

    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading
            when (val result = deleteCollectionUseCase(collectionId)) {
                is Either.Left -> {
                    _deleteState.value = DeleteState.Error(result.value)
                }
                is Either.Right -> {
                    _deleteState.value = DeleteState.Success
                }
            }
        }
    }

    fun clearDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
}
