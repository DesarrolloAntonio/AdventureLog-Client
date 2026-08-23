package com.desarrollodroide.adventurelog.feature.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Attachment
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.desarrollodroide.adventurelog.feature.ui.util.AttachmentOpener
import com.desarrollodroide.adventurelog.feature.ui.util.AuthenticatedFileDownloader
import kotlinx.coroutines.launch

sealed class LocationState {
    data object Loading : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Error(val message: String) : LocationState()
}

class AdventureDetailViewModel(
    private val getLocationUseCase: GetLocationUseCase,
    private val fileDownloader: AuthenticatedFileDownloader,
    private val attachmentOpener: AttachmentOpener,
    observeCollectionsUseCase: ObserveCollectionsUseCase
) : ViewModel() {

    private val _attachmentMessage = MutableStateFlow<String?>(null)
    val attachmentMessage: StateFlow<String?> = _attachmentMessage.asStateFlow()

    private val _openingAttachmentId = MutableStateFlow<String?>(null)
    val openingAttachmentId: StateFlow<String?> = _openingAttachmentId.asStateFlow()

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Loading)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val allCollections: StateFlow<List<UltraSlimCollection>> = observeCollectionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val collections: StateFlow<List<UltraSlimCollection>> = _locationState.map { state ->
        when (state) {
            is LocationState.Success -> {
                allCollections.value.filter { collection ->
                    state.location.collections.contains(collection.id)
                }
            }
            else -> emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadLocation(locationId: String) {
        viewModelScope.launch {
            _locationState.value = LocationState.Loading
            
            println("📍 [ViewModel] Loading location: $locationId")
            
            when (val result = getLocationUseCase(locationId)) {
                is Either.Right -> {
                    println("✅ [ViewModel] Location loaded: ${result.value.name}")
                    _locationState.value = LocationState.Success(result.value)
                    getLocationUseCase.clearSelectedLocation()
                }
                is Either.Left -> {
                    println("❌ [ViewModel] Error loading location: ${result.value}")
                    _locationState.value = LocationState.Error(result.value)
                }
            }
        }
    }

    fun editAdventure(adventureId: String) {
        println("Edit adventure: $adventureId")
    }

    fun openMap(latitude: String, longitude: String) {
        println("Open map at: $latitude, $longitude")
    }

    /**
     * Attachments are served behind the same auth check as photos, so the file is fetched with
     * the signed-in client and handed to a viewer as a local copy. Opening the URL directly - in
     * a browser or a document app - would come back 403.
     */
    fun openAttachment(attachment: Attachment) {
        if (_openingAttachmentId.value != null) return

        viewModelScope.launch {
            _openingAttachmentId.value = attachment.id
            val bytes = fileDownloader.download(attachment.file)

            _attachmentMessage.value = when {
                bytes == null -> "Could not download this attachment."
                !attachmentOpener.open(bytes, attachment.displayFileName()) ->
                    "Nothing on this device can open a .${attachment.extension} file."
                else -> null
            }
            _openingAttachmentId.value = null
        }
    }

    fun clearAttachmentMessage() {
        _attachmentMessage.value = null
    }

    private fun Attachment.displayFileName(): String {
        val fromUrl = file.substringAfterLast('/').substringBefore('?')
        val base = name?.takeIf { it.isNotBlank() } ?: fromUrl.substringBeforeLast('.')
        return if (extension.isBlank()) base else "$base.${extension.trimStart('.')}"
    }
}