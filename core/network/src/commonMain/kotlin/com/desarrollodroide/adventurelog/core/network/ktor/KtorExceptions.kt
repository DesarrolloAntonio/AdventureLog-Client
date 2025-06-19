package com.desarrollodroide.adventurelog.core.network.ktor

class HttpException(val code: Int, override val message: String) : Exception(message)
