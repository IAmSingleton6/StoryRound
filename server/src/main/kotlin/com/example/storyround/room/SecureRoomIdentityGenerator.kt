package com.example.storyround.room

import com.example.storyround.identity.IdentifierGenerator
import com.example.storyround.random.RandomNumberGenerator

internal class SecureRoomIdentityGenerator(
    private val randomNumberGenerator: RandomNumberGenerator,
    private val identifierGenerator: IdentifierGenerator,
) : RoomIdentityGenerator {
    override fun generateRoomCode(): String =
        buildString(ROOM_CODE_LENGTH) {
            repeat(ROOM_CODE_LENGTH) { append(ROOM_CODE_ALPHABET[randomNumberGenerator.nextInt(ROOM_CODE_ALPHABET.length)]) }
        }

    override fun generatePlayerId(): String = identifierGenerator.generate()

    override fun generateSessionToken(): String = identifierGenerator.generate()
}

private const val ROOM_CODE_LENGTH = 4
private const val ROOM_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
