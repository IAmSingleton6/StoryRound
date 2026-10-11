package com.example.storyround

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.persistence.StoryRepository
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.DefaultUserTextSanitizer
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.StorySummary
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.room.InMemoryRoomRegistry
import com.example.storyround.room.InMemoryRoomStore
import com.example.storyround.room.RoomIdentityGenerator
import com.example.storyround.room.RoomStateMapperImpl
import com.example.storyround.room.TurnRules
import com.example.storyround.time.SystemTimeProvider
import com.example.storyround.time.TimeProvider
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.server.testing.testApplication
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GameWebSocketTest {
    private val json = Json { classDiscriminator = "type" }

    @Test
    fun `given two clients when joining a room then both receive synchronized lobby state`() =
        testApplication {
            application {
                module(newRegistry())
            }
            val client = createClient { install(WebSockets) }
            val host = client.webSocketSession("/game")
            host.sendMessage(ClientMessage.CreateRoom("Alex"))
            val hostSession = assertIs<ServerMessage.SessionEstablished>(host.receiveMessage())
            val created = assertIs<ServerMessage.RoomStateChanged>(host.receiveMessage())
            val hostPlayer = assertIs<ServerMessage.PlayerJoined>(host.receiveMessage()).player

            val guest = client.webSocketSession("/game")
            guest.sendMessage(ClientMessage.JoinRoom("AB12", "Sam"))
            assertIs<ServerMessage.SessionEstablished>(guest.receiveMessage())
            val guestLobby = assertIs<ServerMessage.RoomStateChanged>(guest.receiveMessage())
            guest.receiveMessage()
            val hostLobby = assertIs<ServerMessage.RoomStateChanged>(host.receiveMessage())

            assertEquals(created.room.roomCode, guestLobby.room.roomCode)
            assertEquals(listOf("Alex", "Sam"), guestLobby.room.players.map { it.name })
            assertEquals(guestLobby.room, hostLobby.room)
            assertEquals("player-1", hostPlayer.id)
            assertEquals(hostPlayer.id, hostSession.playerId)
        }

    @Test
    fun `given a started game when non current player contributes then server sends an error`() =
        testApplication {
            application {
                module(newRegistry())
            }
            val client = createClient { install(WebSockets) }
            val host = client.webSocketSession("/game")
            host.sendMessage(ClientMessage.CreateRoom("Alex"))
            host.receiveMessage()
            host.receiveMessage()
            val hostPlayer = assertIs<ServerMessage.PlayerJoined>(host.receiveMessage()).player

            val guest = client.webSocketSession("/game")
            guest.sendMessage(ClientMessage.JoinRoom("AB12", "Sam"))
            guest.receiveMessage()
            guest.receiveMessage()
            val guestPlayer = assertIs<ServerMessage.PlayerJoined>(guest.receiveMessage()).player
            host.receiveMessage()
            host.receiveMessage()

            host.sendMessage(ClientMessage.StartGame("AB12", hostPlayer.id, hostPlayer.id, maxRounds = 3))
            host.receiveMessage()
            guest.receiveMessage()
            host.sendMessage(ClientMessage.SubmitPrompt("AB12", hostPlayer.id, "A door opened."))
            host.receiveMessage()
            host.receiveMessage()
            guest.receiveMessage()
            guest.receiveMessage()

            guest.sendMessage(ClientMessage.SubmitContribution("AB12", guestPlayer.id, "Trying to skip ahead."))

            val error = assertIs<ServerMessage.Error>(guest.receiveMessage())
            assertEquals("NOT_CURRENT_PLAYER", error.code)
        }

    @Test
    fun `given a saved session when reconnecting then original room state is restored`() =
        testApplication {
            application {
                module(newRegistry())
            }
            val client = createClient { install(WebSockets) }
            val original = client.webSocketSession("/game")
            original.sendMessage(ClientMessage.CreateRoom("Alex"))
            val session = assertIs<ServerMessage.SessionEstablished>(original.receiveMessage())
            original.receiveMessage()
            original.receiveMessage()

            val reopened = client.webSocketSession("/game")
            reopened.sendMessage(ClientMessage.Reconnect("AB12", session.playerId, session.sessionToken))

            val restoredSession = assertIs<ServerMessage.SessionEstablished>(reopened.receiveMessage())
            val restoredRoom = assertIs<ServerMessage.RoomStateChanged>(reopened.receiveMessage())
            assertEquals(session, restoredSession)
            assertEquals(listOf(session.playerId), restoredRoom.room.players.map { it.id })
        }

    @Test
    fun `given a final contribution when the game finishes then the completed story is persisted`() =
        testApplication {
            val repository = RecordingStoryRepository()
            val timeProvider = FixedTimeProvider(42L)
            application {
                module(
                    timeProvider = timeProvider,
                    registry = newRegistry(rounds = 1, timeProvider = timeProvider),
                    storyRepository = repository,
                )
            }
            val client = createClient { install(WebSockets) }
            val host = client.webSocketSession("/game")

            host.sendMessage(ClientMessage.CreateRoom("Alex"))
            val session = assertIs<ServerMessage.SessionEstablished>(host.receiveMessage())
            host.receiveMessage()
            host.receiveMessage()
            host.sendMessage(ClientMessage.StartGame("AB12", session.playerId, session.playerId, maxRounds = 1))
            host.receiveMessage()
            host.sendMessage(ClientMessage.SubmitPrompt("AB12", session.playerId, "A door opened."))
            host.receiveMessage()
            host.receiveMessage()
            host.sendMessage(ClientMessage.SubmitContribution("AB12", session.playerId, "The story ended."))
            host.receiveMessage()
            host.receiveMessage()
            val finished = assertIs<ServerMessage.GameFinished>(host.receiveMessage())

            val savedRoom = assertNotNull(repository.savedRoom)
            assertEquals("story-1", finished.storyId)
            assertEquals("A door opened.", savedRoom.prompt)
            assertEquals(listOf("The story ended."), savedRoom.contributions.map { it.text })
            assertEquals(42L, repository.createdAtEpochMillis)

            val reopened = client.webSocketSession("/game")
            reopened.sendMessage(ClientMessage.Reconnect("AB12", session.playerId, session.sessionToken))
            assertIs<ServerMessage.SessionEstablished>(reopened.receiveMessage())
            assertIs<ServerMessage.RoomStateChanged>(reopened.receiveMessage())
            val reconnectedFinished = assertIs<ServerMessage.GameFinished>(reopened.receiveMessage())
            assertEquals("story-1", reconnectedFinished.storyId)
        }

    @Test
    fun `given an active turn expires when clients are connected then expiry and next turn are broadcast to everyone`() =
        testApplication {
            application {
                module(newRegistry(firstTurnMillis = 150))
            }
            val client = createClient { install(WebSockets) }
            val host = client.webSocketSession("/game")
            host.sendMessage(ClientMessage.CreateRoom("Alex"))
            val hostSession = assertIs<ServerMessage.SessionEstablished>(host.receiveMessage())
            host.receiveMessage()
            host.receiveMessage()

            val guest = client.webSocketSession("/game")
            guest.sendMessage(ClientMessage.JoinRoom("AB12", "Sam"))
            guest.receiveMessage()
            guest.receiveMessage()
            guest.receiveMessage()
            host.receiveMessage()
            host.receiveMessage()

            host.sendMessage(ClientMessage.StartGame("AB12", hostSession.playerId, hostSession.playerId, maxRounds = 3))
            host.receiveMessage()
            guest.receiveMessage()
            host.sendMessage(ClientMessage.SubmitPrompt("AB12", hostSession.playerId, "A door opened."))
            host.receiveMessage()
            assertIs<ServerMessage.TurnStarted>(host.receiveMessage())
            guest.receiveMessage()
            assertIs<ServerMessage.TurnStarted>(guest.receiveMessage())

            assertExpiryBroadcast(host, expectedExpiredPlayerId = hostSession.playerId)
            assertExpiryBroadcast(guest, expectedExpiredPlayerId = hostSession.playerId)
        }

    @Test
    fun `given story persistence fails after game completion when final contribution is submitted then clients are notified`() =
        testApplication {
            application {
                module(newRegistry(rounds = 1), FailingStoryRepository())
            }
            val client = createClient { install(WebSockets) }
            val host = client.webSocketSession("/game")

            host.sendMessage(ClientMessage.CreateRoom("Alex"))
            val session = assertIs<ServerMessage.SessionEstablished>(host.receiveMessage())
            host.receiveMessage()
            host.receiveMessage()
            host.sendMessage(ClientMessage.StartGame("AB12", session.playerId, session.playerId, maxRounds = 1))
            host.receiveMessage()
            host.sendMessage(ClientMessage.SubmitPrompt("AB12", session.playerId, "A door opened."))
            host.receiveMessage()
            host.receiveMessage()
            host.sendMessage(ClientMessage.SubmitContribution("AB12", session.playerId, "The story ended."))

            host.receiveMessage()
            host.receiveMessage()
            val finished = assertIs<ServerMessage.GameFinished>(host.receiveMessage())
            val error = assertIs<ServerMessage.Error>(host.receiveMessage())

            assertEquals(null, finished.storyId)
            assertEquals("STORY_SAVE_FAILED", error.code)
        }

    private suspend fun DefaultWebSocketSession.sendMessage(message: ClientMessage) {
        send(Frame.Text(json.encodeToString(ClientMessage.serializer(), message)))
    }

    private suspend fun DefaultWebSocketSession.receiveMessage(): ServerMessage =
        withTimeout(2_000) {
            while (true) {
                val frame = incoming.receive()
                if (frame is Frame.Text) return@withTimeout json.decodeFromString<ServerMessage>(frame.readText())
            }
            error("A WebSocket session closed before sending a text message.")
        }

    private suspend fun assertExpiryBroadcast(
        session: DefaultWebSocketSession,
        expectedExpiredPlayerId: String,
    ) {
        assertIs<ServerMessage.RoomStateChanged>(session.receiveMessage())
        val expired = assertIs<ServerMessage.TurnExpired>(session.receiveMessage())
        assertEquals(expectedExpiredPlayerId, expired.playerId)
        assertIs<ServerMessage.TurnStarted>(session.receiveMessage())
    }

    private fun newRegistry(
        rounds: Int = 3,
        firstTurnMillis: Long = 15_000L,
        timeProvider: TimeProvider = SystemTimeProvider(),
    ): InMemoryRoomRegistry {
        var nextPlayerId = 1
        val identityGenerator = mockk<RoomIdentityGenerator>()
        every { identityGenerator.generateRoomCode() } returns "AB12"
        every { identityGenerator.generatePlayerId() } answers { "player-${nextPlayerId++}" }
        every { identityGenerator.generateSessionToken() } returns "session-token"
        val randomNumberGenerator = mockk<RandomNumberGenerator>()
        every { randomNumberGenerator.nextInt(any()) } returns 0
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

    private class RecordingStoryRepository : StoryRepository {
        var savedRoom: RoomState? = null
        var createdAtEpochMillis: Long? = null

        override fun save(
            room: RoomState,
            createdAtEpochMillis: Long,
        ): SavedStory {
            savedRoom = room
            this.createdAtEpochMillis = createdAtEpochMillis
            return SavedStory(
                id = "story-1",
                prompt = requireNotNull(room.prompt),
                promptPlayerId = requireNotNull(room.openingPlayerId),
                createdAtEpochMillis = createdAtEpochMillis,
                players = room.players,
                contributions = room.contributions,
            )
        }

        override fun find(id: String): SavedStory? = null

        override fun list(): List<StorySummary> = emptyList()
    }

    private class FailingStoryRepository : StoryRepository {
        override fun save(
            room: RoomState,
            createdAtEpochMillis: Long,
        ): SavedStory = error("Database connection lost")

        override fun find(id: String): SavedStory? = null

        override fun list(): List<StorySummary> = emptyList()
    }

    private class FixedTimeProvider(
        private val nowEpochMillis: Long,
    ) : TimeProvider {
        override fun nowEpochMillis(): Long = nowEpochMillis
    }
}
