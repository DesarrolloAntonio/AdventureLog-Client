package com.desarrollodroide.adventurelog.feature.login.model

data class LoginFormState(
    // Testing data
//    val userName: String = "memnoch",
//    val password: String = "JordalanA1",
//    val serverUrl: String = "http://192.168.1.27:8016/",
    val userName: String = "",
    val password: String = "",
    val serverUrl: String = "",
    val rememberSession: Boolean = false,
    val userNameError: Boolean = false,
    val passwordError: Boolean = false,
    val urlError: Boolean = false
)