package com.example.storyround.integration

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import kotlin.test.Test

class ProtocolHardeningTest : ServerIntegrationTest() {
    @Test
    fun `should reply that the message is invalid when a frame is not JSON`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex sends plain text") {
                player("Alex").sendRawText("hello")
            }

            THEN("Alex is told the message could not be read") {
                player("Alex").assertErrorReceived("INVALID_MESSAGE")
            }
        }

    @Test
    fun `should reply that the message is invalid when its type is unknown`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex sends a message of an unknown type") {
                player("Alex").sendRawText("""{"type":"FLY_AWAY"}""")
            }

            THEN("Alex is told the message could not be read") {
                player("Alex").assertErrorReceived("INVALID_MESSAGE")
            }
        }

    @Test
    fun `should reply that the message is invalid when a required field is missing`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex sends CREATE_ROOM without a name") {
                player("Alex").sendRawText("""{"type":"CREATE_ROOM"}""")
            }

            THEN("Alex is told the message could not be read") {
                player("Alex").assertErrorReceived("INVALID_MESSAGE")
            }
        }

    @Test
    fun `should keep the connection usable when a message was invalid`() =
        serverTest {
            GIVEN("Alex has sent an unreadable message") {
                player("Alex").sendRawText("hello")
                player("Alex").assertErrorReceived("INVALID_MESSAGE")
            }

            WHEN("Alex creates a room on the same connection") {
                player("Alex").createRoom()
            }

            THEN("Alex hosts the new room") {
                player("Alex").assertHost("Alex")
            }
        }

    @Test
    fun `should ignore binary frames`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex sends a binary frame") {
                player("Alex").sendRawBinary(byteArrayOf(1, 2, 3))
            }

            THEN("nothing is sent back") {
                player("Alex").assertNoMoreMessages()
            }
        }

    @Test
    fun `should close the connection when a frame is larger than 16 KB`() =
        serverTest {
            GIVEN("Alex is connected") {
                player("Alex").connect()
            }

            WHEN("Alex sends a 17 KB frame") {
                player("Alex").sendRawText("x".repeat(17 * 1024))
            }

            THEN("the server closes Alex's connection") {
                player("Alex").assertDisconnectedByServer()
            }
        }
}
