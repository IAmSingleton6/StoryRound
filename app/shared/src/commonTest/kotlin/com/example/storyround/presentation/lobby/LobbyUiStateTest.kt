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
    fun `given the current player when hosting is checked then only the host sees host controls`() {
        // When
        val guestIsHost = lobby.isHost
        val hostIsHost = lobby.copy(currentPlayerId = alex.id).isHost

        // Then
        assertFalse(guestIsHost)
        assertTrue(hostIsHost)
    }

    @Test
    fun `given seats taken when open seats are counted then they never drop below zero`() {
        // When
        val openSeats = lobby.openSeats
        val openSeatsWhenOverfull = lobby.copy(maxPlayers = 1).openSeats

        // Then
        assertEquals(8, openSeats)
        assertEquals(0, openSeatsWhenOverfull)
    }

    @Test
    fun `given players and open seats when seats are read then each player is seated and the open seats share one tile`() {
        // When
        val seats = lobby.seats

        // Then
        assertEquals(
            listOf(
                LobbySeat.Taken(alex, isHost = true, isCurrentPlayer = false),
                LobbySeat.Taken(sam, isHost = false, isCurrentPlayer = true),
                LobbySeat.Open(8),
            ),
            seats,
        )
    }

    @Test
    fun `given a full lobby when seats are read then there is no open seats tile`() {
        // When
        val seats = lobby.copy(maxPlayers = 2).seats

        // Then
        assertEquals(listOf(alex, sam), seats.map { (it as LobbySeat.Taken).player })
    }
}
