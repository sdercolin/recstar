package ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import model.sorting.SortingMethod
import repository.AppRecordRepository
import repository.LocalAppRecordRepository
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
    private val appRecordRepository: AppRecordRepository,
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
    private var totalCount: Int = 0
    private val _sessions: MutableStateFlow<List<SessionRepository.Item>> = MutableStateFlow(emptyList())
    val sessions: Flow<List<SessionRepository.Item>> = _sessions

    private fun List<SessionRepository.Item>.mapSessions(): List<SessionRepository.Item> =
        filter { session -> searchText.isEmpty() || session.name.contains(searchText) }
            .let { sortingMethod.sort(it) }

    private fun updateSessions() {
        screenModelScope.launch {
            _sessions.value = sessionRepository.items.first().mapSessions()
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

    private val searchTextState = mutableStateOf("")
    var searchText: String
        get() = searchTextState.value
        set(value) {
            searchTextState.value = value
            updateSessions()
        }

    val allowedSortingMethods: List<SortingMethod> = SortingMethod.entries.toList()

    fun hasSessions(): Boolean = totalCount > 0

    private val sortingMethodState =
        mutableStateOf(appRecordRepository.value.sessionSortingMethod ?: SortingMethod.UsedDesc)
    var sortingMethod: SortingMethod
        get() = sortingMethodState.value
        set(value) {
            sortingMethodState.value = value
            appRecordRepository.update { copy(sessionSortingMethod = value) }
            updateSessions()
        }

    init {
        sessionRepository.fetch()
        screenModelScope.launch {
            sessionRepository.items.collect { items ->
                _sessions.value = items.mapSessions()
                totalCount = items.size
            }
        }
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
