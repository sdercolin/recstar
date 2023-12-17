package model

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import io.File
import io.Paths
import io.guideAudioDirectory
import kotlinx.serialization.Serializable
import model.GuideAudio.Node

/**
 * Model for a guide audio.
 *
 * @property name The name of the guide audio.
 * @property path The path to the guide audio file, relative to the guide audio directory.
 * @property nodes Control nodes of the guide audio. See [Node] for details.
 */
@Immutable
@Serializable
data class GuideAudio(
    val name: String,
    val path: String,
    val nodes: List<Node>,
) : JavaSerializable {
    fun getFile(): File = Paths.guideAudioDirectory.resolve(path)

    /**
     * A control node of the guide audio.
     *
     * @property timeMs The time position of the node in milliseconds. If null, the node is placed at the end of the
     *     audio.
     * @property isRecordingStart Whether the node triggers the start of recording.
     * @property isRecordingEnd Whether the node triggers the end of recording.
     * @property isSwitching Whether the node triggers switching to the next sentence.
     * @property repeatTargetNodeIndex The index of the node to seek to when the node is reached.
     * @property comment The comment of the node.
     */
    @Immutable
    @Serializable
    data class Node(
        val timeMs: Long? = null,
        val isRecordingStart: Boolean = false,
        val isRecordingEnd: Boolean = false,
        val isSwitching: Boolean = false,
        val repeatTargetNodeIndex: Int? = null,
        val comment: String? = null,
    ) : JavaSerializable

    val switchingNode get() = nodes.firstOrNull { it.isSwitching }
    val repeatStartingNode
        get() = nodes.firstOrNull { it.repeatTargetNodeIndex != null }?.repeatTargetNodeIndex?.let {
            nodes[it]
        }
}

private fun createDefault(audioFile: File) =
    GuideAudio(
        name = audioFile.nameWithoutExtension,
        path = audioFile.name,
        nodes = listOf(
            Node(timeMs = 0, isRecordingStart = true),
            Node(timeMs = null, isRecordingEnd = true, isSwitching = true, repeatTargetNodeIndex = 0),
        ),
    )

/**
 * Creates a [GuideAudio] from the given audio file. If an OREMO guide BGM [rawConfigFile] exists, it will be imported.
 * Otherwise, a default config will be created.
 */
fun createGuideAudioConfig(
    audioFile: File,
    rawConfigFile: File?,
): Result<GuideAudio> =
    runCatching {
        if (rawConfigFile == null) {
            return@runCatching createDefault(audioFile)
        }
        val lines = rawConfigFile.readTextDetectEncoding().lines()
        val timeRate = when (val unit = lines.first()) {
            "sec" -> 1000L
            "msec" -> 1L
            else -> throw IllegalArgumentException("Unsupported time unit: $unit}")
        }
        val nodeLines = lines
            .asSequence()
            .drop(1)
            .filterNot { it.startsWith("#") }
            .filter { it.isNotBlank() }
            .map { line -> line.split(",").map { it.trim() } }
            .sortedBy { it.first().toInt() }
            .toList()
        val nodeIndexes = nodeLines.map { it.first().toInt() - 1 }
        require(nodeIndexes == nodeLines.indices.toList()) {
            "Invalid row numbers: ${nodeIndexes.joinToString(", ")}"
        }
        val nodes = nodeLines.map { line ->
            val timeMs = (line[1].toDouble() * timeRate).toLong()
            val isRecordingStart = line.getOrNull(2)?.toInt() == 1
            val isRecordingEnd = line.getOrNull(3)?.toInt() == 1
            val isSwitching = line.getOrNull(4)?.toInt() == 1
            val repeatTargetNodeIndex = line.getOrNull(5)?.toInt()?.takeIf { it > 0 }?.let { it - 1 }
            val comment = line.getOrNull(6)
            Node(
                timeMs = timeMs,
                isRecordingStart = isRecordingStart,
                isRecordingEnd = isRecordingEnd,
                isSwitching = isSwitching,
                repeatTargetNodeIndex = repeatTargetNodeIndex,
                comment = comment,
            )
        }
        GuideAudio(audioFile.nameWithoutExtension, audioFile.name, nodes)
    }
