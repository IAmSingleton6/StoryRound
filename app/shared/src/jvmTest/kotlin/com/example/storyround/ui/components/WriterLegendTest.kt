package com.example.storyround.ui.components

import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.base.ComponentTest
import com.example.storyround.testing.fixtures.alex
import com.example.storyround.testing.fixtures.sam
import kotlin.test.Test

class WriterLegendTest : ComponentTest<String?, Nothing>({ viewerId, _ -> WriterLegend(writers, currentPlayerId = viewerId) }) {
    @Test
    fun `should list each writer with their pieces when the legend is shown`() =
        componentTest {
            GIVEN("Alex wrote two pieces and Sam one, viewed by neither") {
                useState(null)
            }

            WHEN("the legend is shown") {
                show()
            }

            THEN("each writer is listed with their pieces") {
                writerLegendRobot.assertWriterShown("Alex", pieces = 2)
                writerLegendRobot.assertWriterShown("Sam", pieces = 1)
            }
        }

    @Test
    fun `should mark the viewer's own chip as theirs when they wrote part of the story`() =
        componentTest {
            GIVEN("Alex is viewing") {
                useState(alex.id)
            }

            WHEN("the legend is shown") {
                show()
            }

            THEN("Alex's chip reads Alex (you)") {
                writerLegendRobot.assertWriterShown("Alex (you)", pieces = 2)
            }
        }

    private companion object {
        val writers = listOf(StoryWriterUi(alex, 2), StoryWriterUi(sam, 1))
    }
}
