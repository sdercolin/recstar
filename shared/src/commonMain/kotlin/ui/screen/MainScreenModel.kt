package ui.screen

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import model.SessionItem
import model.SortingMethod
import repository.AppRecordRepository
import repository.LocalAppRecordRepository
import repository.LocalSessionRepository
import repository.SessionRepository
import ui.common.AlertDialogController
import ui.common.ItemListScreenModel
import ui.common.LocalAlertDialogController
import ui.string.*
import util.Log

class MainScreenModel(
    private val sessionRepository: SessionRepository,
    private val appRecordRepository: AppRecordRepository,
    private val navigator: Navigator,
    alertDialogController: AlertDialogController,
) : ItemListScreenModel<SessionItem>(
        alertDialogController,
        allowedSortingMethods = SortingMethod.entries.toList(),
        initialSortingMethod = appRecordRepository.value.sessionSortingMethod ?: SortingMethod.UsedDesc,
        saveSortingMethod = { appRecordRepository.update { copy(sessionSortingMethod = it) } },
    ) {
    init {
        load()
    }

    override fun getDeleteAlertTitle(): String = stringStatic(Strings.MainScreenDeleteItemsTitle)

    override fun getDeleteAlertMessage(count: Int): String = stringStatic(Strings.MainScreenDeleteItemsMessage, count)

    @Composable
    override fun getItemsEmptyPlaceholder(): String = string(Strings.MainScreenEmpty)

    override fun fetch() = sessionRepository.fetch()

    override val upstream: Flow<List<SessionItem>> get() = sessionRepository.items

    override fun onClick(item: SessionItem) {
        val session = sessionRepository.get(item.name)
            .getOrElse {
                Log.e("Failed to get session ${item.name}", it)
                return
            }
        navigator push SessionScreen(session)
    }

    override fun onDelete(items: List<SessionItem>) {
        sessionRepository.delete(items.map { it.name })
    }
}

@Composable
fun MainScreen.rememberMainScreenModel(): MainScreenModel {
    val sessionRepository = LocalSessionRepository.current
    val appRecordRepository = LocalAppRecordRepository.current
    val navigator = LocalNavigator.currentOrThrow
    val alertDialogController = LocalAlertDialogController.current
    return rememberScreenModel {
        MainScreenModel(sessionRepository, appRecordRepository, navigator, alertDialogController)
    }
}
