package com.desarrollodroide.adventurelog.feature.login.model

data class LoginFormState(
    val userName: String = "",
    val password: String = "",
    val serverUrl: String = "",
    val rememberSession: Boolean = false,
    val userNameError: Boolean = false,
    val passwordError: Boolean = false,
    val urlError: Boolean = false
)