package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionItemUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveChecklistUseCase
import com.desarrollodroide.adventurelog.core.model.Checklist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A line being edited. It has no id yet on the way in, and needs none: the server takes the set. */
data class ChecklistLine(val name: String, val checked: Boolean)

data class ChecklistFormState(
    val name: String = "",
    val lines: List<ChecklistLine> = emptyList(),
    val draft: String = "",
    val date: String = "",
    val isPublic: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val error: String? = null,
    val saved: Boolean = false
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
    val doneCount: Int get() = lines.count { it.checked }

    /** What a save would send is the same. A half-typed line that was never added is not sent. */
    fun sameContentAs(other: ChecklistFormState): Boolean =
        name.trim() == other.name.trim() && lines == other.lines && date == other.date && isPublic == other.isPublic
}

class AddEditChecklistViewModel(
    private val saveChecklistUseCase: SaveChecklistUseCase,
    private val getCollectionItemUseCase: GetCollectionItemUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChecklistFormState())
    val state: StateFlow<ChecklistFormState> = _state.asStateFlow()

    private var checklistId: String? = null

    /** The checklist as it was read, to tell a save that changes something from one that does not. */
    private var asLoaded: ChecklistFormState? = null

    /** The checklist as the server has it now - see AddEditNoteViewModel.load. */
    fun load(collectionId: String, id: String?) {
        if (id == null || checklistId == id) return
        checklistId = id
        _state.value = ChecklistFormState(isLoading = true)
        viewModelScope.launch {
            when (val result = getCollectionItemUseCase(collectionId) { c -> c.checklists.firstOrNull { it.id == id } }) {
                is Either.Left -> _state.update { it.copy(isLoading = false, loadError = result.value) }
                is Either.Right -> {
                    val checklist = result.value
                    _state.value = ChecklistFormState(
                        name = checklist.name,
                        lines = checklist.items.map { ChecklistLine(it.name, it.isChecked) },
                        date = checklist.date?.substringBefore('T').orEmpty(),
                        isPublic = checklist.isPublic
                    ).also { asLoaded = it }
                }
            }
        }
    }

    fun retryLoad(collectionId: String) {
        val id = checklistId ?: return
        checklistId = null
        load(collectionId, id)
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onDraftChange(value: String) = _state.update { it.copy(draft = value) }
    fun onDateChange(value: String) = _state.update { it.copy(date = value) }
    fun onPublicChange(value: Boolean) = _state.update { it.copy(isPublic = value) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun addLine() {
        val text = _state.value.draft.trim()
        if (text.isEmpty()) return
        _state.update {
            it.copy(lines = it.lines + ChecklistLine(text, false), draft = "")
        }
    }

    fun toggleLine(index: Int) = _state.update { state ->
        state.copy(
            lines = state.lines.mapIndexed { i, line ->
                if (i == index) line.copy(checked = !line.checked) else line
            }
        )
    }

    fun removeLine(index: Int) = _state.update { state ->
        state.copy(lines = state.lines.filterIndexed { i, _ -> i != index })
    }

    fun save(collectionId: String) {
        val form = _state.value
        if (!form.canSave) return

        // The server throws every item away and makes new ones on any save - it ignores the ids it
        // is sent, and a body without items empties the list (measured, QA 04 CO-04). Nothing the
        // app sends can keep them, so a save that changes nothing sends nothing.
        val before = asLoaded
        if (checklistId != null && before != null && form.sameContentAs(before)) {
            _state.update { it.copy(saved = true) }
            return
        }
        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = saveChecklistUseCase(
                checklistId = checklistId,
                collectionId = collectionId,
                name = form.name.trim(),
                items = form.lines.map { it.name to it.checked },
                date = form.date.takeIf { it.isNotBlank() },
                isPublic = form.isPublic
            )
            when (result) {
                is Either.Right -> _state.update { it.copy(isSaving = false, saved = true) }
                is Either.Left -> _state.update { it.copy(isSaving = false, error = result.value) }
            }
        }
    }
}
