package repository

import androidx.compose.runtime.staticCompositionLocalOf
import io.File
import io.Paths
import io.guideAudioDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.GuideAudio
import model.createGuideAudioConfig
import util.DateTime
import util.Log
import util.parseJson
import util.stringifyJson

/**
 * A repository to manage guide audio files.
 */
class GuideAudioRepository {
    private val folder = Paths.guideAudioDirectory.also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }

    private val map = mutableMapOf<String, GuideAudio>()
    private val _items = MutableStateFlow(emptyList<String>())

    /**
     * The list of existing guide audio names.
     */
    val items: StateFlow<List<String>> = _items

    /**
     * Fetches the list of guide audio names.
     */
    fun fetch() {
        val items = folder.listFiles()
            .filter { it.name.endsWith(GUIDE_AUDIO_CONFIG_FILE_NAME_SUFFIX) }
            .map { it.name.removeSuffix(".$GUIDE_AUDIO_CONFIG_FILE_NAME_SUFFIX") }
        _items.value = items
    }

    /**
     * Imports a guide audio from the given files.
     *
     * @return true if the import was successful, false otherwise.
     */
    fun import(
        audioFile: File,
        rawConfigFile: File?,
        findConfig: Boolean,
    ): Boolean {
        if (audioFile.isFile.not() || audioFile.extension != GUIDE_AUDIO_FILE_EXTENSION) {
            Log.e("GuideAudioRepository.import: invalid audio file ${audioFile.absolutePath}")
            return false
        }
        val resolvedRawConfigFile = if (rawConfigFile == null && findConfig) {
            val configFileName = "${audioFile.nameWithoutExtension}.$GUIDE_AUDIO_RAW_CONFIG_FILE_EXTENSION"
            audioFile.parentFile?.resolve(configFileName)
        } else {
            rawConfigFile
        }
        val config = createGuideAudioConfig(audioFile, resolvedRawConfigFile)
            .onSuccess {
                val importedWav = folder.resolve("${it.name}.$GUIDE_AUDIO_FILE_EXTENSION")
                audioFile.copyTo(importedWav, overwrite = true)
                audioFile.lastModified = DateTime.getNow()
                val importedConfig = it.copy(path = importedWav.absolutePath)
                val importedConfigFile = folder.resolve("${it.name}.$GUIDE_AUDIO_CONFIG_FILE_NAME_SUFFIX")
                importedConfigFile.writeText(importedConfig.stringifyJson())
                Log.i("GuideAudioRepository.import: saved to ${importedConfig.path}")
            }
            .onFailure { t ->
                Log.e("GuideAudioRepository.import: failed to import ${audioFile.absolutePath}", t)
            }
            .getOrNull() ?: return false
        map[config.name] = config
        _items.value = listOf(config.name) + _items.value.minus(config.name)
        return true
    }

    /**
     * Gets the guide audio item with the given name.
     */
    fun get(name: String): GuideAudio =
        map[name]
            ?: folder.resolve("$name.$GUIDE_AUDIO_CONFIG_FILE_NAME_SUFFIX").readText().parseJson<GuideAudio>()
                .also { map[name] = it }

    /**
     * Deletes the guide audio item with the given name.
     */
    fun delete(names: List<String>) {
        names.forEach { name ->
            val file = folder.resolve("$name.$GUIDE_AUDIO_CONFIG_FILE_NAME_SUFFIX")
            val audioFile = folder.resolve("$name.$GUIDE_AUDIO_FILE_EXTENSION")
            file.delete()
            audioFile.delete()
        }
        _items.value = _items.value.filterNot { it in names }
    }

    companion object {
        private const val GUIDE_AUDIO_CONFIG_FILE_NAME_SUFFIX = "config.json"
        const val GUIDE_AUDIO_RAW_CONFIG_FILE_EXTENSION = "txt"
        const val GUIDE_AUDIO_FILE_EXTENSION = "wav"
    }
}

val LocalGuideAudioRepository =
    staticCompositionLocalOf<GuideAudioRepository> { error("No GuideAudioRepository provided") }
