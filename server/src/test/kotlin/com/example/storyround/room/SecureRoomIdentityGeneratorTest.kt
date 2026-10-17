package com.example.storyround.room

import com.example.storyround.identity.IdentifierGenerator
import com.example.storyround.random.RandomNumberGenerator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class SecureRoomIdentityGeneratorTest {
    private val randomNumberGenerator = mockk<RandomNumberGenerator>()
    private val identifierGenerator = mockk<IdentifierGenerator>()
    private val generator = SecureRoomIdentityGenerator(randomNumberGenerator, identifierGenerator)

    @Test
    fun `given injected generators when identities are requested then their generated values are used`() {
        // Given
        every { randomNumberGenerator.nextInt(any()) } returns 1
        every { identifierGenerator.generate() } returnsMany listOf("player-id", "session-token")

        // When
        val roomCode = generator.generateRoomCode()
        val playerId = generator.generatePlayerId()
        val sessionToken = generator.generateSessionToken()

        // Then
        assertEquals("BBBB", roomCode)
        assertEquals("player-id", playerId)
        assertEquals("session-token", sessionToken)
        verify(exactly = 4) { randomNumberGenerator.nextInt(32) }
    }
}
