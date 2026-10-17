package com.example.storyround.game

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomRegistry
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClientMessageHandlerImplTest {
    private val registry = mockk<RoomRegistry>()
    private val coordinator = mockk<RoomCoordinator>()
    private val sockets = mockk<RoomSocketHub>(relaxUnitFun = true)
    private val handler = ClientMessageHandlerImpl(registry, coordinator, sockets)
    private val session = mockk<DefaultWebSocketServerSession>()
    private val result = RegistryResult.Error("CODE", "Message")
    private val notConnectedAsPlayer = ServerMessage.Error("NOT_CONNECTED_AS_PLAYER", "This connection is not playing as that player.")

    @BeforeTest
    fun setup() {
        every { sockets.bindingOf(any()) } returns null
        coEvery { coordinator.runForNewRoom(any(), any()) } coAnswers { secondArg<() -> RegistryResult>().invoke() }
        coEvery { coordinator.runInRoom(any(), any(), any()) } coAnswers { thirdArg<() -> RegistryResult>().invoke() }
    }

    @Test
    fun `should open a room through the coordinator when a player creates a room`() =
        runTest {
            // Given
            every { registry.createRoom("Alex") } returns result

            // When
            handler.handle(session, ClientMessage.CreateRoom("Alex"))

            // Then
            coVerify { coordinator.runForNewRoom(session, any()) }
            verify { registry.createRoom("Alex") }
        }

    @Test
    fun `should apply joining and reconnecting to the named room when the connection is not seated yet`() =
        runTest {
            // Given
            every { registry.joinRoom("AB12", "Sam") } returns result
            every { registry.reconnect("AB12", "alex", "token") } returns result

            // When
            handler.handle(session, ClientMessage.JoinRoom("AB12", "Sam"))
            handler.handle(session, ClientMessage.Reconnect("AB12", "alex", "token"))

            // Then
            coVerify(exactly = 2) { coordinator.runInRoom("AB12", session, any()) }
            verify {
                registry.joinRoom("AB12", "Sam")
                registry.reconnect("AB12", "alex", "token")
            }
        }

    @Test
    fun `should apply each player command to the room when the connection is seated as that player`() =
        runTest {
            // Given
            every { sockets.bindingOf(session) } returns PlayerBinding("AB12", "alex")
            every { registry.startGame("AB12", "alex", null, RoomSettings(rounds = 3)) } returns result
            every { registry.submitPrompt("AB12", "alex", "A door opened.") } returns result
            every { registry.submitContribution("AB12", "alex", "A horse arrived.") } returns result
            every { registry.playAgain("AB12", "alex") } returns result
            every { registry.leaveRoom("AB12", "alex") } returns result

            // When
            listOf(
                ClientMessage.StartGame("AB12", "alex", openingPlayerId = null, settings = RoomSettings(rounds = 3)),
                ClientMessage.SubmitPrompt("AB12", "alex", "A door opened."),
                ClientMessage.SubmitContribution("AB12", "alex", "A horse arrived."),
                ClientMessage.PlayAgain("AB12", "alex"),
                ClientMessage.LeaveRoom("AB12", "alex"),
            ).forEach { message -> handler.handle(session, message) }

            // Then
            coVerify(exactly = 5) { coordinator.runInRoom("AB12", session, any()) }
            verify(exactly = 1) {
                registry.startGame("AB12", "alex", null, RoomSettings(rounds = 3))
                registry.submitPrompt("AB12", "alex", "A door opened.")
                registry.submitContribution("AB12", "alex", "A horse arrived.")
                registry.playAgain("AB12", "alex")
                registry.leaveRoom("AB12", "alex")
            }
        }

    @Test
    fun `should refuse a player command before the rules run when the connection is seated as someone else`() =
        runTest {
            // Given
            every { sockets.bindingOf(session) } returns PlayerBinding("AB12", "sam")

            // When
            handler.handle(session, ClientMessage.StartGame("AB12", "alex", null, RoomSettings(rounds = 3)))

            // Then
            coVerify {
                sockets.send(session, notConnectedAsPlayer)
            }
            coVerify(exactly = 0) { coordinator.runInRoom(any(), any(), any()) }
        }

    @Test
    fun `should refuse a player command when the connection never joined`() =
        runTest {
            // Given
            every { sockets.bindingOf(session) } returns null

            // When
            handler.handle(session, ClientMessage.SubmitContribution("AB12", "sam", "Hello."))

            // Then
            coVerify { sockets.send(session, notConnectedAsPlayer) }
            coVerify(exactly = 0) { coordinator.runInRoom(any(), any(), any()) }
        }

    @Test
    fun `should join the canonical room when the typed room code is lower case and padded`() =
        runTest {
            // Given
            every { registry.joinRoom("AB12", "Sam") } returns result

            // When
            handler.handle(session, ClientMessage.JoinRoom(" ab12 ", "Sam"))

            // Then
            coVerify { coordinator.runInRoom("AB12", session, any()) }
            verify { registry.joinRoom("AB12", "Sam") }
        }

    @Test
    fun `should accept a player command in the canonical room when its room code is lower case and padded`() =
        runTest {
            // Given
            every { sockets.bindingOf(session) } returns PlayerBinding("AB12", "alex")
            every { registry.startGame("AB12", "alex", null, RoomSettings(rounds = 3)) } returns result

            // When
            handler.handle(session, ClientMessage.StartGame(" ab12 ", "alex", null, RoomSettings(rounds = 3)))

            // Then
            coVerify { coordinator.runInRoom("AB12", session, any()) }
            verify { registry.startGame("AB12", "alex", null, RoomSettings(rounds = 3)) }
        }
}
