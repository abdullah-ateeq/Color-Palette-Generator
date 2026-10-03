package com.example.chromamix.domain

import com.example.chromamix.data.model.HslColor
import com.example.chromamix.data.model.PaletteType
import com.example.chromamix.data.model.RgbColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random
import kotlin.math.abs

class PaletteGeneratorTest {

    private val baseRgb = RgbColor(63, 81, 181) // #3F51B5

    // 10. Monochromatic generation
    @Test
    fun testMonochromaticGeneration() {
        val palette = PaletteGenerator.generatePalette(baseRgb, PaletteType.MONOCHROMATIC, 5)
        assertEquals(5, palette.size)
        val baseHsl = ColorUtils.rgbToHsl(baseRgb)
        for (color in palette) {
            val hsl = ColorUtils.rgbToHsl(color)
            // Monochromatic hues must stay approximately consistent
            assertEquals(baseHsl.hue, hsl.hue, 3.0f)
        }
    }

    // 11. Complementary generation
    @Test
    fun testComplementaryGeneration() {
        // Red #FF0000 -> Hue 0°, Complementary should be 180° (Cyan)
        val red = RgbColor(255, 0, 0)
        val palette = PaletteGenerator.generatePalette(red, PaletteType.COMPLEMENTARY, 5)
        assertEquals(5, palette.size)
        // Verify complementary hue ~180° exists in palette
        val hasComplementaryHue = palette.any { color ->
            val hsl = ColorUtils.rgbToHsl(color)
            abs(hsl.hue - 180f) < 4.0f
        }
        assertTrue("Complementary hue 180° should be in palette", hasComplementaryHue)
    }

    // 12. Analogous generation
    @Test
    fun testAnalogousGeneration() {
        val palette = PaletteGenerator.generatePalette(baseRgb, PaletteType.ANALOGOUS, 5)
        assertEquals(5, palette.size)
        val baseHsl = ColorUtils.rgbToHsl(baseRgb)
        val hues = palette.map { ColorUtils.rgbToHsl(it).hue }
        // For 5 colors: offsets are -30, -15, 0, +15, +30
        for (i in hues.indices) {
            val expectedOffset = when (i) {
                0 -> -30f
                1 -> -15f
                2 -> 0f
                3 -> 15f
                4 -> 30f
                else -> 0f
            }
            val expectedHue = ColorUtils.normalizeHue(baseHsl.hue + expectedOffset)
            assertEquals(expectedHue, hues[i], 3.0f)
        }
    }

    // 13. Triadic generation
    @Test
    fun testTriadicGeneration() {
        val red = RgbColor(255, 0, 0) // Hue 0°
        val palette = PaletteGenerator.generatePalette(red, PaletteType.TRIADIC, 3)
        assertEquals(3, palette.size)
        val hues = palette.map { ColorUtils.rgbToHsl(it).hue }
        assertEquals(0f, hues[0], 2.0f)
        assertEquals(120f, hues[1], 2.0f)
        assertEquals(240f, hues[2], 2.0f)
    }

    // 14. Split complementary generation
    @Test
    fun testSplitComplementaryGeneration() {
        val red = RgbColor(255, 0, 0) // Hue 0°
        val palette = PaletteGenerator.generatePalette(red, PaletteType.SPLIT_COMPLEMENTARY, 3)
        assertEquals(3, palette.size)
        val hues = palette.map { ColorUtils.rgbToHsl(it).hue }
        assertEquals(0f, hues[0], 2.0f)
        assertEquals(150f, hues[1], 2.0f)
        assertEquals(210f, hues[2], 2.0f)
    }

    // 15. Palette size 3
    @Test
    fun testPaletteSize3() {
        PaletteType.values().forEach { type ->
            val palette = PaletteGenerator.generatePalette(baseRgb, type, 3)
            assertEquals("Palette size 3 failed for $type", 3, palette.size)
        }
    }

    // 16. Palette size 5
    @Test
    fun testPaletteSize5() {
        PaletteType.values().forEach { type ->
            val palette = PaletteGenerator.generatePalette(baseRgb, type, 5)
            assertEquals("Palette size 5 failed for $type", 5, palette.size)
        }
    }

    // 17. Palette size 7
    @Test
    fun testPaletteSize7() {
        PaletteType.values().forEach { type ->
            val palette = PaletteGenerator.generatePalette(baseRgb, type, 7)
            assertEquals("Palette size 7 failed for $type", 7, palette.size)
        }
    }

    // 18. Random color generation
    @Test
    fun testRandomColorGeneration() {
        val rng = Random(42) // Deterministic seed for reproducible test
        for (i in 0 until 50) {
            val color = PaletteGenerator.generateRandomBaseColor(rng)
            assertTrue("Red channel out of bounds: ${color.red}", color.red in 0..255)
            assertTrue("Green channel out of bounds: ${color.green}", color.green in 0..255)
            assertTrue("Blue channel out of bounds: ${color.blue}", color.blue in 0..255)

            val hsl = ColorUtils.rgbToHsl(color)
            assertTrue("Hue out of range: ${hsl.hue}", hsl.hue in 0f..360f)
            assertTrue("Saturation too low (muddy/gray): ${hsl.saturation}", hsl.saturation >= 35f)
            assertTrue("Lightness too extreme: ${hsl.lightness}", hsl.lightness in 25f..80f)
        }
    }
}
