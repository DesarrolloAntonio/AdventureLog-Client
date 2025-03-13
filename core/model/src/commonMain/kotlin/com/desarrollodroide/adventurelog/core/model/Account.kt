package com.desarrollodroide.adventurelog.core.model

class Account(
    val id: Int = -1,
    val userName: String,
    val password: String,
    val serverUrl: String,
) {
    constructor() : this(
        id = -1,
        userName = "",
        password = "",
        serverUrl = "",
    )

    companion object {
        val mock = Account(
            id = 1,
            userName = "user@example.com",
            password = "securePassword123",
            serverUrl = "https://api.example.com",
        )
    }
}