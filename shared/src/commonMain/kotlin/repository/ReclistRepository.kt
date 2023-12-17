package repository

import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import io.Paths
import io.reclistsDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import model.Reclist
import model.parseReclist
import util.Log

/**
 * A repository to manage reclist files.
 */
class ReclistRepository(private val appPreferenceRepository: AppPreferenceRepository) {
    private lateinit var folder: File
    private val map = mutableMapOf<String, Reclist>()
    private val _items = MutableStateFlow(emptyList<String>())
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val normalizeKanaNfc: Boolean
        get() = appPreferenceRepository.value.normalizeKanaNfc

    /**
     * The list of reclist names.
     */
    val items: StateFlow<List<String>> = _items

    init {
        init()
        coroutineScope.launch {
            appPreferenceRepository.flow.collect {
                map.clear()
            }
        }
    }

    /**
     * Initializes the repository.
     */
    fun init() {
        folder = Paths.reclistsDirectory.also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        map.clear()
        _items.value = emptyList()
    }

    /**
     * Imports a reclist from the given file.
     *
     * @return true if the import was successful, false otherwise.
     */
    fun import(file: File): Boolean {
        Log.i("ReclistRepository.import: ${file.absolutePath}")
        val reclist = parseReclist(file, normalizeKanaNfc)
            .onFailure {
                Log.e("ReclistRepository.import: failed to parse ${file.absolutePath}", it)
            }
            .getOrNull() ?: return false
        val name = file.nameWithoutExtension
        if (map.containsKey(name)) {
            Log.w("ReclistRepository.import: $name already exists, overwriting...")
        }
        map[name] = reclist
        file.copyTo(folder.resolve(file.name), overwrite = true)
        _items.value = listOf(name) + _items.value.minus(name)
        return true
    }

    /**
     * Fetches the list of reclists.
     */
    fun fetch() {
        val items = folder.listFiles()
            .filter { it.extension == "txt" }
            .map { it.nameWithoutExtension }
        _items.value = items
    }

    /**
     * Gets the reclist with the given name.
     */
    fun get(name: String): Reclist =
        map[name] ?: parseReclist(folder.resolve("$name.txt"), normalizeKanaNfc).getOrThrow()

    /**
     * Deletes the reclists with the given names.
     */
    fun delete(names: List<String>) {
        names.forEach { name ->
            val file = folder.resolve("$name.txt")
            file.delete()
        }
        _items.value = _items.value.filterNot { it in names }
    }
}

val LocalReclistRepository = staticCompositionLocalOf<ReclistRepository> { error("No ReclistRepository provided") }
