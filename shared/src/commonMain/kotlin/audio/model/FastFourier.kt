package audio.model

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Implementation of Fast Fourier Transform. The original code is from https://github.com/psambit9791/jdsp.
 */
class FastFourier(signal: DoubleArray) {
    private val signal = extendSignal(signal)
    private var output = Array(signal.size) { Complex(0.0, 0.0) }

    private fun extendSignal(signal: DoubleArray): DoubleArray {
        val power = ln(signal.size.toDouble()) / ln(2.0)
        val raisedPower = ceil(power)
        val newLength = 2.0.pow(raisedPower).roundToInt()
        return if (newLength != signal.size) {
            zeroPadSignal(signal, newLength - signal.size)
        } else {
            signal
        }
    }

    fun transform() {
        val n = signal.size
        val isNPowerOf2 = (n > 0) && (n and (n - 1) == 0)
        require(isNPowerOf2) { "The number of samples must be a power of 2" }
        val dataR = signal.copyOf()
        val dataI = DoubleArray(n)
        if (n == 1) {
            output = createComplexArray(dataR, dataI)
            return
        }
        if (n == 2) {
            val srcR0 = dataR[0]
            val srcR1 = dataR[1]
            val srcI0 = dataI[0]
            val srcI1 = dataI[1]

            dataR[0] = srcR0 + srcR1
            dataI[0] = srcI0 + srcI1
            dataR[1] = srcR0 - srcR1
            dataI[1] = srcI0 - srcI1
            output = createComplexArray(dataR, dataI)
            return
        }

        bitReversalShuffle2(dataR, dataI)

        var i0 = 0
        while (i0 < n) {
            val i1 = i0 + 1
            val i2 = i0 + 2
            val i3 = i0 + 3
            val srcR0 = dataR[i0]
            val srcI0 = dataI[i0]
            val srcR1 = dataR[i2]
            val srcI1 = dataI[i2]
            val srcR2 = dataR[i1]
            val srcI2 = dataI[i1]
            val srcR3 = dataR[i3]
            val srcI3 = dataI[i3]

            // 4-term DFT
            // X_0 = x_0 + x_1 + x_2 + x_3
            dataR[i0] = srcR0 + srcR1 + srcR2 + srcR3
            dataI[i0] = srcI0 + srcI1 + srcI2 + srcI3
            // X_1 = x_0 - x_2 + j * (x_3 - x_1)
            dataR[i1] = srcR0 - srcR2 + (srcI1 - srcI3)
            dataI[i1] = srcI0 - srcI2 + (srcR3 - srcR1)
            // X_2 = x_0 - x_1 + x_2 - x_3
            dataR[i2] = srcR0 - srcR1 + srcR2 - srcR3
            dataI[i2] = srcI0 - srcI1 + srcI2 - srcI3
            // X_3 = x_0 - x_2 + j * (x_1 - x_3)
            dataR[i3] = srcR0 - srcR2 + (srcI3 - srcI1)
            dataI[i3] = srcI0 - srcI2 + (srcR1 - srcR3)
            i0 += 4
        }

        var lastN0 = 4
        var lastLogN0 = 2
        while (lastN0 < n) {
            val n0 = lastN0 shl 1
            val logN0 = lastLogN0 + 1
            val wSubN0R: Double = W_SUB_N_R[logN0]
            val wSubN0I: Double = W_SUB_N_I[logN0]

            // Combine even/odd transforms of size lastN0 into a transform of size N0 (lastN0 * 2).
            var destEvenStartIndex = 0
            while (destEvenStartIndex < n) {
                val destOddStartIndex = destEvenStartIndex + lastN0
                var wSubN0ToRR = 1.0
                var wSubN0ToRI = 0.0
                for (r in 0 until lastN0) {
                    val grR = dataR[destEvenStartIndex + r]
                    val grI = dataI[destEvenStartIndex + r]
                    val hrR = dataR[destOddStartIndex + r]
                    val hrI = dataI[destOddStartIndex + r]

                    // dest[destEvenStartIndex + r] = Gr + WsubN0ToR * Hr
                    dataR[destEvenStartIndex + r] = (grR + wSubN0ToRR * hrR - wSubN0ToRI * hrI)
                    dataI[destEvenStartIndex + r] = grI + wSubN0ToRR * hrI + wSubN0ToRI * hrR
                    // dest[destOddStartIndex + r] = Gr - WsubN0ToR * Hr
                    dataR[destOddStartIndex + r] = grR - (wSubN0ToRR * hrR - wSubN0ToRI * hrI)
                    dataI[destOddStartIndex + r] = grI - (wSubN0ToRR * hrI + wSubN0ToRI * hrR)

                    // WsubN0ToR *= WsubN0R
                    val nextWsubN0ToRR = (wSubN0ToRR * wSubN0R - wSubN0ToRI * wSubN0I)
                    val nextWsubN0ToRI = wSubN0ToRR * wSubN0I + wSubN0ToRI * wSubN0R
                    wSubN0ToRR = nextWsubN0ToRR
                    wSubN0ToRI = nextWsubN0ToRI
                }
                destEvenStartIndex += n0
            }
            lastN0 = n0
            lastLogN0 = logN0
        }

        output = createComplexArray(dataR, dataI)
    }

    fun getMagnitude(): DoubleArray {
        return output.map { it.getAbs() }.take(output.size / 2 + 1).toDoubleArray()
    }
}

private fun zeroPadSignal(
    signal: DoubleArray,
    padSize: Int,
): DoubleArray {
    val paddedSignal = DoubleArray(signal.size + padSize) { 0.0 }
    signal.copyInto(paddedSignal, 0, 0, signal.size)
    return paddedSignal
}

private fun bitReversalShuffle2(
    a: DoubleArray,
    b: DoubleArray,
) {
    val n = a.size
    require(b.size == n)
    val halfOfN = n shr 1
    var j = 0
    for (i in 0 until n) {
        if (i < j) {
            // swap indices i & j
            var temp = a[i]
            a[i] = a[j]
            a[j] = temp
            temp = b[i]
            b[i] = b[j]
            b[j] = temp
        }
        var k = halfOfN
        while (k in 1..j) {
            j -= k
            k = k shr 1
        }
        j += k
    }
}

private fun createComplexArray(
    dataR: DoubleArray,
    dataI: DoubleArray,
): Array<Complex> {
    require(dataR.size == dataI.size)
    val n = dataR.size
    return Array(n) { i -> Complex(dataR[i], dataI[i]) }
}

private fun initializeTwiddleFactors(size: Int): Pair<DoubleArray, DoubleArray> {
    val r = DoubleArray(size)
    val i = DoubleArray(size)
    for (k in 0 until size) {
        r[k] = cos(2 * PI / 2.0.pow(k))
        i[k] = -sin(2 * PI / 2.0.pow(k))
    }
    return Pair(r, i)
}

private val twiddleFactors by lazy {
    initializeTwiddleFactors(63)
}

private val W_SUB_N_R get() = twiddleFactors.first
private val W_SUB_N_I get() = twiddleFactors.second

private class Complex(val r: Double, val i: Double) {
    fun getAbs(): Double {
        if (r.isNaN() || i.isNaN()) {
            return Double.NaN
        }
        if (r.isInfinite() || i.isInfinite()) {
            return Double.POSITIVE_INFINITY
        }
        return if (abs(r) < abs(i)) {
            if (i == 0.0) {
                return abs(r)
            }
            val q: Double = r / i
            abs(i) * sqrt(1 + q * q)
        } else {
            if (r == 0.0) {
                return abs(i)
            }
            val q: Double = i / r
            abs(r) * sqrt(1 + q * q)
        }
    }
}
