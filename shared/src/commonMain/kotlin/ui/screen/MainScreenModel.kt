package ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.StateFlow
import repository.LocalSessionRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.LocalAlertDialogController
import ui.common.requestConfirmCancellable
import ui.string.*
import util.Log

class MainScreenModel(
    private val sessionRepository: SessionRepository,
    private val navigator: Navigator,
    private val alertDialogController: AlertDialogController,
) : ScreenModel {
    val sessions: StateFlow<List<String>> = sessionRepository.items

    var isSelectingForDeletion: Boolean by mutableStateOf(false)
        private set
    val selectedSessions: SnapshotStateList<String> = mutableStateListOf()

    init {
        sessionRepository.fetch()
    }

    fun startSelectingForDeletion() {
        isSelectingForDeletion = true
    }

    fun cancelSelectingForDeletion() {
        isSelectingForDeletion = false
        selectedSessions.clear()
    }

    fun selectForDeletion(
        name: String,
        isSelected: Boolean,
    ) {
        if (isSelected) {
            selectedSessions.add(name)
        } else {
            selectedSessions.remove(name)
        }
    }

    fun openSession(name: String) {
        val session = sessionRepository.get(name)
            .getOrElse {
                Log.e("Failed to get session $name", it)
                return
            }
        navigator push SessionScreen(session)
    }

    fun deleteSelectedSessions() {
        if (selectedSessions.isEmpty()) {
            return
        }
        alertDialogController.requestConfirmCancellable(
            title = stringStatic(Strings.MainScreenDeleteItemsConfirmationTitle),
            message = stringStatic(
                Strings.MainScreenDeleteItemsConfirmationMessage,
                selectedSessions.size,
            ),
            onConfirm = {
                val selectedSessions = selectedSessions.toList()
                cancelSelectingForDeletion()
                sessionRepository.delete(selectedSessions)
            },
        )
    }
}

@Composable
fun MainScreen.rememberMainScreenModel(): MainScreenModel {
    val sessionRepository = LocalSessionRepository.current
    val navigator = LocalNavigator.currentOrThrow
    val alertDialogController = LocalAlertDialogController.current
    return rememberScreenModel {
        MainScreenModel(sessionRepository, navigator, alertDialogController)
    }
}
