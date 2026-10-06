package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.network.api.ChecklistApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.request.ChecklistItemRequest
import com.desarrollodroide.adventurelog.core.network.model.request.ChecklistRequest
import com.desarrollodroide.adventurelog.core.network.model.response.ChecklistDTO
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class KtorChecklistApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : ChecklistApi {

    private val logger = Logger.withTag("KtorChecklistApi")

    override suspend fun createChecklist(
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): Checklist {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/checklists/"
        logger.d { "Creating checklist at: $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                ChecklistRequest(
                    name = name,
                    items = items.map { ChecklistItemRequest(it.first, it.second) },
                    date = date?.takeIf { it.isNotBlank() },
                    isPublic = isPublic,
                    collection = collectionId
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body<ChecklistDTO>().toDomainModel()
        }
        logger.e { "Failed to create checklist: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to create the checklist: ${response.status}"
        )
    }

    override suspend fun updateChecklist(
        checklistId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Checklist {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/checklists/$checklistId/"
        logger.d { "Updating checklist at: $url" }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                ChecklistRequest(
                    name = name,
                    items = items.map { ChecklistItemRequest(it.first, it.second) },
                    date = date?.takeIf { it.isNotBlank() },
                    isPublic = isPublic
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body<ChecklistDTO>().toDomainModel()
        }
        logger.e { "Failed to update checklist: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to save the checklist: ${response.status}"
        )
    }

    override suspend fun deleteChecklist(checklistId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/checklists/$checklistId/"
        logger.d { "Deleting checklist at: $url" }

        val response = httpClient.delete(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            logger.e { "Failed to delete checklist: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to delete the checklist: ${response.status}"
            )
        }
    }
}
