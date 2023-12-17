package util

import kotlin.test.Test
import kotlin.test.assertEquals

class KanaCharacterNormalizerTest {
    @Test
    fun `single voiced character should be converted`() = test("か\u3099", "が")

    @Test
    fun `single semi-voiced character should be converted`() = test("は\u309A", "ぱ")

    @Test
    fun `single mis-voiced character should be kept with mark discarded`() = test("ら\u3099", "ら")

    @Test
    fun `voiced character followed by semi-voiced character should be converted`() = test("か\u3099ハ\u309A", "がパ")

    @Test
    fun `a mix of voiced and normal characters should be converted`() = test("か\u3099は\u309Aらきく\u3099", "がぱらきぐ")

    private fun test(
        input: String,
        expected: String,
    ) {
        val actual = KanaCharacterNormalizer.convert(input)
        assertEquals(expected, actual)
    }
}
