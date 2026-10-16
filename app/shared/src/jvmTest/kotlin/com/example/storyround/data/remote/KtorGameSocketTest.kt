package com.example.storyround.data.remote

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.StoryRoundTestServer
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.actor.SocketRobot
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class KtorGameSocketTest {
    private val server = StoryRoundTestServer()

    @Test
    fun `should deliver the server's reply as a typed message when a command is sent`() =
        socketTest {
            GIVEN("a connection to the server") {
                socketRobot.connect(GAME_URL)
            }

            WHEN("Alex asks to create a room") {
                socketRobot.send(ClientMessage.CreateRoom("Alex"))
            }

            THEN("the server's first reply arrives as a session message") {
                socketRobot.assertSessionEstablished()
            }
        }

    @Test
    fun `should report an unreadable message when the server sends a frame that is not a game message`() =
        socketTest {
            GIVEN("a connection to a server route that sends a frame that is not a game message") {
                socketRobot.connect(UNREADABLE_URL)
            }

            WHEN("the app reads the next message") {
                socketRobot.readNextMessage()
            }

            THEN("it is reported as unreadable rather than failing") {
                socketRobot.assertLastMessageUnreadable()
            }
        }

    @Test
    fun `should end the messages and report why when the server closes the connection`() =
        socketTest {
            GIVEN("a connection to the server") {
                socketRobot.connect(GAME_URL)
            }

            WHEN("the app sends a message larger than the server accepts") {
                socketRobot.send(ClientMessage.CreateRoom("x".repeat(OVERSIZED_LENGTH)))
            }

            THEN("the incoming messages end and the connection reports why") {
                socketRobot.assertClosedWithReason()
            }
        }

    private fun socketTest(body: suspend SocketTestScope.() -> Unit): TestResult =
        runTest {
            server.start()
            try {
                SocketTestScope(SocketRobot(server)).body()
            } finally {
                server.stop()
            }
        }

    private class SocketTestScope(
        val socketRobot: SocketRobot,
    )

    private companion object {
        const val GAME_URL = "ws://localhost/game"
        const val UNREADABLE_URL = "ws://localhost${StoryRoundTestServer.UNREADABLE_FRAME_PATH}"

        const val OVERSIZED_LENGTH = 17 * 1024
    }
}
