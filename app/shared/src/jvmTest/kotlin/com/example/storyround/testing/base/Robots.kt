package com.example.storyround.testing.base

import androidx.compose.ui.test.ComposeUiTest
import com.example.storyround.testing.robot.FinishedRobot
import com.example.storyround.testing.robot.HistoryRobot
import com.example.storyround.testing.robot.HomeRobot
import com.example.storyround.testing.robot.LobbyRobot
import com.example.storyround.testing.robot.OpeningPromptRobot
import com.example.storyround.testing.robot.PlayingRobot
import com.example.storyround.testing.robot.SavedStoryRobot
import com.example.storyround.testing.robot.component.BannerRobot
import com.example.storyround.testing.robot.component.ButtonRobot
import com.example.storyround.testing.robot.component.PlayerStripRobot
import com.example.storyround.testing.robot.component.StoryTextRobot
import com.example.storyround.testing.robot.component.TextFieldRobot
import com.example.storyround.testing.robot.component.WriterLegendRobot
import com.example.storyround.testing.robot.component.WritingFieldRobot

interface ScreenRobots {
    val homeRobot: HomeRobot
    val lobbyRobot: LobbyRobot
    val openingPromptRobot: OpeningPromptRobot
    val playingRobot: PlayingRobot
    val finishedRobot: FinishedRobot
    val historyRobot: HistoryRobot
    val savedStoryRobot: SavedStoryRobot
}

class ScreenRobotSet(
    ui: ComposeUiTest,
) : ScreenRobots {
    override val homeRobot = HomeRobot(ui)
    override val lobbyRobot = LobbyRobot(ui)
    override val openingPromptRobot = OpeningPromptRobot(ui)
    override val playingRobot = PlayingRobot(ui)
    override val finishedRobot = FinishedRobot(ui)
    override val historyRobot = HistoryRobot(ui)
    override val savedStoryRobot = SavedStoryRobot(ui)
}

interface ComponentRobots {
    val writingFieldRobot: WritingFieldRobot
    val textFieldRobot: TextFieldRobot
    val buttonRobot: ButtonRobot
    val playerStripRobot: PlayerStripRobot
    val writerLegendRobot: WriterLegendRobot
    val storyTextRobot: StoryTextRobot
    val bannerRobot: BannerRobot
}

class ComponentRobotSet(
    ui: ComposeUiTest,
) : ComponentRobots {
    override val writingFieldRobot = WritingFieldRobot(ui)
    override val textFieldRobot = TextFieldRobot(ui)
    override val buttonRobot = ButtonRobot(ui)
    override val playerStripRobot = PlayerStripRobot(ui)
    override val writerLegendRobot = WriterLegendRobot(ui)
    override val storyTextRobot = StoryTextRobot(ui)
    override val bannerRobot = BannerRobot(ui)
}
