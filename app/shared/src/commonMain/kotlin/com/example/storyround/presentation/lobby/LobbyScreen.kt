package com.example.storyround.presentation.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.countOf
import com.example.storyround.ui.components.HostCrown
import com.example.storyround.ui.components.PillButton
import com.example.storyround.ui.components.PlayerAvatar
import com.example.storyround.ui.components.RaisedButton
import com.example.storyround.ui.components.ReconnectingBanner
import com.example.storyround.ui.components.StageBackground
import com.example.storyround.ui.components.StoryRoundIcons
import com.example.storyround.ui.components.TintedIcon
import com.example.storyround.ui.components.dashedBorder
import com.example.storyround.ui.components.hardShadow
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.theme.StoryRoundTheme

private const val PLAYER_GRID_COLUMNS = 2
private val TicketShape = RoundedCornerShape(20.dp)
private val PanelShape = RoundedCornerShape(20.dp)
private val SeatShape = RoundedCornerShape(16.dp)
private val ChipShape = RoundedCornerShape(20.dp)
private val YouTagShape = RoundedCornerShape(6.dp)

@Composable
internal fun LobbyScreen(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) = StageBackground {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LobbyTopBar(onLeave = { onAction(LobbyAction.Leave) })
            RoomTicket(roomCode = uiState.roomCode, playerCount = uiState.players.size, maxPlayers = uiState.maxPlayers)
            PlayersSection(uiState)
            if (uiState.isHost) {
                HostSettings(uiState, onAction)
            } else {
                uiState.host?.let { host -> WaitingForHost(host) }
            }
        }
        if (uiState.isHost) {
            HostFooter(onStart = { onAction(LobbyAction.StartGame) })
        }
    }
    if (uiState.isReconnecting) {
        ReconnectingBanner(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun LobbyTopBar(onLeave: () -> Unit) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PillButton(onClick = onLeave, contentPadding = PaddingValues(start = 10.dp, end = 16.dp), spacing = 6.dp) {
            TintedIcon(StoryRoundIcons.Back, tint = colors.onStage, modifier = Modifier.size(18.dp))
            BasicText("Leave lobby", style = typography.pill.copy(color = colors.onStage))
        }
        BasicText("LOBBY", style = typography.eyebrow.copy(color = colors.onStageMuted))
    }
}

@Composable
private fun RoomTicket(
    roomCode: String,
    playerCount: Int,
    maxPlayers: Int,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .tilt(degrees = -1.5f, dx = 0.dp, dy = 0.dp)
                .hardShadow(offsetY = 6.dp, color = colors.inkShadow, shape = TicketShape)
                .background(colors.paper, TicketShape)
                .border(2.5.dp, colors.ink, TicketShape)
                .padding(start = 22.dp, top = 14.dp, end = 20.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText("ROOM CODE", style = typography.eyebrowSmall.copy(color = colors.onPaperMuted))
            BasicText(roomCode, style = typography.roomCode.copy(color = colors.ink))
            BasicText("Friends join with this code", style = typography.caption.copy(color = colors.onPaperMuted))
        }
        Column(
            modifier =
                Modifier
                    .size(66.dp)
                    .rotate(8f)
                    .background(colors.cue, CircleShape)
                    .border(2.5.dp, colors.ink, CircleShape)
                    .semantics(mergeDescendants = true) { contentDescription = "$playerCount of $maxPlayers players" },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(playerCount.toString(), style = typography.numberSmall.copy(color = colors.ink))
            BasicText("OF $maxPlayers", style = typography.micro.copy(color = colors.ink))
        }
    }
}

@Composable
private fun PlayersSection(uiState: LobbyUiState) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText("PLAYERS", style = typography.eyebrow.copy(color = colors.onStageMuted))
            BasicText(
                "${uiState.players.size} / ${uiState.maxPlayers} PLAYERS",
                style = typography.metaStrong.copy(color = colors.onStageMuted),
            )
        }
        val seats: List<PlayerUi?> = uiState.players + if (uiState.openSeats > 0) listOf(null) else emptyList()
        seats.chunked(PLAYER_GRID_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { player ->
                    if (player == null) {
                        OpenSeats(uiState.openSeats)
                    } else {
                        PlayerSeat(
                            player = player,
                            isHost = player.id == uiState.hostId,
                            isCurrentPlayer = player.id == uiState.currentPlayerId,
                        )
                    }
                }
                repeat(PLAYER_GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RowScope.PlayerSeat(
    player: PlayerUi,
    isHost: Boolean,
    isCurrentPlayer: Boolean,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier =
            Modifier
                .weight(1f)
                .height(52.dp)
                .background(colors.stageRaised, SeatShape)
                .border(2.dp, if (isCurrentPlayer) colors.cue else colors.stageHairline, SeatShape)
                .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerAvatar(player, size = 34.dp)
        BasicText(
            text = player.name,
            modifier = Modifier.weight(1f),
            style = typography.bodyStrong.copy(color = colors.onStage),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isHost) HostCrown(fill = colors.marigold, outline = colors.ink, modifier = Modifier.size(18.dp))
        if (isCurrentPlayer) {
            BasicText(
                "YOU",
                modifier = Modifier.background(colors.cue, YouTagShape).padding(horizontal = 6.dp, vertical = 2.dp),
                style = typography.tag.copy(color = colors.ink),
            )
        }
    }
}

@Composable
private fun RowScope.OpenSeats(count: Int) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            Modifier
                .weight(1f)
                .height(52.dp)
                .dashedBorder(2.dp, colors.stageOutlineSoft, SeatShape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText("$count seats open", style = StoryRoundTheme.typography.captionStrong.copy(color = colors.onStageMuted))
    }
}

@Composable
private fun HostSettings(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.stageRaised, PanelShape)
                .border(2.dp, colors.stageHairline, PanelShape)
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText("Who opens the story?", style = typography.title.copy(color = colors.onStage))
            BasicText(
                text = uiState.openingPlayer?.let { uiState.displayName(it) } ?: "Random player",
                style = typography.callout.copy(color = colors.cue),
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OpenerChip(
                label = "Random",
                selected = uiState.openingPlayerId == null,
                onSelect = { onAction(LobbyAction.OpeningPlayerSelected(null)) },
            ) {
                Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                    TintedIcon(StoryRoundIcons.Dice, tint = it, modifier = Modifier.size(18.dp))
                }
            }
            uiState.players.forEach { player ->
                OpenerChip(
                    label = uiState.displayName(player),
                    selected = uiState.openingPlayerId == player.id,
                    onSelect = { onAction(LobbyAction.OpeningPlayerSelected(player.id)) },
                ) {
                    PlayerAvatar(player, size = 26.dp, borderWidth = 1.5.dp)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(2.dp).background(colors.stageDivider))
        RoundsStepper(uiState, onAction)
    }
}

@Composable
private fun OpenerChip(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    leading: @Composable (tint: Color) -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val content = if (selected) colors.ink else colors.onStage
    Row(
        modifier =
            Modifier
                .height(40.dp)
                .background(if (selected) colors.cue else colors.stageScrim, ChipShape)
                .border(2.dp, if (selected) colors.ink else colors.stageOutlineSoft, ChipShape)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
                .padding(start = 6.dp, end = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading(content)
        BasicText(label, style = StoryRoundTheme.typography.callout.copy(color = content))
    }
}

@Composable
private fun RoundsStepper(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText("Rounds", style = typography.title.copy(color = colors.onStage))
            BasicText(
                "Everyone writes ${countOf(uiState.maxRounds, "time")} · ${uiState.totalTurns} turns",
                style = typography.meta.copy(color = colors.onStageMuted),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            StepperButton(
                icon = StoryRoundIcons.Minus,
                label = "Fewer rounds",
                enabled = uiState.canDecreaseRounds,
                onClick = { onAction(LobbyAction.RoundsChanged(uiState.maxRounds - 1)) },
            )
            BasicText(
                text = uiState.maxRounds.toString(),
                modifier = Modifier.width(44.dp).semantics { liveRegion = LiveRegionMode.Polite },
                style = typography.numberLarge.copy(color = colors.onStage, textAlign = TextAlign.Center),
            )
            StepperButton(
                icon = StoryRoundIcons.Plus,
                label = "More rounds",
                enabled = uiState.canIncreaseRounds,
                onClick = { onAction(LobbyAction.RoundsChanged(uiState.maxRounds + 1)) },
            )
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            Modifier
                .size(44.dp)
                .background(if (enabled) colors.paper else colors.stageDisabledFill, CircleShape)
                .border(2.dp, if (enabled) colors.ink else colors.stageDisabledOutline, CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        TintedIcon(icon, tint = if (enabled) colors.ink else colors.onStageDisabled, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun WaitingForHost(host: PlayerUi) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.stageRaised, PanelShape)
                .dashedBorder(2.dp, colors.stageOutlineSoft, PanelShape)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerAvatar(host, size = 44.dp, borderWidth = 2.5.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText("Waiting for ${host.name} to start the game…", style = typography.title.copy(color = colors.onStage))
            BasicText(
                "The host picks who opens and how many rounds.",
                style = typography.meta.copy(color = colors.onStageMuted),
            )
        }
    }
}

@Composable
private fun HostFooter(onStart: () -> Unit) {
    val colors = StoryRoundTheme.colors
    Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BasicText(
            text = "You are the host. Start when you're ready.",
            modifier = Modifier.fillMaxWidth(),
            style = StoryRoundTheme.typography.caption.copy(color = colors.onStageMuted, textAlign = TextAlign.Center),
        )
        RaisedButton(label = "START GAME", onClick = onStart, modifier = Modifier.fillMaxWidth())
    }
}
