package com.example.storyround.flow

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.AppFlowTest
import com.example.storyround.testing.fixtures.ROOM_CODE
import com.example.storyround.testing.fixtures.alexPlayer
import com.example.storyround.testing.fixtures.lobbyRoom
import com.example.storyround.testing.fixtures.savedStoryOf
import com.example.storyround.testing.fixtures.tokenOf
import com.example.storyround.testing.scenario.appOnHome
import kotlin.test.Test

class HistoryFlowTest : AppFlowTest() {
    @Test
    fun `should list saved stories newest first when history is opened`() =
        appFlowTest {
            GIVEN("an older and a newer story are saved and the app is on Home") {
                storageRobot.saveStories(
                    savedStoryOf("An older story.", createdAtEpochMillis = 1L),
                    savedStoryOf("A newer story.", createdAtEpochMillis = 2L),
                )
                appOnHome()
            }

            WHEN("Alex opens story history") {
                homeRobot.tapHistory()
            }

            THEN("the newer story is listed first") {
                historyRobot.assertStoriesInOrder("A newer story.", "An older story.")
            }
        }

    @Test
    fun `should open a saved story when its card is pressed`() =
        appFlowTest {
            GIVEN("story history is open with one saved story") {
                storageRobot.saveStories(savedStoryOf("A door opened."))
                appOnHome()
                homeRobot.tapHistory()
            }

            WHEN("Alex presses the story's card") {
                historyRobot.tapStory("A door opened.")
            }

            THEN("the whole saved story is shown") {
                savedStoryRobot.assertStoryShown("A door opened. Then it rained.")
            }
        }

    @Test
    fun `should return to history when back is pressed on a saved story`() =
        appFlowTest {
            GIVEN("a saved story is open") {
                storageRobot.saveStories(savedStoryOf("A door opened."))
                appOnHome()
                homeRobot.tapHistory()
                historyRobot.tapStory("A door opened.")
            }

            WHEN("Alex presses back") {
                savedStoryRobot.tapBack()
            }

            THEN("story history is shown again") {
                historyRobot.assertStoriesShown("A door opened.")
            }
        }

    @Test
    fun `should return home when back is pressed in history and there is no room`() =
        appFlowTest {
            GIVEN("story history is open from Home") {
                storageRobot.saveStories(savedStoryOf("A door opened."))
                appOnHome()
                homeRobot.tapHistory()
            }

            WHEN("Alex presses back") {
                historyRobot.tapBack()
            }

            THEN("Home is shown") {
                homeRobot.assertShown()
            }
        }

    @Test
    fun `should return home when back is pressed on empty history`() =
        appFlowTest {
            GIVEN("empty story history is open from Home") {
                appOnHome()
                homeRobot.tapHistory()
                historyRobot.assertEmpty()
            }

            WHEN("Alex presses back") {
                historyRobot.tapEmptyStateBack()
            }

            THEN("Home is shown") {
                homeRobot.assertShown()
            }
        }

    @Test
    fun `should return to the room when back is pressed in history after the room was restored`() =
        appFlowTest {
            GIVEN("Alex opened history while the app was resuming a lobby session, and the server then restored it") {
                storageRobot.saveSession(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer))
                appRobot.launch()
                homeRobot.tapHistory()
                serverRobot.awaitSent(ClientMessage.Reconnect(ROOM_CODE, alexPlayer.id, tokenOf(alexPlayer)))
                serverRobot.resume(alexPlayer, lobbyRoom())
                historyRobot.assertShown()
            }

            WHEN("Alex presses back") {
                historyRobot.tapBack()
            }

            THEN("Alex is in the lobby") {
                lobbyRobot.assertRoomCodeShown(ROOM_CODE)
            }
        }
}
