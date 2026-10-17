package com.example.storyround.integration

import com.example.storyround.protocol.GameStatus
import com.example.storyround.protocol.RoomSettings
import com.example.storyround.testing.GIVEN
import com.example.storyround.testing.THEN
import com.example.storyround.testing.WHEN
import com.example.storyround.testing.gameAwaitingPrompt
import com.example.storyround.testing.gameInPlay
import com.example.storyround.testing.roomInLobby
import kotlin.test.Test

class OneWordModeTest : ServerIntegrationTest() {
    @Test
    fun `given a lobby when the host starts a one word game then everyone sees the one word setting`() =
        serverTest {
            GIVEN("Alex hosts a lobby with Sam") {
                roomInLobby(host = "Alex", guests = listOf("Sam"))
            }

            WHEN("Alex starts a two-round game with one word each") {
                player("Alex").startGame(rounds = 2, oneWordPerTurn = true)
            }

            THEN("Sam sees the game waiting for the prompt, set to one word each over two rounds") {
                player("Sam").assertRoom("a two-round one word game") {
                    it.status == GameStatus.Prompt && it.settings == RoomSettings(rounds = 2, oneWordPerTurn = true)
                }
            }
        }

    @Test
    fun `given a one word game when the opener submits several words then the prompt is refused`() =
        serverTest {
            GIVEN("Alex is writing the opening of a one word game") {
                gameAwaitingPrompt(host = "Alex", oneWordPerTurn = true)
            }

            WHEN("Alex submits a whole sentence") {
                player("Alex").submitPrompt("A door opened.")
            }

            THEN("Alex is told it must be one word") {
                player("Alex").assertErrorReceived("INVALID_WORD")
            }
        }

    @Test
    fun `given a one word game when the opener submits one word then the story starts with it`() =
        serverTest {
            GIVEN("Alex is writing the opening of a one word game") {
                gameAwaitingPrompt(host = "Alex", oneWordPerTurn = true)
            }

            WHEN("Alex submits a single word") {
                player("Alex").submitPrompt(" Once ")
            }

            THEN("the story is playing from that word") {
                player("Alex").assertRoom("the story playing from one word") {
                    it.status == GameStatus.Playing && it.prompt == "Once"
                }
            }
        }

    @Test
    fun `given a one word game when the writer submits several words then the contribution is refused`() =
        serverTest {
            GIVEN("Alex is writing the first turn of a one word game") {
                gameInPlay(host = "Alex", prompt = "Once", oneWordPerTurn = true)
            }

            WHEN("Alex submits three words") {
                player("Alex").submitContribution("upon a time")
            }

            THEN("Alex is told it must be one word") {
                player("Alex").assertErrorReceived("INVALID_WORD")
            }
        }
}
