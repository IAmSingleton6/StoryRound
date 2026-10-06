package com.example.storyround.presentation.model

import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.SavedStory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameUiModelsTest {
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
            )

        val mapped = room.toRoomUi()

        assertEquals("ABCD", mapped.roomCode)
        assertEquals(
            "Alex",
            mapped.contributions
                .single()
                .player.name,
        )
        assertEquals(3, mapped.contributions.single().order)
        assertEquals(12_000, mapped.turn?.deadlineEpochMillis)
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
            )

        assertNull(room.toRoomUi().turn)
    }

    @Test
    fun `given saved story with departed player contribution when mapped then unknown contribution is omitted`() {
        val story =
            SavedStory(
                id = "story-1",
                prompt = "Prompt",
                createdAtEpochMillis = 1,
                players = listOf(Player("alex", "Alex", PlayerColour.Blue)),
                contributions = listOf(Contribution("unknown", "Ignored", turnNumber = 1)),
            )

        assertEquals(emptyList(), story.toStoryUi().contributions)
    }
}
