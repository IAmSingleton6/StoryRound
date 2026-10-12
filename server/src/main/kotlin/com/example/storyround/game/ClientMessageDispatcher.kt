package com.example.storyround.game

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.room.RegistryResult

internal interface ClientMessageDispatcher {
    fun dispatch(message: ClientMessage): RegistryResult
}
