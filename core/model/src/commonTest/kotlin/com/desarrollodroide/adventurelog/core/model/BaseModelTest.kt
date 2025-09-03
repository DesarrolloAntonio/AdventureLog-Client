package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Base class for model testing that provides common functionality
 * to reduce code duplication across model tests.
 */
abstract class BaseModelTest<T : Any> {
    
    protected val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    /**
     * Tests serialization and deserialization of the model.
     * Subclasses should call this method with their specific model instance.
     */
    protected fun testSerialization(
        model: T,
        serializer: KSerializer<T>,
        assertions: (String) -> Unit
    ) {
        val jsonString = json.encodeToString(serializer, model)
        assertions(jsonString)
        
        val deserialized = json.decodeFromString(serializer, jsonString)
        assertEquals(model, deserialized)
    }
    
    /**
     * Tests equality and hashCode implementation.
     */
    protected fun testEquality(
        original: T,
        equal: T,
        different: List<T>
    ) {
        assertEquals(original, equal)
        assertEquals(original.hashCode(), equal.hashCode())
        
        different.forEach { 
            assertTrue(original != it, "Expected $original to not equal $it")
        }
    }
}
