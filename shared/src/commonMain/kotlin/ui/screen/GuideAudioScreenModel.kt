package ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import repository.GuideAudioRepository
import repository.LocalGuideAudioRepository
import repository.LocalSessionRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.EditableListConfig
import ui.common.EditableListScreenModel
import ui.common.EditableListScreenModelImpl
import ui.common.LocalAlertDialogController
import ui.string.*

class GuideAudioScreenModel(
    private val sessionName: String,
    private val sessionRepository: SessionRepository,
    private val guideAudioRepository: GuideAudioRepository,
    private val alertDialogController: AlertDialogController,
) : ScreenModel,
    EditableListScreenModel<String> by EditableListScreenModelImpl(
        alertDialogController,
        EditableListConfig(
            deleteAlertTitle = { stringStatic(Strings.GuideAudioScreenDeleteItemsTitle) },
            deleteAlertMessage = { count -> stringStatic(Strings.GuideAudioScreenDeleteItemsMessage, count) },
            onDelete = { guideAudioRepository.delete(it) },
        ),
    ) {
    val names: StateFlow<List<String>> = guideAudioRepository.items
    private var selected: String? by mutableStateOf(
        sessionRepository.get(sessionName).getOrNull()?.guideAudioConfig?.name,
    )

    init {
        guideAudioRepository.fetch()
    }

    fun isSelected(name: String): Boolean = selected == name

    private var job: Job? = null

    fun select(name: String) {
        job?.cancel()
        val newName = if (selected == name) null else name
        job = screenModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.get(sessionName).getOrThrow()
            val newConfig = newName?.let { guideAudioRepository.get(it) }
            val updated = session.copy(guideAudioConfig = newConfig)
            sessionRepository.update(updated)
            selected = newName
        }
    }

    override fun onDispose() {
        job?.cancel()
    }
}

@Composable
fun GuideAudioScreen.rememberGuideAudioScreenModel(): GuideAudioScreenModel {
    val sessionRepository = LocalSessionRepository.current
    val guideAudioRepository = LocalGuideAudioRepository.current
    val alertDialogController = LocalAlertDialogController.current
    return rememberScreenModel {
        GuideAudioScreenModel(sessionName, sessionRepository, guideAudioRepository, alertDialogController)
    }
}
