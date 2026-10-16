package com.example.storyround.presentation.mapper

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.model.GameState
import com.example.storyround.presentation.story.StoryLocalState
import com.example.storyround.presentation.story.StoryUiState
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.UserTextSanitizerImpl
import com.example.storyround.protocol.UserTextValidatorImpl
import com.example.storyround.rules.TurnRulesImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoryUiStateMapperImplTest {
    private val configuration = GameConfiguration(firstTurnMillis = 15_000L, turnReductionMillis = 1_000L, minimumTurnMillis = 5_000L)
    private val mapper =
        StoryUiStateMapperImpl(
            RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl(), StorySegmentMapperImpl()),
            TurnRulesImpl(configuration),
            UserTextValidatorImpl(UserTextSanitizerImpl()),
        )
    private val alex = Player("alex", "Alex", PlayerColour.Blue)
    private val sam = Player("sam", "Sam", PlayerColour.Yellow)

    @Test
    fun `given the third timed turn of two players and two rounds when mapped then its round and turn and timing are exposed`() {
        // Given
        val state = GameState(room = playingRoom(turnNumber = 2), playerId = alex.id)

        // When
        val uiState = assertIs<StoryUiState.Playing>(mapper.map(state, StoryLocalState(nowEpochMillis = 0L)))

        // Then
        assertEquals(2, uiState.round)
        assertEquals(3, uiState.turn)
        assertEquals(4, uiState.totalTurns)
        assertEquals(13_000L, uiState.turnDurationMillis)
        assertEquals("Sam", uiState.nextPlayer?.name)
        assertEquals(12_000L, uiState.nextTurnDurationMillis)
        assertEquals(listOf("A door opened.", "A horse arrived.", "It wore a hat."), uiState.segments.map { it.text })
    }

    @Test
    fun `given the final turn when mapped then there is no next writer`() {
        // Given
        val state = GameState(room = playingRoom(turnNumber = 3), playerId = alex.id)

        // When
        val uiState = assertIs<StoryUiState.Playing>(mapper.map(state, StoryLocalState(nowEpochMillis = 0L)))

        // Then
        assertNull(uiState.nextPlayer)
    }

    @Test
    fun `given a running turn when mapped then the time left is counted to the deadline and kept within the turn`() {
        // Given
        val state = GameState(room = playingRoom(turnNumber = 2, deadline = 20_000L), playerId = alex.id)

        // When
        val running = mapper.map(state, StoryLocalState(nowEpochMillis = 12_700L))
        val beforeTheTurn = mapper.map(state, StoryLocalState(nowEpochMillis = 0L))
        val pastTheDeadline = mapper.map(state, StoryLocalState(nowEpochMillis = 25_000L))

        // Then
        assertEquals(7_300L, assertIs<StoryUiState.Playing>(running).timeLeftMillis)
        assertEquals(13_000L, assertIs<StoryUiState.Playing>(beforeTheTurn).timeLeftMillis)
        assertEquals(0L, assertIs<StoryUiState.Playing>(pastTheDeadline).timeLeftMillis)
    }

    @Test
    fun `given drafts and a hand-over when mapped then they are shown with the room`() {
        // Given
        val prompt = GameState(room = promptRoom(), playerId = alex.id)
        val playing = GameState(room = playingRoom(turnNumber = 2), playerId = alex.id)
        val local =
            StoryLocalState(
                nowEpochMillis = 0L,
                openingPromptDraft = "A door",
                contributionDraft = "A hat",
                timeUpHandOverPlayerId = sam.id,
            )

        // When
        val openingPrompt = assertIs<StoryUiState.OpeningPrompt>(mapper.map(prompt, local))
        val turn = assertIs<StoryUiState.Playing>(mapper.map(playing, local))

        // Then
        assertEquals("A door", openingPrompt.openingPromptDraft)
        assertEquals("A hat", turn.contributionDraft)
        assertEquals("Sam", turn.timeUpHandOver?.name)
    }

    @Test
    fun `given a one word game when drafts are mapped then the word limit applies and a draft with a space breaks the rule`() {
        // Given
        val oneWord = RoomSettings(rounds = 2, oneWordPerTurn = true)
        val prompt = GameState(room = promptRoom().copy(settings = oneWord), playerId = alex.id)
        val playing = GameState(room = playingRoom(turnNumber = 2).copy(settings = oneWord), playerId = alex.id)
        val local = StoryLocalState(nowEpochMillis = 0L, openingPromptDraft = "Once ", contributionDraft = "a horse")

        // When
        val openingPrompt = assertIs<StoryUiState.OpeningPrompt>(mapper.map(prompt, local))
        val turn = assertIs<StoryUiState.Playing>(mapper.map(playing, local))

        // Then
        assertTrue(openingPrompt.oneWordPerTurn)
        assertEquals(30, openingPrompt.maxDraftLength)
        assertTrue(openingPrompt.isDraftValid)
        assertFalse(openingPrompt.breaksOneWordRule)
        assertFalse(turn.isDraftValid)
        assertTrue(turn.breaksOneWordRule)
    }

    @Test
    fun `given a free writing game when a sentence is mapped then it is valid within the story text limit`() {
        // Given
        val playing = GameState(room = playingRoom(turnNumber = 2), playerId = alex.id)
        val local = StoryLocalState(nowEpochMillis = 0L, contributionDraft = "A horse arrived.")

        // When
        val turn = assertIs<StoryUiState.Playing>(mapper.map(playing, local))

        // Then
        assertFalse(turn.oneWordPerTurn)
        assertEquals(280, turn.maxDraftLength)
        assertTrue(turn.isDraftValid)
        assertFalse(turn.breaksOneWordRule)
    }

    @Test
    fun `given no room when mapped then the story is loading`() {
        // When
        val uiState = mapper.map(GameState(), StoryLocalState(nowEpochMillis = 0L))

        // Then
        assertEquals(StoryUiState.Loading, uiState)
    }

    @Test
    fun `given play with no current writer when mapped then the story is loading`() {
        // Given
        val state = GameState(room = playingRoom(turnNumber = 2).copy(currentPlayerId = null), playerId = alex.id)

        // When
        val uiState = mapper.map(state, StoryLocalState(nowEpochMillis = 0L))

        // Then
        assertEquals(StoryUiState.Loading, uiState)
    }

    private fun promptRoom() =
        RoomState(
            roomCode = "AB12",
            hostId = alex.id,
            players = listOf(alex, sam),
            status = GameStatus.Prompt,
            openingPlayerId = alex.id,
            maxPlayers = 10,
            settings = RoomSettings(rounds = 2),
        )

    private fun playingRoom(
        turnNumber: Int,
        deadline: Long? = null,
    ) = RoomState(
        roomCode = "AB12",
        hostId = alex.id,
        players = listOf(alex, sam),
        status = GameStatus.Playing,
        openingPlayerId = alex.id,
        prompt = "A door opened.",
        contributions = listOf(Contribution(alex.id, "A horse arrived.", 0), Contribution(sam.id, "It wore a hat.", 1)),
        currentPlayerId = listOf(alex, sam)[turnNumber % 2].id,
        turnNumber = turnNumber,
        turnDeadlineEpochMillis = deadline,
        maxPlayers = 10,
        settings = RoomSettings(rounds = 2),
    )
}
