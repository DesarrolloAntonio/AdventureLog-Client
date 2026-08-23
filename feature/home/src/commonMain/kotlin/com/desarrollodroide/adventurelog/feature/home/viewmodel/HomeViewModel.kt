package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetDashboardUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.model.fullName
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getDashboardUseCase: GetDashboardUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val logger = co.touchlab.kermit.Logger.withTag("HomeViewModel")

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> = _userDetails.asStateFlow()

    init {
        observeUserSession()
        loadDashboard()
    }

    /**
     * The session carries the display name. It can land after the dashboard does, so the greeting
     * is patched into whatever state is already on screen rather than triggering a reload.
     */
    private fun observeUserSession() {
        viewModelScope.launch {
            userRepository.getUserSession().collect { userDetails ->
                _userDetails.value = userDetails

                _uiState.update { current ->
                    if (current is HomeUiState.Success) {
                        current.copy(userName = userDetails?.fullName ?: "User")
                    } else {
                        current
                    }
                }
            }
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { HomeUiState.Loading }

            when (val result = getDashboardUseCase()) {
                is Either.Left -> {
                    logger.e { "Error loading dashboard: ${result.value}" }
                    _uiState.update { HomeUiState.Error(result.value) }
                }

                is Either.Right -> {
                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    _uiState.update {
                        HomeUiState.Success(
                            userName = _userDetails.value?.fullName ?: "User",
                            dashboard = result.value,
                            today = today
                        )
                    }
                }
            }
        }
    }

    /**
     * Performs user logout
     * Clears all session data and navigates back to login
     */
    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
            } catch (e: Exception) {
                logger.e(e) { "Error during logout" }
            }
        }
    }

    fun selectLocation(location: Location) {
        getLocationsUseCase.selectLocation(location)
    }
}
