package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AttachmentTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun `should create Attachment with all properties`() {
        // Given
        val attachment = Attachment(
            id = "att-123",
            file = "https://example.com/files/document.pdf",
            extension = "pdf",
            name = "Adventure Guide",
            user = "789"
        )

        // Then
        assertEquals("att-123", attachment.id)
        assertEquals("https://example.com/files/document.pdf", attachment.file)
        assertEquals("pdf", attachment.extension)
        assertEquals("Adventure Guide", attachment.name)
        assertEquals("789", attachment.user)
    }

    @Test
    fun `should serialize to JSON correctly`() {
        // Given
        val attachment = Attachment(
            id = "test-att",
            file = "https://test.com/file.docx",
            extension = "docx",
            name = "Test Document",
            user = "123"
        )

        // When
        val jsonString = json.encodeToString(Attachment.serializer(), attachment)

        // Then
        assertTrue(jsonString.contains("\"id\":\"test-att\""))
        assertTrue(jsonString.contains("\"file\":\"https://test.com/file.docx\""))
        assertTrue(jsonString.contains("\"extension\":\"docx\""))
        assertTrue(jsonString.contains("\"name\":\"Test Document\""))
        assertTrue(jsonString.contains("\"user\":\"123\""))
    }

    @Test
    fun `should deserialize from JSON correctly`() {
        // Given
        val jsonString = """
            {
                "id": "json-att",
                "file": "https://json.com/download/map.jpg",
                "extension": "jpg",
                "name": "Trail Map",
                "user": "456"
            }
        """.trimIndent()

        // When
        val attachment = json.decodeFromString<Attachment>(jsonString)

        // Then
        assertEquals("json-att", attachment.id)
        assertEquals("https://json.com/download/map.jpg", attachment.file)
        assertEquals("jpg", attachment.extension)
        assertEquals("Trail Map", attachment.name)
        assertEquals("456", attachment.user)
    }

    @Test
    fun `should correctly compare Attachment instances`() {
        // Given
        val attachment1 = Attachment(
            id = "1",
            file = "https://example.com/file1.pdf",
            extension = "pdf",
            name = "Document 1",
            user = "1"
        )

        val attachment2 = attachment1.copy()
        val attachment3 = attachment1.copy(id = "2")
        val attachment4 = attachment1.copy(name = "Different Name")

        // Then
        assertEquals(attachment1, attachment2)
        assertNotEquals(attachment1, attachment3)
        assertNotEquals(attachment1, attachment4)
        assertEquals(attachment1.hashCode(), attachment2.hashCode())
    }

    @Test
    fun `should handle various file extensions`() {
        // Given
        val attachments = listOf(
            Attachment("1", "file1.pdf", "pdf", "PDF Document", "1"),
            Attachment("2", "file2.docx", "docx", "Word Document", "1"),
            Attachment("3", "file3.jpg", "jpg", "Image", "1"),
            Attachment("4", "file4.png", "png", "Screenshot", "1"),
            Attachment("5", "file5.gpx", "gpx", "GPS Track", "1"),
            Attachment("6", "file6.kml", "kml", "Map Data", "1")
        )

        // Then
        assertEquals("pdf", attachments[0].extension)
        assertEquals("docx", attachments[1].extension)
        assertEquals("jpg", attachments[2].extension)
        assertEquals("png", attachments[3].extension)
        assertEquals("gpx", attachments[4].extension)
        assertEquals("kml", attachments[5].extension)
    }

    @Test
    fun `should handle attachments with same user`() {
        // Given
        val userId = "100"
        val attachments = List(3) { index ->
            Attachment(
                id = "att-$index",
                file = "https://example.com/file$index.pdf",
                extension = "pdf",
                name = "Document $index",
                user = userId
            )
        }

        // Then
        assertTrue(attachments.all { it.user == userId })
        assertEquals(3, attachments.size)
    }

    @Test
    fun `should handle empty name`() {
        // Given
        val attachment = Attachment(
            id = "empty-name",
            file = "https://example.com/unnamed.pdf",
            extension = "pdf",
            name = "",
            user = "1"
        )

        // Then
        assertEquals("", attachment.name)
        assertTrue(attachment.name!!.isEmpty())
    }
}