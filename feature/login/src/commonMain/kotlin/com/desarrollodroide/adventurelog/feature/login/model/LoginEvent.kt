package com.desarrollodroide.adventurelog.feature.login.model

sealed class LoginEvent {
    data class SendClicked(val userName: String, val password: String) : LoginEvent()
}