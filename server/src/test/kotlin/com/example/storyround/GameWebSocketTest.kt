package com.example.storyround

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.UserTextSanitizerImpl
import com.example.storyround.protocol.UserTextValidatorImpl
import com.example.storyround.protocol.protocolJson
import com.example.storyround.random.RandomNumberGenerator
import com.example.storyround.room.InMemoryRoomStore
import com.example.storyround.room.RoomIdentityGenerator
import com.example.storyround.room.RoomRegistryImpl
import com.example.storyround.room.RoomStateMapperImpl
import com.example.storyround.rules.TurnRulesImpl
import com.example.storyround.time.SystemTimeProvider
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GameWebSocketTest {
    private val json = protocolJson()

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

            host.sendMessage(ClientMessage.StartGame("AB12", hostPlayer.id, hostPlayer.id, settings = RoomSettings(rounds = 3)))
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
    fun `given a one word game when the opening writer sends several words then the server sends an error`() =
        testApplication {
            application {
                module(newRegistry())
            }
            val client = createClient { install(WebSockets) }
            val host = client.webSocketSession("/game")
            host.sendMessage(ClientMessage.CreateRoom("Alex"))
            val session = assertIs<ServerMessage.SessionEstablished>(host.receiveMessage())
            host.receiveMessage()
            host.receiveMessage()
            val settings = RoomSettings(rounds = 1, oneWordPerTurn = true)
            host.sendMessage(ClientMessage.StartGame("AB12", session.playerId, session.playerId, settings))
            val started = assertIs<ServerMessage.RoomStateChanged>(host.receiveMessage())

            host.sendMessage(ClientMessage.SubmitPrompt("AB12", session.playerId, "A door opened."))

            val error = assertIs<ServerMessage.Error>(host.receiveMessage())
            assertEquals(settings, started.room.settings)
            assertEquals("INVALID_WORD", error.code)
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
    fun `given a finished game when a player reconnects then the completed story is sent again`() =
        testApplication {
            application {
                module(newRegistry(rounds = 1))
            }
            val client = createClient { install(WebSockets) }
            val host = client.webSocketSession("/game")

            host.sendMessage(ClientMessage.CreateRoom("Alex"))
            val session = assertIs<ServerMessage.SessionEstablished>(host.receiveMessage())
            host.receiveMessage()
            host.receiveMessage()
            host.sendMessage(ClientMessage.StartGame("AB12", session.playerId, session.playerId, settings = RoomSettings(rounds = 1)))
            host.receiveMessage()
            host.sendMessage(ClientMessage.SubmitPrompt("AB12", session.playerId, "A door opened."))
            host.receiveMessage()
            host.receiveMessage()
            host.sendMessage(ClientMessage.SubmitContribution("AB12", session.playerId, "The story ended."))
            host.receiveMessage()
            host.receiveMessage()
            val finished = assertIs<ServerMessage.GameFinished>(host.receiveMessage())

            val reopened = client.webSocketSession("/game")
            reopened.sendMessage(ClientMessage.Reconnect("AB12", session.playerId, session.sessionToken))
            assertIs<ServerMessage.SessionEstablished>(reopened.receiveMessage())
            assertIs<ServerMessage.RoomStateChanged>(reopened.receiveMessage())
            val reconnectedFinished = assertIs<ServerMessage.GameFinished>(reopened.receiveMessage())
            assertEquals("A door opened.", finished.room.prompt)
            assertEquals(listOf("The story ended."), finished.room.contributions.map { it.text })
            assertEquals(finished.room, reconnectedFinished.room)
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

            host.sendMessage(
                ClientMessage.StartGame("AB12", hostSession.playerId, hostSession.playerId, settings = RoomSettings(rounds = 3)),
            )
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
    ): RoomRegistryImpl {
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
        return RoomRegistryImpl(
            roomStore = InMemoryRoomStore(),
            identityGenerator = identityGenerator,
            timeProvider = SystemTimeProvider(),
            turnRules = TurnRulesImpl(gameConfiguration),
            roomStateMapper = RoomStateMapperImpl(),
            gameConfiguration = gameConfiguration,
            randomNumberGenerator = randomNumberGenerator,
            userTextValidator = UserTextValidatorImpl(UserTextSanitizerImpl()),
        )
    }
}
