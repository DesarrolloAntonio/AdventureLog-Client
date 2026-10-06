package com.desarrollodroide.adventurelog.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.SharingRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetUserStatsUseCase
import com.desarrollodroide.adventurelog.core.model.PublicUser
import com.desarrollodroide.adventurelog.core.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsersUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val users: List<PublicUser> = emptyList(),
    val query: String = "",
    /** The person whose profile is open, if any. */
    val profile: ProfileState? = null
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

/** One public profile: who it is and what they have done, as the web's profile page opens with. */
data class ProfileState(
    val person: PublicUser,
    val stats: UserStats? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * The people on this server who have made their profile public.
 *
 * The call behind it already existed - collection sharing uses it to decide who a collection can
 * reach - but nothing ever showed the list on its own, so the web's Usuarios section had no
 * counterpart here.
 */
class UsersViewModel(
    private val sharingRepository: SharingRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase
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

    /**
     * A person in the list used to open nothing (QA 09, MC-05). The web opens their profile, whose
     * first half is their numbers - `/api/stats/counts/<username>`, which answers for anyone with a
     * public profile, and everyone on this list has one.
     */
    fun openProfile(person: PublicUser) {
        _uiState.update { it.copy(profile = ProfileState(person)) }
        viewModelScope.launch {
            val result = getUserStatsUseCase(person.username)
            _uiState.update { state ->
                val open = state.profile
                // Closed, or someone else opened, while this was on its way.
                if (open == null || open.person.uuid != person.uuid) return@update state
                state.copy(
                    profile = when (result) {
                        is Either.Right -> open.copy(stats = result.value, isLoading = false, error = null)
                        is Either.Left -> open.copy(isLoading = false, error = result.value)
                    }
                )
            }
        }
    }

    fun retryProfile() {
        _uiState.value.profile?.person?.let(::openProfile)
    }

    fun closeProfile() = _uiState.update { it.copy(profile = null) }
}
