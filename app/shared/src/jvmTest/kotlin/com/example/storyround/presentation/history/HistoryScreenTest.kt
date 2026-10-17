package com.example.storyround.presentation.history

import com.example.storyround.presentation.model.UiText
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.savedDoorStory
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.error_reconnecting
import kotlin.test.Test

internal class HistoryScreenTest : ComponentTest<HistoryUiState, HistoryAction>({ state, onAction -> HistoryScreen(state, onAction) }) {
    @Test
    fun `should summarise each saved story on its card`() =
        componentTest {
            GIVEN("one story with two players and three contributions is saved") {
                useState(HistoryUiState.Content(listOf(savedDoorStory)))
            }

            WHEN("story history is shown") {
                show()
            }

            THEN("its card shows the prompt and its size") {
                historyRobot.assertStoriesShown("A door opened.")
                historyRobot.assertStorySummary("2 players · 3 contributions")
            }
        }

    @Test
    fun `should emit open story when a card is pressed`() =
        componentTest {
            GIVEN("story history is showing a saved story") {
                useState(HistoryUiState.Content(listOf(savedDoorStory)))
                show()
            }

            WHEN("the player presses its card") {
                historyRobot.tapStory("A door opened.")
            }

            THEN("that story is opened") {
                actions.assertEmitted(HistoryAction.OpenStory("story-1"))
            }
        }

    @Test
    fun `should emit back when back is pressed`() =
        componentTest {
            GIVEN("story history is showing a saved story") {
                useState(HistoryUiState.Content(listOf(savedDoorStory)))
                show()
            }

            WHEN("the player presses back") {
                historyRobot.tapBack()
            }

            THEN("history is closed") {
                actions.assertEmitted(HistoryAction.Back)
            }
        }

    @Test
    fun `should explain there are no stories when history is empty`() =
        componentTest {
            GIVEN("no stories are saved") {
                useState(HistoryUiState.Content(emptyList()))
            }

            WHEN("story history is shown") {
                show()
            }

            THEN("it explains stories are saved here once a game finishes") {
                historyRobot.assertEmpty()
            }
        }

    @Test
    fun `should emit back when the empty history's back button is pressed`() =
        componentTest {
            GIVEN("empty story history is showing") {
                useState(HistoryUiState.Content(emptyList()))
                show()
            }

            WHEN("the player presses back") {
                historyRobot.tapEmptyStateBack()
            }

            THEN("history is closed") {
                actions.assertEmitted(HistoryAction.Back)
            }
        }

    @Test
    fun `should announce the error above the saved stories when the connection has a problem`() =
        componentTest {
            GIVEN("the app is reconnecting while a story is saved") {
                useState(HistoryUiState.Content(listOf(savedDoorStory), error = UiText.Resource(Res.string.error_reconnecting)))
            }

            WHEN("story history is shown") {
                show()
            }

            THEN("the error is announced and the story is still readable") {
                historyRobot.assertErrorShown("Reconnecting...")
                historyRobot.assertStoriesShown("A door opened.")
            }
        }
}
