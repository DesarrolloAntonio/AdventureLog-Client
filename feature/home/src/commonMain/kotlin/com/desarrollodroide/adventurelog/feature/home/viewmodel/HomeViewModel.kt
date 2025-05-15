package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAdventuresUseCase: GetAdventuresUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> = _userDetails.asStateFlow()

    init {
        loadUserProfile()
        loadAdventures()
    }
    
    /**
     * Load adventures from the API
     */
    private fun loadAdventures() {
        viewModelScope.launch {
            try {
                // Show loading state
                _uiState.update { HomeUiState.Loading }
                
                // Call our use case
                when (val result = getAdventuresUseCase(1)) {
                    is com.desarrollodroide.adventurelog.core.common.Either.Left -> {
                        val errorMessage = result.value
                        _uiState.update { HomeUiState.Error(errorMessage) }
                        println("ERROR LOADING ADVENTURES: $errorMessage")
                    }
                    is com.desarrollodroide.adventurelog.core.common.Either.Right -> {
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

    private fun loadUserProfile() {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(500)
            
            // Load user profile data
            // In a real implementation, this would come from a repository or API
            _userDetails.value = UserDetails(
                id = 1,
                profilePic = "http://192.168.1.27:8016/media/profile-pics/1200x655_iStock-2097492658.webp",
                uuid = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
                publicProfile = true,
                username = "memnoch",
                email = "antonio@test.com",
                firstName = "Antonio",
                lastName = "Corrales",
                dateJoined = "2025-01-30T07:15:10.367579Z",
                isStaff = false,
                hasPassword = "true",
                sessionToken = "session_token_value"
            )
        }
    }
}