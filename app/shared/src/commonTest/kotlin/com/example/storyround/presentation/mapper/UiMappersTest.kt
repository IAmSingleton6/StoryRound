package com.example.storyround.presentation.mapper

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UiMappersTest {
    private val playerMapper = PlayerUiMapperImpl()
    private val contributionMapper = ContributionUiMapperImpl()
    private val roomMapper = RoomUiMapperImpl(playerMapper, contributionMapper)
    private val savedStoryMapper = SavedStoryUiMapperImpl(playerMapper, contributionMapper)

    @Test
    fun `given protocol player when mapped then UI preserves display data`() {
        val player = Player(id = "alex", name = "Alex", colour = PlayerColour.Blue)

        val mapped = playerMapper.map(player)

        assertEquals("alex", mapped.id)
        assertEquals("Alex", mapped.name)
        assertEquals(PlayerColour.Blue, mapped.colour)
    }

    @Test
    fun `given contributions with an unknown player when mapped then unknown contribution is omitted`() {
        val alex = playerMapper.map(Player("alex", "Alex", PlayerColour.Blue))

        val mapped = contributionMapper.map(listOf(Contribution("unknown", "Ignored", 1)), mapOf(alex.id to alex))

        assertEquals(emptyList(), mapped)
    }

    @Test
    fun `given active room when mapped then UI keeps player contribution and deadline`() {
        val alex = Player(id = "alex", name = "Alex", colour = PlayerColour.Blue)
        val room =
            RoomState(
                roomCode = "ABCD",
                hostId = alex.id,
                players = listOf(alex),
                status = GameStatus.Playing,
                prompt = "A door opened.",
                contributions = listOf(Contribution(alex.id, "A horse arrived.", turnNumber = 3)),
                currentPlayerId = alex.id,
                turnNumber = 4,
                turnDeadlineEpochMillis = 12_000,
                maxPlayers = 10,
                maxRounds = 3,
            )

        val mapped = roomMapper.map(room)

        assertEquals("ABCD", mapped.roomCode)
        assertEquals(
            "Alex",
            mapped.contributions
                .single()
                .player.name,
        )
        assertEquals(3, mapped.contributions.single().order)
        assertEquals(12_000, mapped.turn?.deadlineEpochMillis)
        assertEquals("Alex", mapped.promptAuthor?.name)
    }

    @Test
    fun `given room without complete turn data when mapped then UI has no turn`() {
        val room =
            RoomState(
                roomCode = "ABCD",
                hostId = "alex",
                players = listOf(Player("alex", "Alex", PlayerColour.Blue)),
                status = GameStatus.Playing,
                currentPlayerId = "alex",
                maxPlayers = 10,
                maxRounds = 3,
            )

        assertNull(roomMapper.map(room).turn)
    }

    @Test
    fun `given saved story with departed player contribution when mapped then unknown contribution is omitted`() {
        val story =
            SavedStory(
                id = "story-1",
                prompt = "Prompt",
                promptPlayerId = "alex",
                createdAtEpochMillis = 1,
                players = listOf(Player("alex", "Alex", PlayerColour.Blue)),
                contributions = listOf(Contribution("unknown", "Ignored", turnNumber = 1)),
            )

        val mapped = savedStoryMapper.map(story)

        assertEquals(emptyList(), mapped.contributions)
        assertEquals("Alex", mapped.promptAuthor?.name)
    }
}
