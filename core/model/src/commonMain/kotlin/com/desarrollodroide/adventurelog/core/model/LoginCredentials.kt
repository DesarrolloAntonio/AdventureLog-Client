package com.desarrollodroide.adventurelog.core.model

data class LoginCredentials(
    val username: String,
    val password: String,
    val serverUrl: String,
    val rememberCredentials: Boolean
)