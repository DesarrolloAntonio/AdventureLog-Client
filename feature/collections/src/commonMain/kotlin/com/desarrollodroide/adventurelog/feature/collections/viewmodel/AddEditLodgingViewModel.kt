package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveLodgingUseCase
import com.desarrollodroide.adventurelog.core.model.Lodging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

/** What the server accepts in `type`, in the order the web offers them. */
val LodgingTypes = listOf(
    "hotel", "hostel", "resort", "bnb", "campground", "cabin", "apartment", "house", "villa",
    "motel", "other"
)

data class LodgingFormState(
    val name: String = "",
    val type: String = "hotel",
    val description: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val reservationNumber: String = "",
    val price: String = "",
    val link: String = "",
    val location: String = "",
    val isPublic: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

class AddEditLodgingViewModel(
    private val saveLodgingUseCase: SaveLodgingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LodgingFormState())
    val state: StateFlow<LodgingFormState> = _state.asStateFlow()

    private var lodgingId: String? = null
    private var existingTimezone: String? = null

    fun prefill(lodging: Lodging?) {
        if (lodging == null || lodgingId == lodging.id) return
        lodgingId = lodging.id
        existingTimezone = lodging.timezone
        _state.value = LodgingFormState(
            name = lodging.name,
            type = lodging.type,
            description = lodging.description.orEmpty(),
            checkIn = lodging.checkIn?.substringBefore('T').orEmpty(),
            checkOut = lodging.checkOut?.substringBefore('T').orEmpty(),
            reservationNumber = lodging.reservationNumber.orEmpty(),
            price = lodging.price.orEmpty(),
            link = lodging.link.orEmpty(),
            location = lodging.location.orEmpty(),
            isPublic = lodging.isPublic
        )
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v) }
    fun onTypeChange(v: String) = _state.update { it.copy(type = v) }
    fun onDescriptionChange(v: String) = _state.update { it.copy(description = v) }
    fun onCheckInChange(v: String) = _state.update { it.copy(checkIn = v) }
    fun onCheckOutChange(v: String) = _state.update { it.copy(checkOut = v) }
    fun onReservationChange(v: String) = _state.update { it.copy(reservationNumber = v) }
    fun onPriceChange(v: String) = _state.update { it.copy(price = v) }
    fun onLinkChange(v: String) = _state.update { it.copy(link = v) }
    fun onLocationChange(v: String) = _state.update { it.copy(location = v) }
    fun onPublicChange(v: Boolean) = _state.update { it.copy(isPublic = v) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun save(collectionId: String) {
        val form = _state.value
        if (!form.canSave) return
        _state.update { it.copy(isSaving = true, error = null) }

        // The device's IANA zone, never the bare "UTC" - the server's choices do not include it,
        // which is what made transportation fail with a 400 on its first attempt.
        val timezone = existingTimezone?.takeIf { it.isNotBlank() }
            ?: TimeZone.currentSystemDefault().id

        viewModelScope.launch {
            val result = saveLodgingUseCase(
                lodgingId = lodgingId,
                collectionId = collectionId,
                name = form.name.trim(),
                type = form.type,
                description = form.description.trim(),
                checkIn = form.checkIn.takeIf { it.isNotBlank() },
                checkOut = form.checkOut.takeIf { it.isNotBlank() },
                timezone = timezone.takeIf { form.checkIn.isNotBlank() || form.checkOut.isNotBlank() },
                reservationNumber = form.reservationNumber.trim(),
                price = form.price.takeIf { it.isNotBlank() },
                link = form.link.trim(),
                location = form.location.trim(),
                isPublic = form.isPublic
            )
            when (result) {
                is Either.Right -> _state.update { it.copy(isSaving = false, saved = true) }
                is Either.Left -> _state.update { it.copy(isSaving = false, error = result.value) }
            }
        }
    }
}
