package com.example.storyround

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.storyround.presentation.finished.FinishedRoute
import com.example.storyround.presentation.history.HistoryRoute
import com.example.storyround.presentation.history.SavedStoryRoute
import com.example.storyround.presentation.home.HomeRoute
import com.example.storyround.presentation.lobby.LobbyRoute
import com.example.storyround.presentation.navigation.AppDestination
import com.example.storyround.presentation.navigation.AppViewModel
import com.example.storyround.presentation.story.StoryRoute
import com.example.storyround.ui.components.StageBackground
import com.example.storyround.ui.theme.StoryRoundTheme
import org.koin.compose.viewmodel.koinViewModel

private const val ROUTE_FADE_MILLIS = 300

@Composable
fun App() {
    val viewModel = koinViewModel<AppViewModel>()
    val destination by viewModel.destination.collectAsState()

    StoryRoundTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            StageBackground {
                Crossfade(
                    targetState = destination,
                    modifier = Modifier.fillMaxSize(),
                    animationSpec = tween(ROUTE_FADE_MILLIS),
                    label = "route",
                ) { shown -> AppDestinationContent(shown, viewModel) }
            }
        }
    }
}

@Composable
private fun AppDestinationContent(
    destination: AppDestination,
    viewModel: AppViewModel,
) {
    when (destination) {
        AppDestination.Home -> HomeRoute(onOpenHistory = viewModel::showHistory)
        AppDestination.Lobby -> LobbyRoute(onLeave = viewModel::leaveLobby)
        AppDestination.Story -> StoryRoute()
        AppDestination.Finished -> FinishedRoute(onHome = viewModel::returnHome)
        AppDestination.History -> HistoryRoute(onBack = viewModel::closeHistory)
        AppDestination.SavedStory -> SavedStoryRoute(onBack = viewModel::closeSavedStory)
    }
}
