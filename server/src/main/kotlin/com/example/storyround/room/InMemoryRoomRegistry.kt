package com.example.storyround.room

import com.example.storyround.time.SystemTimeProvider
import com.example.storyround.time.TimeProvider
import java.security.SecureRandom
import java.util.UUID

class InMemoryRoomRegistry(
    private val roomCodeGenerator: () -> String = ::generateRoomCode,
    private val playerIdGenerator: () -> String = { UUID.randomUUID().toString() },
    private val timeProvider: TimeProvider = SystemTimeProvider(),
    private val rounds: Int = TurnRules.DEFAULT_ROUNDS,
    private val firstTurnMillis: Long = TurnRules.DEFAULT_FIRST_TURN_MILLIS,
    private val reductionMillis: Long = TurnRules.DEFAULT_REDUCTION_MILLIS,
    private val minimumTurnMillis: Long = TurnRules.DEFAULT_MINIMUM_TURN_MILLIS,
    private val sessionTokenGenerator: () -> String = { UUID.randomUUID().toString() },
) : RoomRegistry {
    private val engine: GameRoomEngine =
        DefaultGameRoomEngine(
            roomStore = InMemoryRoomStore(),
            roomCodeGenerator = roomCodeGenerator,
            playerIdGenerator = playerIdGenerator,
            timeProvider = timeProvider,
            turnRules = TurnRules(rounds, firstTurnMillis, reductionMillis, minimumTurnMillis),
            sessionTokenGenerator = sessionTokenGenerator,
        )

    override fun createRoom(playerName: String) = engine.createRoom(playerName)

    override fun joinRoom(
        roomCode: String,
        playerName: String,
    ) = engine.joinRoom(roomCode, playerName)

    override fun reconnect(
        roomCode: String,
        playerId: String,
        sessionToken: String,
    ) = engine.reconnect(roomCode, playerId, sessionToken)

    override fun startGame(
        roomCode: String,
        playerId: String,
    ) = engine.startGame(roomCode, playerId)

    override fun submitPrompt(
        roomCode: String,
        playerId: String,
        text: String,
    ) = engine.submitPrompt(roomCode, playerId, text)

    override fun submitContribution(
        roomCode: String,
        playerId: String,
        text: String,
    ) = engine.submitContribution(roomCode, playerId, text)

    override fun playAgain(
        roomCode: String,
        playerId: String,
    ) = engine.playAgain(roomCode, playerId)

    override fun expireTurn(
        roomCode: String,
        deadlineEpochMillis: Long,
    ) = engine.expireTurn(roomCode, deadlineEpochMillis)

    companion object {
        private val random = SecureRandom()
        private const val ROOM_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        private fun generateRoomCode(): String =
            buildString(4) {
                repeat(4) { append(ROOM_CODE_ALPHABET[random.nextInt(ROOM_CODE_ALPHABET.length)]) }
            }
    }
}
