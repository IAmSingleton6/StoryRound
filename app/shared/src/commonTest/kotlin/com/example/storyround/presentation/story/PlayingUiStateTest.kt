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
        assertEquals("7.3", playing(timeLeftMillis = 7_300L).timeLeftText)
        assertEquals("7.3", playing(timeLeftMillis = 7_201L).timeLeftText)
        assertEquals("0.0", playing(timeLeftMillis = 0L).timeLeftText)
    }

    @Test
    fun `given five seconds or less left when read then the turn is urgent`() {
        assertFalse(playing(timeLeftMillis = 5_001L).isUrgent)
        assertTrue(playing(timeLeftMillis = 5_000L).isUrgent)
    }

    @Test
    fun `given a following turn when the turn label is read then it names the next writer and their time`() {
        assertEquals("13s turn · next: Sam, 12s", playing(playerId = alex.id).nextTurnLabel)
        assertEquals("13s turn · next: you, 12s", playing(playerId = sam.id).nextTurnLabel)
        assertEquals("13s turn · final turn", playing(nextPlayer = null).nextTurnLabel)
    }

    @Test
    fun `given the newest piece when its tag is read then it names the author, or YOU for this player`() {
        assertEquals("ALEX", playing(playerId = sam.id).newestTag)
        assertEquals("YOU", playing(playerId = alex.id).newestTag)
        assertNull(playing(segments = emptyList()).newestTag)
    }

    @Test
    fun `given a draft when read then it can only be submitted on your turn and warns near the limit`() {
        assertTrue(playing(playerId = alex.id, draft = "A horse.").canSubmitContribution)
        assertFalse(playing(playerId = sam.id, draft = "A horse.").canSubmitContribution)
        assertFalse(playing(playerId = alex.id, draft = "  ").canSubmitContribution)
        assertTrue(playing(draft = "a".repeat(260)).isDraftNearLimit)
    }

    @Test
    fun `given one word turns when the draft is read then a broken word is explained and the warning comes near the word limit`() {
        assertEquals("One word only, no spaces", playing(draft = "a horse", isDraftValid = false, oneWordPerTurn = true).draftError)
        assertNull(playing(draft = "horse", isDraftValid = true, oneWordPerTurn = true).draftError)
        assertNull(playing(draft = "", oneWordPerTurn = true).draftError)
        assertNull(playing(draft = "a horse", isDraftValid = false).draftError)
        assertTrue(playing(draft = "a".repeat(25), oneWordPerTurn = true).isDraftNearLimit)
        assertFalse(playing(draft = "a".repeat(24), oneWordPerTurn = true).isDraftNearLimit)
    }

    private fun playing(
        playerId: String = alex.id,
        timeLeftMillis: Long = 10_000L,
        nextPlayer: PlayerUi? = sam,
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
        playerId = playerId,
        nowEpochMillis = 20_000L - timeLeftMillis,
        contributionDraft = draft,
        segments = segments,
        turnDurationMillis = 13_000L,
        nextPlayer = nextPlayer,
        nextTurnDurationMillis = 12_000L,
        oneWordPerTurn = oneWordPerTurn,
        isDraftValid = isDraftValid,
    )
}
