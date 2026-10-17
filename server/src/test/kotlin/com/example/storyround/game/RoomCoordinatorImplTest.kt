package com.example.storyround.game

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomEvent
import com.example.storyround.room.RoomRegistry
import com.example.storyround.room.TurnOutcome
import com.example.storyround.time.TimeProvider
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomCoordinatorImplTest {
    private val registry = mockk<RoomRegistry>()
    private val sockets = mockk<RoomSocketHub>(relaxUnitFun = true)
    private val timers = mockk<RoomTimers>(relaxUnitFun = true)
    private val timeProvider = mockk<TimeProvider> { every { nowEpochMillis() } returns 1_000L }
    private val logger = mockk<Logger>(relaxed = true)
    private val coordinator = RoomCoordinatorImpl(registry, sockets, timers, timeProvider, IDLE_TIMEOUT_MILLIS, logger)
    private val session = mockk<DefaultWebSocketServerSession>()
    private val alex = Player("alex", "Alex", PlayerColour.entries[0])
    private val sam = Player("sam", "Sam", PlayerColour.entries[1])
    private val lobby = RoomState("AB12", "alex", listOf(alex, sam), GameStatus.Lobby, maxPlayers = 10, settings = RoomSettings(rounds = 3))
    private val playing = lobby.copy(status = GameStatus.Playing, currentPlayerId = "sam", turnDeadlineEpochMillis = 15_000L)
    private val finished = lobby.copy(status = GameStatus.Finished)
    private val contribution = Contribution("alex", "A horse arrived.", 0)
    private val turnExpiry = slot<suspend () -> Unit>()
    private val idleClose = slot<suspend () -> Unit>()
    private val announcing = CompletableDeferred<Unit>()
    private var secondActionRan = false

    @BeforeTest
    fun setup() {
        every { sockets.bind(any(), any(), any()) } returns null
        every { sockets.bindingOf(any()) } returns null
        every { sockets.hasConnections(any()) } returns true
    }

    @Test
    fun `should seat the host and show everyone they joined when a room is created`() =
        runTest {
            // Given
            val result = RegistryResult.Success(lobby, RoomEvent.RoomCreated(alex, "token"))

            // When
            coordinator.runForNewRoom(session) { result }

            // Then
            coVerifySequence {
                sockets.bind(session, "AB12", "alex")
                sockets.send(session, ServerMessage.SessionEstablished("alex", "token"))
                sockets.broadcast("AB12", ServerMessage.RoomStateChanged(lobby))
                sockets.broadcast("AB12", ServerMessage.PlayerJoined(alex))
            }
            verify { timers.cancel("AB12", RoomTimer.IdleClose) }
        }

    @Test
    fun `should tell only the sender why when the room refuses an action`() =
        runTest {
            // Given
            val result = RegistryResult.Error("ROOM_NOT_FOUND", "No game found.")

            // When
            coordinator.runInRoom("ZZZZ", session) { result }

            // Then
            coVerify(exactly = 1) { sockets.send(session, ServerMessage.Error("ROOM_NOT_FOUND", "No game found.")) }
            coVerify(exactly = 0) { sockets.broadcast(any(), any()) }
        }

    @Test
    fun `given a room is created when the coordinator reacts then the new room and its host are logged`() =
        runTest {
            // Given
            val result = RegistryResult.Success(lobby, RoomEvent.RoomCreated(alex, "token"))

            // When
            coordinator.runForNewRoom(session) { result }

            // Then
            verify { logger.info(any(), match { it.contains("AB12") && it.contains("alex") && "token" !in it }, null) }
        }

    @Test
    fun `given a seated player when the room refuses their action then the refusal and its code are logged`() =
        runTest {
            // Given
            every { sockets.bindingOf(session) } returns PlayerBinding("AB12", "sam")
            val result = RegistryResult.Error("NOT_HOST", "Only the host can start the game.")

            // When
            coordinator.runInRoom("AB12", session) { result }

            // Then
            verify { logger.info(any(), match { it.contains("sam") && it.contains("AB12") && it.contains("NOT_HOST") }, null) }
        }

    @Test
    fun `should update the story and time the next turn when a contribution starts the next turn`() =
        runTest {
            // Given
            val result =
                RegistryResult.Success(playing, RoomEvent.ContributionAdded(contribution, TurnOutcome.NextTurn("sam", 15_000L)))

            // When
            coordinator.runInRoom("AB12", session) { result }

            // Then
            coVerifySequence {
                sockets.broadcast("AB12", ServerMessage.RoomStateChanged(playing))
                sockets.broadcast("AB12", ServerMessage.ContributionAdded(contribution))
                timers.schedule("AB12", RoomTimer.TurnExpiry, 14_000L, any())
                sockets.broadcast("AB12", ServerMessage.TurnStarted("sam", 15_000L))
            }
        }

    @Test
    fun `should expire the turn and announce the finish when the turn timer fires on the last turn`() =
        runTest {
            // Given
            startSamsTimedTurn()
            every { registry.expireTurn("AB12", 15_000L) } returns
                RegistryResult.Success(finished, RoomEvent.TurnExpired("sam", TurnOutcome.GameFinished))

            // When
            turnExpiry.captured()

            // Then
            coVerify {
                sockets.broadcast("AB12", ServerMessage.RoomStateChanged(finished))
                sockets.broadcast("AB12", ServerMessage.TurnExpired("sam"))
                timers.cancel("AB12", RoomTimer.TurnExpiry)
                sockets.broadcast("AB12", ServerMessage.GameFinished(finished))
            }
        }

    @Test
    fun `should announce nothing when a turn timer fires after its deadline stopped applying`() =
        runTest {
            // Given
            startSamsTimedTurn()
            every { registry.expireTurn("AB12", 15_000L) } returns null

            // When
            turnExpiry.captured()

            // Then
            coVerify(exactly = 0) { sockets.broadcast("AB12", match { it is ServerMessage.TurnExpired }) }
        }

    @Test
    fun `should send only the returning player the room and the finished story when they reconnect to a finished game`() =
        runTest {
            // Given
            val result = RegistryResult.Success(finished, RoomEvent.PlayerReconnected(alex, "token"))

            // When
            coordinator.runInRoom("AB12", session) { result }

            // Then
            coVerifySequence {
                sockets.bind(session, "AB12", "alex")
                sockets.send(session, ServerMessage.SessionEstablished("alex", "token"))
                sockets.send(session, ServerMessage.RoomStateChanged(finished))
                sockets.send(session, ServerMessage.GameFinished(finished))
            }
            verify { timers.cancel("AB12", RoomTimer.IdleClose) }
        }

    @Test
    fun `should release the player's connections and tell the others when a player leaves the lobby`() =
        runTest {
            // Given
            val result = RegistryResult.Success(lobby, RoomEvent.PlayerLeft("sam"))

            // When
            coordinator.runInRoom("AB12", session) { result }

            // Then
            coVerifySequence {
                sockets.unbindPlayer("AB12", "sam")
                sockets.hasConnections("AB12")
                sockets.broadcast("AB12", ServerMessage.RoomStateChanged(lobby))
                sockets.broadcast("AB12", ServerMessage.PlayerLeft("sam"))
            }
        }

    @Test
    fun `should release the room's timers and connections when the last player leaves`() =
        runTest {
            // Given
            val result = RegistryResult.Success(lobby, RoomEvent.RoomClosed)

            // When
            coordinator.runInRoom("AB12", session) { result }

            // Then
            verify {
                timers.cancelAll("AB12")
                sockets.unbindRoom("AB12")
            }
            coVerify(exactly = 0) { sockets.broadcast(any(), any()) }
        }

    @Test
    fun `should close the room when the idle timeout passes with nobody back`() =
        runTest {
            // Given
            dropLastConnection()
            every { registry.closeRoom("AB12") } returns RegistryResult.Success(lobby, RoomEvent.RoomClosed)

            // When
            idleClose.captured()

            // Then
            verify {
                registry.closeRoom("AB12")
                timers.cancelAll("AB12")
                sockets.unbindRoom("AB12")
            }
        }

    @Test
    fun `should keep the room open when the idle timeout passes after someone reconnected`() =
        runTest {
            // Given
            dropLastConnection()
            every { sockets.hasConnections("AB12") } returns true

            // When
            idleClose.captured()

            // Then
            verify(exactly = 0) { registry.closeRoom(any()) }
        }

    @Test
    fun `should not start an idle timer when a connection drops while others are still connected`() =
        runTest {
            // Given
            every { sockets.bindingOf(session) } returns PlayerBinding("AB12", "alex")
            every { sockets.unbind(session) } returns PlayerBinding("AB12", "alex")

            // When
            coordinator.connectionClosed(session)

            // Then
            verify(exactly = 0) { timers.schedule(any(), RoomTimer.IdleClose, any(), any()) }
        }

    @Test
    fun `should do nothing when a connection that never joined drops`() =
        runTest {
            // Given
            every { sockets.bindingOf(session) } returns null

            // When
            coordinator.connectionClosed(session)

            // Then
            verify(exactly = 0) { sockets.unbind(any()) }
            verify(exactly = 0) { timers.schedule(any(), any(), any(), any()) }
        }

    @Test
    fun `should let the room a connection left close when idle when that connection joins another room`() =
        runTest {
            // Given
            every { sockets.bind(session, "CD34", "sam") } returns PlayerBinding("AB12", "sam")
            every { sockets.hasConnections("AB12") } returns false
            val result = RegistryResult.Success(lobby.copy(roomCode = "CD34"), RoomEvent.PlayerJoined(sam, "token"))

            // When
            coordinator.runInRoom("CD34", session) { result }

            // Then
            verify { timers.schedule("AB12", RoomTimer.IdleClose, IDLE_TIMEOUT_MILLIS, any()) }
            verify(exactly = 0) { timers.schedule("CD34", RoomTimer.IdleClose, any(), any()) }
        }

    @Test
    fun `should hold a second action when the room is still announcing the first`() =
        runTest {
            // Given
            startAnnouncingGameStart()

            // When
            backgroundScope.launch { coordinator.runInRoom("AB12", session, ::samLeaves) }
            runCurrent()

            // Then
            assertFalse(secondActionRan)
        }

    @Test
    fun `should apply the held action when the room finishes announcing the first`() =
        runTest {
            // Given
            startAnnouncingGameStart()
            backgroundScope.launch { coordinator.runInRoom("AB12", session, ::samLeaves) }
            runCurrent()

            // When
            announcing.complete(Unit)
            runCurrent()

            // Then
            assertTrue(secondActionRan)
        }

    private suspend fun startSamsTimedTurn() {
        coordinator.runInRoom("AB12", session) {
            RegistryResult.Success(playing, RoomEvent.PromptSubmitted(TurnOutcome.NextTurn("sam", 15_000L)))
        }
        verify { timers.schedule("AB12", RoomTimer.TurnExpiry, 14_000L, capture(turnExpiry)) }
    }

    private suspend fun dropLastConnection() {
        every { sockets.bindingOf(session) } returns PlayerBinding("AB12", "alex")
        every { sockets.unbind(session) } returns PlayerBinding("AB12", "alex")
        every { sockets.hasConnections("AB12") } returns false
        coordinator.connectionClosed(session)
        verify { timers.schedule("AB12", RoomTimer.IdleClose, IDLE_TIMEOUT_MILLIS, capture(idleClose)) }
    }

    private suspend fun TestScope.startAnnouncingGameStart() {
        coordinator.runForNewRoom(session) { RegistryResult.Success(lobby, RoomEvent.RoomCreated(alex, "token")) }
        coEvery { sockets.broadcast("AB12", ServerMessage.RoomStateChanged(playing)) } coAnswers { announcing.await() }
        backgroundScope.launch { coordinator.runInRoom("AB12", session) { RegistryResult.Success(playing, RoomEvent.GameStarted) } }
        runCurrent()
    }

    private fun samLeaves(): RegistryResult {
        secondActionRan = true
        return RegistryResult.Success(lobby.copy(players = listOf(alex)), RoomEvent.PlayerLeft("sam"))
    }

    private companion object {
        const val IDLE_TIMEOUT_MILLIS = 60_000L
    }
}
