package com.example.storyround.identity

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UuidIdentifierGeneratorTest {
    private val generator = UuidIdentifierGenerator()

    @Test
    fun `given two identifiers when generated then each is a distinct UUID`() {
        // When
        val first = generator.generate()
        val second = generator.generate()

        // Then
        assertNotEquals(first, second)
        assertEquals(first, UUID.fromString(first).toString())
    }
}
