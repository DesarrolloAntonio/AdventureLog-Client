package com.desarrollodroide.adventurelog

import androidx.paging.CombinedLoadStates
import androidx.paging.DifferCallback
import androidx.paging.LoadState
import androidx.paging.NullPaddedList
import androidx.paging.PagingData
import androidx.paging.PagingDataDiffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/** What a SwiftUI list needs to know about loading, flattened from [CombinedLoadStates]. */
data class PagingStatus(
    val isRefreshing: Boolean,
    val isLoadingMore: Boolean,
    val endReached: Boolean,
    val error: String?,
)

/**
 * Presents a `Flow<PagingData<T>>` to SwiftUI without Compose: the paging library's own
 * [PagingDataDiffer] (androidx.paging 3.3.0-alpha02, what app.cash.paging resolves to on iOS) does
 * the work, and the loaded items come out as a [StateFlow]. Swift calls [itemAccessed] as each row
 * appears; that indexed get is what sends the access hint and loads the next page. Runs in
 * [scope], the ViewModel's viewModelScope, so it stops when the screen's owner is cleared.
 */
class PagingListPresenter<T : Any>(flow: Flow<PagingData<T>>, scope: CoroutineScope) {
    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items.asStateFlow()

    private val _status = MutableStateFlow(PagingStatus(isRefreshing = true, isLoadingMore = false, endReached = false, error = null))
    val status: StateFlow<PagingStatus> = _status.asStateFlow()

    private val callback = object : DifferCallback {
        override fun onChanged(position: Int, count: Int) = publish()
        override fun onInserted(position: Int, count: Int) = publish()
        override fun onRemoved(position: Int, count: Int) = publish()
    }

    private val differ = object : PagingDataDiffer<T>(callback, Dispatchers.Main) {
        override suspend fun presentNewList(
            previousList: NullPaddedList<T>,
            newList: NullPaddedList<T>,
            lastAccessedIndex: Int,
            onListPresentable: () -> Unit,
        ): Int? {
            onListPresentable()
            publish()
            return null
        }
    }

    init {
        scope.launch { flow.collectLatest { differ.collectFrom(it) } }
        scope.launch { differ.loadStateFlow.filterNotNull().collect { _status.value = it.toStatus() } }
    }

    private fun publish() {
        _items.value = differ.snapshot().items
    }

    /** The row at [index] is on screen. An indexed get, not a peek: only get sends the hint. */
    fun itemAccessed(index: Int) {
        if (index in 0 until differ.size) differ[index]
    }

    fun retry() = differ.retry()
    fun refresh() = differ.refresh()

    private fun CombinedLoadStates.toStatus() = PagingStatus(
        isRefreshing = refresh is LoadState.Loading,
        isLoadingMore = append is LoadState.Loading,
        endReached = append.endOfPaginationReached,
        error = (refresh as? LoadState.Error ?: append as? LoadState.Error)?.error?.message,
    )
}
