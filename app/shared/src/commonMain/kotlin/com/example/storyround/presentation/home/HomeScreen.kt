package com.example.storyround.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
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
import androidx.compose.ui.unit.dp
import com.example.storyround.ui.components.ErrorBanner
import com.example.storyround.ui.components.SrButton
import com.example.storyround.ui.components.SrButtonVariant
import com.example.storyround.ui.components.SrIcon
import com.example.storyround.ui.components.SrIcons
import com.example.storyround.ui.components.SrTextField
import com.example.storyround.ui.components.StagePillButton
import com.example.storyround.ui.components.StageScaffold
import com.example.storyround.ui.components.hardShadow
import com.example.storyround.ui.components.paperSheet
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.theme.StoryRoundTheme

private const val CONNECTING_LABEL = "CONNECTING…"
private val LogoCardShape = RoundedCornerShape(18.dp)
private val TopBarInsets
    @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
) = StageScaffold {
    Column(Modifier.fillMaxSize().imePadding()) {
        HomeTopBar(historyCount = uiState.historyCount, onOpenHistory = { onAction(HomeAction.OpenHistory) })
        HomeHero(Modifier.weight(1f))
        uiState.error?.let { error ->
            ErrorBanner(error, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
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
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(TopBarInsets)
                .padding(start = 20.dp, top = 12.dp, end = 20.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        StagePillButton(
            onClick = onOpenHistory,
            contentPadding = PaddingValues(start = 14.dp, end = 8.dp),
            modifier =
                Modifier.semantics {
                    contentDescription = "Story history, $historyCount saved ${if (historyCount == 1) "story" else "stories"}"
                },
        ) {
            SrIcon(SrIcons.Book, tint = colors.onStage, modifier = Modifier.size(18.dp))
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
                .defaultMinSize(minWidth = 26.dp)
                .height(26.dp)
                .background(colors.marigold, RoundedCornerShape(13.dp))
                .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(count.toString(), style = StoryRoundTheme.typography.badge.copy(color = colors.ink))
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
    val wordmark = StoryRoundTheme.typography.display.copy(color = colors.ink)
    Box(Modifier.size(width = 250.dp, height = 150.dp).semantics(mergeDescendants = true) { heading() }) {
        LogoCard(colors.cue, Modifier.matchParentSize().tilt(degrees = 6f, dx = 10.dp, dy = 4.dp))
        LogoCard(colors.coral, Modifier.matchParentSize().tilt(degrees = -7f, dx = (-8).dp, dy = 10.dp))
        Column(
            modifier =
                Modifier
                    .matchParentSize()
                    .tilt(degrees = -2f, dx = 0.dp, dy = 0.dp)
                    .hardShadow(offsetY = 8.dp, color = colors.inkShadow, shape = LogoCardShape)
                    .background(colors.paper, LogoCardShape)
                    .border(3.dp, colors.ink, LogoCardShape),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText("Story", style = wordmark)
            BasicText("Round", style = wordmark)
        }
    }
}

@Composable
private fun LogoCard(
    color: Color,
    modifier: Modifier,
) {
    Box(modifier.background(color, LogoCardShape).border(3.dp, StoryRoundTheme.colors.ink, LogoCardShape))
}

@Composable
private fun Tagline() {
    val colors = StoryRoundTheme.colors
    val tagline =
        remember(colors) {
            buildAnnotatedString {
                append("Write a story. One sentence at a time. ")
                withStyle(SpanStyle(color = colors.marigold, fontWeight = FontWeight.ExtraBold)) {
                    append("Less time every turn.")
                }
            }
        }
    BasicText(
        text = tagline,
        modifier = Modifier.widthIn(max = 290.dp),
        style = StoryRoundTheme.typography.lead.copy(color = colors.onStageSoft, textAlign = TextAlign.Center),
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
        SrTextField(
            value = uiState.playerName,
            onValueChange = { onAction(HomeAction.PlayerNameChanged(it)) },
            label = "Your name",
            placeholder = "What should we call you?",
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
        SrButton(
            label = if (uiState.isConnecting) CONNECTING_LABEL else "CREATE GAME",
            onClick = { onAction(HomeAction.CreateGame) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canCreateGame,
            shadowColor = ink,
        )
        SrButton(
            label = "JOIN GAME",
            onClick = { onAction(HomeAction.ShowJoinGame) },
            modifier = Modifier.fillMaxWidth(),
            variant = SrButtonVariant.White,
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
    SrTextField(
        value = uiState.roomCode,
        onValueChange = { onAction(HomeAction.RoomCodeChanged(it)) },
        label = "Room code",
        placeholder = "ABCD",
        textStyle = typography.codeInput.copy(textAlign = TextAlign.Center),
        height = 64.dp,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
    )
    SrButton(
        label = if (uiState.isConnecting) CONNECTING_LABEL else "JOIN GAME",
        onClick = { onAction(HomeAction.JoinGame) },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.canJoinGame,
        shadowColor = StoryRoundTheme.colors.ink,
    )
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable(role = Role.Button) { onAction(HomeAction.CancelJoin) },
        contentAlignment = Alignment.Center,
    ) {
        BasicText("Cancel", style = typography.link.copy(color = StoryRoundTheme.colors.ink))
    }
}
