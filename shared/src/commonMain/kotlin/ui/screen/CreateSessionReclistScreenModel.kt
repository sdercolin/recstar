package ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.StateFlow
import repository.LocalReclistRepository
import repository.LocalSessionRepository
import repository.ReclistRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.EditableListConfig
import ui.common.EditableListScreenModel
import ui.common.EditableListScreenModelImpl
import ui.common.LocalAlertDialogController
import ui.common.requestConfirm
import ui.string.*
import util.Log

class CreateSessionReclistScreenModel(
    private val sessionRepository: SessionRepository,
    private val reclistRepository: ReclistRepository,
    private val navigator: Navigator,
    private val alertDialogController: AlertDialogController,
) : ScreenModel,
    EditableListScreenModel<String> by EditableListScreenModelImpl(
        alertDialogController,
        EditableListConfig(
            deleteAlertTitle = { stringStatic(Strings.CreateSessionReclistScreenDeleteItemsTitle) },
            deleteAlertMessage = { count -> stringStatic(Strings.CreateSessionReclistScreenDeleteItemsMessage, count) },
            onDelete = { reclistRepository.delete(it) },
        ),
    ) {
    val reclists: StateFlow<List<String>> = reclistRepository.items

    init {
        reclistRepository.fetch()
    }

    private var finished = false

    fun select(name: String) {
        if (finished) return
        finished = true
        val reclist = reclistRepository.get(name)
        val session = sessionRepository.create(reclist)
            .onFailure {
                Log.e(it)
                alertDialogController.requestConfirm(
                    message = stringStatic(Strings.CreateSessionReclistScreenFailure),
                )
                finished = false
            }
            .getOrNull() ?: return
        navigator.replace(SessionScreen(session))
    }
}

@Composable
fun CreateSessionReclistScreen.rememberCreateSessionReclistScreenModel(): CreateSessionReclistScreenModel {
    val sessionRepository = LocalSessionRepository.current
    val reclistRepository = LocalReclistRepository.current
    val navigator = LocalNavigator.currentOrThrow
    val alertDialogController = LocalAlertDialogController.current
    return rememberScreenModel {
        CreateSessionReclistScreenModel(sessionRepository, reclistRepository, navigator, alertDialogController)
    }
}
