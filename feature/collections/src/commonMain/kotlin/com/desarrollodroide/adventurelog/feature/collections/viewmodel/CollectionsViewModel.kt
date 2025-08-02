package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.filter
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.GetCollectionsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.DeleteCollectionUseCase
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class CollectionsViewModel(
    private val getCollectionsPagingUseCase: GetCollectionsPagingUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
