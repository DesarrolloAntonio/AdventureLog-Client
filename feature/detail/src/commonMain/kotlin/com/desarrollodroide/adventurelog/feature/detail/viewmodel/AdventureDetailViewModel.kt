package com.desarrollodroide.adventurelog.feature.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetShareImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Attachment
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.toSafeFileName
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.feature.detail.domain.FileHandoff
import com.desarrollodroide.adventurelog.feature.detail.domain.Handoff

private val logger = Logger.withTag("AdventureDetailViewModel")

sealed class LocationState {
    data object Loading : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Error(val message: String) : LocationState()
}

class AdventureDetailViewModel(
    private val getLocationUseCase: GetLocationUseCase,
    private val fileHandoff: FileHandoff,
    private val getShareImageUseCase: GetShareImageUseCase,
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

    // combine, not map + allCollections.value: the location can finish loading before
    // observeCollectionsUseCase()'s first emission lands, and a one-shot read of .value at that
    // instant would freeze this on an empty list forever, since _locationState never emits again
    // once it reaches Success.
    val collections: StateFlow<List<UltraSlimCollection>> = combine(
        _locationState,
        allCollections
    ) { state, all ->
        when (state) {
            is LocationState.Success -> {
                all.filter { collection ->
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
            
            logger.d { "📍 [ViewModel] Loading location: $locationId" }
            
            when (val result = getLocationUseCase(locationId)) {
                is Either.Right -> {
                    logger.d { "✅ [ViewModel] Location loaded: ${result.value.name}" }
                    _locationState.value = LocationState.Success(result.value)
                    getLocationUseCase.clearSelectedLocation()
                }
                is Either.Left -> {
                    logger.e { "❌ [ViewModel] Error loading location: ${result.value}" }
                    _locationState.value = LocationState.Error(result.value)
                }
            }
        }
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

            _attachmentMessage.value = when (
                fileHandoff.open(attachment.file, attachment.displayFileName())
            ) {
                Handoff.COULD_NOT_FETCH -> "Could not download this attachment."
                Handoff.NOTHING_TAKES_IT ->
                    "Nothing on this device can open a .${attachment.extension} file."
                Handoff.DONE -> null
            }
            _openingAttachmentId.value = null
        }
    }

    /**
     * Shares the card the server renders, not a link: this server sits on a private network, so a
     * URL would be useless to whoever receives it.
     */
    fun shareLocation(location: Location) {
        viewModelScope.launch {
            _attachmentMessage.value = when (val result = getShareImageUseCase(location.id)) {
                is Either.Left -> result.value
                is Either.Right -> {
                    val fileName = location.name.toSafeFileName(extension = "png")
                    when (fileHandoff.share(result.value, fileName)) {
                        Handoff.DONE -> null
                        else -> "Nothing on this device can share an image."
                    }
                }
            }
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