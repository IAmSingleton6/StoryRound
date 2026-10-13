package com.example.storyround.presentation.mapper

import com.example.storyround.presentation.model.ContributionUi
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.Contribution
import com.example.storyround.protocol.PlayerColour
import kotlin.test.Test
import kotlin.test.assertEquals

class ContributionUiMapperImplTest {
    private val mapper = ContributionUiMapperImpl()
    private val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)

    @Test
    fun `given contributions by known players when mapped then each keeps its author, text and order`() {
        val mapped = mapper.map(listOf(Contribution(alex.id, "A horse arrived.", 3)), mapOf(alex.id to alex))

        assertEquals(listOf(ContributionUi(alex, "A horse arrived.", 3)), mapped)
    }

    @Test
    fun `given a contribution by a player who is no longer listed when mapped then it is omitted`() {
        assertEquals(emptyList(), mapper.map(listOf(Contribution("unknown", "Ignored", 1)), mapOf(alex.id to alex)))
    }
}
