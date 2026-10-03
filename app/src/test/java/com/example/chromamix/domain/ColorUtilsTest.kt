package com.example.chromamix.domain

import com.example.chromamix.data.model.HslColor
import com.example.chromamix.data.model.RgbColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ColorUtilsTest {

    private val deltaFloat = 1.0f

    // 1. HEX → RGB
    @Test
    fun testHexToRgb() {
        val red = ColorUtils.hexToRgb("#FF0000")
        assertNotNull(red)
        assertEquals(255, red!!.red)
        assertEquals(0, red.green)
        assertEquals(0, red.blue)

        val green = ColorUtils.hexToRgb("00FF00") // without hash
        assertNotNull(green)
        assertEquals(0, green!!.red)
        assertEquals(255, green.green)
        assertEquals(0, green.blue)

        val blue = ColorUtils.hexToRgb("#0000FF")
        assertNotNull(blue)
        assertEquals(0, blue!!.red)
        assertEquals(0, blue.green)
        assertEquals(255, blue.blue)

        val custom = ColorUtils.hexToRgb("#3F51B5")
        assertNotNull(custom)
        assertEquals(63, custom!!.red)
        assertEquals(81, custom.green)
        assertEquals(181, custom.blue)
    }

    // 2. RGB → HEX
    @Test
    fun testRgbToHex() {
        val rgbRed = RgbColor(255, 0, 0)
        assertEquals("#FF0000", ColorUtils.colorToHex(rgbRed))

        val rgbIndigo = RgbColor(63, 81, 181)
        assertEquals("#3F51B5", ColorUtils.colorToHex(rgbIndigo))

        val rgbBlack = RgbColor(0, 0, 0)
        assertEquals("#000000", ColorUtils.colorToHex(rgbBlack))

        val rgbWhite = RgbColor(255, 255, 255)
        assertEquals("#FFFFFF", ColorUtils.colorToHex(rgbWhite))
    }

    // 3. RGB → HSL
    @Test
    fun testRgbToHsl() {
        // Red #FF0000 -> 0°, 100%, 50%
        val hslRed = ColorUtils.rgbToHsl(RgbColor(255, 0, 0))
        assertEquals(0f, hslRed.hue, deltaFloat)
        assertEquals(100f, hslRed.saturation, deltaFloat)
        assertEquals(50f, hslRed.lightness, deltaFloat)

        // Green #00FF00 -> 120°, 100%, 50%
        val hslGreen = ColorUtils.rgbToHsl(RgbColor(0, 255, 0))
        assertEquals(120f, hslGreen.hue, deltaFloat)
        assertEquals(100f, hslGreen.saturation, deltaFloat)
        assertEquals(50f, hslGreen.lightness, deltaFloat)

        // Blue #0000FF -> 240°, 100%, 50%
        val hslBlue = ColorUtils.rgbToHsl(RgbColor(0, 0, 255))
        assertEquals(240f, hslBlue.hue, deltaFloat)
        assertEquals(100f, hslBlue.saturation, deltaFloat)
        assertEquals(50f, hslBlue.lightness, deltaFloat)

        // #3F51B5 -> 231°, 48%, 48% (with ~1% tolerance)
        val hslIndigo = ColorUtils.rgbToHsl(RgbColor(63, 81, 181))
        assertEquals(231f, hslIndigo.hue, 2f)
        assertEquals(48f, hslIndigo.saturation, 2f)
        assertEquals(48f, hslIndigo.lightness, 2f)
    }

    // 4. HSL → RGB
    @Test
    fun testHslToRgb() {
        val rgbRed = ColorUtils.hslToRgb(HslColor(0f, 100f, 50f))
        assertEquals(255, rgbRed.red)
        assertEquals(0, rgbRed.green)
        assertEquals(0, rgbRed.blue)

        val rgbGreen = ColorUtils.hslToRgb(HslColor(120f, 100f, 50f))
        assertEquals(0, rgbGreen.red)
        assertEquals(255, rgbGreen.green)
        assertEquals(0, rgbGreen.blue)

        val rgbBlue = ColorUtils.hslToRgb(HslColor(240f, 100f, 50f))
        assertEquals(0, rgbBlue.red)
        assertEquals(0, rgbBlue.green)
        assertEquals(255, rgbBlue.blue)
    }

    // 5. HEX formatting
    @Test
    fun testHexFormatting() {
        val rgb = RgbColor(63, 81, 181)
        val hex = ColorUtils.colorToHex(rgb)
        // Must be uppercase, 7 chars, starts with #
        assertEquals("#3F51B5", hex)
        assertTrue(hex.startsWith("#"))
        assertEquals(7, hex.length)
        assertTrue(hex.all { it == '#' || it in '0'..'9' || it in 'A'..'F' })
        assertFalse(hex.any { it in 'a'..'f' }) // No lowercase
    }

    // 6. Invalid HEX
    @Test
    fun testInvalidHex() {
        assertFalse(ColorUtils.isValidHex("#FFF"))
        assertFalse(ColorUtils.isValidHex("GGGGGG"))
        assertFalse(ColorUtils.isValidHex("#12345"))
        assertFalse(ColorUtils.isValidHex("#1234567"))
        assertFalse(ColorUtils.isValidHex(""))
        assertFalse(ColorUtils.isValidHex("#"))
        assertFalse(ColorUtils.isValidHex("#ZZZZZZ"))

        assertNull(ColorUtils.hexToRgb("#FFF"))
        assertNull(ColorUtils.hexToRgb("GGGGGG"))
        assertNull(ColorUtils.hexToRgb("#12345"))
        assertNull(ColorUtils.hexToRgb("#1234567"))
        assertNull(ColorUtils.hexToRgb(""))
    }

    // 7. RGB validation and clamping
    @Test
    fun testRgbValidation() {
        val clamped = ColorUtils.clampRgb(300, -50, 128)
        assertEquals(255, clamped.red)
        assertEquals(0, clamped.green)
        assertEquals(128, clamped.blue)

        // Normal values should pass constructor
        val valid = RgbColor(0, 128, 255)
        assertEquals(0, valid.red)
        assertEquals(128, valid.green)
        assertEquals(255, valid.blue)
    }

    // 8. HSL validation and clamping
    @Test
    fun testHslValidation() {
        val clamped = ColorUtils.clampHsl(400f, 150f, -10f)
        assertEquals(40f, clamped.hue, 0.01f) // 400 normalized = 40
        assertEquals(100f, clamped.saturation, 0.01f)
        assertEquals(0f, clamped.lightness, 0.01f)
    }

    // 9. Hue normalization
    @Test
    fun testHueNormalization() {
        assertEquals(10f, ColorUtils.normalizeHue(370f), 0.01f)
        assertEquals(340f, ColorUtils.normalizeHue(-20f), 0.01f)
        assertEquals(0f, ColorUtils.normalizeHue(0f), 0.01f)
        assertEquals(0f, ColorUtils.normalizeHue(360f), 0.01f)
        assertEquals(0f, ColorUtils.normalizeHue(720f), 0.01f)
        assertEquals(180f, ColorUtils.normalizeHue(-180f), 0.01f)
        assertEquals(0f, ColorUtils.normalizeHue(Float.NaN), 0.01f)
        assertEquals(0f, ColorUtils.normalizeHue(Float.POSITIVE_INFINITY), 0.01f)
    }

    // 19. Contrast / readable text calculation
    @Test
    fun testContrastReadableText() {
        // Pure White background should require dark text
        val white = RgbColor(255, 255, 255)
        assertFalse(ColorUtils.isDarkColor(white))
        assertTrue(ColorUtils.calculateLuminance(white) > 0.9)

        // Pure Black background should require white text
        val black = RgbColor(0, 0, 0)
        assertTrue(ColorUtils.isDarkColor(black))
        assertEquals(0.0, ColorUtils.calculateLuminance(black), 0.001)

        // Deep Navy / Indigo #3F51B5
        val indigo = RgbColor(63, 81, 181)
        assertTrue(ColorUtils.isDarkColor(indigo))

        // Bright Yellow #FFEE58
        val yellow = RgbColor(255, 238, 88)
        assertFalse(ColorUtils.isDarkColor(yellow))
    }
}
