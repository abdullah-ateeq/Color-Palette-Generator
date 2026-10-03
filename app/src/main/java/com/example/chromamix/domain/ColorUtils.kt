package com.example.chromamix.domain

import androidx.compose.ui.graphics.Color
import com.example.chromamix.data.model.HslColor
import com.example.chromamix.data.model.RgbColor
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

object ColorUtils {

    fun normalizeHue(hue: Float): Float {
        if (hue.isNaN() || hue.isInfinite()) return 0f
        val mod = hue % 360f
        val result = if (mod < 0f) mod + 360f else mod
        return if (result >= 360f) 0f else result
    }

    fun isValidHex(hex: String): Boolean {
        val clean = if (hex.startsWith("#")) hex.substring(1) else hex
        if (clean.length != 6) return false
        return clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    fun hexToRgb(hex: String): RgbColor? {
        val clean = if (hex.startsWith("#")) hex.substring(1) else hex
        if (clean.length != 6) return null
        if (!clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
        val value = clean.toIntOrNull(16) ?: return null
        val r = (value shr 16) and 0xFF
        val g = (value shr 8) and 0xFF
        val b = value and 0xFF
        return RgbColor(r, g, b)
    }

    fun colorToHex(rgb: RgbColor): String {
        return String.format(Locale.US, "#%02X%02X%02X", rgb.red, rgb.green, rgb.blue)
    }

    fun rgbToHsl(rgb: RgbColor): HslColor {
        val r = rgb.red / 255f
        val g = rgb.green / 255f
        val b = rgb.blue / 255f

        val cMax = maxOf(r, maxOf(g, b))
        val cMin = minOf(r, minOf(g, b))
        val delta = cMax - cMin

        val l = (cMax + cMin) / 2f

        val h = when {
            delta == 0f -> 0f
            cMax == r -> 60f * (((g - b) / delta) % 6f)
            cMax == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        val normalizedH = normalizeHue(h)

        val s = if (delta == 0f) {
            0f
        } else {
            val denominator = 1f - abs(2f * l - 1f)
            if (denominator <= 0.00001f) 0f else (delta / denominator)
        }

        return HslColor(
            hue = normalizedH,
            saturation = (s * 100f).coerceIn(0f, 100f),
            lightness = (l * 100f).coerceIn(0f, 100f)
        )
    }

    fun hslToRgb(hsl: HslColor): RgbColor {
        val h = normalizeHue(hsl.hue)
        val s = (hsl.saturation / 100f).coerceIn(0f, 1f)
        val l = (hsl.lightness / 100f).coerceIn(0f, 1f)

        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs(((h / 60f) % 2f) - 1f))
        val m = l - c / 2f

        val (rPrime, gPrime, bPrime) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val r = round((rPrime + m) * 255f).toInt().coerceIn(0, 255)
        val g = round((gPrime + m) * 255f).toInt().coerceIn(0, 255)
        val b = round((bPrime + m) * 255f).toInt().coerceIn(0, 255)

        return RgbColor(r, g, b)
    }

    fun rgbToHsv(rgb: RgbColor): FloatArray {
        val r = rgb.red / 255f
        val g = rgb.green / 255f
        val b = rgb.blue / 255f

        val cMax = maxOf(r, maxOf(g, b))
        val cMin = minOf(r, minOf(g, b))
        val delta = cMax - cMin

        val h = when {
            delta == 0f -> 0f
            cMax == r -> 60f * (((g - b) / delta) % 6f)
            cMax == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        val normalizedH = normalizeHue(h)

        val s = if (cMax == 0f) 0f else delta / cMax
        val v = cMax

        return floatArrayOf(normalizedH, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))
    }

    fun hsvToRgb(hue: Float, saturation: Float, value: Float): RgbColor {
        val h = normalizeHue(hue)
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)

        val c = v * s
        val x = c * (1f - abs(((h / 60f) % 2f) - 1f))
        val m = v - c

        val (rPrime, gPrime, bPrime) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val r = round((rPrime + m) * 255f).toInt().coerceIn(0, 255)
        val g = round((gPrime + m) * 255f).toInt().coerceIn(0, 255)
        val b = round((bPrime + m) * 255f).toInt().coerceIn(0, 255)

        return RgbColor(r, g, b)
    }

    fun calculateLuminance(rgb: RgbColor): Double {
        fun linearize(channel: Int): Double {
            val s = channel / 255.0
            return if (s <= 0.04045) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        val r = linearize(rgb.red)
        val g = linearize(rgb.green)
        val b = linearize(rgb.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    fun isDarkColor(rgb: RgbColor): Boolean {
        return calculateLuminance(rgb) <= 0.179
    }

    fun getReadableTextColor(rgb: RgbColor): Color {
        return if (isDarkColor(rgb)) Color.White else Color(0xFF1E1E1E)
    }

    fun clampRgb(r: Int, g: Int, b: Int): RgbColor {
        return RgbColor(
            red = r.coerceIn(0, 255),
            green = g.coerceIn(0, 255),
            blue = b.coerceIn(0, 255)
        )
    }

    fun clampHsl(h: Float, s: Float, l: Float): HslColor {
        return HslColor(
            hue = normalizeHue(h),
            saturation = s.coerceIn(0f, 100f),
            lightness = l.coerceIn(0f, 100f)
        )
    }

    fun rgbToComposeColor(rgb: RgbColor): Color {
        return Color(rgb.red, rgb.green, rgb.blue)
    }

    fun composeColorToRgb(color: Color): RgbColor {
        return RgbColor(
            red = (color.red * 255f).toInt().coerceIn(0, 255),
            green = (color.green * 255f).toInt().coerceIn(0, 255),
            blue = (color.blue * 255f).toInt().coerceIn(0, 255)
        )
    }
}
