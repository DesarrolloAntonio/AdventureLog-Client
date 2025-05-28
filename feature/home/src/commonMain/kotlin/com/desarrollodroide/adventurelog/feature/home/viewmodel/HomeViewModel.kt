package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.domain.LogoutUseCase
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAdventuresUseCase: GetAdventuresUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> = _userDetails.asStateFlow()

    init {
        observeUserSession()
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
            }
        }
    }

    /**
     * Load adventures from the API
     */
    private fun loadAdventures() {
        viewModelScope.launch {
            try {
                // Show loading state
                _uiState.update { HomeUiState.Loading }

                // Call our use case with pageSize=3 for Recent adventures
                when (val result = getAdventuresUseCase(
                    page = 1,
                    pageSize = 3
                )) {
                    is Either.Left -> {
                        val errorMessage = result.value
                        _uiState.update { HomeUiState.Error(errorMessage) }
                        println("ERROR LOADING ADVENTURES: $errorMessage")
                    }

                    is Either.Right -> {
                        val adventures = result.value
                        _uiState.update {
                            HomeUiState.Success(
                                userName = _userDetails.value?.firstName ?: "User",
                                recentAdventures = adventures
                            )
                        }
                        println("LOADED ADVENTURES SUCCESSFULLY: ${adventures.size} items")
                        adventures.forEach {
                            println("Adventure: ${it.id} - ${it.name}")
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
}