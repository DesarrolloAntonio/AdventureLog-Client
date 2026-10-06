package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.network.api.NoteApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.request.NoteRequest
import com.desarrollodroide.adventurelog.core.network.model.response.NoteDTO
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

class KtorNoteApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : NoteApi {

    private val logger = Logger.withTag("KtorNoteApi")

    override suspend fun createNote(
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): Note {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/notes/"
        logger.d { "Creating note at: $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                NoteRequest(
                    name = name,
                    content = content,
                    date = date?.takeIf { it.isNotBlank() },
                    isPublic = isPublic,
                    collection = collectionId
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body<NoteDTO>().toDomainModel()
        }
        logger.e { "Failed to create note: ${response.status}" }
        throw HttpException(response.status.value, "Failed to create the note: ${response.status}")
    }

    override suspend fun updateNote(
        noteId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Note {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/notes/$noteId/"
        logger.d { "Updating note at: $url" }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                NoteRequest(
                    name = name,
                    content = content,
                    date = date?.takeIf { it.isNotBlank() },
                    isPublic = isPublic
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body<NoteDTO>().toDomainModel()
        }
        logger.e { "Failed to update note: ${response.status}" }
        throw HttpException(response.status.value, "Failed to save the note: ${response.status}")
    }

    override suspend fun deleteNote(noteId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/notes/$noteId/"
        logger.d { "Deleting note at: $url" }

        val response = httpClient.delete(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            logger.e { "Failed to delete note: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to delete the note: ${response.status}"
            )
        }
    }
}
