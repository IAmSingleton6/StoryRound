package com.example.storyround.data.remote

import com.example.storyround.data.local.RemoteSession
import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.StoryHistoryStore
import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.story.SavedStoryMapperImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.io.EOFException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteGameRepositoryTest {
    private val player = Player("player-1", "Alex", PlayerColour.Blue)
    private val socket = mockk<GameSocket>()
    private val logger = mockk<Logger>(relaxed = true)
    private val reconnectPolicy = mockk<ReconnectPolicy> { every { shouldReconnect(any(), any()) } returns false }
    private var savedSession: RemoteSession? = null
    private val sessionStore =
        mockk<RemoteSessionStore> {
            every { load() } answers { savedSession }
            every { save(any()) } answers { savedSession = firstArg() }
            every { clear() } answers { savedSession = null }
        }
    private var savedStories = emptyList<SavedStory>()
    private val historyStore =
        mockk<StoryHistoryStore> {
            every { load() } answers { savedStories }
            every { save(any()) } answers {
                val story = firstArg<SavedStory>()
                savedStories = listOf(story) + savedStories.filterNot { it.id == story.id }
            }
        }

    @Test
    fun `given socket connection failure when connecting then state reports the failure and it is logged`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val repository = repository()

            repository.connect()

            assertFalse(repository.state.value.isConnecting)
            assertFalse(repository.state.value.isConnected)
            assertEquals(SERVER_UNREACHABLE_MESSAGE, repository.state.value.error)
            verify { logger.warn(any(), match { it.contains("offline") }, null) }
        }

    @Test
    fun `given the launch connection failed when creating a room then it connects again and creates without resuming the old session`() =
        runTest {
            savedSession = RemoteSession("OLD1", player.id, "token-1")
            val connection = openConnection()
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline") andThen connection
            val repository = repository()
            repository.connect()

            repository.createRoom("Alex")

            coVerify(exactly = 2) { socket.connect(SERVER_URL) }
            coVerify(exactly = 1) { connection.send(ClientMessage.CreateRoom("Alex")) }
            coVerify(exactly = 0) { connection.send(ofType<ClientMessage.Reconnect>()) }
            assertNull(repository.state.value.error)
        }

    @Test
    fun `given the server is still unreachable when joining a room then a player-facing error is shown`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val repository = repository()

            repository.joinRoom("ABCD", "Sam")

            assertEquals(SERVER_UNREACHABLE_MESSAGE, repository.state.value.error)
            assertFalse(repository.state.value.isConnecting)
        }

    @Test
    fun `given the server drops the socket when reading then the repository reports a disconnect instead of crashing`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } returns connection(flow { throw EOFException() })
            val repository = repository()

            repository.connect()
            runCurrent()

            assertFalse(repository.state.value.isConnected)
        }

    @Test
    fun `given invalid reconnect session when server responds then stored session is cleared without retrying`() =
        runTest {
            savedSession = RemoteSession("AB12", player.id, "token-1")
            val rejection = GameSocketIncomingMessage.Received(ServerMessage.Error("INVALID_SESSION", "Session expired."))
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(rejection))
            val repository = repository()

            repository.connect()
            runCurrent()

            assertNull(savedSession)
            assertNull(repository.state.value.error)
            assertFalse(repository.state.value.isConnected)
            coVerify(exactly = 1) { socket.connect(SERVER_URL) }
        }

    @Test
    fun `given a saved session when connecting then reconnect command is sent`() =
        runTest {
            savedSession = RemoteSession("AB12", player.id, "token-1")
            val connection = openConnection()
            coEvery { socket.connect(SERVER_URL) } returns connection
            val repository = repository()

            repository.connect()
            runCurrent()

            coVerify(exactly = 1) { connection.send(ClientMessage.Reconnect("AB12", player.id, "token-1")) }
            assertEquals(player.id, repository.state.value.playerId)
            assertTrue(repository.state.value.isConnected)
        }

    @Test
    fun `given an unexpected disconnect with a saved session when the policy allows then the session is resumed`() =
        runTest {
            savedSession = RemoteSession("AB12", player.id, "token-1")
            every { reconnectPolicy.shouldReconnect(true, false) } returns true
            every { reconnectPolicy.delayMillis(any()) } returns 1_000L
            val resumed = openConnection()
            coEvery { socket.connect(SERVER_URL) } returns connection(flow { throw EOFException() }) andThen resumed
            val repository = repository()

            repository.connect()
            runCurrent()
            advanceTimeBy(1_001L)
            runCurrent()

            coVerify(exactly = 2) { socket.connect(SERVER_URL) }
            coVerify(exactly = 1) { resumed.send(ClientMessage.Reconnect("AB12", player.id, "token-1")) }
            assertTrue(repository.state.value.isConnected)
        }

    @Test
    fun `given an unreadable server message when received then state exposes a connection error`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(GameSocketIncomingMessage.Unreadable))
            val repository = repository()

            repository.connect()
            runCurrent()

            assertEquals(UNREADABLE_MESSAGE, repository.state.value.error)
            assertFalse(repository.state.value.isConnected)
        }

    @Test
    fun `given a completed game when it is received then the story is saved on this device`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(finished()))
            val repository = repository()

            repository.connect()
            runCurrent()

            assertEquals(
                listOf("A door opened."),
                repository.state.value.history
                    .map { it.prompt },
            )
            assertEquals(player.id, savedStories.single().promptPlayerId)
            assertEquals(1L, savedStories.single().createdAtEpochMillis)
        }

    @Test
    fun `given the same finished game arrives again after a reconnect when saved then it is kept only once`() =
        runTest {
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(finished(), finished()))
            val repository = repository()

            repository.connect()
            runCurrent()

            assertEquals(1, savedStories.size)
        }

    @Test
    fun `given stories saved on this device when the repository starts and disconnects then history is kept`() =
        runTest {
            savedStories = listOf(SavedStory("story-1", "A door opened.", player.id, 1, listOf(player), emptyList()))
            val repository = repository()

            assertEquals(
                listOf("story-1"),
                repository.state.value.history
                    .map(SavedStory::id),
            )

            repository.disconnect()

            assertEquals(
                listOf("story-1"),
                repository.state.value.history
                    .map(SavedStory::id),
            )
        }

    @Test
    fun `given a player in a lobby when they leave then the server is told and the saved session is cleared`() =
        runTest {
            val room = RoomState("AB12", player.id, listOf(player), GameStatus.Lobby, maxPlayers = 10, maxRounds = 3)
            val connection = openConnection(GameSocketIncomingMessage.Received(ServerMessage.RoomStateChanged(room)))
            coEvery { socket.connect(SERVER_URL) } returns connection
            savedSession = RemoteSession("AB12", player.id, "token-1")
            val repository = repository()
            repository.connect()
            runCurrent()

            repository.leaveRoom()
            runCurrent()

            coVerify(exactly = 1) { connection.send(ClientMessage.LeaveRoom("AB12", player.id)) }
            coVerify(exactly = 1) { connection.close() }
            assertNull(savedSession)
            assertNull(repository.state.value.room)
        }

    @Test
    fun `given no room when a room action is taken then the player is told to join a room first`() =
        runTest {
            val repository = repository()

            repository.submitContribution("A horse arrived.")

            assertEquals(NO_ROOM_MESSAGE, repository.state.value.error)
        }

    @Test
    fun `given a saved story when it is opened and closed then it is selected and released`() =
        runTest {
            savedStories = listOf(SavedStory("story-1", "A door opened.", player.id, 1, listOf(player), emptyList()))
            val repository = repository()

            assertTrue(repository.openStory("story-1"))
            assertEquals(
                "story-1",
                repository.state.value.selectedStory
                    ?.id,
            )
            assertFalse(repository.openStory("missing"))

            repository.closeStory()

            assertNull(repository.state.value.selectedStory)
        }

    private fun TestScope.repository() =
        RemoteGameRepository(
            serverUrl = SERVER_URL,
            sessionStore = sessionStore,
            storyHistoryStore = historyStore,
            reconnectPolicy = reconnectPolicy,
            sessionReducer = GameSessionReducerImpl(),
            connectionManager = GameConnectionManagerImpl(socket, backgroundScope),
            savedStoryMapper = SavedStoryMapperImpl(),
            timeProvider = mockk { every { nowEpochMillis() } returns 1L },
            scope = backgroundScope,
            logger = logger,
        )

    private fun openConnection(vararg messages: GameSocketIncomingMessage): GameSocketConnection =
        connection(
            flow {
                messages.forEach { emit(it) }
                awaitCancellation()
            },
        )

    private fun connection(incoming: Flow<GameSocketIncomingMessage>): GameSocketConnection =
        mockk(relaxed = true) {
            every { incomingMessages } returns incoming
            coEvery { closeReason() } returns null
        }

    private fun finished(): GameSocketIncomingMessage {
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
        return GameSocketIncomingMessage.Received(ServerMessage.GameFinished(room))
    }

    private companion object {
        const val SERVER_URL = "ws://localhost:8080/game"
    }
}
