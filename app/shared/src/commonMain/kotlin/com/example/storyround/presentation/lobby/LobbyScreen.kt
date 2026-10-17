package com.example.storyround.presentation.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.storyround.presentation.model.PlayerUi
import com.example.storyround.presentation.model.nameFor
import com.example.storyround.ui.components.CircleIconButton
import com.example.storyround.ui.components.HostCrown
import com.example.storyround.ui.components.PillButton
import com.example.storyround.ui.components.PlayerAvatar
import com.example.storyround.ui.components.RaisedButton
import com.example.storyround.ui.components.RoomScreenPadding
import com.example.storyround.ui.components.StageScreen
import com.example.storyround.ui.components.TintedIcon
import com.example.storyround.ui.components.dashedBorder
import com.example.storyround.ui.components.printedSurface
import com.example.storyround.ui.components.stagePanel
import com.example.storyround.ui.components.tilt
import com.example.storyround.ui.preview.PhonePreview
import com.example.storyround.ui.preview.PreviewPlayers
import com.example.storyround.ui.preview.ScreenPreview
import com.example.storyround.ui.theme.CardShape
import com.example.storyround.ui.theme.OutlineWidth
import com.example.storyround.ui.theme.StoryRoundTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import storyround.app.shared.generated.resources.Res
import storyround.app.shared.generated.resources.count_times
import storyround.app.shared.generated.resources.count_turns
import storyround.app.shared.generated.resources.ic_back
import storyround.app.shared.generated.resources.ic_dice
import storyround.app.shared.generated.resources.ic_minus
import storyround.app.shared.generated.resources.ic_plus
import storyround.app.shared.generated.resources.lobby_fewer_rounds
import storyround.app.shared.generated.resources.lobby_host_picks
import storyround.app.shared.generated.resources.lobby_host_ready
import storyround.app.shared.generated.resources.lobby_leave
import storyround.app.shared.generated.resources.lobby_more_rounds
import storyround.app.shared.generated.resources.lobby_one_word
import storyround.app.shared.generated.resources.lobby_one_word_summary
import storyround.app.shared.generated.resources.lobby_open_seats
import storyround.app.shared.generated.resources.lobby_opener_question
import storyround.app.shared.generated.resources.lobby_player_count_description
import storyround.app.shared.generated.resources.lobby_player_count_of
import storyround.app.shared.generated.resources.lobby_players
import storyround.app.shared.generated.resources.lobby_players_count
import storyround.app.shared.generated.resources.lobby_random
import storyround.app.shared.generated.resources.lobby_random_player
import storyround.app.shared.generated.resources.lobby_room_code
import storyround.app.shared.generated.resources.lobby_room_code_hint
import storyround.app.shared.generated.resources.lobby_rounds
import storyround.app.shared.generated.resources.lobby_rounds_summary
import storyround.app.shared.generated.resources.lobby_start_game
import storyround.app.shared.generated.resources.lobby_title
import storyround.app.shared.generated.resources.lobby_waiting_for_host
import storyround.app.shared.generated.resources.lobby_you

private const val PLAYER_GRID_COLUMNS = 2
private const val ROOM_TICKET_TILT_DEGREES = -1.5f
private const val PLAYER_COUNT_BADGE_TILT_DEGREES = 8f
private val SeatShape = RoundedCornerShape(16.dp)
private val YouTagShape = RoundedCornerShape(6.dp)
private val SeatHeight = 52.dp
private val PlayerCountBadgeSize = 66.dp
private val ChipHeight = 40.dp
private val ChipAvatarSize = 26.dp
private val StepperIconSize = 18.dp
private val StepperValueWidth = 44.dp
private val SwitchWidth = 52.dp
private val SwitchHeight = 32.dp
private val SwitchPadding = 4.dp
private val SwitchKnobSize = 20.dp

@Composable
internal fun LobbyScreen(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) = StageScreen(contentPadding = RoomScreenPadding, isReconnecting = uiState.isReconnecting) {
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
        StartGameFooter(onStart = { onAction(LobbyAction.StartGame) })
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
            TintedIcon(Res.drawable.ic_back, tint = colors.onStage, modifier = Modifier.size(18.dp))
            BasicText(stringResource(Res.string.lobby_leave), style = typography.labelMedium.copy(color = colors.onStage))
        }
        BasicText(stringResource(Res.string.lobby_title), style = typography.eyebrowMedium.copy(color = colors.onStageMuted))
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
                .tilt(degrees = ROOM_TICKET_TILT_DEGREES)
                .printedSurface(
                    fill = colors.paper,
                    ink = colors.ink,
                    shape = CardShape,
                    shadowOffset = 6.dp,
                    shadowColor = colors.inkShadow,
                ).padding(start = 22.dp, top = 14.dp, end = 20.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(stringResource(Res.string.lobby_room_code), style = typography.eyebrowSmall.copy(color = colors.onPaperMuted))
            BasicText(roomCode, style = typography.roomCode.copy(color = colors.ink))
            BasicText(stringResource(Res.string.lobby_room_code_hint), style = typography.bodySmall.copy(color = colors.onPaperMuted))
        }
        PlayerCountBadge(playerCount, maxPlayers)
    }
}

@Composable
private fun PlayerCountBadge(
    playerCount: Int,
    maxPlayers: Int,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    val description = stringResource(Res.string.lobby_player_count_description, playerCount, maxPlayers)
    Column(
        modifier =
            Modifier
                .size(PlayerCountBadgeSize)
                .rotate(PLAYER_COUNT_BADGE_TILT_DEGREES)
                .background(colors.cue, CircleShape)
                .border(OutlineWidth.Regular, colors.ink, CircleShape)
                .semantics(mergeDescendants = true) { contentDescription = description },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(playerCount.toString(), style = typography.numberSmall.copy(color = colors.ink))
        BasicText(stringResource(Res.string.lobby_player_count_of, maxPlayers), style = typography.tag.copy(color = colors.ink))
    }
}

@Composable
private fun PlayersSection(uiState: LobbyUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlayersHeader(playerCount = uiState.players.size, maxPlayers = uiState.maxPlayers)
        SeatGrid(uiState.seats)
    }
}

@Composable
private fun PlayersHeader(
    playerCount: Int,
    maxPlayers: Int,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        BasicText(stringResource(Res.string.lobby_players), style = typography.eyebrowMedium.copy(color = colors.onStageMuted))
        BasicText(
            stringResource(Res.string.lobby_players_count, playerCount, maxPlayers),
            style = typography.labelXSmall.copy(color = colors.onStageMuted),
        )
    }
}

@Composable
private fun SeatGrid(seats: List<LobbySeat>) {
    seats.chunked(PLAYER_GRID_COLUMNS).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { seat ->
                when (seat) {
                    is LobbySeat.Taken -> PlayerSeat(seat)
                    is LobbySeat.Open -> OpenSeatsTile(pluralStringResource(Res.plurals.lobby_open_seats, seat.count, seat.count))
                }
            }
            repeat(PLAYER_GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun RowScope.PlayerSeat(seat: LobbySeat.Taken) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier =
            Modifier
                .weight(1f)
                .height(SeatHeight)
                .background(colors.stageRaised, SeatShape)
                .border(OutlineWidth.Thin, if (seat.isCurrentPlayer) colors.cue else colors.stageHairline, SeatShape)
                .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerAvatar(seat.player, size = 34.dp)
        BasicText(
            text = seat.player.name,
            modifier = Modifier.weight(1f),
            style = typography.labelLarge.copy(color = colors.onStage),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (seat.isHost) HostCrown(fill = colors.marigold, outline = colors.ink, modifier = Modifier.size(18.dp))
        if (seat.isCurrentPlayer) YouTag()
    }
}

@Composable
private fun YouTag() {
    val colors = StoryRoundTheme.colors
    BasicText(
        stringResource(Res.string.lobby_you),
        modifier = Modifier.background(colors.cue, YouTagShape).padding(horizontal = 6.dp, vertical = 2.dp),
        style = StoryRoundTheme.typography.tag.copy(color = colors.ink),
    )
}

@Composable
private fun RowScope.OpenSeatsTile(label: String) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            Modifier
                .weight(1f)
                .height(SeatHeight)
                .dashedBorder(OutlineWidth.Thin, colors.stageOutlineSoft, SeatShape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(label, style = StoryRoundTheme.typography.labelSmall.copy(color = colors.onStageMuted))
    }
}

@Composable
private fun HostSettings(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    val colors = StoryRoundTheme.colors
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .stagePanel(fill = colors.stageRaised, outline = colors.stageHairline)
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OpeningPlayerPicker(uiState, onAction)
        StageDivider()
        RoundsStepper(uiState, onAction)
        StageDivider()
        OneWordToggle(
            isOn = uiState.oneWordPerTurn,
            onToggle = { onAction(LobbyAction.OneWordPerTurnChanged(it)) },
        )
    }
}

@Composable
private fun OpeningPlayerPicker(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(stringResource(Res.string.lobby_opener_question), style = typography.titleMedium.copy(color = colors.onStage))
            val openerLabel = uiState.openingPlayer?.nameFor(uiState.currentPlayerId) ?: stringResource(Res.string.lobby_random_player)
            BasicText(openerLabel, style = typography.labelSmall.copy(color = colors.cue))
        }
        OpenerChips(uiState, onAction)
    }
}

@Composable
private fun OpenerChips(
    uiState: LobbyUiState,
    onAction: (LobbyAction) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OpenerChip(
            label = stringResource(Res.string.lobby_random),
            selected = uiState.openingPlayerId == null,
            onSelect = { onAction(LobbyAction.OpeningPlayerSelected(null)) },
        ) { tint ->
            Box(Modifier.size(ChipAvatarSize), contentAlignment = Alignment.Center) {
                TintedIcon(Res.drawable.ic_dice, tint = tint, modifier = Modifier.size(18.dp))
            }
        }
        uiState.players.forEach { player ->
            OpenerChip(
                label = player.nameFor(uiState.currentPlayerId),
                selected = uiState.openingPlayerId == player.id,
                onSelect = { onAction(LobbyAction.OpeningPlayerSelected(player.id)) },
            ) {
                PlayerAvatar(player, size = ChipAvatarSize, borderWidth = OutlineWidth.Fine)
            }
        }
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
    val contentColor = if (selected) colors.ink else colors.onStage
    Row(
        modifier =
            Modifier
                .height(ChipHeight)
                .background(if (selected) colors.cue else colors.stageScrim, CardShape)
                .border(OutlineWidth.Thin, if (selected) colors.ink else colors.stageOutlineSoft, CardShape)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
                .padding(start = 6.dp, end = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading(contentColor)
        BasicText(label, style = StoryRoundTheme.typography.labelSmall.copy(color = contentColor))
    }
}

@Composable
private fun StageDivider() {
    Box(Modifier.fillMaxWidth().height(OutlineWidth.Thin).background(StoryRoundTheme.colors.stageDivider))
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
            BasicText(stringResource(Res.string.lobby_rounds), style = typography.titleMedium.copy(color = colors.onStage))
            BasicText(roundsSummary(uiState.maxRounds, uiState.totalTurns), style = typography.bodyXSmall.copy(color = colors.onStageMuted))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            StepperButton(
                icon = Res.drawable.ic_minus,
                label = stringResource(Res.string.lobby_fewer_rounds),
                enabled = uiState.canDecreaseRounds,
                onClick = { onAction(LobbyAction.DecreaseRounds) },
            )
            BasicText(
                text = uiState.maxRounds.toString(),
                modifier = Modifier.width(StepperValueWidth).semantics { liveRegion = LiveRegionMode.Polite },
                style = typography.numberLarge.copy(color = colors.onStage, textAlign = TextAlign.Center),
            )
            StepperButton(
                icon = Res.drawable.ic_plus,
                label = stringResource(Res.string.lobby_more_rounds),
                enabled = uiState.canIncreaseRounds,
                onClick = { onAction(LobbyAction.IncreaseRounds) },
            )
        }
    }
}

@Composable
private fun roundsSummary(
    rounds: Int,
    turns: Int,
): String =
    stringResource(
        Res.string.lobby_rounds_summary,
        pluralStringResource(Res.plurals.count_times, rounds, rounds),
        pluralStringResource(Res.plurals.count_turns, turns, turns),
    )

@Composable
private fun StepperButton(
    icon: DrawableResource,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = StoryRoundTheme.colors
    CircleIconButton(
        icon = icon,
        contentDescription = label,
        onClick = onClick,
        fill = if (enabled) colors.paper else colors.stageDisabledFill,
        outline = if (enabled) colors.ink else colors.stageDisabledOutline,
        tint = if (enabled) colors.ink else colors.onStageDisabled,
        iconSize = StepperIconSize,
        enabled = enabled,
    )
}

@Composable
private fun OneWordToggle(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val colors = StoryRoundTheme.colors
    val typography = StoryRoundTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth().toggleable(value = isOn, role = Role.Switch, onValueChange = onToggle),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(stringResource(Res.string.lobby_one_word), style = typography.titleMedium.copy(color = colors.onStage))
            BasicText(stringResource(Res.string.lobby_one_word_summary), style = typography.bodyXSmall.copy(color = colors.onStageMuted))
        }
        ToggleSwitch(isOn)
    }
}

@Composable
private fun ToggleSwitch(isOn: Boolean) {
    val colors = StoryRoundTheme.colors
    Box(
        modifier =
            Modifier
                .size(width = SwitchWidth, height = SwitchHeight)
                .background(if (isOn) colors.cue else colors.stageDisabledFill, CircleShape)
                .border(OutlineWidth.Thin, if (isOn) colors.ink else colors.stageOutlineSoft, CircleShape)
                .padding(SwitchPadding),
        contentAlignment = if (isOn) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(SwitchKnobSize).background(if (isOn) colors.ink else colors.onStageMuted, CircleShape))
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
                .background(colors.stageRaised, CardShape)
                .dashedBorder(OutlineWidth.Thin, colors.stageOutlineSoft, CardShape)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerAvatar(host, size = 44.dp, borderWidth = OutlineWidth.Regular)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                stringResource(Res.string.lobby_waiting_for_host, host.name),
                style = typography.titleMedium.copy(color = colors.onStage),
            )
            BasicText(
                stringResource(Res.string.lobby_host_picks),
                style = typography.bodyXSmall.copy(color = colors.onStageMuted),
            )
        }
    }
}

@Composable
private fun StartGameFooter(onStart: () -> Unit) {
    val colors = StoryRoundTheme.colors
    Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BasicText(
            text = stringResource(Res.string.lobby_host_ready),
            modifier = Modifier.fillMaxWidth(),
            style = StoryRoundTheme.typography.bodySmall.copy(color = colors.onStageMuted, textAlign = TextAlign.Center),
        )
        RaisedButton(label = stringResource(Res.string.lobby_start_game), onClick = onStart, modifier = Modifier.fillMaxWidth())
    }
}

@PhonePreview
@Composable
private fun LobbyScreenHostPreview() = ScreenPreview { LobbyScreen(LobbyPreviewData.host(), onAction = {}) }

@PhonePreview
@Composable
private fun LobbyScreenHostOpenerChosenPreview() =
    ScreenPreview { LobbyScreen(LobbyPreviewData.host().copy(openingPlayerId = PreviewPlayers.sam.id), onAction = {}) }

@PhonePreview
@Composable
private fun LobbyScreenGuestPreview() = ScreenPreview { LobbyScreen(LobbyPreviewData.guest(), onAction = {}) }

@PhonePreview
@Composable
private fun LobbyScreenReconnectingPreview() =
    ScreenPreview { LobbyScreen(LobbyPreviewData.guest().copy(isReconnecting = true), onAction = {}) }
