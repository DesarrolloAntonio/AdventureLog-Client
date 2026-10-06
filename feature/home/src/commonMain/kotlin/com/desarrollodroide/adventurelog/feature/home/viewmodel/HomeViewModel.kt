package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.EndRejectedSessionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetDashboardUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.model.fullName
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getDashboardUseCase: GetDashboardUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: UserRepository,
    private val initializeSessionUseCase: InitializeSessionUseCase,
    private val endRejectedSessionsUseCase: EndRejectedSessionsUseCase
) : ViewModel() {

    private val logger = co.touchlab.kermit.Logger.withTag("HomeViewModel")

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> = _userDetails.asStateFlow()

    /**
     * True once there is no session, however it ended: Sign out, the server rejecting it, or an app
     * restored after process death that never had one to keep. The shell takes the user to Login
     * on it, so no screen is left talking to a server as nobody.
     */
    val signedOut: StateFlow<Boolean> = userRepository.getUserSession()
        .map { it == null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, userRepository.activeSession == null)

    init {
        observeUserSession()
        viewModelScope.launch { endRejectedSessionsUseCase() }
        loadDashboard()
    }

    /**
     * Asks the server whether the session still stands. Called each time the app comes back to the
     * foreground: a session can expire, or be signed out from the web, while the app waits in the
     * background, and nothing else would notice until a screen misread the answer.
     */
    fun recheckSession() {
        viewModelScope.launch { initializeSessionUseCase() }
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

    /**
     * Reloads the dashboard behind what is on screen, for each time Home comes back into view.
     *
     * Home loaded once per ViewModel and never again: a place added from its own hero, a trip
     * created elsewhere, a visit marked on another tab - it kept the old counts until the app was
     * restarted (measured: 22 places on screen, 23 on the server, after switching tabs and after
     * coming back from the background). Unlike [loadDashboard] this keeps the dashboard showing
     * while it asks, and keeps it if the server can't be reached - a stale screen is better than a
     * spinner followed by an error over data that was fine a moment ago.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun refreshDashboard() {
        // Nothing shown yet (first load in flight, or its error on screen): that has its own path.
        if (_uiState.value !is HomeUiState.Success) return
        viewModelScope.launch {
            when (val result = getDashboardUseCase()) {
                is Either.Left -> logger.w { "Could not refresh the dashboard, keeping what is shown: ${result.value}" }
                is Either.Right -> {
                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    _uiState.update { current ->
                        if (current is HomeUiState.Success) current.copy(dashboard = result.value, today = today)
                        else current
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
     * Performs user logout. Clearing the session is what takes the user to Login, through
     * [signedOut] - navigating here as well raced the clearing against the next screen reading it.
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
