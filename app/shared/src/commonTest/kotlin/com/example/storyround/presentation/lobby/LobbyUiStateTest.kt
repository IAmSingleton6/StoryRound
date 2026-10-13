package com.example.storyround.presentation.lobby

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LobbyUiStateTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)
    private val lobby = LobbyUiState(players = listOf(alex, sam), maxPlayers = 10, hostId = alex.id, currentPlayerId = sam.id)

    @Test
    fun `given this player and another when names are shown then this player reads as You`() {
        assertEquals("You", lobby.displayName(sam))
        assertEquals("Alex", lobby.displayName(alex))
    }

    @Test
    fun `given the current player when hosting is checked then only the host sees host controls`() {
        assertFalse(lobby.isHost)
        assertTrue(lobby.copy(currentPlayerId = alex.id).isHost)
    }

    @Test
    fun `given seats taken when open seats are counted then they never drop below zero`() {
        assertEquals(8, lobby.openSeats)
        assertEquals(0, lobby.copy(maxPlayers = 1).openSeats)
    }
}
