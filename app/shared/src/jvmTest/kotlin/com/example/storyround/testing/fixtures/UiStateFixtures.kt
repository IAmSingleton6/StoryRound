package com.example.storyround.testing.fixtures

import com.example.storyround.presentation.finished.FinishedUiState
import com.example.storyround.presentation.history.SavedStoryUiState
import com.example.storyround.presentation.lobby.LobbyUiState
import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.RoomUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryUi
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.presentation.model.TurnUi
import com.example.storyround.presentation.story.StoryUiState
import com.example.storyround.protocol.MAX_STORY_TEXT_LENGTH
import com.example.storyround.protocol.MAX_WORD_LENGTH
import com.example.storyround.protocol.PlayerColour

internal val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
internal val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)

internal fun hostLobby(
    players: List<PlayerUi> = listOf(alex, sam),
    maxRounds: Int = 1,
    canDecreaseRounds: Boolean = false,
    canIncreaseRounds: Boolean = true,
    oneWordPerTurn: Boolean = false,
    isReconnecting: Boolean = false,
) = LobbyUiState(
    roomCode = "7F2K",
    players = players,
    maxPlayers = 10,
    hostId = alex.id,
    currentPlayerId = alex.id,
    maxRounds = maxRounds,
    canDecreaseRounds = canDecreaseRounds,
    canIncreaseRounds = canIncreaseRounds,
    totalTurns = maxRounds * players.size,
    oneWordPerTurn = oneWordPerTurn,
    isReconnecting = isReconnecting,
)

internal fun guestLobby() = hostLobby().copy(currentPlayerId = sam.id)

internal fun openingPrompt(
    isOpeningWriter: Boolean,
    draft: String = "",
    openingPlayer: PlayerUi? = alex,
    isReconnecting: Boolean = false,
    oneWordPerTurn: Boolean = false,
    isDraftValid: Boolean = draft.isNotBlank(),
) = StoryUiState.OpeningPrompt(
    room = storyRoom(prompt = "", turn = null),
    openingPromptDraft = draft,
    openingPlayer = openingPlayer,
    isOpeningWriter = isOpeningWriter,
    isReconnecting = isReconnecting,
    oneWordPerTurn = oneWordPerTurn,
    maxDraftLength = draftLimit(oneWordPerTurn),
    isDraftValid = isDraftValid,
)

internal fun alexWritingTurnThree(
    viewerId: String,
    timeLeftMillis: Long,
    draft: String = "",
    nextPlayer: PlayerUi? = sam,
    timeUpHandOver: PlayerUi? = null,
    isReconnecting: Boolean = false,
    oneWordPerTurn: Boolean = false,
    isDraftValid: Boolean = draft.isNotBlank(),
) = StoryUiState.Playing(
    room = storyRoom(prompt = "A door opened.", turn = TurnUi(alex, number = 2, deadlineEpochMillis = TURN_DEADLINE)),
    activePlayer = alex,
    playerId = viewerId,
    timeLeftMillis = timeLeftMillis,
    contributionDraft = draft,
    segments = listOf(StorySegmentUi("A door opened.", alex)),
    round = 2,
    turn = 3,
    totalTurns = 4,
    turnDurationMillis = 13_000L,
    nextPlayer = nextPlayer,
    nextTurnDurationMillis = 12_000L,
    timeUpHandOver = timeUpHandOver,
    isReconnecting = isReconnecting,
    oneWordPerTurn = oneWordPerTurn,
    maxDraftLength = draftLimit(oneWordPerTurn),
    isDraftValid = isDraftValid,
)

internal fun finishedStory(
    viewerId: String = alex.id,
    isReconnecting: Boolean = false,
) = FinishedUiState(
    segments = listOf(StorySegmentUi("A door opened.", alex), StorySegmentUi("A horse arrived.", sam)),
    writers = listOf(StoryWriterUi(alex, 1), StoryWriterUi(sam, 1)),
    contributionCount = 1,
    host = alex,
    playerId = viewerId,
    canPlayAgain = viewerId == alex.id,
    isReconnecting = isReconnecting,
)

internal val savedDoorStory =
    StoryUi(
        id = "story-1",
        prompt = "A door opened.",
        players = listOf(alex, sam),
        contributions = List(3) { turn -> ContributionUi(if (turn % 2 == 0) sam else alex, "Piece $turn.", turn) },
        promptAuthor = alex,
        writers = listOf(StoryWriterUi(alex, 2), StoryWriterUi(sam, 2)),
    )

internal fun savedStory() =
    SavedStoryUiState(
        segments = listOf(StorySegmentUi("A door opened.", alex), StorySegmentUi("A horse arrived.", sam)),
        writers = listOf(StoryWriterUi(alex, 1), StoryWriterUi(sam, 1)),
    )

private const val TURN_DEADLINE = 20_000L

private fun draftLimit(oneWordPerTurn: Boolean) = if (oneWordPerTurn) MAX_WORD_LENGTH else MAX_STORY_TEXT_LENGTH

private fun storyRoom(
    prompt: String,
    turn: TurnUi?,
) = RoomUi(
    roomCode = "AB12",
    hostId = alex.id,
    players = listOf(alex, sam),
    maxPlayers = 10,
    prompt = prompt,
    contributions = emptyList(),
    turn = turn,
    promptAuthor = alex,
    maxRounds = 2,
)
