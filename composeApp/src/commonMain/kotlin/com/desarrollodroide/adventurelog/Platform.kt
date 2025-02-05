package com.desarrollodroide.adventurelog

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform