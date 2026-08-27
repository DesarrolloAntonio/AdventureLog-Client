package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TagsTest {

    @Test
    fun `an importer's identifier is a machine tag`() {
        assertTrue("tb:dae062bf-80d3-46e4-aef6-aa3e8c53ebea".isMachineTag())
    }

    @Test
    fun `the prefix is not what makes it one - the identifier is`() {
        assertTrue("DAE062BF-80D3-46E4-AEF6-AA3E8C53EBEA".isMachineTag())
        assertTrue("imported:cdb2e7c5-dd9d-4091-9b13-4f59ee4dff51:v2".isMachineTag())
        assertFalse("tb:hiking".isMachineTag())
    }

    @Test
    fun `tags a person would type are kept`() {
        listOf("hiking", "with the kids", "2024", "tb", "a-b-c-d-e").forEach {
            assertFalse(it.isMachineTag(), it)
        }
    }

    @Test
    fun `a near-miss is not a UUID`() {
        // One group short of the shape, so it is somebody's odd label rather than an identifier.
        assertFalse("dae062bf-80d3-46e4-aef6".isMachineTag())
    }

    @Test
    fun `filtering keeps the order of what is left`() {
        val tags = listOf(
            "hiking",
            "tb:dae062bf-80d3-46e4-aef6-aa3e8c53ebea",
            "with the kids"
        )
        assertEquals(listOf("hiking", "with the kids"), tags.userTags())
    }
}
