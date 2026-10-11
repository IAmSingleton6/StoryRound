package com.example.storyround.room

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.protocol.DefaultUserTextSanitizer
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.MAX_PLAYER_NAME_LENGTH
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.time.TimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class InMemoryRoomRegistryTest {
    private var nextPlayerId = 0
    private val registry = newRegistry()

    @Test
    fun `given a host when room is created then host receives blue and lobby state`() {
        val success = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        assertEquals("AB12", success.room.roomCode)
        assertEquals(GameStatus.Lobby, success.room.status)
        assertEquals("Alex", success.affectedPlayer?.name)
    }

    @Test
    fun `given a player name longer than the limit when creating a room then it is rejected`() {
        val result = registry.createRoom("A".repeat(MAX_PLAYER_NAME_LENGTH + 1))

        assertEquals(RegistryResult.Error("INVALID_NAME", "Enter a player name of 2 to 20 characters."), result)
    }

    @Test
    fun `given a player name shorter than the limit after sanitising when joining then it is rejected`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))

        val result = registry.joinRoom(created.room.roomCode, " S\u200B ")

        assertEquals(RegistryResult.Error("INVALID_NAME", "Enter a player name of 2 to 20 characters."), result)
    }

    @Test
    fun `given a player name with padding and control characters when creating a room then the sanitised name is used`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("  Mary\u0000 \n Jane  "))

        assertEquals("Mary Jane", created.affectedPlayer?.name)
    }

    @Test
    fun `given the only player leaves a lobby when reconnecting then the room is gone`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = requireNotNull(created.affectedPlayer).id

        val left = assertIs<RegistryResult.Success>(registry.leaveRoom(created.room.roomCode, hostId))

        assertEquals(emptyList(), left.room.players)
        assertEquals(
            RegistryResult.Error("ROOM_NOT_FOUND", "That room does not exist."),
            registry.reconnect(created.room.roomCode, hostId, requireNotNull(created.sessionToken)),
        )
    }

    @Test
    fun `given a generated code is already in use when creating a room then another code is generated`() {
        val collidingRegistry = newRegistry(roomCodes = listOf("AB12", "AB12", "CD34"))

        val first = assertIs<RegistryResult.Success>(collidingRegistry.createRoom("Alex"))
        val second = assertIs<RegistryResult.Success>(collidingRegistry.createRoom("Sam"))

        assertEquals("AB12", first.room.roomCode)
        assertEquals("CD34", second.room.roomCode)
        assertEquals(listOf("Alex"), first.room.players.map { it.name })
    }

    @Test
    fun `given no room with the code when joining then the player is told to check the code`() {
        assertEquals(
            RegistryResult.Error("ROOM_NOT_FOUND", "No game found with that code. Check it and try again."),
            registry.joinRoom("ZZZZ", "Sam"),
        )
    }

    @Test
    fun `given a room when player joins then next unique colour is assigned`() {
        registry.createRoom("Alex")
        val success = assertIs<RegistryResult.Success>(registry.joinRoom("ab12", "Sam"))
        assertEquals(2, success.room.players.size)
        assertEquals(PlayerColour.Green, success.affectedPlayer?.colour)
    }

    @Test
    fun `given a non host when starting then an error is returned`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))
        assertEquals(
            RegistryResult.Error("NOT_HOST", "Only the host can start the game."),
            registry.startGame(created.room.roomCode, requireNotNull(joined.affectedPlayer).id, null, 3),
        )
    }

    @Test
    fun `given host prompt when submitted then first timed turn starts for host`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        registry.startGame(created.room.roomCode, requireNotNull(created.affectedPlayer).id, requireNotNull(created.affectedPlayer).id, 3)

        val result =
            assertIs<RegistryResult.Success>(
                registry.submitPrompt(created.room.roomCode, requireNotNull(created.affectedPlayer).id, "A door opened."),
            )

        assertEquals(GameStatus.Playing, result.room.status)
        assertEquals("A door opened.", result.room.prompt)
        assertEquals(created.affectedPlayer.id, result.room.currentPlayerId)
        assertIs<ServerMessage.TurnStarted>(result.events.single())
    }

    @Test
    fun `given host selects another opening writer when game starts then only that player can submit the prompt`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))
        val hostId = requireNotNull(created.affectedPlayer).id
        val samId = requireNotNull(joined.affectedPlayer).id

        val started = assertIs<RegistryResult.Success>(registry.startGame(created.room.roomCode, hostId, samId, 2))

        assertEquals(samId, started.room.openingPlayerId)
        assertEquals(
            RegistryResult.Error("NOT_PROMPT_OWNER", "Only the selected player can start the story."),
            registry.submitPrompt(created.room.roomCode, hostId, "A door opened."),
        )
        assertEquals(
            GameStatus.Playing,
            assertIs<RegistryResult.Success>(registry.submitPrompt(created.room.roomCode, samId, "A door opened.")).room.status,
        )
    }

    @Test
    fun `given random opening writer selection when game starts then injected random generator chooses the writer`() {
        val randomRegistry = newRegistry(randomPlayerIndex = 1)
        val created = assertIs<RegistryResult.Success>(randomRegistry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(randomRegistry.joinRoom(created.room.roomCode, "Sam"))

        val started =
            assertIs<RegistryResult.Success>(
                randomRegistry.startGame(
                    created.room.roomCode,
                    requireNotNull(created.affectedPlayer).id,
                    openingPlayerId = null,
                    maxRounds = 2,
                ),
            )

        assertEquals(requireNotNull(joined.affectedPlayer).id, started.room.openingPlayerId)
    }

    @Test
    fun `given two configured rounds for one player when contributions are submitted then game ends after two turns`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = requireNotNull(created.affectedPlayer).id
        registry.startGame(created.room.roomCode, hostId, hostId, 2)
        registry.submitPrompt(created.room.roomCode, hostId, "A door opened.")

        val afterFirstContribution = assertIs<RegistryResult.Success>(registry.submitContribution(created.room.roomCode, hostId, "First."))
        val afterSecondContribution =
            assertIs<RegistryResult.Success>(registry.submitContribution(created.room.roomCode, hostId, "Second."))

        assertEquals(GameStatus.Playing, afterFirstContribution.room.status)
        assertEquals(GameStatus.Finished, afterSecondContribution.room.status)
        assertEquals(2, afterSecondContribution.room.contributions.size)
    }

    @Test
    fun `given a prompt with line breaks and invisible characters when submitted then the sanitised prompt is stored`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = requireNotNull(created.affectedPlayer).id
        registry.startGame(created.room.roomCode, hostId, hostId, 3)

        val result = assertIs<RegistryResult.Success>(registry.submitPrompt(created.room.roomCode, hostId, "A door\n\n\u202Eopened.  "))

        assertEquals("A door opened.", result.room.prompt)
    }

    @Test
    fun `given a contribution that is blank after sanitising or over the limit when submitted then it is rejected`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = requireNotNull(created.affectedPlayer).id
        registry.startGame(created.room.roomCode, hostId, hostId, 3)
        registry.submitPrompt(created.room.roomCode, hostId, "A door opened.")
        val expected = RegistryResult.Error("INVALID_TEXT", "Enter up to 280 characters.")

        assertEquals(expected, registry.submitContribution(created.room.roomCode, hostId, " \u200B\n "))
        assertEquals(expected, registry.submitContribution(created.room.roomCode, hostId, "A".repeat(MAX_STORY_TEXT_LENGTH + 1)))
    }

    @Test
    fun `given another player turn when host submits then contribution is rejected`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))
        registry.startGame(created.room.roomCode, requireNotNull(created.affectedPlayer).id, requireNotNull(created.affectedPlayer).id, 3)
        registry.submitPrompt(created.room.roomCode, requireNotNull(created.affectedPlayer).id, "A door opened.")
        registry.submitContribution(created.room.roomCode, requireNotNull(created.affectedPlayer).id, "Alex continues.")

        val result = registry.submitContribution(created.room.roomCode, requireNotNull(created.affectedPlayer).id, "Not Alex's turn.")

        assertEquals(RegistryResult.Error("NOT_CURRENT_PLAYER", "It is not your turn."), result)
        assertEquals(PlayerColour.Green, joined.affectedPlayer?.colour)
    }

    @Test
    fun `given an expired turn when registry advances then contribution is omitted`() {
        val timeProvider = MutableTimeProvider()
        val timedRegistry = newRegistry("TIME", { "host" }, timeProvider, rounds = 1, firstTurnMillis = 10L)
        val created = assertIs<RegistryResult.Success>(timedRegistry.createRoom("Alex"))
        timedRegistry.startGame("TIME", "host", "host", 1)
        val prompt = assertIs<RegistryResult.Success>(timedRegistry.submitPrompt("TIME", "host", "Start."))
        timeProvider.nowEpochMillis = requireNotNull(prompt.room.turnDeadlineEpochMillis)

        assertEquals(
            RegistryResult.Error("TURN_EXPIRED", "That turn has already ended."),
            timedRegistry.submitContribution("TIME", "host", "Too late."),
        )

        val expired = assertIs<RegistryResult.Success>(timedRegistry.expireTurn("TIME", timeProvider.nowEpochMillis))

        assertEquals(GameStatus.Finished, expired.room.status)
        assertEquals(emptyList(), expired.room.contributions)
        assertIs<ServerMessage.TurnExpired>(expired.events.first())
        assertIs<ServerMessage.GameFinished>(expired.events.last())
        assertNull(expired.room.currentPlayerId)
    }

    @Test
    fun `given a single player final contribution when submitted then game finishes`() {
        val oneTurnRegistry = newRegistry("DONE", { "host" }, rounds = 1)
        val created = assertIs<RegistryResult.Success>(oneTurnRegistry.createRoom("Alex"))
        oneTurnRegistry.startGame("DONE", "host", "host", 1)
        oneTurnRegistry.submitPrompt("DONE", "host", "Start.")

        val finished = assertIs<RegistryResult.Success>(oneTurnRegistry.submitContribution("DONE", "host", "The end."))

        assertEquals(GameStatus.Finished, finished.room.status)
        assertEquals(listOf("The end."), finished.room.contributions.map { it.text })
        assertIs<ServerMessage.ContributionAdded>(finished.events.first())
        assertIs<ServerMessage.GameFinished>(finished.events.last())
    }

    @Test
    fun `given a finished game when host plays again then the same players return to an empty lobby`() {
        val oneTurnRegistry = newRegistry("AGAIN", { "host" }, rounds = 1)
        oneTurnRegistry.createRoom("Alex")
        oneTurnRegistry.startGame("AGAIN", "host", "host", 1)
        oneTurnRegistry.submitPrompt("AGAIN", "host", "Start.")
        oneTurnRegistry.submitContribution("AGAIN", "host", "The end.")

        val replay = assertIs<RegistryResult.Success>(oneTurnRegistry.playAgain("AGAIN", "host"))

        assertEquals(GameStatus.Lobby, replay.room.status)
        assertEquals(listOf("Alex"), replay.room.players.map { it.name })
        assertNull(replay.room.prompt)
        assertEquals(emptyList(), replay.room.contributions)
        assertNull(replay.room.currentPlayerId)
    }

    private class MutableTimeProvider : TimeProvider {
        var nowEpochMillis: Long = 0L

        override fun nowEpochMillis(): Long = nowEpochMillis
    }

    private fun newRegistry(
        roomCode: String = "AB12",
        playerIdGenerator: () -> String = { "player-${++nextPlayerId}" },
        timeProvider: TimeProvider = MutableTimeProvider(),
        rounds: Int = 3,
        firstTurnMillis: Long = GameConfiguration().firstTurnMillis,
        randomPlayerIndex: Int = 0,
        roomCodes: List<String> = listOf(roomCode),
    ): InMemoryRoomRegistry {
        val identityGenerator = mockk<RoomIdentityGenerator>()
        every { identityGenerator.generateRoomCode() } returnsMany roomCodes
        every { identityGenerator.generatePlayerId() } answers { playerIdGenerator() }
        every { identityGenerator.generateSessionToken() } returns "session-token"
        val randomNumberGenerator = mockk<RandomNumberGenerator>()
        every { randomNumberGenerator.nextInt(any()) } returns randomPlayerIndex
        val gameConfiguration =
            GameConfiguration(
                defaultRounds = rounds,
                firstTurnMillis = firstTurnMillis,
                minimumTurnMillis = firstTurnMillis,
            )
        return InMemoryRoomRegistry(
            roomStore = InMemoryRoomStore(),
            roomIdentityGenerator = identityGenerator,
            timeProvider = timeProvider,
            turnRules = TurnRules(gameConfiguration),
            roomStateMapper = RoomStateMapperImpl(),
            gameConfiguration = gameConfiguration,
            randomNumberGenerator = randomNumberGenerator,
            userTextSanitizer = DefaultUserTextSanitizer(),
        )
    }
}
