package com.example.storyround.room

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.MAX_PLAYER_NAME_LENGTH
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.time.TimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class InMemoryRoomRegistryTest {
    private var nextPlayerId = 0
    private val registry =
        InMemoryRoomRegistry(
            roomCodeGenerator = { "AB12" },
            playerIdGenerator = { "player-${++nextPlayerId}" },
        )

    @Test
    fun `given a host when room is created then host receives blue and lobby state`() {
        val success = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        assertEquals("AB12", success.room.roomCode)
        assertEquals(GameStatus.Lobby, success.room.status)
        assertEquals("Alex", success.affectedPlayer?.name)
    }

    @Test
    fun `given a player name longer than the persistence limit when creating a room then it is rejected`() {
        val result = registry.createRoom("A".repeat(MAX_PLAYER_NAME_LENGTH + 1))

        assertEquals(
            RegistryResult.Error("INVALID_NAME", "Enter a player name up to 100 characters."),
            result,
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
            registry.startGame(created.room.roomCode, requireNotNull(joined.affectedPlayer).id),
        )
    }

    @Test
    fun `given host prompt when submitted then first timed turn starts for host`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        registry.startGame(created.room.roomCode, requireNotNull(created.affectedPlayer).id)

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
    fun `given another player turn when host submits then contribution is rejected`() {
        val created = assertIs<RegistryResult.Success>(registry.createRoom("Alex"))
        val joined = assertIs<RegistryResult.Success>(registry.joinRoom(created.room.roomCode, "Sam"))
        registry.startGame(created.room.roomCode, requireNotNull(created.affectedPlayer).id)
        registry.submitPrompt(created.room.roomCode, requireNotNull(created.affectedPlayer).id, "A door opened.")
        registry.submitContribution(created.room.roomCode, requireNotNull(created.affectedPlayer).id, "Alex continues.")

        val result = registry.submitContribution(created.room.roomCode, requireNotNull(created.affectedPlayer).id, "Not Alex's turn.")

        assertEquals(RegistryResult.Error("NOT_CURRENT_PLAYER", "It is not your turn."), result)
        assertEquals(PlayerColour.Green, joined.affectedPlayer?.colour)
    }

    @Test
    fun `given an expired turn when registry advances then contribution is omitted`() {
        val timeProvider = MutableTimeProvider()
        val timedRegistry =
            InMemoryRoomRegistry(
                roomCodeGenerator = { "TIME" },
                playerIdGenerator = { "host" },
                timeProvider = timeProvider,
                rounds = 1,
                firstTurnMillis = 10L,
                minimumTurnMillis = 10L,
            )
        val created = assertIs<RegistryResult.Success>(timedRegistry.createRoom("Alex"))
        timedRegistry.startGame("TIME", "host")
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
        val oneTurnRegistry = InMemoryRoomRegistry({ "DONE" }, { "host" }, rounds = 1)
        val created = assertIs<RegistryResult.Success>(oneTurnRegistry.createRoom("Alex"))
        oneTurnRegistry.startGame("DONE", "host")
        oneTurnRegistry.submitPrompt("DONE", "host", "Start.")

        val finished = assertIs<RegistryResult.Success>(oneTurnRegistry.submitContribution("DONE", "host", "The end."))

        assertEquals(GameStatus.Finished, finished.room.status)
        assertEquals(listOf("The end."), finished.room.contributions.map { it.text })
        assertIs<ServerMessage.ContributionAdded>(finished.events.first())
        assertIs<ServerMessage.GameFinished>(finished.events.last())
    }

    @Test
    fun `given a finished game when host plays again then the same players return to an empty lobby`() {
        val oneTurnRegistry = InMemoryRoomRegistry({ "AGAIN" }, { "host" }, rounds = 1)
        oneTurnRegistry.createRoom("Alex")
        oneTurnRegistry.startGame("AGAIN", "host")
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
}
