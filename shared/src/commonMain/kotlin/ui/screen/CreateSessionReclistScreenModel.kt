package ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import model.ReclistItem
import model.SortingMethod
import repository.AppRecordRepository
import repository.LocalAppRecordRepository
import repository.LocalReclistRepository
import repository.LocalSessionRepository
import repository.ReclistRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.ItemListScreenModel
import ui.common.LocalAlertDialogController
import ui.common.requestConfirm
import ui.string.*
import util.Log

class CreateSessionReclistScreenModel(
    private val sessionRepository: SessionRepository,
    private val appRecordRepository: AppRecordRepository,
    private val reclistRepository: ReclistRepository,
    private val navigator: Navigator,
    private val alertDialogController: AlertDialogController,
) : ItemListScreenModel<ReclistItem>(
        alertDialogController,
        allowedSortingMethods = SortingMethod.entries.toList(),
        initialSortingMethod = appRecordRepository.value.reclistSortingMethod ?: SortingMethod.NameAsc,
        saveSortingMethod = { appRecordRepository.update { copy(reclistSortingMethod = it) } },
    ) {
    init {
        load()
    }

    private var finished = false

    override fun getDeleteAlertTitle(): String = stringStatic(Strings.CreateSessionReclistScreenDeleteItemsTitle)

    override fun getDeleteAlertMessage(count: Int): String =
        stringStatic(Strings.CreateSessionReclistScreenDeleteItemsMessage, count)

    @Composable
    override fun getItemsEmptyPlaceholder(): String = string(Strings.CreateSessionReclistScreenEmpty)

    override fun fetch() = reclistRepository.fetch()

    override val upstream: Flow<List<ReclistItem>>
        get() = reclistRepository.items

    override fun onDelete(items: List<ReclistItem>) = reclistRepository.delete(items.map { it.name })

    override fun onClick(item: ReclistItem) {
        if (finished) return
        finished = true

        fun onFailure(t: Throwable) {
            Log.e(t)
            alertDialogController.requestConfirm(
                message = stringStatic(Strings.CreateSessionReclistScreenFailure),
            )
            finished = false
        }

        val reclist = reclistRepository.get(item.name)
            .onFailure { onFailure(it) }
            .getOrNull() ?: return
        val session = sessionRepository.create(reclist)
            .onFailure { onFailure(it) }
            .getOrNull() ?: return
        reclistRepository.updateUsedTime(item.name)
        navigator.replace(SessionScreen(session))
    }
}

@Composable
fun CreateSessionReclistScreen.rememberCreateSessionReclistScreenModel(): CreateSessionReclistScreenModel {
    val sessionRepository = LocalSessionRepository.current
    val appRecordRepository = LocalAppRecordRepository.current
    val reclistRepository = LocalReclistRepository.current
    val navigator = LocalNavigator.currentOrThrow
    val alertDialogController = LocalAlertDialogController.current
    return rememberScreenModel {
        CreateSessionReclistScreenModel(
            sessionRepository,
            appRecordRepository,
            reclistRepository,
            navigator,
            alertDialogController,
        )
    }
}
