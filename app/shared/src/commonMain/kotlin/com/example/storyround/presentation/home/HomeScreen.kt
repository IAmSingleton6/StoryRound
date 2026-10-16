package com.example.storyround.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.UiText
import com.example.storyround.presentation.model.asString
import com.example.storyround.ui.components.ErrorBanner
import com.example.storyround.ui.components.LabeledTextField
import com.example.storyround.ui.components.PillButton
import com.example.storyround.ui.components.RaisedButton
import com.example.storyround.ui.components.RaisedButtonColor
import com.example.storyround.ui.components.StageBackground
import com.example.storyround.ui.components.TextLinkButton
import com.example.storyround.ui.components.TintedIcon
import com.example.storyround.ui.components.paperSheet
import com.example.storyround.ui.components.printedSurface
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.ScreenPreview
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.count_saved_stories
import storyround.app.shared.generated.resources.error_server_unreachable
import storyround.app.shared.generated.resources.home_cancel
import storyround.app.shared.generated.resources.home_connecting
import storyround.app.shared.generated.resources.home_create_game
import storyround.app.shared.generated.resources.home_history_description
import storyround.app.shared.generated.resources.home_join_game
import storyround.app.shared.generated.resources.home_name_label
import storyround.app.shared.generated.resources.home_name_placeholder
import storyround.app.shared.generated.resources.home_room_code_label
import storyround.app.shared.generated.resources.home_room_code_placeholder
import storyround.app.shared.generated.resources.home_tagline
import storyround.app.shared.generated.resources.home_tagline_emphasis
import storyround.app.shared.generated.resources.home_wordmark_round
import storyround.app.shared.generated.resources.home_wordmark_story
import storyround.app.shared.generated.resources.ic_book

private const val LOGO_TILT_DEGREES = -2f
private const val CUE_CARD_TILT_DEGREES = 6f
private const val CORAL_CARD_TILT_DEGREES = -7f
private val LogoCardShape = RoundedCornerShape(18.dp)
private val LogoSize = DpSize(width = 250.dp, height = 150.dp)
private val HistoryBadgeSize = 26.dp
private val HistoryBadgeShape = RoundedCornerShape(percent = 50)
private val RoomCodeFieldHeight = 64.dp
private val TopBarInsets
    @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
) = StageBackground {
    Column(Modifier.fillMaxSize().imePadding()) {
        HomeTopBar(historyCount = uiState.historyCount, onOpenHistory = { onAction(HomeAction.OpenHistory) })
        HomeHero(Modifier.weight(1f))
        uiState.error?.let { error ->
            ErrorBanner(error.asString(), Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
        }
        HomeSheet(uiState, onAction)
    }
}

@Composable
private fun HomeTopBar(
    historyCount: Int,
    onOpenHistory: () -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val savedStories = pluralStringResource(Res.plurals.count_saved_stories, historyCount, historyCount)
    val description = stringResource(Res.string.home_history_description, savedStories)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(TopBarInsets)
                .padding(start = 20.dp, top = 12.dp, end = 20.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        PillButton(
            onClick = onOpenHistory,
            contentPadding = PaddingValues(start = 14.dp, end = 8.dp),
            modifier =
                Modifier.semantics {
                    contentDescription = description
                },
        ) {
            TintedIcon(Res.drawable.ic_book, tint = colors.onStage, modifier = Modifier.size(18.dp))
            HistoryCountBadge(historyCount)
        }
    }
}

@Composable
private fun HistoryCountBadge(count: Int) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            Modifier
                .clearAndSetSemantics {}
                .defaultMinSize(minWidth = HistoryBadgeSize)
                .height(HistoryBadgeSize)
                .background(colors.marigold, HistoryBadgeShape)
                .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(count.toString(), style = StoryRoundTheme.typography.eyebrowMedium.copy(color = colors.ink))
    }
}

@Composable
private fun HomeHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LogoStack()
        Tagline()
    }
}

@Composable
private fun LogoStack() {
    val colors = StoryRoundTheme.colors
    val wordmark = StoryRoundTheme.typography.displayLarge.copy(color = colors.ink)
    Box(Modifier.size(LogoSize).semantics(mergeDescendants = true) { heading() }) {
        LogoCard(colors.cue, Modifier.matchParentSize().tilt(degrees = CUE_CARD_TILT_DEGREES, dx = 10.dp, dy = 4.dp))
        LogoCard(colors.coral, Modifier.matchParentSize().tilt(degrees = CORAL_CARD_TILT_DEGREES, dx = (-8).dp, dy = 10.dp))
        Column(
            modifier =
                Modifier
                    .matchParentSize()
                    .tilt(degrees = LOGO_TILT_DEGREES)
                    .printedSurface(
                        fill = colors.paper,
                        ink = colors.ink,
                        shape = LogoCardShape,
                        shadowOffset = 8.dp,
                        shadowColor = colors.inkShadow,
                        outlineWidth = OutlineWidth.Thick,
                    ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(stringResource(Res.string.home_wordmark_story), style = wordmark)
            BasicText(stringResource(Res.string.home_wordmark_round), style = wordmark)
        }
    }
}

@Composable
private fun LogoCard(
    color: Color,
    modifier: Modifier,
) {
    Box(modifier.background(color, LogoCardShape).border(OutlineWidth.Thick, StoryRoundTheme.colors.ink, LogoCardShape))
}

@Composable
private fun Tagline() {
    val colors = StoryRoundTheme.colors
    val lead = stringResource(Res.string.home_tagline)
    val emphasis = stringResource(Res.string.home_tagline_emphasis)
    val tagline =
        remember(colors, lead, emphasis) {
            buildAnnotatedString {
                append(lead)
                withStyle(SpanStyle(color = colors.marigold, fontWeight = FontWeight.ExtraBold)) {
                    append(emphasis)
                }
            }
        }
    BasicText(
        text = tagline,
        modifier = Modifier.widthIn(max = 290.dp),
        style = StoryRoundTheme.typography.bodyLarge.copy(color = colors.onStageSoft, textAlign = TextAlign.Center),
    )
}

@Composable
private fun HomeSheet(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
) {
    val colors = StoryRoundTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .paperSheet(paper = colors.paper, edge = colors.ink)
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LabeledTextField(
            value = uiState.playerName,
            onValueChange = { onAction(HomeAction.PlayerNameChanged(it)) },
            label = stringResource(Res.string.home_name_label),
            placeholder = stringResource(Res.string.home_name_placeholder),
            keyboardOptions =
                KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        )
        if (uiState.isJoining) {
            JoinActions(uiState, onAction)
        } else {
            StartActions(uiState, onAction)
        }
    }
}

@Composable
private fun StartActions(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
) {
    val ink = StoryRoundTheme.colors.ink
    Column(Modifier.padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RaisedButton(
            label = stringResource(if (uiState.isConnecting) Res.string.home_connecting else Res.string.home_create_game),
            onClick = { onAction(HomeAction.CreateGame) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canCreateGame,
            shadowColor = ink,
        )
        RaisedButton(
            label = stringResource(Res.string.home_join_game),
            onClick = { onAction(HomeAction.ShowJoinGame) },
            modifier = Modifier.fillMaxWidth(),
            color = RaisedButtonColor.White,
            shadowColor = ink,
        )
    }
}

@Composable
private fun JoinActions(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
) {
    val typography = StoryRoundTheme.typography
    LabeledTextField(
        value = uiState.roomCode,
        onValueChange = { onAction(HomeAction.RoomCodeChanged(it)) },
        label = stringResource(Res.string.home_room_code_label),
        placeholder = stringResource(Res.string.home_room_code_placeholder),
        textStyle = typography.codeInput.copy(textAlign = TextAlign.Center),
        height = RoomCodeFieldHeight,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
    )
    RaisedButton(
        label = stringResource(if (uiState.isConnecting) Res.string.home_connecting else Res.string.home_join_game),
        onClick = { onAction(HomeAction.JoinGame) },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.canJoinGame,
        shadowColor = StoryRoundTheme.colors.ink,
    )
    TextLinkButton(
        label = stringResource(Res.string.home_cancel),
        onClick = { onAction(HomeAction.CancelJoin) },
        color = StoryRoundTheme.colors.ink,
        modifier = Modifier.fillMaxWidth(),
    )
}

@PhonePreview
@Composable
private fun HomeScreenPreview() = ScreenPreview { HomeScreen(HomeUiState(historyCount = 3), onAction = {}) }

@PhonePreview
@Composable
private fun HomeScreenReadyPreview() =
    ScreenPreview { HomeScreen(HomeUiState(playerName = "Alex", hasValidPlayerName = true, historyCount = 3), onAction = {}) }

@PhonePreview
@Composable
private fun HomeScreenJoiningPreview() =
    ScreenPreview {
        HomeScreen(HomeUiState(playerName = "Alex", hasValidPlayerName = true, roomCode = "7F2K", isJoining = true), onAction = {})
    }

@PhonePreview
@Composable
private fun HomeScreenConnectingPreview() =
    ScreenPreview { HomeScreen(HomeUiState(playerName = "Alex", hasValidPlayerName = true, isConnecting = true), onAction = {}) }

@PhonePreview
@Composable
private fun HomeScreenErrorPreview() =
    ScreenPreview {
        val error = UiText.Resource(Res.string.error_server_unreachable)
        HomeScreen(HomeUiState(playerName = "Alex", hasValidPlayerName = true, error = error), onAction = {})
    }
