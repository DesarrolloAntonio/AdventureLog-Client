package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveNoteUseCase
import com.desarrollodroide.adventurelog.core.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteFormState(
    val name: String = "",
    val content: String = "",
    val date: String = "",
    val isPublic: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

class AddEditNoteViewModel(
    private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NoteFormState())
    val state: StateFlow<NoteFormState> = _state.asStateFlow()

    private var noteId: String? = null

    fun prefill(note: Note?) {
        if (note == null || noteId == note.id) return
        noteId = note.id
        _state.value = NoteFormState(
            name = note.name,
            content = note.content.orEmpty(),
            date = note.date?.substringBefore('T').orEmpty(),
            isPublic = note.isPublic
        )
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onContentChange(value: String) = _state.update { it.copy(content = value) }
    fun onDateChange(value: String) = _state.update { it.copy(date = value) }
    fun onPublicChange(value: Boolean) = _state.update { it.copy(isPublic = value) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun save(collectionId: String) {
        val form = _state.value
        if (!form.canSave) return
        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = saveNoteUseCase(
                noteId = noteId,
                collectionId = collectionId,
                name = form.name.trim(),
                content = form.content.trim(),
                date = form.date.takeIf { it.isNotBlank() },
                isPublic = form.isPublic
            )
            when (result) {
                is Either.Right -> _state.update { it.copy(isSaving = false, saved = true) }
                is Either.Left -> _state.update {
                    it.copy(isSaving = false, error = result.value)
                }
            }
        }
    }
}
