package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttachmentTest : BaseModelTest<Attachment>() {

    @Test
    fun `should create Attachment with all properties`() {
        val attachment = TestDataFactory.createAttachment(
            id = "att-123",
            file = "https://example.com/files/document.pdf",
            extension = "pdf",
            name = "Adventure Guide",
            user = "789"
        )

        assertEquals("att-123", attachment.id)
        assertEquals("https://example.com/files/document.pdf", attachment.file)
        assertEquals("pdf", attachment.extension)
        assertEquals("Adventure Guide", attachment.name)
        assertEquals("789", attachment.user)
    }

    @Test
    fun `should serialize and deserialize correctly`() {
        val attachment = TestDataFactory.createAttachment(
            id = "test-att",
            file = "https://test.com/file.docx",
            extension = "docx",
            name = "Test Document",
            user = "123"
        )

        testSerialization(attachment, Attachment.serializer()) { jsonString ->
            assertTrue(jsonString.contains("\"id\":\"test-att\""))
            assertTrue(jsonString.contains("\"file\":\"https://test.com/file.docx\""))
            assertTrue(jsonString.contains("\"extension\":\"docx\""))
            assertTrue(jsonString.contains("\"name\":\"Test Document\""))
            assertTrue(jsonString.contains("\"user\":\"123\""))
        }
    }

    @Test
    fun `should correctly implement equals and hashCode`() {
        val attachment1 = TestDataFactory.createAttachment()
        val attachment2 = attachment1.copy()
        val attachment3 = attachment1.copy(id = "2")
        val attachment4 = attachment1.copy(name = "Different Name")

        testEquality(
            original = attachment1,
            equal = attachment2,
            different = listOf(attachment3, attachment4)
        )
    }

    @Test
    fun `should handle various file extensions`() {
        val extensions = listOf("pdf", "docx", "jpg", "png", "gpx", "kml")
        
        extensions.forEach { ext ->
            val attachment = TestDataFactory.createAttachment(
                file = "file.$ext",
                extension = ext
            )
            assertEquals(ext, attachment.extension)
        }
    }

    @Test
    fun `should handle edge cases`() {
        val emptyNameAttachment = TestDataFactory.createAttachment(name = "")
        assertEquals("", emptyNameAttachment.name)
        assertTrue(emptyNameAttachment.name!!.isEmpty())
        
        val userId = "100"
        val attachments = List(3) { index ->
            TestDataFactory.createAttachment(
                id = "att-$index",
                user = userId
            )
        }
        assertTrue(attachments.all { it.user == userId })
    }
}
