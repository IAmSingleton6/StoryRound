package com.example.storyround.testing.scenario

import com.example.storyround.protocol.ClientMessage
import com.example.storyround.protocol.Player
import com.example.storyround.protocol.RoomState
import com.example.storyround.testing.base.AppFlowTestScope
import com.example.storyround.testing.fixtures.tokenOf

suspend fun AppFlowTestScope.appOnHome() {
    appRobot.launch()
    homeRobot.assertShown()
}

suspend fun AppFlowTestScope.appResumedInto(
    room: RoomState,
    player: Player,
) {
    storageRobot.saveSession(room.roomCode, player.id, tokenOf(player))
    appRobot.launch()
    serverRobot.awaitSent(ClientMessage.Reconnect(room.roomCode, player.id, tokenOf(player)))
    serverRobot.resume(player, room)
}
