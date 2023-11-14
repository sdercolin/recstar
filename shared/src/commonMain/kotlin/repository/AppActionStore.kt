package repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import model.Action
import ui.model.Screen
import ui.screen.CreateSessionReclistScreen
import ui.screen.MainScreen
import ui.screen.SessionScreen

/**
 * A store to collect and dispatch app actions.
 */
class AppActionStore(private val scope: CoroutineScope) {
    private val _actions = MutableSharedFlow<Action>(replay = 0)
    val actions: Flow<Action> = _actions

    private var currentScreen: Screen by mutableStateOf(MainScreen)

    fun onScreenChange(screen: Screen) {
        currentScreen = screen
    }

    fun isEnabled(action: Action): Boolean =
        when (action) {
            Action.NewSession -> currentScreen != CreateSessionReclistScreen
            Action.ImportReclist -> true
            Action.OpenDirectory -> currentScreen is SessionScreen
            Action.Exit -> currentScreen != MainScreen
            Action.RenameSession -> currentScreen is SessionScreen
            Action.EditList -> currentScreen !is SessionScreen
            Action.NextSentence -> currentScreen is SessionScreen
            Action.PreviousSentence -> currentScreen is SessionScreen
            Action.ToggleRecording -> currentScreen is SessionScreen
        }

    fun dispatch(action: Action) {
        scope.launch {
            _actions.emit(action)
        }
    }
}

val LocalAppActionStore = staticCompositionLocalOf<AppActionStore> {
    error("No AppActionStore provided")
}
