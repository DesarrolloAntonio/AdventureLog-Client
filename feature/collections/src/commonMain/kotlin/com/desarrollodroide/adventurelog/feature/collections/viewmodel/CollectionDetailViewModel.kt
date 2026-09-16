package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import com.desarrollodroide.adventurelog.core.domain.usecase.DuplicateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetShareImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RemoveLocationFromCollectionUseCase
import com.desarrollodroide.adventurelog.core.model.toSafeFileName
import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles
import com.desarrollodroide.adventurelog.core.model.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionTab
import com.desarrollodroide.adventurelog.feature.collections.ui.components.CollectionView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteChecklistUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteLodgingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteNoteUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.AddItineraryEntryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.AutoGenerateItineraryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteItineraryEntryUseCase
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind

data class CollectionDetailUiState(
    val collection: Collection? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class DeleteState {
    data object Idle : DeleteState()
    data object Loading : DeleteState()
    data class Success(val message: String) : DeleteState()
    data class Error(val message: String) : DeleteState()
}

/**
 * The day the itinerary item picker is adding to.
 *
 * A null [date] is the trip-context bucket. [label] is what the sheet calls that day - the day's
 * own name where it has one, "Day 5" where it has not - because "Add to 2025-09-16" is the
 * database's way of saying it, not a person's.
 */
data class ItineraryTarget(val date: String?, val label: String)

sealed class UpdateCollectionsState {
    data object Idle : UpdateCollectionsState()
    data object Loading : UpdateCollectionsState()
    data object Success : UpdateCollectionsState()
    data class Error(val message: String) : UpdateCollectionsState()
}

class CollectionDetailViewModel(
    private val getCollectionDetailUseCase: GetCollectionDetailUseCase,
    private val deleteLocationUseCase: DeleteLocationUseCase,  // cambiar aquí
    private val deleteTransportationUseCase: DeleteTransportationUseCase,
    private val updateLocationCollectionsUseCase: UpdateLocationCollectionsUseCase,  // cambiar aquí
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val getAllCollectionsUseCase: GetAllCollectionsUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val deleteChecklistUseCase: DeleteChecklistUseCase,
    private val deleteLodgingUseCase: DeleteLodgingUseCase,
    private val autoGenerateItineraryUseCase: AutoGenerateItineraryUseCase,
    private val addItineraryEntryUseCase: AddItineraryEntryUseCase,
    private val deleteItineraryEntryUseCase: DeleteItineraryEntryUseCase,
    private val duplicateLocationUseCase: DuplicateLocationUseCase,
    private val getShareImageUseCase: GetShareImageUseCase,
    private val removeLocationFromCollectionUseCase: RemoveLocationFromCollectionUseCase,
    private val platformFiles: PlatformFiles
) : ViewModel() {

    /** The outcome of a place card's action, for the snackbar. */
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    /**
     * Duplicate and Share externally were offered on a place inside a collection and did nothing
     * (QA 04, CO-09). The copy is made outside any collection - that is the server's duplicate -
     * so the message says where to find it.
     */
    fun duplicateLocation(location: Location) {
        viewModelScope.launch {
            _actionMessage.value = when (val result = duplicateLocationUseCase(location.id)) {
                is Either.Left -> result.value
                is Either.Right -> "Duplicated as \"${result.value.name}\" in Places"
            }
        }
    }

    /** The card the server renders, as Places shares it: the server is private, a link would not open. */
    fun shareLocation(location: Location) {
        viewModelScope.launch {
            _actionMessage.value = when (val result = getShareImageUseCase(location.id)) {
                is Either.Left -> result.value
                is Either.Right ->
                    if (platformFiles.share(result.value, location.name.toSafeFileName(extension = "png"))) null
                    else "Nothing on this device can share an image."
            }
        }
    }

    /** Out of this collection only - Delete removes the place everywhere (QA 04, CO-10). */
    fun removeFromCollection(location: Location, collectionId: String) {
        viewModelScope.launch {
            _actionMessage.value = when (val result = removeLocationFromCollectionUseCase(location.id, collectionId)) {
                is Either.Left -> result.value
                is Either.Right -> {
                    loadCollection(collectionId)
                    "\"${location.name}\" is no longer in this collection"
                }
            }
        }
    }
    
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

    private val _selectedView = MutableStateFlow(CollectionView.ITEMS)
    val selectedView: StateFlow<CollectionView> = _selectedView.asStateFlow()

    /**
     * Whether an itinerary call is in flight. Separate from [_deleteState] because these are the
     * one set of actions that both add and remove, and the screen disables its buttons on it
     * rather than showing a snackbar for the happy path.
     */
    private val _itineraryWorking = MutableStateFlow(false)
    val itineraryWorking: StateFlow<Boolean> = _itineraryWorking.asStateFlow()

    /**
     * Which day the item picker is adding to: the date as `yyyy-MM-dd`, or null for the
     * trip-context bucket. [ItineraryTarget] rather than a bare `String?` because null already
     * means "no picker open" in the outer nullability.
     */
    private val _itineraryTarget = MutableStateFlow<ItineraryTarget?>(null)
    val itineraryTarget: StateFlow<ItineraryTarget?> = _itineraryTarget.asStateFlow()

    fun openItineraryPicker(date: String?, label: String) {
        _itineraryTarget.value = ItineraryTarget(date, label)
    }

    fun dismissItineraryPicker() {
        _itineraryTarget.value = null
    }

    fun autoGenerateItinerary() {
        val collection = _uiState.value.collection ?: return
        viewModelScope.launch {
            _itineraryWorking.value = true
            when (val result = autoGenerateItineraryUseCase(collection.id)) {
                is Either.Left -> _deleteState.update { DeleteState.Error(result.value) }
                is Either.Right -> loadCollection(collection.id)
            }
            _itineraryWorking.value = false
        }
    }

    fun addToItinerary(kind: ItineraryItemKind, itemId: String, date: String?) {
        val collection = _uiState.value.collection ?: return
        // After everything already on that day, which is what the server means by order.
        val order = collection.itinerary.count {
            if (date == null) it.isGlobal else it.date?.take(10) == date
        }
        _itineraryTarget.value = null
        viewModelScope.launch {
            _itineraryWorking.value = true
            when (
                val result = addItineraryEntryUseCase(collection.id, kind, itemId, date, order)
            ) {
                is Either.Left -> _deleteState.update { DeleteState.Error(result.value) }
                is Either.Right -> loadCollection(collection.id)
            }
            _itineraryWorking.value = false
        }
    }

    fun removeFromItinerary(entryId: String) {
        val collection = _uiState.value.collection ?: return
        viewModelScope.launch {
            _itineraryWorking.value = true
            when (val result = deleteItineraryEntryUseCase(entryId)) {
                is Either.Left -> _deleteState.update { DeleteState.Error(result.value) }
                is Either.Right -> loadCollection(collection.id)
            }
            _itineraryWorking.value = false
        }
    }
    
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
            
            when (val result = deleteLocationUseCase(adventureId)) {
                is Either.Left -> {
                    _deleteState.update { DeleteState.Error(result.value) }
                }
                is Either.Right -> {
                    _deleteState.update { DeleteState.Success("Place deleted successfully") }
                    _uiState.value.collection?.let { collection ->
                        loadCollection(collection.id)
                    }
                }
            }
        }
    }
    
    fun deleteLodging(lodgingId: String) {
        viewModelScope.launch {
            _deleteState.update { DeleteState.Loading }
            when (val result = deleteLodgingUseCase(lodgingId)) {
                is Either.Left -> _deleteState.update { DeleteState.Error(result.value) }
                is Either.Right -> {
                    _deleteState.update { DeleteState.Success("Lodging deleted") }
                    _uiState.value.collection?.let { loadCollection(it.id) }
                }
            }
        }
    }

    fun deleteChecklist(checklistId: String) {
        viewModelScope.launch {
            _deleteState.update { DeleteState.Loading }
            when (val result = deleteChecklistUseCase(checklistId)) {
                is Either.Left -> _deleteState.update { DeleteState.Error(result.value) }
                is Either.Right -> {
                    _deleteState.update { DeleteState.Success("Checklist deleted") }
                    _uiState.value.collection?.let { loadCollection(it.id) }
                }
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _deleteState.update { DeleteState.Loading }
            when (val result = deleteNoteUseCase(noteId)) {
                is Either.Left -> _deleteState.update { DeleteState.Error(result.value) }
                is Either.Right -> {
                    _deleteState.update { DeleteState.Success("Note deleted") }
                    _uiState.value.collection?.let { loadCollection(it.id) }
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
                    _deleteState.update { DeleteState.Success("Transportation deleted successfully") }
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
            
            when (val result = updateLocationCollectionsUseCase(adventureId, collectionIds)) {
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
    
    fun onViewSelected(view: CollectionView) {
        _selectedView.value = view
    }

    fun onTabSelected(tab: CollectionTab) {
        _selectedTab.value = tab
    }
}
