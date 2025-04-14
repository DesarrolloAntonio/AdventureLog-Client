package com.desarrollodroide.adventurelog.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    // Add Use cases classes here
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _userDetails = MutableStateFlow<UserDetails?>(null)
    val userDetails: StateFlow<UserDetails?> = _userDetails.asStateFlow()

    init {
        loadTempPreviewData()
        loadUserProfile()
    }
    
    /**
     * Temporary function to load preview data while network is not implemented
     */
    private fun loadTempPreviewData() {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(800)
            
            // Use the preview data
            _uiState.update {
                HomeUiState.Success(
                    userName = "User",
                    recentAdventures = PreviewData.adventures
                )
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(500)
            
            // Load user profile data
            // In a real implementation, this would come from a repository or API
            // Load test user profile from provided JSON
            _userDetails.value = UserDetails(
                // Added email field that exists in UserDetails
                // Note: in UserDetails it is String instead of Boolean
                id = 1,
                profilePic = "http://192.168.1.27:8016/media/profile-pics/1200x655_iStock-2097492658.webp",
                uuid = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
                publicProfile = true,
                username = "memnoch",
                email = "antonio@test.com", // Añadido campo email que existe en UserDetails
                firstName = "Antonio",
                lastName = "Corrales",
                dateJoined = "2025-01-30T07:15:10.367579Z",
                isStaff = false,
                hasPassword = "true"
            )
        }
    }
}