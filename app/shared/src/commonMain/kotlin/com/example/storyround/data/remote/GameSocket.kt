package com.example.storyround.data.remote

internal interface GameSocket {
    suspend fun connect(serverUrl: String): GameSocketConnection
}
