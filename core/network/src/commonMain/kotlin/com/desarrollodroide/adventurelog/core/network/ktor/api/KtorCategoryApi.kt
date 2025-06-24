package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.CategoryApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class KtorCategoryNetworkDataSource(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json = defaultJson
) : CategoryApi {

    private val logger = Logger.withTag("KtorCategoryNetworkDataSource")

    override suspend fun getCategories(): List<CategoryDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/categories/"
        
        logger.d { "Fetching categories from: $url" }
        
        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch categories with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        logger.d { "Categories response: $responseText" }
        
        return json.decodeFromString<List<CategoryDTO>>(responseText)
    }
}
