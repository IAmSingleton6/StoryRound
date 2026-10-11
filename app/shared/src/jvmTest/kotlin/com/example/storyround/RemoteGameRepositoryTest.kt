package com.example.storyround

import com.example.storyround.data.local.RemoteSession
import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.StoryHistoryStore
import com.example.storyround.data.remote.DefaultGameConnectionManager
import com.example.storyround.data.remote.GameSessionReducer
import com.example.storyround.data.remote.GameSocket
import com.example.storyround.data.remote.GameSocketConnection
import com.example.storyround.data.remote.GameSocketIncomingMessage
import com.example.storyround.data.remote.ReconnectPolicy
import com.example.storyround.data.remote.RemoteGameRepository
import com.example.storyround.data.remote.SERVER_UNREACHABLE_MESSAGE
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.ServerMessage
import io.ktor.client.HttpClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class RemoteGameRepositoryTest {
    @Test
    fun `given socket connection failure when connecting then state reports the failure`() =
        runBlocking {
            val socket = mockk<GameSocket>()
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } returns null
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val repository = repository(socket, sessionStore)

            repository.connect()

            assertFalse(repository.state.value.isConnecting)
            assertFalse(repository.state.value.isConnected)
            assertEquals(SERVER_UNREACHABLE_MESSAGE, repository.state.value.error)
            coVerify(exactly = 1) { socket.connect(SERVER_URL) }
            repository.close()
        }

    @Test
    fun `given the launch connection failed when creating a room then it connects again and creates without resuming the old session`() =
        runBlocking {
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } returns RemoteSession("OLD1", "player-1", "token-1")
            val connection = mockk<GameSocketConnection>()
            every { connection.incomingMessages } returns flow { awaitCancellation() }
            coEvery { connection.close() } returns Unit
            coEvery { connection.send(any<ClientMessage>()) } returns Unit
            val socket = mockk<GameSocket>()
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline") andThen connection
            val repository = repository(socket, sessionStore)
            repository.connect()

            repository.createRoom("Alex")

            coVerify(exactly = 2) { socket.connect(SERVER_URL) }
            coVerify(exactly = 1) { connection.send(ClientMessage.CreateRoom("Alex")) }
            coVerify(exactly = 0) { connection.send(ofType<ClientMessage.Reconnect>()) }
            assertNull(repository.state.value.error)
            repository.close()
        }

    @Test
    fun `given the server is still unreachable when joining a room then a player-facing error is shown`() =
        runBlocking {
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } returns null
            val socket = mockk<GameSocket>()
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val repository = repository(socket, sessionStore)

            repository.joinRoom("ABCD", "Sam")

            assertEquals(SERVER_UNREACHABLE_MESSAGE, repository.state.value.error)
            assertFalse(repository.state.value.isConnecting)
            repository.close()
        }

    @Test
    fun `given the server drops the socket when reading then the repository reports a disconnect instead of crashing`() =
        runBlocking {
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } returns null
            val connection = mockk<GameSocketConnection>()
            every { connection.incomingMessages } returns flow { throw java.io.EOFException() }
            coEvery { connection.closeReason() } returns null
            coEvery { connection.close() } returns Unit
            val socket = mockk<GameSocket>()
            coEvery { socket.connect(SERVER_URL) } returns connection
            val repository = repository(socket, sessionStore)

            repository.connect()
            withTimeout(1_000) {
                while (repository.state.value.isConnected) delay(10)
            }

            assertFalse(repository.state.value.isConnected)
            repository.close()
        }

    @Test
    fun `given invalid reconnect session when server responds then stored session is cleared without retrying`() =
        runBlocking {
            var savedSession: RemoteSession? = RemoteSession("AB12", "player-1", "token-1")
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } answers { savedSession }
            every { sessionStore.clear() } answers { savedSession = null }
            val connection = mockk<GameSocketConnection>()
            every { connection.incomingMessages } returns
                flowOf(
                    GameSocketIncomingMessage.Received(ServerMessage.Error("INVALID_SESSION", "Session expired.")),
                )
            coEvery { connection.closeReason() } returns null
            coEvery { connection.close() } returns Unit
            coEvery { connection.send(any<ClientMessage>()) } returns Unit
            val socket = mockk<GameSocket>()
            coEvery { socket.connect(SERVER_URL) } returns connection
            val repository = repository(socket, sessionStore)

            repository.connect()
            withTimeout(1_000) {
                while (savedSession != null) delay(10)
            }

            assertNull(repository.state.value.error)
            assertFalse(repository.state.value.isConnected)
            coVerify(exactly = 1) { sessionStore.clear() }
            coVerify(exactly = 1) { socket.connect(SERVER_URL) }
            repository.close()
        }

    @Test
    fun `given a saved session when connecting then reconnect command is sent`() =
        runBlocking {
            val savedSession = RemoteSession("AB12", "player-1", "token-1")
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } returns savedSession
            val connection = mockk<GameSocketConnection>()
            every { connection.incomingMessages } returns flow { awaitCancellation() }
            coEvery { connection.close() } returns Unit
            coEvery { connection.send(any<ClientMessage>()) } returns Unit
            val socket = mockk<GameSocket>()
            coEvery { socket.connect(SERVER_URL) } returns connection
            val repository = repository(socket, sessionStore)

            repository.connect()
            withTimeout(1_000) {
                while (repository.state.value.isConnecting) delay(10)
            }

            coVerify(exactly = 1) {
                connection.send(ClientMessage.Reconnect("AB12", "player-1", "token-1"))
            }
            assertEquals("player-1", repository.state.value.playerId)
            repository.close()
        }

    @Test
    fun `given an unreadable server message when received then state exposes a connection error`() =
        runBlocking {
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } returns null
            val connection = mockk<GameSocketConnection>()
            every { connection.incomingMessages } returns flowOf(GameSocketIncomingMessage.Unreadable())
            coEvery { connection.closeReason() } returns null
            coEvery { connection.close() } returns Unit
            val socket = mockk<GameSocket>()
            coEvery { socket.connect(SERVER_URL) } returns connection
            val repository = repository(socket, sessionStore)

            repository.connect()
            withTimeout(1_000) {
                while (repository.state.value.isConnected) delay(10)
            }

            assertEquals("Received an unreadable server message.", repository.state.value.error)
            assertFalse(repository.state.value.isConnected)
            repository.close()
        }

    @Test
    fun `given a completed game when it is received then history is saved locally`() =
        runBlocking {
            val player = Player("player-1", "Alex", PlayerColour.Blue)
            val room =
                RoomState(
                    roomCode = "AB12",
                    hostId = player.id,
                    players = listOf(player),
                    status = GameStatus.Finished,
                    openingPlayerId = player.id,
                    prompt = "A door opened.",
                    contributions = listOf(Contribution(player.id, "The end.", 0)),
                    maxPlayers = 10,
                    maxRounds = 3,
                )
            var savedStories = emptyList<SavedStory>()
            val historyStore = mockk<StoryHistoryStore>()
            every { historyStore.load() } answers { savedStories }
            every { historyStore.save(any()) } answers { savedStories = listOf(firstArg()) }
            val sessionStore = mockk<RemoteSessionStore>()
            every { sessionStore.load() } returns null
            val connection = mockk<GameSocketConnection>()
            every { connection.incomingMessages } returns flowOf(GameSocketIncomingMessage.Received(ServerMessage.GameFinished(room)))
            coEvery { connection.closeReason() } returns null
            coEvery { connection.close() } returns Unit
            val socket = mockk<GameSocket>()
            coEvery { socket.connect(SERVER_URL) } returns connection
            val repository = repository(socket, sessionStore, historyStore)

            repository.connect()
            withTimeout(1_000) {
                while (repository.state.value.history
                        .isEmpty()
                ) {
                    delay(10)
                }
            }

            assertEquals(
                listOf("A door opened."),
                repository.state.value.history
                    .map { it.prompt },
            )
            assertEquals(player.id, savedStories.single().promptPlayerId)
            repository.close()
        }

    @Test
    fun `given stories saved on this device when the repository starts and disconnects then history is kept`() {
        val player = Player(id = "player-1", name = "Alex", colour = PlayerColour.Blue)
        val savedStory =
            SavedStory(
                id = "story-1",
                prompt = "A door opened.",
                promptPlayerId = player.id,
                createdAtEpochMillis = 1,
                players = listOf(player),
                contributions = listOf(Contribution(player.id, "The end.", 0)),
            )
        val historyStore = mockk<StoryHistoryStore>()
        every { historyStore.load() } returns listOf(savedStory)
        val sessionStore = mockk<RemoteSessionStore>()
        every { sessionStore.load() } returns null
        val repository = repository(mockk<GameSocket>(), sessionStore, historyStore)

        assertEquals(listOf("story-1"), repository.savedStoryIds())

        repository.disconnect()

        assertEquals(listOf("story-1"), repository.savedStoryIds())
        repository.close()
    }

    private fun repository(
        socket: GameSocket,
        sessionStore: RemoteSessionStore,
        historyStore: StoryHistoryStore = mockk(relaxed = true),
    ) = RemoteGameRepository(
        serverUrl = SERVER_URL,
        client = HttpClient(),
        sessionStore = sessionStore,
        reconnectPolicy = mockk<ReconnectPolicy> { every { shouldReconnect(any(), any()) } returns false },
        storyHistoryStore = historyStore,
        sessionReducer = GameSessionReducer(),
        connectionManager = DefaultGameConnectionManager(socket),
        timeProvider = mockk { every { nowEpochMillis() } returns 1L },
    )

    private companion object {
        const val SERVER_URL = "ws://localhost:8080/game"
    }

    private fun RemoteGameRepository.savedStoryIds(): List<String> = state.value.history.map(SavedStory::id)
}
