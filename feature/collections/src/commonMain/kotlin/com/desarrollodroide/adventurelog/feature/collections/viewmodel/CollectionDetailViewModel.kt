package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateAdventureCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

data class CollectionDetailUiState(
    val collection: Collection? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

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

class CollectionDetailViewModel(
    private val getCollectionDetailUseCase: GetCollectionDetailUseCase,
    private val deleteAdventureUseCase: DeleteAdventureUseCase,
    private val deleteTransportationUseCase: DeleteTransportationUseCase,
    private val updateAdventureCollectionsUseCase: UpdateAdventureCollectionsUseCase,
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val getAllCollectionsUseCase: GetAllCollectionsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionDetailUiState(isLoading = true))
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()
    
    val allCollections: StateFlow<List<UltraSlimCollection>> = observeCollectionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _collectionsLoading = MutableStateFlow(false)
    val collectionsLoading: StateFlow<Boolean> = _collectionsLoading.asStateFlow()
    
    init {
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
    
    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()
    
    private val _updateCollectionsState = MutableStateFlow<UpdateCollectionsState>(UpdateCollectionsState.Idle)
    val updateCollectionsState: StateFlow<UpdateCollectionsState> = _updateCollectionsState.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(CollectionTab.ALL)
    val selectedTab: StateFlow<CollectionTab> = _selectedTab.asStateFlow()
    
    fun loadCollection(collectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = getCollectionDetailUseCase(collectionId)) {
                is Either.Left -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = result.value
                        )
                    }
                }
                is Either.Right -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            collection = result.value,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
    
    fun deleteAdventure(adventureId: String) {
        viewModelScope.launch {
            _deleteState.update { DeleteState.Loading }
            
            when (val result = deleteAdventureUseCase(adventureId)) {
                is Either.Left -> {
                    _deleteState.update { DeleteState.Error(result.value) }
                }
                is Either.Right -> {
                    _deleteState.update { DeleteState.Success }
                    _uiState.value.collection?.let { collection ->
                        loadCollection(collection.id)
                    }
                }
            }
        }
    }
    
    fun deleteTransportation(transportationId: String) {
        viewModelScope.launch {
            _deleteState.update { DeleteState.Loading }
            
            when (val result = deleteTransportationUseCase(transportationId)) {
                is Either.Left -> {
                    _deleteState.update { DeleteState.Error(result.value) }
                }
                is Either.Right -> {
                    _deleteState.update { DeleteState.Success }
                    _uiState.value.collection?.let { collection ->
                        loadCollection(collection.id)
                    }
                }
            }
        }
    }
    
    fun updateAdventureCollections(adventureId: String, collectionIds: List<String>) {
        viewModelScope.launch {
            _updateCollectionsState.update { UpdateCollectionsState.Loading }
            
            when (val result = updateAdventureCollectionsUseCase(adventureId, collectionIds)) {
                is Either.Left -> {
                    _updateCollectionsState.update { UpdateCollectionsState.Error(result.value) }
                }
                is Either.Right -> {
                    _updateCollectionsState.update { UpdateCollectionsState.Success }
                    _uiState.value.collection?.let { collection ->
                        loadCollection(collection.id)
                    }
                }
            }
        }
    }
    
    fun clearDeleteState() {
        _deleteState.update { DeleteState.Idle }
    }
    
    fun clearUpdateCollectionsState() {
        _updateCollectionsState.update { UpdateCollectionsState.Idle }
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
    
    fun onTabSelected(tab: CollectionTab) {
        _selectedTab.value = tab
    }
}
