package ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.StateFlow
import model.sorting.SortingMethod
import repository.LocalSessionRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.EditableListConfig
import ui.common.EditableListScreenModel
import ui.common.EditableListScreenModelImpl
import ui.common.LocalAlertDialogController
import ui.string.*
import util.Log

class MainScreenModel(
    private val sessionRepository: SessionRepository,
    private val navigator: Navigator,
    private val alertDialogController: AlertDialogController,
) : ScreenModel,
    EditableListScreenModel<String> by EditableListScreenModelImpl(
        alertDialogController,
        EditableListConfig(
            deleteAlertTitle = { stringStatic(Strings.MainScreenDeleteItemsTitle) },
            deleteAlertMessage = { count -> stringStatic(Strings.MainScreenDeleteItemsMessage, count) },
            onDelete = { sessionRepository.delete(it) },
        ),
    ) {
    val sessions: StateFlow<List<SessionRepository.Item>> = sessionRepository.items
    val initialSortingMethod: SortingMethod = sessionRepository.sortingMethod

    init {
        sessionRepository.fetch()
    }

    fun openSession(name: String) {
        val session = sessionRepository.get(name)
            .getOrElse {
                Log.e("Failed to get session $name", it)
                return
            }
        sessionRepository.updateUsedTime(session.name)
        navigator push SessionScreen(session)
    }

    fun onSortingMethodChanged(method: SortingMethod) {
        sessionRepository.sortingMethod = method
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
