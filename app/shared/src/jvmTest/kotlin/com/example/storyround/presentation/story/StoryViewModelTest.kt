package com.example.storyround.presentation.story

import com.example.storyround.configuration.GameConfiguration
import com.example.storyround.domain.idea.StoryIdeaProvider
import com.example.storyround.domain.model.GameState
import com.example.storyround.domain.repository.GameRepository
import com.example.storyround.presentation.mapper.ContributionUiMapperImpl
import com.example.storyround.presentation.mapper.PlayerUiMapperImpl
import com.example.storyround.presentation.mapper.RoomUiMapperImpl
import com.example.storyround.presentation.mapper.StorySegmentMapperImpl
import com.example.storyround.presentation.mapper.StoryUiStateMapperImpl
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.PlayerColour
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.protocol.RoomState
import com.example.storyround.protocol.UserTextSanitizerImpl
import com.example.storyround.protocol.UserTextValidatorImpl
import com.example.storyround.rules.TurnRulesImpl
import com.example.storyround.time.TimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val alex = Player("alex", "Alex", PlayerColour.Blue)
    private val sam = Player("sam", "Sam", PlayerColour.Yellow)
    private val configuration = GameConfiguration(firstTurnMillis = 15_000L, turnReductionMillis = 1_000L, minimumTurnMillis = 5_000L)
    private val gameState = MutableStateFlow(GameState())
    private val gameRepository = mockk<GameRepository>(relaxed = true) { every { state } returns gameState }
    private val timeProvider = mockk<TimeProvider> { every { nowEpochMillis() } returns 0L }
    private val storyIdeaProvider = mockk<StoryIdeaProvider>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given the opening writer with a draft when they submit then the prompt is sent and the draft cleared`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = promptRoom(openingPlayer = alex), playerId = alex.id)
            val viewModel = viewModel()
            viewModel.onAction(StoryAction.OpeningPromptChanged("A door opened."))

            // When
            viewModel.onAction(StoryAction.SubmitOpeningPrompt)
            advanceUntilIdle()

            // Then
            val uiState = assertIs<StoryUiState.OpeningPrompt>(viewModel.uiState.value)
            assertTrue(uiState.isOpeningWriter)
            assertEquals("Alex", uiState.openingPlayer?.name)
            assertEquals("", uiState.openingPromptDraft)
            coVerify(exactly = 1) { gameRepository.submitPrompt("A door opened.") }
        }

    @Test
    fun `given another player opens and the socket drops when observed then this player waits and sees reconnecting`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = promptRoom(openingPlayer = alex), playerId = sam.id, isConnected = false)
            val viewModel = viewModel()

            // When
            viewModel.onAction(StoryAction.OpeningPromptChanged("I should not be able to send this."))
            advanceUntilIdle()

            // Then
            val uiState = assertIs<StoryUiState.OpeningPrompt>(viewModel.uiState.value)
            assertFalse(uiState.isOpeningWriter)
            assertFalse(uiState.canSubmitOpeningPrompt)
            assertEquals("Alex", uiState.openingPlayer?.name)
            assertTrue(uiState.isReconnecting)
        }

    @Test
    fun `given an opening draft when a random idea is requested then the draft is replaced with an opening idea`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = promptRoom(openingPlayer = alex), playerId = alex.id)
            coEvery { storyIdeaProvider.openingIdea("My own start") } returns "The goldfish won the lottery."
            val viewModel = viewModel()
            viewModel.onAction(StoryAction.OpeningPromptChanged("My own start"))

            // When
            viewModel.onAction(StoryAction.RandomOpeningPrompt)
            advanceUntilIdle()

            // Then
            val uiState = assertIs<StoryUiState.OpeningPrompt>(viewModel.uiState.value)
            assertEquals("The goldfish won the lottery.", uiState.openingPromptDraft)
            assertTrue(uiState.canSubmitOpeningPrompt)
        }

    @Test
    fun `given a draft longer than the limit when typed then it is cut to the story text limit`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = promptRoom(openingPlayer = alex), playerId = alex.id)
            val viewModel = viewModel()

            // When
            viewModel.onAction(StoryAction.OpeningPromptChanged("a".repeat(300)))
            advanceUntilIdle()

            // Then
            assertEquals(280, assertIs<StoryUiState.OpeningPrompt>(viewModel.uiState.value).openingPromptDraft.length)
        }

    @Test
    fun `given a one word game when a long draft is typed then it is cut to the word limit`() =
        runTest(dispatcher) {
            // Given
            val oneWord = RoomSettings(rounds = 2, oneWordPerTurn = true)
            gameState.value = GameState(room = promptRoom(openingPlayer = alex).copy(settings = oneWord), playerId = alex.id)
            val viewModel = viewModel()

            // When
            viewModel.onAction(StoryAction.OpeningPromptChanged("a".repeat(40)))
            advanceUntilIdle()

            // Then
            val uiState = assertIs<StoryUiState.OpeningPrompt>(viewModel.uiState.value)
            assertEquals(30, uiState.openingPromptDraft.length)
            assertTrue(uiState.canSubmitOpeningPrompt)
        }

    @Test
    fun `given the server accepts the prompt when play starts then the same state flow enters playing`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = promptRoom(openingPlayer = alex), playerId = alex.id)
            val viewModel = viewModel()
            advanceUntilIdle()

            // When
            gameState.value = GameState(room = playingRoom(turnNumber = 0), playerId = alex.id)
            advanceUntilIdle()

            // Then
            val uiState = assertIs<StoryUiState.Playing>(viewModel.uiState.value)
            assertEquals("A door opened.", uiState.room.prompt)
            assertEquals(0L, uiState.timeLeftMillis)
        }

    @Test
    fun `given your turn with a draft when it is submitted then the contribution is sent and the draft cleared`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = playingRoom(turnNumber = 0), playerId = alex.id)
            val viewModel = viewModel()
            runCurrent()
            viewModel.onAction(StoryAction.ContributionChanged("A horse appeared."))

            // When
            viewModel.onAction(StoryAction.SubmitContribution)
            runCurrent()

            // Then
            assertEquals("", assertIs<StoryUiState.Playing>(viewModel.uiState.value).contributionDraft)
            coVerify(exactly = 1) { gameRepository.submitContribution("A horse appeared.") }
        }

    @Test
    fun `given a turn that passes without a contribution when play moves on then the hand-over is announced briefly`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = playingRoom(turnNumber = 2), playerId = alex.id)
            val viewModel = viewModel()
            runCurrent()

            // When
            gameState.value = gameState.value.copy(room = playingRoom(turnNumber = 3))
            runCurrent()

            // Then
            assertEquals("Sam", assertIs<StoryUiState.Playing>(viewModel.uiState.value).timeUpHandOver?.name)
            advanceTimeBy(1_801)
            assertNull(assertIs<StoryUiState.Playing>(viewModel.uiState.value).timeUpHandOver)
        }

    @Test
    fun `given a turn that passes with a contribution when play moves on then nothing is announced`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = playingRoom(turnNumber = 2), playerId = alex.id)
            val viewModel = viewModel()
            runCurrent()

            // When
            val nextTurn = playingRoom(turnNumber = 3)
            val contributions = nextTurn.contributions + Contribution(alex.id, "Then it spoke.", 2)
            gameState.value = gameState.value.copy(room = nextTurn.copy(contributions = contributions))
            runCurrent()

            // Then
            assertNull(assertIs<StoryUiState.Playing>(viewModel.uiState.value).timeUpHandOver)
        }

    @Test
    fun `given your turn with a draft when a random idea is requested then it follows on from the latest piece`() =
        runTest(dispatcher) {
            // Given
            gameState.value = GameState(room = playingRoom(turnNumber = 2), playerId = alex.id)
            coEvery { storyIdeaProvider.continuationIdea("It wore a hat.", "Half a thought") } returns "Then came a knock at the door."
            val viewModel = viewModel()
            viewModel.onAction(StoryAction.ContributionChanged("Half a thought"))

            // When
            viewModel.onAction(StoryAction.RandomContribution)
            advanceUntilIdle()

            // Then
            assertEquals("Then came a knock at the door.", assertIs<StoryUiState.Playing>(viewModel.uiState.value).contributionDraft)
        }

    private fun TestScope.viewModel(): StoryViewModel {
        val viewModel =
            StoryViewModel(
                gameRepository = gameRepository,
                timeProvider = timeProvider,
                storyUiStateMapper =
                    StoryUiStateMapperImpl(
                        RoomUiMapperImpl(PlayerUiMapperImpl(), ContributionUiMapperImpl(), StorySegmentMapperImpl()),
                        TurnRulesImpl(configuration),
                        UserTextValidatorImpl(UserTextSanitizerImpl()),
                    ),
                storyIdeaProvider = storyIdeaProvider,
            )
        backgroundScope.launch { viewModel.uiState.collect() }
        return viewModel
    }

    private fun promptRoom(openingPlayer: Player) =
        RoomState(
            roomCode = "AB12",
            hostId = alex.id,
            players = listOf(alex, sam),
            status = GameStatus.Prompt,
            openingPlayerId = openingPlayer.id,
            maxPlayers = 10,
            settings = RoomSettings(rounds = 2),
        )

    private fun playingRoom(turnNumber: Int) =
        RoomState(
            roomCode = "AB12",
            hostId = alex.id,
            players = listOf(alex, sam),
            status = GameStatus.Playing,
            openingPlayerId = alex.id,
            prompt = "A door opened.",
            contributions = listOf(Contribution(alex.id, "A horse arrived.", 0), Contribution(sam.id, "It wore a hat.", 1)),
            currentPlayerId = listOf(alex, sam)[turnNumber % 2].id,
            turnNumber = turnNumber,
            turnDeadlineEpochMillis = null,
            maxPlayers = 10,
            settings = RoomSettings(rounds = 2),
        )
}
