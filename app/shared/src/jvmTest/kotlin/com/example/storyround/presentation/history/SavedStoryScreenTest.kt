package com.example.storyround.presentation.history

import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.savedStory
import kotlin.test.Test

internal class SavedStoryScreenTest :
    ComponentTest<SavedStoryUiState, SavedStoryScreenTest.Navigation>({ state, onAction ->
        SavedStoryScreen(state, onBack = { onAction(SavedStoryScreenTest.Navigation.Back) })
    }) {
    @Test
    fun `should show the saved story's text when it is opened`() =
        componentTest {
            GIVEN("the story \"A door opened. A horse arrived.\" is saved") {
                useState(savedStory())
            }

            WHEN("it is opened") {
                show()
            }

            THEN("the whole story is shown") {
                savedStoryRobot.assertStoryShown("A door opened. A horse arrived.")
            }
        }

    @Test
    fun `should list each writer's pieces when it is opened`() =
        componentTest {
            GIVEN("Alex and Sam wrote one piece each") {
                useState(savedStory())
            }

            WHEN("the saved story is opened") {
                show()
            }

            THEN("both writers are listed with one piece") {
                savedStoryRobot.assertWriterShown("Alex", pieces = 1)
                savedStoryRobot.assertWriterShown("Sam", pieces = 1)
            }
        }

    @Test
    fun `should go back to history when the back pill is pressed`() =
        componentTest {
            GIVEN("a saved story is open") {
                useState(savedStory())
                show()
            }

            WHEN("the player presses the back pill") {
                savedStoryRobot.tapBack()
            }

            THEN("history is reopened") {
                actions.assertEmitted(Navigation.Back)
            }
        }

    @Test
    fun `should go back to history when back to history is pressed`() =
        componentTest {
            GIVEN("a saved story is open") {
                useState(savedStory())
                show()
            }

            WHEN("the player presses back to history") {
                savedStoryRobot.tapBackToHistory()
            }

            THEN("history is reopened") {
                actions.assertEmitted(Navigation.Back)
            }
        }

    enum class Navigation {
        Back,
    }
}
