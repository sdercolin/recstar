package repository

import androidx.compose.runtime.staticCompositionLocalOf
import io.Paths
import io.sessionsDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.Reclist
import model.Session
import model.SessionParams
import model.toParams
import util.DateTime
import util.parseJson
import util.stringifyJson

/**
 * A repository to manage sessions.
 */
class SessionRepository(private val reclistRepository: ReclistRepository) {
    private val folder = Paths.sessionsDirectory.also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }

    private val _items = MutableStateFlow(emptyList<String>())

    /**
     * The list of existing session names.
     */
    val items: StateFlow<List<String>> = _items

    /**
     * Fetches the list of sessions.
     */
    fun fetch() {
        val items = folder.listFiles()
            .filter { it.isDirectory }
            .filter { it.resolve(SESSION_PARAMS_FILE_NAME).isFile }
            .map { it.name }
        _items.value = items
    }

    /**
     * Creates a new session with the given reclist.
     */
    fun create(reclist: Reclist): Result<Session> =
        runCatching {
            val timeSuffix = DateTime.getNowReadableString()
                .replace(" ", "-")
                .replace(":", "-")
            val defaultName = "${reclist.name}-$timeSuffix"
            var name = defaultName
            var repeat = 0
            while (name in _items.value) {
                repeat++
                name = "$defaultName-$repeat"
            }
            Session(
                name,
                reclist,
                folder.resolve(name).absolutePath,
            )
        }.onSuccess {
            _items.value = listOf(it.name) + _items.value
            save(it)
        }

    /**
     * Gets the session with the given name.
     */
    fun get(name: String): Result<Session> =
        runCatching {
            val directory = folder.resolve(name)
            val file = directory.resolve(SESSION_PARAMS_FILE_NAME)
            val params = file.readText().parseJson<SessionParams>()
            val reclist = reclistRepository.get(params.reclistName)
            Session(
                name,
                reclist,
                directory.absolutePath,
            )
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
}

private const val SESSION_PARAMS_FILE_NAME = "session.json"

val LocalSessionRepository = staticCompositionLocalOf<SessionRepository> {
    error("No SessionRepository provided")
}
