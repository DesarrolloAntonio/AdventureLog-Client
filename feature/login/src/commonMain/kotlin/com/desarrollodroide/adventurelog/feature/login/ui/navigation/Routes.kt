package com.desarrollodroide.adventurelog.feature.login.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login {
    const val route = "login"
}

@Serializable
internal data object LoginScreen {
    const val route = "login_screen"
}