package com.example.storyround.ui.components

import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.alex
import com.example.storyround.testing.fixtures.sam
import kotlin.test.Test

class StoryTextTest : ComponentTest<String?, Nothing>({ newestTag, _ -> StoryText(segments, newestTag = newestTag) }) {
    @Test
    fun `should read the whole story as one passage when it is shown`() =
        componentTest {
            GIVEN("the story has two pieces and no tag") {
                useState(null)
            }

            WHEN("the story is shown") {
                show()
            }

            THEN("screen readers hear the pieces joined by a space") {
                storyTextRobot.assertStoryShown("A door opened. A horse arrived.")
            }
        }

    @Test
    fun `should tag the newest piece when a tag is given`() =
        componentTest {
            GIVEN("the newest piece is this player's own") {
                useState("YOU")
            }

            WHEN("the story is shown") {
                show()
            }

            THEN("the newest piece is tagged YOU") {
                storyTextRobot.assertNewestTag("YOU")
            }
        }

    @Test
    fun `should not tag any piece when no tag is given`() =
        componentTest {
            GIVEN("there is no newest-piece tag, as on a finished story") {
                useState(null)
            }

            WHEN("the story is shown") {
                show()
            }

            THEN("no piece is tagged") {
                storyTextRobot.assertNoNewestTag("SAM")
            }
        }

    private companion object {
        val segments = listOf(StorySegmentUi("A door opened.", alex), StorySegmentUi("A horse arrived.", sam))
    }
}
