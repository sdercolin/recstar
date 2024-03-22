package repository

import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import io.Paths
import io.reclistCommentEncodingRecordFile
import io.reclistEncodingRecordFile
import io.reclistRecordFile
import io.reclistsDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.Reclist
import model.ReclistItem
import model.parseReclist
import util.DateTime
import util.Encoding
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
    ),
    ItemEncodingRepository by ItemEncodingRepositoryImpl(
        categoryMapFileMap = mapOf(
            ENCODING_CATEGORY_RECLIST to Paths.reclistEncodingRecordFile,
            ENCODING_CATEGORY_COMMENT to Paths.reclistCommentEncodingRecordFile,
        ),
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
     */
    fun import(
        file: File,
        fileEncoding: Encoding?,
        commentFile: File?,
        commentFileEncoding: Encoding?,
    ): Result<Unit> {
        val name = file.nameWithoutExtension
        Log.i(
            "ReclistRepository.import: ${file.absolutePath}, commentFile: ${commentFile?.absolutePath}" +
                ", fileEncoding: $fileEncoding, commentFileEncoding: $commentFileEncoding",
        )
        val reclist = parseReclist(file, fileEncoding, commentFile, commentFileEncoding)
            .getOrElse {
                Log.e(it)
                return Result.failure(it)
            }
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
        putItemEncoding(ENCODING_CATEGORY_RECLIST, name, fileEncoding)
        if (commentFile != null) {
            putItemEncoding(ENCODING_CATEGORY_COMMENT, name, commentFileEncoding)
        }
        return Result.success(Unit)
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
    fun get(name: String): Result<Reclist> =
        map[name]?.let { Result.success(it) } ?: parseReclist(
            file = getFile(name),
            fileEncoding = getItemEncoding(ENCODING_CATEGORY_RECLIST, name),
            commentFile = getCommentFile(name).takeIf { it.exists() },
            commentFileEncoding = getItemEncoding(ENCODING_CATEGORY_COMMENT, name),
        ).onSuccess {
            map[name] = it
        }

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

private const val ENCODING_CATEGORY_RECLIST = "reclist"
private const val ENCODING_CATEGORY_COMMENT = "comment"
