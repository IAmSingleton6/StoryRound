package com.example.storyround.game

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.room.RegistryResult
import com.example.storyround.room.RoomRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class ClientMessageDispatcherImplTest {
    private val registry = mockk<RoomRegistry>()
    private val dispatcher = ClientMessageDispatcherImpl(registry)
    private val result = RegistryResult.Error("CODE", "Message")

    @Test
    fun `given each client message when dispatched then the matching room action runs and its result is returned`() {
        every { registry.createRoom("Alex") } returns result
        every { registry.joinRoom("AB12", "Sam") } returns result
        every { registry.startGame("AB12", "host", null, RoomSettings(rounds = 3)) } returns result
        every { registry.leaveRoom("AB12", "sam") } returns result
        every { registry.submitPrompt("AB12", "host", "A door opened.") } returns result
        every { registry.submitContribution("AB12", "host", "A horse arrived.") } returns result
        every { registry.playAgain("AB12", "host") } returns result
        every { registry.reconnect("AB12", "host", "token") } returns result

        val messages =
            listOf(
                ClientMessage.CreateRoom("Alex"),
                ClientMessage.JoinRoom("AB12", "Sam"),
                ClientMessage.StartGame("AB12", "host", openingPlayerId = null, settings = RoomSettings(rounds = 3)),
                ClientMessage.LeaveRoom("AB12", "sam"),
                ClientMessage.SubmitPrompt("AB12", "host", "A door opened."),
                ClientMessage.SubmitContribution("AB12", "host", "A horse arrived."),
                ClientMessage.PlayAgain("AB12", "host"),
                ClientMessage.Reconnect("AB12", "host", "token"),
            )

        messages.forEach { message -> assertEquals(result, dispatcher.dispatch(message)) }
        verify(exactly = 1) {
            registry.createRoom("Alex")
            registry.joinRoom("AB12", "Sam")
            registry.startGame("AB12", "host", null, RoomSettings(rounds = 3))
            registry.leaveRoom("AB12", "sam")
            registry.submitPrompt("AB12", "host", "A door opened.")
            registry.submitContribution("AB12", "host", "A horse arrived.")
            registry.playAgain("AB12", "host")
            registry.reconnect("AB12", "host", "token")
        }
    }
}
