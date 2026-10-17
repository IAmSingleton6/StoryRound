package com.example.storyround.room

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.MAX_PLAYER_NAME_LENGTH
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.MAX_WORD_LENGTH
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.UserTextSanitizerImpl
import com.example.storyround.protocol.UserTextValidatorImpl
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.rules.TurnRulesImpl
import com.example.storyround.time.TimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RoomRegistryImplTest {
    private var nextPlayerId = 0
    private val registry = newRegistry()

    @Test
    fun `given a host when room is created then host receives blue and lobby state`() {
        // When
        val success = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))

        // Then
        assertEquals("AB12", success.room.roomCode)
        assertEquals(GameStatus.Lobby, success.room.status)
        assertEquals("Alex", success.host.name)
        assertEquals(RoomEvent.RoomCreated(success.host, "session-token"), success.event)
    }

    @Test
    fun `given a player name longer than the limit when creating a room then it is rejected`() {
        // When
        val result = registry.createRoom("A".repeat(MAX_PLAYER_NAME_LENGTH + 1))

        // Then
        assertEquals(RegistryResult.Error("INVALID_NAME", "Enter a player name of 2 to 20 characters."), result)
    }

    @Test
    fun `given a player name shorter than the limit after sanitising when joining then it is rejected`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))

        // When
        val result = registry.joinRoom(created.room.roomCode, " S\u200B ")

        // Then
        assertEquals(RegistryResult.Error("INVALID_NAME", "Enter a player name of 2 to 20 characters."), result)
    }

    @Test
    fun `given a player name with padding and control characters when creating a room then the sanitised name is used`() {
        // When
        val created = assertIs<RegistryResult.Success>(registry.createRoom("  Mary\u0000 \n Jane  "))

        // Then
        assertEquals("Mary Jane", created.host.name)
    }

    @Test
    fun `given the only player leaves a lobby when they leave then the room is closed and gone`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = created.host.id

        // When
        val left = assertIs<RegistryResult.Success>(registry.leaveRoom(created.room.roomCode, hostId))

        // Then
        assertEquals(RoomEvent.RoomClosed, left.event)
        assertEquals(emptyList(), left.room.players)
        assertEquals(
            RegistryResult.Error("ROOM_NOT_FOUND", "That room does not exist."),
            registry.reconnect(created.room.roomCode, hostId, "session-token"),
        )
    }

    @Test
    fun `given a generated code is already in use when creating a room then another code is generated`() {
        // Given
        val collidingRegistry = newRegistry(roomCodes = listOf("AB12", "AB12", "CD34"))

        // When
        val first = assertIs<RegistryResult.Success>(collidingRegistry.createRoom("Alex"))
        val second = assertIs<RegistryResult.Success>(collidingRegistry.createRoom("Sam"))

        // Then
        assertEquals("AB12", first.room.roomCode)
        assertEquals("CD34", second.room.roomCode)
        assertEquals(listOf("Alex"), first.room.players.map { it.name })
    }

    @Test
    fun `given no room with the code when joining then the player is told to check the code`() {
        // When
        val result = registry.joinRoom("ZZZZ", "Sam")

        // Then
        assertEquals(RegistryResult.Error("ROOM_NOT_FOUND", "No game found with that code. Check it and try again."), result)
    }

    @Test
    fun `given a room when player joins then next unique colour is assigned`() {
        // Given
        registry.createRoom("Alex")

        // When
        val success = assertIs<RegistryResult.Success>(registry.joinRoom("AB12", "Sam"))

        // Then
        assertEquals(2, success.room.players.size)
        assertEquals(PlayerColour.Green, success.joinedPlayer.colour)
        assertEquals(RoomEvent.PlayerJoined(success.joinedPlayer, "session-token"), success.event)
    }

    @Test
    fun `given a player left the lobby when someone else joins then they get the free colour instead of a duplicate`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val sam = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))
        registry.joinRoom(created.room.roomCode, "Jo")
        registry.leaveRoom(created.room.roomCode, sam.joinedPlayer.id)

        // When
        val kim = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Kim"))

        // Then
        assertEquals(PlayerColour.Green, kim.joinedPlayer.colour)
        assertEquals(
            kim.room.players.size,
            kim.room.players
                .map { it.colour }
                .toSet()
                .size,
        )
    }

    @Test
    fun `given the host leaves a lobby with other players when they leave then the next player becomes host`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val sam = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))

        // When
        val left = assertIs<RegistryResult.Success>(registry.leaveRoom(created.room.roomCode, created.host.id))

        // Then
        assertEquals(sam.joinedPlayer.id, left.room.hostId)
        assertEquals(RoomEvent.PlayerLeft(created.host.id), left.event)
    }

    @Test
    fun `given a full room when another player joins then the join is refused`() {
        // Given
        val fullRegistry = newRegistry(maxPlayers = 1)
        val created = assertIs<RegistryResult.Success>(fullRegistry.createRoom("Alex"))

        // When
        val result = fullRegistry.joinRoom(created.room.roomCode, "Sam")

        // Then
        assertEquals(RegistryResult.Error("ROOM_FULL", "This room already has 1 players."), result)
    }

    @Test
    fun `given a non host when starting then an error is returned`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))

        // When
        val result = registry.startGame(created.room.roomCode, joined.joinedPlayer.id, null, RoomSettings(rounds = 3))

        // Then
        assertEquals(RegistryResult.Error("NOT_HOST", "Only the host can start the game."), result)
    }

    @Test
    fun `given host prompt when submitted then first timed turn starts for host`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        registry.startGame(created.room.roomCode, created.host.id, created.host.id, RoomSettings(rounds = 3))

        // When
        val result =
            assertIs<RegistryResult.Success>(
                registry.submitPrompt(created.room.roomCode, created.host.id, "A door opened."),
            )

        // Then
        assertEquals(GameStatus.Playing, result.room.status)
        assertEquals("A door opened.", result.room.prompt)
        assertEquals(created.host.id, result.room.currentPlayerId)
        assertEquals(
            RoomEvent.PromptSubmitted(TurnOutcome.NextTurn(created.host.id, requireNotNull(result.room.turnDeadlineEpochMillis))),
            result.event,
        )
    }

    @Test
    fun `given host selects another opening writer when game starts then only that player can submit the prompt`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))
        val hostId = created.host.id
        val samId = joined.joinedPlayer.id

        // When
        val started = assertIs<RegistryResult.Success>(registry.startGame(created.room.roomCode, hostId, samId, RoomSettings(rounds = 2)))

        // Then
        assertEquals(RoomEvent.GameStarted, started.event)
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
        // Given
        val randomRegistry = newRegistry(randomPlayerIndex = 1)
        val created = assertIs<RegistryResult.Success>(randomRegistry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(randomRegistry.joinRoom(created.room.roomCode, "Sam"))

        // When
        val started =
            assertIs<RegistryResult.Success>(
                randomRegistry.startGame(
                    created.room.roomCode,
                    created.host.id,
                    openingPlayerId = null,
                    settings = RoomSettings(rounds = 2),
                ),
            )

        // Then
        assertEquals(joined.joinedPlayer.id, started.room.openingPlayerId)
    }

    @Test
    fun `given two configured rounds for one player when contributions are submitted then game ends after two turns`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = created.host.id
        registry.startGame(created.room.roomCode, hostId, hostId, RoomSettings(rounds = 2))
        registry.submitPrompt(created.room.roomCode, hostId, "A door opened.")

        // When
        val afterFirstContribution = assertIs<RegistryResult.Success>(registry.submitContribution(created.room.roomCode, hostId, "First."))
        val afterSecondContribution =
            assertIs<RegistryResult.Success>(registry.submitContribution(created.room.roomCode, hostId, "Second."))

        // Then
        assertEquals(GameStatus.Playing, afterFirstContribution.room.status)
        assertEquals(
            RoomEvent.ContributionAdded(
                Contribution(hostId, "First.", 0),
                TurnOutcome.NextTurn(hostId, requireNotNull(afterFirstContribution.room.turnDeadlineEpochMillis)),
            ),
            afterFirstContribution.event,
        )
        assertEquals(GameStatus.Finished, afterSecondContribution.room.status)
        assertEquals(2, afterSecondContribution.room.contributions.size)
    }

    @Test
    fun `given a prompt with line breaks and invisible characters when submitted then the sanitised prompt is stored`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = created.host.id
        registry.startGame(created.room.roomCode, hostId, hostId, RoomSettings(rounds = 3))

        // When
        val result = assertIs<RegistryResult.Success>(registry.submitPrompt(created.room.roomCode, hostId, "A door\n\n\u202Eopened.  "))

        // Then
        assertEquals("A door opened.", result.room.prompt)
    }

    @Test
    fun `given a contribution that is blank after sanitising or over the limit when submitted then it is rejected`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = created.host.id
        registry.startGame(created.room.roomCode, hostId, hostId, RoomSettings(rounds = 3))
        registry.submitPrompt(created.room.roomCode, hostId, "A door opened.")

        // When
        val blank = registry.submitContribution(created.room.roomCode, hostId, " \u200B\n ")
        val overLimit = registry.submitContribution(created.room.roomCode, hostId, "A".repeat(MAX_STORY_TEXT_LENGTH + 1))

        // Then
        val expected = RegistryResult.Error("INVALID_TEXT", "Enter up to 280 characters.")
        assertEquals(expected, blank)
        assertEquals(expected, overLimit)
    }

    @Test
    fun `given one word turns when the host starts the game then the room keeps the settings`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = created.host.id

        // When
        val started = registry.startGame(created.room.roomCode, hostId, hostId, RoomSettings(rounds = 2, oneWordPerTurn = true))

        // Then
        assertEquals(RoomSettings(rounds = 2, oneWordPerTurn = true), assertIs<RegistryResult.Success>(started).room.settings)
    }

    @Test
    fun `given one word turns when the opening is several words then it is rejected and a single word starts the story`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = created.host.id
        registry.startGame(created.room.roomCode, hostId, hostId, RoomSettings(rounds = 3, oneWordPerTurn = true))

        // When
        val rejected = registry.submitPrompt(created.room.roomCode, hostId, "A door opened.")
        val accepted = registry.submitPrompt(created.room.roomCode, hostId, " Once ")

        // Then
        assertEquals(RegistryResult.Error("INVALID_WORD", "Enter one word of up to 30 characters, with no spaces."), rejected)
        assertEquals("Once", assertIs<RegistryResult.Success>(accepted).room.prompt)
    }

    @Test
    fun `given one word turns when a contribution is several words or too long then it is rejected and a single word is added`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val hostId = created.host.id
        registry.startGame(created.room.roomCode, hostId, hostId, RoomSettings(rounds = 3, oneWordPerTurn = true))
        registry.submitPrompt(created.room.roomCode, hostId, "Once")

        // When
        val severalWords = registry.submitContribution(created.room.roomCode, hostId, "upon a time")
        val tooLong = registry.submitContribution(created.room.roomCode, hostId, "a".repeat(MAX_WORD_LENGTH + 1))
        val singleWord = registry.submitContribution(created.room.roomCode, hostId, "upon")

        // Then
        val expected = RegistryResult.Error("INVALID_WORD", "Enter one word of up to 30 characters, with no spaces.")
        assertEquals(expected, severalWords)
        assertEquals(expected, tooLong)
        assertEquals(listOf("upon"), assertIs<RegistryResult.Success>(singleWord).room.contributions.map { it.text })
    }

    @Test
    fun `given another player turn when host submits then contribution is rejected`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))
        registry.startGame(created.room.roomCode, created.host.id, created.host.id, RoomSettings(rounds = 3))
        registry.submitPrompt(created.room.roomCode, created.host.id, "A door opened.")
        registry.submitContribution(created.room.roomCode, created.host.id, "Alex continues.")

        // When
        val result = registry.submitContribution(created.room.roomCode, created.host.id, "Not Alex's turn.")

        // Then
        assertEquals(RegistryResult.Error("NOT_CURRENT_PLAYER", "It is not your turn."), result)
        assertEquals(PlayerColour.Green, joined.joinedPlayer.colour)
    }

    @Test
    fun `given an expired turn when registry advances then contribution is omitted`() {
        // Given
        var now = 0L
        val timeProvider = mockk<TimeProvider> { every { nowEpochMillis() } answers { now } }
        val timedRegistry = newRegistry("TIME", { "host" }, timeProvider, rounds = 1, firstTurnMillis = 10L)
        val created = assertIs<RegistryResult.Success>(timedRegistry.createRoom("Alex"))
        timedRegistry.startGame("TIME", "host", "host", RoomSettings(rounds = 1))
        val prompt = assertIs<RegistryResult.Success>(timedRegistry.submitPrompt("TIME", "host", "Start."))
        now = requireNotNull(prompt.room.turnDeadlineEpochMillis)

        // When
        val lateContribution = timedRegistry.submitContribution("TIME", "host", "Too late.")
        val expired = assertIs<RegistryResult.Success>(timedRegistry.expireTurn("TIME", now))

        // Then
        assertEquals(RegistryResult.Error("TURN_EXPIRED", "That turn has already ended."), lateContribution)
        assertEquals(GameStatus.Finished, expired.room.status)
        assertEquals(emptyList(), expired.room.contributions)
        assertEquals(RoomEvent.TurnExpired("host", TurnOutcome.GameFinished), expired.event)
        assertNull(expired.room.currentPlayerId)
    }

    @Test
    fun `given a single player final contribution when submitted then game finishes`() {
        // Given
        val oneTurnRegistry = newRegistry("DONE", { "host" }, rounds = 1)
        val created = assertIs<RegistryResult.Success>(oneTurnRegistry.createRoom("Alex"))
        oneTurnRegistry.startGame("DONE", "host", "host", RoomSettings(rounds = 1))
        oneTurnRegistry.submitPrompt("DONE", "host", "Start.")

        // When
        val finished = assertIs<RegistryResult.Success>(oneTurnRegistry.submitContribution("DONE", "host", "The end."))

        // Then
        assertEquals(GameStatus.Finished, finished.room.status)
        assertEquals(listOf("The end."), finished.room.contributions.map { it.text })
        assertEquals(RoomEvent.ContributionAdded(Contribution("host", "The end.", 0), TurnOutcome.GameFinished), finished.event)
    }

    @Test
    fun `given a finished game when host plays again then the same players return to an empty lobby`() {
        // Given
        val oneTurnRegistry = newRegistry("AGAIN", { "host" }, rounds = 1)
        oneTurnRegistry.createRoom("Alex")
        oneTurnRegistry.startGame("AGAIN", "host", "host", RoomSettings(rounds = 1))
        oneTurnRegistry.submitPrompt("AGAIN", "host", "Start.")
        oneTurnRegistry.submitContribution("AGAIN", "host", "The end.")

        // When
        val replay = assertIs<RegistryResult.Success>(oneTurnRegistry.playAgain("AGAIN", "host"))

        // Then
        assertEquals(RoomEvent.GameReset, replay.event)
        assertEquals(GameStatus.Lobby, replay.room.status)
        assertEquals(listOf("Alex"), replay.room.players.map { it.name })
        assertNull(replay.room.prompt)
        assertEquals(emptyList(), replay.room.contributions)
        assertNull(replay.room.currentPlayerId)
    }

    @Test
    fun `given a saved session when the player reconnects then the reconnection is reported with their session`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))

        // When
        val reconnected = assertIs<RegistryResult.Success>(registry.reconnect("AB12", created.host.id, "session-token"))

        // Then
        assertEquals(RoomEvent.PlayerReconnected(created.host, "session-token"), reconnected.event)
    }

    @Test
    fun `given an open room when it is closed then it is gone and closing again does nothing`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))

        // When
        val closed = registry.closeRoom("AB12")

        // Then
        assertEquals(RoomEvent.RoomClosed, closed?.event)
        assertEquals(created.room.roomCode, closed?.room?.roomCode)
        assertEquals(
            RegistryResult.Error("ROOM_NOT_FOUND", "That room does not exist."),
            registry.reconnect(created.room.roomCode, created.host.id, "session-token"),
        )
        assertNull(registry.closeRoom("AB12"))
    }

    @Test
    fun `given a deadline that no longer applies when expiring then nothing happens`() {
        // Given
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        registry.startGame("AB12", created.host.id, created.host.id, RoomSettings(rounds = 3))
        val prompt = assertIs<RegistryResult.Success>(registry.submitPrompt("AB12", created.host.id, "Start."))

        // When
        val expired = registry.expireTurn("AB12", requireNotNull(prompt.room.turnDeadlineEpochMillis) + 1)

        // Then
        assertNull(expired)
    }

    private val RegistryResult.Success.host: Player
        get() = assertIs<RoomEvent.RoomCreated>(event).host

    private val RegistryResult.Success.joinedPlayer: Player
        get() = assertIs<RoomEvent.PlayerJoined>(event).player

    private fun newRegistry(
        roomCode: String = "AB12",
        playerIdGenerator: () -> String = { "player-${++nextPlayerId}" },
        timeProvider: TimeProvider = mockk { every { nowEpochMillis() } returns 0L },
        rounds: Int = 3,
        firstTurnMillis: Long = GameConfiguration().firstTurnMillis,
        randomPlayerIndex: Int = 0,
        roomCodes: List<String> = listOf(roomCode),
        maxPlayers: Int = GameConfiguration().maxPlayers,
    ): RoomRegistryImpl {
        val identityGenerator = mockk<RoomIdentityGenerator>()
        every { identityGenerator.generateRoomCode() } returnsMany roomCodes
        every { identityGenerator.generatePlayerId() } answers { playerIdGenerator() }
        every { identityGenerator.generateSessionToken() } returns "session-token"
        val randomNumberGenerator = mockk<RandomNumberGenerator>()
        every { randomNumberGenerator.nextInt(any()) } returns randomPlayerIndex
        val gameConfiguration =
            GameConfiguration(
                defaultRounds = rounds,
                maxPlayers = maxPlayers,
                firstTurnMillis = firstTurnMillis,
                minimumTurnMillis = firstTurnMillis,
            )
        return RoomRegistryImpl(
            roomStore = InMemoryRoomStore(),
            identityGenerator = identityGenerator,
            timeProvider = timeProvider,
            turnRules = TurnRulesImpl(gameConfiguration),
            roomStateMapper = RoomStateMapperImpl(),
            gameConfiguration = gameConfiguration,
            randomNumberGenerator = randomNumberGenerator,
            userTextValidator = UserTextValidatorImpl(UserTextSanitizerImpl()),
        )
    }
}
