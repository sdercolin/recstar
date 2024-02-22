package repository

import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import io.Paths
import io.reclistRecordFile
import io.reclistsDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.Reclist
import model.ReclistItem
import model.parseReclist
import util.DateTime
import util.Log

/**
 * A repository to manage reclist files.
 */
class ReclistRepository(
    private val _items: MutableStateFlow<List<ReclistItem>> = MutableStateFlow(emptyList()),
) : ItemUsedTimeRepository<ReclistItem> by ItemUsedTimeRepositoryImpl(
        recordFile = Paths.reclistRecordFile,
        updateItems = { update ->
            _items.value = update(_items.value)
        },
    ) {
    private lateinit var folder: File
    private val map = mutableMapOf<String, Reclist>()

    /**
     * The list of reclist references.
     */
    val items: StateFlow<List<ReclistItem>> = _items

    init {
        init()
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
        loadUsedTimes()
    }

    /**
     * Imports a reclist from the given file.
     *
     * @return true if the import was successful, false otherwise.
     */
    fun import(
        file: File,
        commentFile: File?,
    ): Boolean {
        val name = file.nameWithoutExtension
        Log.i("ReclistRepository.import: ${file.absolutePath}, commentFile: ${commentFile?.absolutePath}")
        val reclist = parseReclist(file, commentFile)
            .onFailure {
                Log.e("ReclistRepository.import: failed to parse ${file.absolutePath}", it)
            }
            .getOrNull() ?: return false
        if (map.containsKey(name)) {
            Log.w("ReclistRepository.import: $name already exists, overwriting...")
        }
        file.copyTo(folder.resolve(file.name), overwrite = true)
        commentFile?.copyTo(getCommentFile(name, folder), overwrite = true)
        val newItem = ReclistItem(name, DateTime.getNow())
        val items = _items.value.toMutableList()
        items.removeAll { it.name == name }
        items.add(newItem)
        _items.value = items
        map[name] = reclist
        saveUsedTime(name, newItem.lastUsed)
        return true
    }

    /**
     * Fetches the list of reclists.
     */
    fun fetch() {
        val items = folder.listFiles()
            .filter { it.extension == Reclist.FILE_EXTENSION }
            .filterNot { it.name.endsWith(Reclist.COMMENT_FILE_NAME_SUFFIX_EXTENSION) }
            .map { it.nameWithoutExtension }
            .map { ReclistItem(it, getUsedTime(it)) }
        _items.value = items
    }

    /**
     * Gets the reclist with the given name.
     */
    fun get(name: String): Reclist =
        map[name] ?: parseReclist(
            file = getFile(name),
            commentFile = getCommentFile(name).takeIf { it.exists() },
        ).getOrThrow()

    /**
     * Deletes the reclists with the given names.
     */
    fun delete(names: List<String>) {
        names.forEach { name ->
            getFile(name).delete()
            getCommentFile(name).takeIf { it.exists() }?.delete()
        }
        _items.value = _items.value.filterNot { it.name in names }
    }

    private fun getFile(name: String): File = folder.resolve("$name${Reclist.FILE_NAME_SUFFIX}")

    private fun getCommentFile(
        name: String,
        folder: File = this.folder,
    ): File = folder.resolve("$name${Reclist.COMMENT_FILE_NAME_SUFFIX_EXTENSION}")
}

val LocalReclistRepository = staticCompositionLocalOf<ReclistRepository> { error("No ReclistRepository provided") }
