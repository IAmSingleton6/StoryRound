package com.example.storyround.room

internal interface RoomIdentityGenerator {
    fun generateRoomCode(): String

    fun generatePlayerId(): String

    fun generateSessionToken(): String
}
