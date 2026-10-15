package com.example.storyround.ui.preview

import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.protocol.PlayerColour

internal object PreviewPlayers {
    val alex = PlayerUi("alex", "Alex", PlayerColour.Blue)
    val sam = PlayerUi("sam", "Sam", PlayerColour.Yellow)
    val priya = PlayerUi("priya", "Priya", PlayerColour.Green)
    val jordan = PlayerUi("jordan", "Jordan", PlayerColour.Purple)

    val all = listOf(alex, sam, priya, jordan)

    val allColours =
        PlayerColour.entries.mapIndexed { index, colour ->
            PlayerUi(id = "player-$index", name = colour.name, colour = colour)
        }
}
