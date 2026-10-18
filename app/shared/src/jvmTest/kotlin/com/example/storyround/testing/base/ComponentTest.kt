package com.example.storyround.testing.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.storyround.ui.theme.StoryRoundTheme
import kotlinx.coroutines.test.TestResult

abstract class ComponentTest<S, A>(
    private val content: @Composable (state: S, onAction: (A) -> Unit) -> Unit,
) {
    protected fun componentTest(body: suspend ComponentTestScope<S, A>.() -> Unit): TestResult =
        runComposeUiTest { ComponentTestScope(this, content).body() }
}

class ComponentTestScope<S, A>(
    private val ui: ComposeUiTest,
    private val content: @Composable (state: S, onAction: (A) -> Unit) -> Unit,
) : ScreenRobots by ScreenRobotSet(ui),
    ComponentRobots by ComponentRobotSet(ui) {
    val actions = ActionRecorder<A>()

    private var state: (() -> S)? = null

    fun useState(state: S) {
        this.state = { state }
    }

    fun show() {
        val shown = checkNotNull(state) { "Call useState before show." }()
        ui.setContent { StoryRoundTheme { content(shown, actions::record) } }
    }
}
