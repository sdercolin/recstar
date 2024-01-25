package repository

import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import io.Paths
import io.reclistsDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.Reclist
import model.parseReclist
import util.Log

/**
 * A repository to manage reclist files.
 */
class ReclistRepository {
    private lateinit var folder: File
    private val map = mutableMapOf<String, Reclist>()
    private val _items = MutableStateFlow(emptyList<String>())

    /**
     * The list of reclist names.
     */
    val items: StateFlow<List<String>> = _items

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
        map[name] = reclist
        file.copyTo(folder.resolve(file.name), overwrite = true)
        commentFile?.copyTo(folder.resolve(commentFile.name), overwrite = true)
        _items.value = listOf(name) + _items.value.minus(name)
        sort()
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
        _items.value = items
        sort()
    }

    /**
     * Gets the reclist with the given name.
     */
    fun get(name: String): Reclist =
        map[name] ?: parseReclist(
            file = getFile(name),
            commentFile = getCommentFile(name),
        ).getOrThrow()

    /**
     * Deletes the reclists with the given names.
     */
    fun delete(names: List<String>) {
        names.forEach { name ->
            getFile(name).delete()
            getCommentFile(name)?.delete()
        }
        _items.value = _items.value.filterNot { it in names }
    }

    private fun sort() {
        _items.value = _items.value.sortedBy { it }
    }

    private fun getFile(name: String): File = folder.resolve("$name${Reclist.FILE_NAME_SUFFIX}")

    private fun getCommentFile(
        name: String,
        folder: File = this.folder,
    ): File? = folder.resolve("$name${Reclist.COMMENT_FILE_NAME_SUFFIX_EXTENSION}").takeIf { it.exists() }
}

val LocalReclistRepository = staticCompositionLocalOf<ReclistRepository> { error("No ReclistRepository provided") }
