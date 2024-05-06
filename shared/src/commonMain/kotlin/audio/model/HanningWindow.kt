package audio.model

import kotlin.math.PI
import kotlin.math.cos

/**
 * Implementation of Hanning window.
 * The original code is from https://github.com/psambit9791/jdsp.
 */
class HanningWindow(private val len: Int, private val sym: Boolean = true) {
    var window: DoubleArray = generateWindow()

    private fun generateWindow(): DoubleArray {
        val w = doubleArrayOf(0.5, 0.5)
        val gc = GeneralCosineWindow(len, w, sym)
        return gc.window
    }
}

private class GeneralCosineWindow(
    private val len: Int,
    private val weights: DoubleArray,
    private val sym: Boolean = true,
) {
    private var extendVal = false
    var window: DoubleArray = generateWindow()
        private set

    private fun extend(
        length: Int,
        sym: Boolean,
    ): Int {
        return if (!sym) {
            this.extendVal = true
            length + 1
        } else {
            this.extendVal = false
            length
        }
    }

    private fun truncate(arr: DoubleArray): DoubleArray {
        return if (extendVal) {
            arr.copyOf(arr.size - 1)
        } else {
            arr
        }
    }

    private fun generateWindow(): DoubleArray {
        val tempLen: Int = extend(len, sym)
        val tempArr: DoubleArray = linspace(PI, PI, tempLen, true)
        val window = DoubleArray(tempLen)
        window.fill(0.0)
        for (i in weights.indices) {
            for (j in tempArr.indices) {
                window[j] = window[j] + weights[i] * cos(i * tempArr[j])
            }
        }
        return truncate(window)
    }
}

private fun linspace(
    start: Double,
    stop: Double,
    samples: Int,
    includeEnd: Boolean,
): DoubleArray {
    val time = DoubleArray(samples)
    val t: Double
    val span = stop - start
    var i = start
    t = if (includeEnd) {
        span / (samples - 1)
    } else {
        span / samples
    }
    var index = 0
    time[index] = i
    index = 1
    while (index < time.size) {
        i += t
        time[index] = i
        index++
    }
    if (includeEnd) {
        time[time.size - 1] = stop
    }
    return time
}
