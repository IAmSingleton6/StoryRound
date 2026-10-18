package com.example.storyround.testing.actor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.storyround.App
import com.example.storyround.di.appModule
import com.example.storyround.logging.LoggingConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class AppRobot(
    private val ui: ComposeUiTest,
    private val edges: () -> List<Module>,
) {
    private var running: RunningApp? by mutableStateOf(null)
    private var isContentSet = false

    fun launch() {
        check(running == null) { "The app is already running; use relaunch()." }
        val app = RunningApp(edges())
        if (isContentSet) {
            ui.runOnUiThread { running = app }
        } else {
            running = app
            ui.setContent { RunningAppContent() }
            isContentSet = true
        }
    }

    fun relaunch() {
        close()
        launch()
    }

    fun close() {
        val app = running ?: return
        ui.runOnUiThread { running = null }
        app.end()
    }

    @Composable
    private fun RunningAppContent() {
        val app = running ?: return
        key(app) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides app) {
                KoinIsolatedContext(app.koin) { App() }
            }
        }
    }

    private class RunningApp(
        edges: List<Module>,
    ) : ViewModelStoreOwner {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        private val runtime =
            module {
                single { scope }
                single { LoggingConfiguration.Development }
            }
        val koin: KoinApplication = koinApplication { modules(listOf(appModule()) + edges + runtime) }
        override val viewModelStore = ViewModelStore()

        fun end() {
            viewModelStore.clear()
            scope.cancel()
            koin.close()
        }
    }
}
