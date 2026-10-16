package com.example.storyround.data.remote

import com.example.storyround.data.local.LocalStoryHistoryRepository
import com.example.storyround.data.local.RemoteSession
import com.example.storyround.data.local.RemoteSessionStore
import com.example.storyround.data.local.StoryHistoryStore
import com.example.storyround.domain.model.GameError
import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
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
    private val historyRepository =
        LocalStoryHistoryRepository(historyStore, SavedStoryMapperImpl(), mockk { every { nowEpochMillis() } returns 1L }, logger)

    @Test
    fun `given socket connection failure when connecting then state reports the failure and it is logged`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val repository = repository()

            // When
            repository.connect()

            // Then
            assertFalse(repository.state.value.isConnecting)
            assertFalse(repository.state.value.isConnected)
            assertEquals(GameError.ServerUnreachable, repository.state.value.error)
            verify { logger.warn(any(), match { it.contains("offline") }, null) }
        }

    @Test
    fun `given the launch connection failed when creating a room then it connects again and creates without resuming the old session`() =
        runTest {
            // Given
            savedSession = RemoteSession("OLD1", player.id, "token-1")
            val connection = openConnection()
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline") andThen connection
            val repository = repository()
            repository.connect()

            // When
            repository.createRoom("Alex")

            // Then
            coVerify(exactly = 2) { socket.connect(SERVER_URL) }
            coVerify(exactly = 1) { connection.send(ClientMessage.CreateRoom("Alex")) }
            coVerify(exactly = 0) { connection.send(ofType<ClientMessage.Reconnect>()) }
            assertNull(repository.state.value.error)
        }

    @Test
    fun `given the server is still unreachable when joining a room then a player-facing error is shown`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } throws IllegalStateException("offline")
            val repository = repository()

            // When
            repository.joinRoom("ABCD", "Sam")

            // Then
            assertEquals(GameError.ServerUnreachable, repository.state.value.error)
            assertFalse(repository.state.value.isConnecting)
        }

    @Test
    fun `given the server drops the socket when reading then the repository reports a disconnect instead of crashing`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } returns connection(flow { throw EOFException() })
            val repository = repository()

            // When
            repository.connect()
            runCurrent()

            // Then
            assertFalse(repository.state.value.isConnected)
        }

    @Test
    fun `given invalid reconnect session when server responds then stored session is cleared without retrying`() =
        runTest {
            // Given
            savedSession = RemoteSession("AB12", player.id, "token-1")
            val rejection = GameSocketIncomingMessage.Received(ServerMessage.Error("INVALID_SESSION", "Session expired."))
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(rejection))
            val repository = repository()

            // When
            repository.connect()
            runCurrent()

            // Then
            assertNull(savedSession)
            assertNull(repository.state.value.error)
            assertFalse(repository.state.value.isConnected)
            coVerify(exactly = 1) { socket.connect(SERVER_URL) }
        }

    @Test
    fun `given a saved session when connecting then reconnect command is sent`() =
        runTest {
            // Given
            savedSession = RemoteSession("AB12", player.id, "token-1")
            val connection = openConnection()
            coEvery { socket.connect(SERVER_URL) } returns connection
            val repository = repository()

            // When
            repository.connect()
            runCurrent()

            // Then
            coVerify(exactly = 1) { connection.send(ClientMessage.Reconnect("AB12", player.id, "token-1")) }
            assertEquals(player.id, repository.state.value.playerId)
            assertTrue(repository.state.value.isConnected)
        }

    @Test
    fun `given an unexpected disconnect with a saved session when the policy allows then the session is resumed`() =
        runTest {
            // Given
            savedSession = RemoteSession("AB12", player.id, "token-1")
            every { reconnectPolicy.shouldReconnect(true, false) } returns true
            every { reconnectPolicy.delayMillis(any()) } returns 1_000L
            val resumed = openConnection()
            coEvery { socket.connect(SERVER_URL) } returns connection(flow { throw EOFException() }) andThen resumed
            val repository = repository()

            // When
            repository.connect()
            runCurrent()
            advanceTimeBy(1_001L)
            runCurrent()

            // Then
            coVerify(exactly = 2) { socket.connect(SERVER_URL) }
            coVerify(exactly = 1) { resumed.send(ClientMessage.Reconnect("AB12", player.id, "token-1")) }
            assertTrue(repository.state.value.isConnected)
        }

    @Test
    fun `given an unreadable server message when received then state exposes a connection error`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(GameSocketIncomingMessage.Unreadable))
            val repository = repository()

            // When
            repository.connect()
            runCurrent()

            // Then
            assertEquals(GameError.UnreadableMessage, repository.state.value.error)
            assertFalse(repository.state.value.isConnected)
        }

    @Test
    fun `given a completed game when it is received then the story is saved on this device`() =
        runTest {
            // Given
            coEvery { socket.connect(SERVER_URL) } returns connection(flowOf(finished()))
            val repository = repository()

            // When
            repository.connect()
            runCurrent()

            // Then
            assertEquals(
                listOf("A door opened."),
                historyRepository.state.value.stories
                    .map { it.prompt },
            )
            assertEquals(player.id, savedStories.single().promptPlayerId)
            assertEquals(1L, savedStories.single().createdAtEpochMillis)
        }

    @Test
    fun `given a player in a lobby when they leave then the server is told and the saved session is cleared`() =
        runTest {
            // Given
            val room = RoomState("AB12", player.id, listOf(player), GameStatus.Lobby, maxPlayers = 10, settings = RoomSettings(rounds = 3))
            val connection = openConnection(GameSocketIncomingMessage.Received(ServerMessage.RoomStateChanged(room)))
            coEvery { socket.connect(SERVER_URL) } returns connection
            savedSession = RemoteSession("AB12", player.id, "token-1")
            val repository = repository()
            repository.connect()
            runCurrent()

            // When
            repository.leaveRoom()
            runCurrent()

            // Then
            coVerify(exactly = 1) { connection.send(ClientMessage.LeaveRoom("AB12", player.id)) }
            coVerify(exactly = 1) { connection.close() }
            assertNull(savedSession)
            assertNull(repository.state.value.room)
        }

    @Test
    fun `given no room when a room action is taken then the player is told to join a room first`() =
        runTest {
            // Given
            val repository = repository()

            // When
            repository.submitContribution("A horse arrived.")

            // Then
            assertEquals(GameError.NoRoom, repository.state.value.error)
        }

    @Test
    fun `should show the server's message when a join is refused because the room does not exist`() =
        runTest {
            // Given
            val refusal = ServerMessage.Error("ROOM_NOT_FOUND", "No game found with that code. Check it and try again.")
            coEvery { socket.connect(SERVER_URL) } returns openConnection(GameSocketIncomingMessage.Received(refusal))
            val joiningRepository = repository()

            // When
            joiningRepository.joinRoom("ZZZZ", "Sam")
            runCurrent()

            // Then
            assertEquals(GameError.Server("No game found with that code. Check it and try again."), joiningRepository.state.value.error)
        }

    private fun TestScope.repository() =
        RemoteGameRepository(
            serverUrl = SERVER_URL,
            sessionStore = sessionStore,
            storyHistoryRepository = historyRepository,
            reconnectPolicy = reconnectPolicy,
            sessionReducer = GameSessionReducerImpl(),
            connectionManager = GameConnectionManagerImpl(socket, backgroundScope, logger),
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
                settings = RoomSettings(rounds = 3),
            )
        return GameSocketIncomingMessage.Received(ServerMessage.GameFinished(room))
    }

    private companion object {
        const val SERVER_URL = "ws://localhost:8080/game"
    }
}
