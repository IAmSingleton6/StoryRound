package com.example.storyround.game

import com.example.storyround.logging.Logger
import com.example.storyround.protocol.ErrorCode
import com.example.storyround.protocol.ServerMessage
import com.example.storyround.protocol.protocolJson
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GameWebSocketHandlerTest {
    private val messageHandler = mockk<ClientMessageHandler>(relaxed = true)
    private val coordinator = mockk<RoomCoordinator>(relaxed = true)
    private val sockets = mockk<RoomSocketHub>(relaxed = true)
    private val logger = mockk<Logger>(relaxed = true)
    private val handler = GameWebSocketHandler(messageHandler, coordinator, sockets, protocolJson(), logger)

    @Test
    fun `given an unreadable frame when it arrives then the sender is told and a warning without its contents is logged`() =
        runTest {
            // Given
            val session = sessionReceiving(Frame.Text("""{"sessionToken":"secret-token""""))

            // When
            handler.handle(session)

            // Then
            coVerify { sockets.send(session, match { it is ServerMessage.Error && it.code == ErrorCode.INVALID_MESSAGE }) }
            verify { logger.warn(any(), match { it.contains("unreadable") && "secret-token" !in it }, null) }
        }

    private fun sessionReceiving(vararg frames: Frame): DefaultWebSocketServerSession {
        val incoming = Channel<Frame>(Channel.UNLIMITED)
        frames.forEach(incoming::trySend)
        incoming.close()
        return mockk(relaxed = true) { every { this@mockk.incoming } returns incoming }
    }
}
