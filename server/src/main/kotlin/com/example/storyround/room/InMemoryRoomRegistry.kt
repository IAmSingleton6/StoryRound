package com.example.storyround.room

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.identity.RandomNumberGenerator
import com.example.storyround.time.TimeProvider

internal class InMemoryRoomRegistry(
    roomStore: RoomStore,
    roomIdentityGenerator: RoomIdentityGenerator,
    timeProvider: TimeProvider,
    turnRules: TurnRules,
    roomStateMapper: RoomStateMapper,
    gameConfiguration: GameConfiguration,
    randomNumberGenerator: RandomNumberGenerator,
) : RoomRegistry {
    private val engine: GameRoomEngine =
        DefaultGameRoomEngine(
            roomStore = roomStore,
            roomCodeGenerator = roomIdentityGenerator::generateRoomCode,
            playerIdGenerator = roomIdentityGenerator::generatePlayerId,
            timeProvider = timeProvider,
            turnRules = turnRules,
            sessionTokenGenerator = roomIdentityGenerator::generateSessionToken,
            roomStateMapper = roomStateMapper,
            gameConfiguration = gameConfiguration,
            randomNumberGenerator = randomNumberGenerator,
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
        openingPlayerId: String?,
        maxRounds: Int,
    ) =
        engine.startGame(roomCode, playerId, openingPlayerId, maxRounds)

    override fun leaveRoom(roomCode: String, playerId: String) = engine.leaveRoom(roomCode, playerId)

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

}
