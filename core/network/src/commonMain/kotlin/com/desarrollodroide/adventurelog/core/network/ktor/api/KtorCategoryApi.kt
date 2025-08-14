package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.CategoryApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.request.CreateCategoryRequest
import com.desarrollodroide.adventurelog.core.network.model.request.UpdateCategoryRequest
import com.desarrollodroide.adventurelog.core.network.model.response.CategoryDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class KtorCategoryApi(
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
    
    override suspend fun getCategoryById(categoryId: String): CategoryDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/categories/$categoryId/"
        
        logger.d { "Fetching category from: $url" }
        
        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch category with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<CategoryDTO>(responseText)
    }
    
    override suspend fun createCategory(
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/categories/"
        
        val requestBody = CreateCategoryRequest(
            name = name,
            displayName = displayName,
            icon = icon
        )
        
        logger.d { "Creating category at: $url" }
        
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to create category with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<CategoryDTO>(responseText)
    }
    
    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): CategoryDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/categories/$categoryId/"
        
        val requestBody = UpdateCategoryRequest(
            name = name,
            displayName = displayName,
            icon = icon
        )
        
        logger.d { "Updating category at: $url" }
        
        val response = httpClient.put(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to update category with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<CategoryDTO>(responseText)
    }
    
    override suspend fun deleteCategory(categoryId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/categories/$categoryId/"
        
        logger.d { "Deleting category at: $url" }
        
        val response = httpClient.delete(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to delete category with status: ${response.status}"
            )
        }
    }
}
