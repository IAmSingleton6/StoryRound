package com.example.storyround.testing.actor

import com.example.storyround.data.local.RemoteSession
import com.example.storyround.data.local.SettingsRemoteSessionStore
import com.example.storyround.data.local.SettingsStoryHistoryStore
import com.example.storyround.data.local.storageJson
import com.example.storyround.logging.RecordingLogger
import com.example.storyround.protocol.SavedStory
import com.example.storyround.testing.awaitInRealTime
import com.russhwolf.settings.MapSettings

class DeviceStorageRobot {
    internal val settings = MapSettings()
    private val sessions = SettingsRemoteSessionStore(settings, storageJson(), RecordingLogger())
    private val history = SettingsStoryHistoryStore(settings, storageJson(), RecordingLogger())

    fun saveSession(
        roomCode: String,
        playerId: String,
        sessionToken: String,
    ) = sessions.save(RemoteSession(roomCode, playerId, sessionToken))

    fun saveStories(vararg stories: SavedStory) = stories.forEach(history::save)

    suspend fun assertSessionSaved(
        roomCode: String,
        playerId: String,
    ) = awaitInRealTime("a saved session for $playerId in $roomCode") {
        sessions.load()?.let { it.roomCode == roomCode && it.playerId == playerId } == true
    }

    suspend fun assertNoSessionSaved() = awaitInRealTime("no saved session, but found ${sessions.load()}") { sessions.load() == null }

    suspend fun assertHistory(vararg prompts: String) =
        awaitInRealTime("saved stories ${prompts.toList()}, but found ${history.load().map { it.prompt }}") {
            history.load().map { it.prompt } == prompts.toList()
        }
}
