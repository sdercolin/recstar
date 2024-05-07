import audio.model.FastFourier
import kotlin.test.Test
import kotlin.test.assertContentEquals

class FastFourierTest {
    @Test
    fun testFastFourier() {
        dataSets.forEach { signal ->
            val fft = FastFourier(signal)
            fft.transform()
            val result = fft.getMagnitude()
            val expected = getExpected(signal)
            assertContentEquals(expected, result)
        }
    }

    private fun getExpected(signal: DoubleArray): DoubleArray {
        val jdspFft = com.github.psambit9791.jdsp.transform.FastFourier(signal)
        jdspFft.transform()
        return jdspFft.getMagnitude(true)
    }
}

private val dataSets = listOf(
    doubleArrayOf(0.0, 0.0, 0.0, 0.0),
    doubleArrayOf(1.0, 2.0, 3.0, 4.0),
    doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0),
    doubleArrayOf(0.0, 1.0, 0.0, 0.0),
    doubleArrayOf(1.0, -1.0, 1.0, -1.0),
    doubleArrayOf(1.0, 0.5, -0.5, -1.0, -0.5, 0.5),
    doubleArrayOf(0.0, 1.0, 0.0, -1.0, 0.0, 1.0, 0.0, -1.0),
    doubleArrayOf(1.0, 0.0, -1.0, 0.0),
    doubleArrayOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
    doubleArrayOf(1.0, 3.0, 5.0, 7.0, 9.0, 11.0, 13.0, 15.0),
    DoubleArray(16) { it.toDouble() },
    DoubleArray(32) { Math.random() },
)
