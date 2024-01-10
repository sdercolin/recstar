package repository

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import exception.SessionRenameExistingException
import exception.SessionRenameInvalidException
import io.File
import io.Paths
import io.sessionUsageRecordFile
import io.sessionsDirectory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import model.Reclist
import model.Session
import model.SessionParams
import model.sorting.Sortable
import model.sorting.SortableListOwner
import model.sorting.SortingMethod
import model.toParams
import util.DateTime
import util.Log
import util.isValidFileName
import util.parseJson
import util.stringifyJson

/**
 * A repository to manage sessions.
 */
class SessionRepository(
    private val reclistRepository: ReclistRepository,
    private val guideAudioRepository: GuideAudioRepository,
    private val appRecordRepository: AppRecordRepository,
) : SortableListOwner<SessionRepository.Item> {
    @Immutable
    @Serializable
    data class Item(
        val name: String,
        val lastUsed: Long,
    ) : Sortable<Item>, JavaSerializable {
        override val sortableName: String
            get() = name

        override val sortableUsedTime: Long
            get() = lastUsed
    }

    private lateinit var folder: File

    private val usedTimeMap: MutableMap<String, Long> = mutableMapOf()

    private fun getUsedTime(name: String): Long = usedTimeMap[name] ?: 0L

    private fun saveUsedTime(
        name: String,
        time: Long,
    ) {
        usedTimeMap[name] = time
        runCatching {
            Paths.sessionUsageRecordFile.writeText(usedTimeMap.stringifyJson())
        }.onFailure {
            Log.w(it)
        }
    }

    private val map = mutableMapOf<String, Session>()
    private val _items = MutableStateFlow(emptyList<Item>())

    /**
     * The list of existing session references.
     */
    val items: StateFlow<List<Item>> = _items

    private val _sessionUpdated = MutableSharedFlow<String>()

    /**
     * A flow of session names that have been updated.
     */
    val sessionUpdated: Flow<String> = _sessionUpdated

    init {
        init()
    }

    /**
     * Initializes the repository.
     */
    fun init() {
        folder = Paths.sessionsDirectory.also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        map.clear()
        _items.value = emptyList()
        runCatching {
            Paths.sessionUsageRecordFile.takeIf { it.exists() }?.readText()?.parseJson<Map<String, Long>>()
                ?.toMutableMap() ?: mutableMapOf()
        }.onSuccess { map ->
            usedTimeMap.clear()
            usedTimeMap.putAll(map)
        }.onFailure {
            Log.w(it)
        }
    }

    /**
     * Fetches the list of sessions.
     */
    fun fetch() {
        val items = folder.listFiles()
            .filter { it.isDirectory }
            .filter { it.resolve(SESSION_PARAMS_FILE_NAME).isFile }
            .map { Item(it.name, getUsedTime(it.name)) }
        _items.value = items
        sort()
    }

    /**
     * Creates a new session with the given reclist.
     */
    fun create(reclist: Reclist): Result<Session> =
        runCatching {
            val timeSuffix = DateTime.getNowReadableString(withTime = false)
            val defaultName = "${reclist.name} $timeSuffix"
            var name = defaultName
            var repeat = 0
            while (name in _items.value.map { it.name }) {
                repeat++
                name = "$defaultName ($repeat)"
            }
            Session(
                name,
                reclist,
                folder.resolve(name).absolutePath,
            )
        }.onSuccess { newSession ->
            val newItem = Item(newSession.name, DateTime.getNow())
            val items = _items.value.toMutableList()
            items.removeAll { it.name == newItem.name }
            items.add(newItem)
            _items.value = items
            sort()
            map[newSession.name] = newSession
            saveUsedTime(newSession.name, newItem.lastUsed)
            save(newSession)
        }

    private fun createFromParams(
        name: String,
        directory: File,
        params: SessionParams,
    ): Session {
        val reclist = reclistRepository.get(params.reclistName)
        val guideAudioConfig = params.guideAudioName?.let { guideAudioRepository.get(it) }
        return Session(
            name = name,
            reclist = reclist,
            locationPath = directory.absolutePath,
            guideAudioConfig = guideAudioConfig,
            skipFinishedSentence = params.skipFinishedSentence,
        )
    }

    /**
     * Gets the session with the given name.
     */
    fun get(name: String): Result<Session> =
        runCatching {
            map[name]?.let { return@runCatching it }
            val directory = folder.resolve(name)
            val file = directory.resolve(SESSION_PARAMS_FILE_NAME)
            val params = file.readText().parseJson<SessionParams>()
            createFromParams(name, directory, params)
        }.onSuccess {
            map[name] = it
        }

    /**
     * Updates the used time of the session with the given name to the current time.
     */
    fun updateUsedTime(name: String) {
        _items.value = _items.value.map {
            if (it.name == name) {
                Item(name, DateTime.getNow())
            } else {
                it
            }
        }
        sort()
        saveUsedTime(name, DateTime.getNow())
    }

    /**
     * Rename the session from [oldName] to [newName]. This will cause the session folder to be renamed as well.
     */
    fun rename(
        oldName: String,
        newName: String,
    ): Result<Session> =
        runCatching {
            if (oldName == newName) {
                return@runCatching get(oldName).getOrThrow()
            }
            if (newName.isValidFileName().not()) {
                throw SessionRenameInvalidException(newName)
            }
            val oldDirectory = folder.resolve(oldName)
            val newDirectory = folder.resolve(newName)
            if (newDirectory.exists()) {
                throw SessionRenameExistingException(newName)
            }
            oldDirectory.copyTo(newDirectory)
            oldDirectory.delete()
            val file = newDirectory.resolve(SESSION_PARAMS_FILE_NAME)
            val params = file.readText().parseJson<SessionParams>()
            createFromParams(
                name = newName,
                directory = newDirectory,
                params = params,
            )
        }.onSuccess { newSession ->
            val items = _items.value.toMutableList()
            items.removeAll { it.name == oldName }
            val newItem = Item(newName, DateTime.getNow())
            items.add(newItem)
            _items.value = items
            map.remove(oldName)
            map[newItem.name] = newSession
            save(newSession)
            sort()
            saveUsedTime(newName, newItem.lastUsed)
        }

    /**
     * Updates the session with the given name. The name of the session cannot be changed.
     */
    suspend fun update(session: Session) {
        map[session.name] = session
        save(session)
        _sessionUpdated.emit(session.name)
    }

    /**
     * Deletes the given sessions.
     */
    fun delete(names: List<String>) {
        names.forEach { name ->
            val directory = folder.resolve(name)
            directory.delete()
            map.remove(name)
        }
        _items.value = _items.value.filterNot { it.name in names }
    }

    private fun save(session: Session) {
        val directory = folder.resolve(session.name)
        if (directory.exists().not()) {
            directory.mkdirs()
        }
        val file = directory.resolve(SESSION_PARAMS_FILE_NAME)
        val params = session.toParams()
        file.writeText(params.stringifyJson())
    }

    override val allowedSortingMethods: List<SortingMethod> = SortingMethod.entries.toList()
    override var sortingMethod: SortingMethod =
        appRecordRepository.value.sessionSortingMethod ?: SortingMethod.UsedDesc
        set(value) {
            field = value
            sort()
            appRecordRepository.update { copy(sessionSortingMethod = value) }
        }
    override var sortableList: List<Item>
        get() = _items.value.toMutableList()
        set(value) {
            _items.value = value
        }
}

private const val SESSION_PARAMS_FILE_NAME = "session.json"

val LocalSessionRepository = staticCompositionLocalOf<SessionRepository> {
    error("No SessionRepository provided")
}
