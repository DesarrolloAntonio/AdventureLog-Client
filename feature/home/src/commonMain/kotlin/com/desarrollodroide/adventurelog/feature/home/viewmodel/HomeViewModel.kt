package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetUserStatsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.model.StatsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAdventuresUseCase: GetAdventuresUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: UserRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()

    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> = _userDetails.asStateFlow()

    init {
        observeUserSession()
        // Cargar adventures y stats de forma independiente
        loadAdventures()
    }

    /**
     * Observe user session changes
     */
    private fun observeUserSession() {
        viewModelScope.launch {
            userRepository.getUserSession().collect { userDetails ->
                _userDetails.value = userDetails

                // Update UI state with the new user name if needed
                if (_uiState.value is HomeUiState.Success) {
                    _uiState.update { currentState ->
                        if (currentState is HomeUiState.Success) {
                            currentState.copy(
                                userName = userDetails?.firstName ?: "User"
                            )
                        } else {
                            currentState
                        }
                    }
                }
                
                // Load user stats when we have user details
                userDetails?.let {
                    loadUserStats(it.username)
                }
            }
        }
    }

    private fun loadAdventures() {
        viewModelScope.launch {
            try {
                // Show loading state
                _uiState.update { HomeUiState.Loading }

                // Load only 5 adventures for home screen
                when (val result = getAdventuresUseCase(
                    page = 1,
                    pageSize = 5  // Only load what we need to show
                )) {
                    is Either.Left -> {
                        val errorMessage = result.value
                        _uiState.update { HomeUiState.Error(errorMessage) }
                        println("ERROR LOADING ADVENTURES: $errorMessage")
                    }

                    is Either.Right -> {
                        val allAdventures = result.value
                        // Take only the first 3 for recent adventures
                        val recentAdventures = allAdventures.take(3)

                        _uiState.update {
                            HomeUiState.Success(
                                userName = _userDetails.value?.firstName ?: "User",
                                recentAdventures = recentAdventures
                            )
                        }
                        println("LOADED ADVENTURES SUCCESSFULLY: ${allAdventures.size} total, showing ${recentAdventures.size} recent")
                        recentAdventures.forEach {
                            println("Recent Adventure: ${it.id} - ${it.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle any unexpected errors
                _uiState.update { HomeUiState.Error(e.message ?: "Error inesperado") }
                println("EXCEPTION LOADING ADVENTURES: ${e.message}")
                e.printStackTrace()
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
                println("HomeViewModel: Logout completed successfully")
            } catch (e: Exception) {
                println("HomeViewModel: Error during logout: ${e.message}")
            }
        }
    }
    
    private fun loadUserStats(username: String) {
        viewModelScope.launch {
            // Set loading state for stats
            _statsState.update { StatsUiState.Loading }
            
            when (val result = getUserStatsUseCase(username)) {
                is Either.Left -> {
                    println("ERROR LOADING USER STATS: ${result.value}")
                    _statsState.update { StatsUiState.Error(result.value) }
                }
                is Either.Right -> {
                    val stats = result.value
                    println("LOADED USER STATS SUCCESSFULLY: $stats")
                    _statsState.update { StatsUiState.Success(stats) }
                }
            }
        }
    }
}