package com.example.storyround.presentation.story

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayingUiStateTest {
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)

    @Test
    fun `given time left when the countdown is read then it rounds up to one decimal place`() {
        // When
        val exact = playing(timeLeftMillis = 7_300L).timeLeftText
        val roundedUp = playing(timeLeftMillis = 7_201L).timeLeftText
        val none = playing(timeLeftMillis = 0L).timeLeftText

        // Then
        assertEquals("7.3", exact)
        assertEquals("7.3", roundedUp)
        assertEquals("0.0", none)
    }

    @Test
    fun `given five seconds or less left when read then the turn is urgent`() {
        // When
        val justOverFiveSeconds = playing(timeLeftMillis = 5_001L).isUrgent
        val fiveSeconds = playing(timeLeftMillis = 5_000L).isUrgent

        // Then
        assertFalse(justOverFiveSeconds)
        assertTrue(fiveSeconds)
    }

    @Test
    fun `given time left in a turn when the fraction is read then it is the share of the turn remaining`() {
        // When
        val half = playing(timeLeftMillis = 6_500L).timeLeftFraction
        val withoutDuration = playing(timeLeftMillis = 6_500L).copy(turnDurationMillis = 0L).timeLeftFraction

        // Then
        assertEquals(0.5f, half)
        assertEquals(0f, withoutDuration)
    }

    @Test
    fun `given this and the next turn's durations when read in seconds then both are whole seconds`() {
        // When
        val state = playing()

        // Then
        assertEquals(13L, state.turnSeconds)
        assertEquals(12L, state.nextTurnSeconds)
    }

    @Test
    fun `given a story with pieces when the newest author is read then it is the last piece's writer`() {
        // When
        val author = playing(segments = listOf(StorySegmentUi("A door opened.", alex), StorySegmentUi("A horse.", sam))).newestAuthor
        val noPieces = playing(segments = emptyList()).newestAuthor

        // Then
        assertEquals(sam, author)
        assertNull(noPieces)
    }

    @Test
    fun `given a draft when read then it can only be submitted on your turn`() {
        // When
        val onYourTurn = playing(playerId = alex.id, draft = "A horse.").canSubmitContribution
        val onTheirTurn = playing(playerId = sam.id, draft = "A horse.").canSubmitContribution
        val blankDraft = playing(playerId = alex.id, draft = "  ").canSubmitContribution

        // Then
        assertTrue(onYourTurn)
        assertFalse(onTheirTurn)
        assertFalse(blankDraft)
    }

    @Test
    fun `given one word turns when the draft is read then only a draft with a space in the middle breaks the rule`() {
        // When
        val twoWords = playing(draft = "a horse", isDraftValid = false, oneWordPerTurn = true)
        val oneWord = playing(draft = "horse", isDraftValid = true, oneWordPerTurn = true)
        val empty = playing(draft = "", oneWordPerTurn = true)
        val freeWriting = playing(draft = "a horse", isDraftValid = false)

        // Then
        assertTrue(twoWords.breaksOneWordRule)
        assertFalse(twoWords.canSubmitContribution)
        assertFalse(oneWord.breaksOneWordRule)
        assertTrue(oneWord.canSubmitContribution)
        assertFalse(empty.breaksOneWordRule)
        assertFalse(freeWriting.breaksOneWordRule)
    }

    private fun playing(
        playerId: String = alex.id,
        timeLeftMillis: Long = 10_000L,
        segments: List<StorySegmentUi> = listOf(StorySegmentUi("A door opened.", alex)),
        draft: String = "",
        isDraftValid: Boolean = draft.isNotBlank(),
        oneWordPerTurn: Boolean = false,
    ) = StoryUiState.Playing(
        room =
            RoomUi(
                roomCode = "AB12",
                hostId = alex.id,
                players = listOf(alex, sam),
                maxPlayers = 10,
                prompt = "A door opened.",
                contributions = emptyList(),
                turn = TurnUi(alex, number = 2, deadlineEpochMillis = 20_000L),
                maxRounds = 2,
            ),
        activePlayer = alex,
        playerId = playerId,
        timeLeftMillis = timeLeftMillis,
        contributionDraft = draft,
        segments = segments,
        turnDurationMillis = 13_000L,
        nextPlayer = sam,
        round = 2,
        turn = 3,
        totalTurns = 4,
        nextTurnDurationMillis = 12_000L,
        oneWordPerTurn = oneWordPerTurn,
        isDraftValid = isDraftValid,
    )
}
