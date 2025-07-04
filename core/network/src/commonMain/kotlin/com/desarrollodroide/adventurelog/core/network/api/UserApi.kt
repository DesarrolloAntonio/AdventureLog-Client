package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO

interface UserApi {
    /**
     * Get current user details
     */
    suspend fun getUserDetails(): UserDetailsDTO
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(
        username: String? = null,
        email: String? = null,
        displayName: String? = null
    ): UserDetailsDTO
    
    /**
     * Change user password
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Boolean
    
    /**
     * Upload user avatar
     */
    suspend fun uploadAvatar(imageData: ByteArray): String
    
    /**
     * Get user statistics
     */
    suspend fun getUserStats(username: String): UserStatsDTO
}
