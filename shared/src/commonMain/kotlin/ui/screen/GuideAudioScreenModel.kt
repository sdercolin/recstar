package ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import model.GuideAudioItem
import model.SortingMethod
import repository.AppRecordRepository
import repository.GuideAudioRepository
import repository.LocalAppRecordRepository
import repository.LocalGuideAudioRepository
import repository.LocalSessionRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.ItemListScreenModel
import ui.common.LocalAlertDialogController
import ui.string.*

class GuideAudioScreenModel(
    private val sessionName: String,
    private val sessionRepository: SessionRepository,
    private val appRecordRepository: AppRecordRepository,
    private val guideAudioRepository: GuideAudioRepository,
    alertDialogController: AlertDialogController,
) : ItemListScreenModel<GuideAudioItem>(
        alertDialogController,
        allowedSortingMethods = SortingMethod.entries.toList(),
        initialSortingMethod = appRecordRepository.value.guideAudioSortingMethod ?: SortingMethod.NameAsc,
        saveSortingMethod = { appRecordRepository.update { copy(guideAudioSortingMethod = it) } },
    ) {
    private var job: Job? = null

    private var selected: String? by mutableStateOf(
        sessionRepository.get(sessionName).getOrNull()?.guideAudioConfig?.name,
    )

    override val isItemSelectable: Boolean = true

    override fun isItemSelected(name: String): Boolean = selected == name

    init {
        load()
    }

    override fun getDeleteAlertTitle(): String = stringStatic(Strings.GuideAudioScreenDeleteItemsTitle)

    override fun getDeleteAlertMessage(count: Int): String =
        stringStatic(Strings.GuideAudioScreenDeleteItemsMessage, count)

    @Composable
    override fun getItemsEmptyPlaceholder(): String = string(Strings.GuideAudioScreenEmpty)

    override fun fetch() = guideAudioRepository.fetch()

    override val upstream: Flow<List<GuideAudioItem>>
        get() = guideAudioRepository.items

    override fun onDelete(items: List<GuideAudioItem>) {
        guideAudioRepository.delete(items.map { it.name })
    }

    override fun onClick(item: GuideAudioItem) {
        job?.cancel()
        val newName = if (selected == item.name) null else item.name
        job = screenModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.get(sessionName).getOrThrow()
            val newConfig = newName?.let { guideAudioRepository.get(it) }
            val updated = session.copy(guideAudioConfig = newConfig)
            sessionRepository.update(updated)
            selected = newName
            if (newName != null) {
                guideAudioRepository.updateUsedTime(newName)
            }
        }
    }

    override fun onDispose() {
        job?.cancel()
    }
}

@Composable
fun GuideAudioScreen.rememberGuideAudioScreenModel(): GuideAudioScreenModel {
    val sessionRepository = LocalSessionRepository.current
    val appRecordRepository = LocalAppRecordRepository.current
    val guideAudioRepository = LocalGuideAudioRepository.current
    val alertDialogController = LocalAlertDialogController.current
    return rememberScreenModel {
        GuideAudioScreenModel(
            sessionName,
            sessionRepository,
            appRecordRepository,
            guideAudioRepository,
            alertDialogController,
        )
    }
}
