package com.example.storyround.presentation.mapper

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoomUiMapperImplTest {
    private val mapper = RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl(), StorySegmentMapperImpl())
    private val alex = Player(id = "alex", name = "Alex", colour = PlayerColour.Blue)
    private val sam = Player(id = "sam", name = "Sam", colour = PlayerColour.Green)
    private val playingRoom =
        RoomState(
            roomCode = "ABCD",
            hostId = alex.id,
            players = listOf(alex, sam),
            status = GameStatus.Playing,
            openingPlayerId = sam.id,
            prompt = "A door opened.",
            contributions = listOf(Contribution(alex.id, "A horse arrived.", turnNumber = 3)),
            currentPlayerId = alex.id,
            turnNumber = 4,
            turnDeadlineEpochMillis = 12_000,
            maxPlayers = 10,
            settings = RoomSettings(rounds = 3),
        )

    @Test
    fun `given an active room when mapped then players, contributions, the running turn and the prompt author are kept`() {
        val mapped = mapper.map(playingRoom)

        assertEquals("ABCD", mapped.roomCode)
        assertEquals(10, mapped.maxPlayers)
        assertEquals(
            "Alex",
            mapped.contributions
                .single()
                .player.name,
        )
        assertEquals(3, mapped.contributions.single().order)
        assertEquals(12_000, mapped.turn?.deadlineEpochMillis)
        assertEquals("Sam", mapped.promptAuthor?.name)
        assertEquals("Alex", mapped.host?.name)
    }

    @Test
    fun `given an active room when mapped then its story is laid out with each writer's share`() {
        val mapped = mapper.map(playingRoom)

        assertEquals(listOf("A door opened." to "Sam", "A horse arrived." to "Alex"), mapped.segments.map { it.text to it.author?.name })
        assertEquals(listOf("Alex" to 1, "Sam" to 1), mapped.writers.map { it.player.name to it.pieces })
    }

    @Test
    fun `given a room without a chosen opener when mapped then the host is credited with the prompt`() {
        assertEquals("Alex", mapper.map(playingRoom.copy(openingPlayerId = null)).promptAuthor?.name)
    }

    @Test
    fun `given a room without complete turn data when mapped then it has no running turn`() {
        assertNull(mapper.map(playingRoom.copy(turnDeadlineEpochMillis = null)).turn)
    }
}
