package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.StorySegmentUi
import com.example.storyround.presentation.model.StoryWriterUi
import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals

class StorySegmentMapperImplTest {
    private val mapper = StorySegmentMapperImpl()
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    private val sam = PlayerUi("sam", "Sam", PlayerColour.Green)

    @Test
    fun `given a prompt and contributions when laid out then the prompt comes first and blank pieces are skipped`() {
        // When
        val segments =
            mapper.segments(
                prompt = "  A door opened. ",
                promptAuthor = alex,
                contributions =
                    listOf(
                        ContributionUi(sam, "A horse arrived.", 0),
                        ContributionUi(alex, "   ", 1),
                        ContributionUi(sam, "It wore a hat.", 2),
                    ),
            )

        // Then
        assertEquals(
            listOf(
                StorySegmentUi("A door opened.", alex),
                StorySegmentUi("A horse arrived.", sam),
                StorySegmentUi("It wore a hat.", sam),
            ),
            segments,
        )
    }

    @Test
    fun `given no prompt yet when laid out then the story is empty`() {
        // When
        val segments = mapper.segments(prompt = "", promptAuthor = alex, contributions = emptyList())

        // Then
        assertEquals(emptyList(), segments)
    }

    @Test
    fun `given the pieces of a story when writers are counted then every player is listed in order with their share`() {
        // Given
        val jo = PlayerUi("jo", "Jo", PlayerColour.Pink)
        val segments = listOf(StorySegmentUi("One.", alex), StorySegmentUi("Two.", sam), StorySegmentUi("Three.", alex))

        // When
        val writers = mapper.writers(listOf(alex, sam, jo), segments)

        // Then
        assertEquals(listOf(StoryWriterUi(alex, 2), StoryWriterUi(sam, 1), StoryWriterUi(jo, 0)), writers)
    }
}
