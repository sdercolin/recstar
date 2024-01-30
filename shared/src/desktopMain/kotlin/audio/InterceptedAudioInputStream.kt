package audio

import kotlinx.coroutines.flow.MutableStateFlow
import util.toShortArray
import java.io.IOException
import java.io.InputStream
import javax.sound.sampled.TargetDataLine

class InterceptedAudioInputStream(
    private val line: TargetDataLine,
    bufferSize: Int,
    private val flow: MutableStateFlow<Array<FloatArray>>,
) : InputStream() {
    private val buffer: ByteArray = ByteArray(bufferSize)
    private val shortBuffer = ShortArray(bufferSize / 2)
    private var bytesRead = 0

    private val data = mutableListOf<Float>()

    @Throws(IOException::class)
    override fun read(): Int {
        if (bytesRead <= 0) {
            bytesRead = line.read(buffer, 0, buffer.size)
            if (bytesRead <= 0) {
                return -1
            }
            buffer.toShortArray(shortBuffer, isLittleEndian = true)
            data.addAll(shortBuffer.map { it.toFloat() / Short.MAX_VALUE })
            flow.value = data.map { arrayOf(it).toFloatArray() }.toTypedArray()
        }
        val value = buffer[buffer.size - bytesRead].toInt() and 0xFF
        bytesRead--
        return value
    }
}
