package com.desarrollodroide.adventurelog.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.SharingRepository
import com.desarrollodroide.adventurelog.core.model.PublicUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsersUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val users: List<PublicUser> = emptyList(),
    val query: String = ""
) {
    val filtered: List<PublicUser>
        get() = if (query.isBlank()) {
            users
        } else {
            users.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                    it.username.contains(query, ignoreCase = true)
            }
        }
}

/**
 * The people on this server who have made their profile public.
 *
 * The call behind it already existed - collection sharing uses it to decide who a collection can
 * reach - but nothing ever showed the list on its own, so the web's Usuarios section had no
 * counterpart here.
 */
class UsersViewModel(
    private val sharingRepository: SharingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = sharingRepository.getPublicUsers()) {
                is Either.Right -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = result.value.sortedBy { user -> user.displayName.lowercase() }
                    )
                }
                is Either.Left -> _uiState.update {
                    it.copy(isLoading = false, error = result.value)
                }
            }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }
}
