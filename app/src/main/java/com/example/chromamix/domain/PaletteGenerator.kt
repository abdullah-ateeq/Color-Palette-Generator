package com.example.chromamix.domain

import com.example.chromamix.data.model.HslColor
import com.example.chromamix.data.model.PaletteType
import com.example.chromamix.data.model.RgbColor
import java.util.Random

object PaletteGenerator {

    fun generatePalette(baseColor: RgbColor, type: PaletteType, count: Int): List<RgbColor> {
        val safeCount = when (count) {
            3 -> 3
            7 -> 7
            else -> 5
        }
        val baseHsl = ColorUtils.rgbToHsl(baseColor)
        return when (type) {
            PaletteType.MONOCHROMATIC -> generateMonochromaticPalette(baseHsl, safeCount)
            PaletteType.COMPLEMENTARY -> generateComplementaryPalette(baseHsl, safeCount)
            PaletteType.ANALOGOUS -> generateAnalogousPalette(baseHsl, safeCount)
            PaletteType.TRIADIC -> generateTriadicPalette(baseHsl, safeCount)
            PaletteType.SPLIT_COMPLEMENTARY -> generateSplitComplementaryPalette(baseHsl, safeCount)
        }
    }

    fun generateMonochromaticPalette(base: HslColor, count: Int): List<RgbColor> {
        val h = base.hue
        val s = base.saturation
        val l = base.lightness

        val hslList = when (count) {
            3 -> listOf(
                HslColor(h, (s * 0.9f).coerceIn(15f, 100f), (l * 0.55f).coerceIn(15f, 40f)),
                base,
                HslColor(h, (s * 0.8f).coerceIn(15f, 100f), (l + (100f - l) * 0.65f).coerceIn(65f, 92f))
            )
            7 -> listOf(
                HslColor(h, (s * 0.8f).coerceIn(10f, 100f), (l * 0.25f).coerceIn(10f, 20f)),
                HslColor(h, (s * 0.9f).coerceIn(15f, 100f), (l * 0.50f).coerceIn(18f, 32f)),
                HslColor(h, (s * 0.95f).coerceIn(20f, 100f), (l * 0.75f).coerceIn(28f, 45f)),
                base,
                HslColor(h, (s * 0.95f).coerceIn(20f, 100f), (l + (100f - l) * 0.35f).coerceIn(52f, 68f)),
                HslColor(h, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.65f).coerceIn(70f, 82f)),
                HslColor(h, (s * 0.70f).coerceIn(10f, 100f), (l + (100f - l) * 0.88f).coerceIn(84f, 95f))
            )
            else -> listOf( // 5 colors default
                HslColor(h, (s * 0.85f).coerceIn(15f, 100f), (l * 0.35f).coerceIn(12f, 26f)),
                HslColor(h, (s * 0.95f).coerceIn(20f, 100f), (l * 0.70f).coerceIn(25f, 45f)),
                base,
                HslColor(h, (s * 0.90f).coerceIn(20f, 100f), (l + (100f - l) * 0.45f).coerceIn(55f, 75f)),
                HslColor(h, (s * 0.65f).coerceIn(10f, 100f), (l + (100f - l) * 0.80f).coerceIn(80f, 94f))
            )
        }
        return hslList.map { ColorUtils.hslToRgb(it) }
    }

    fun generateComplementaryPalette(base: HslColor, count: Int): List<RgbColor> {
        val compHue = ColorUtils.normalizeHue(base.hue + 180f)
        val s = base.saturation
        val l = base.lightness

        val hslList = when (count) {
            3 -> listOf(
                HslColor(base.hue, (s * 0.9f).coerceIn(15f, 100f), (l * 0.75f).coerceIn(20f, 60f)),
                base,
                HslColor(compHue, s, l)
            )
            7 -> listOf(
                HslColor(base.hue, (s * 0.85f).coerceIn(15f, 100f), (l * 0.45f).coerceIn(18f, 35f)),
                HslColor(base.hue, (s * 0.95f).coerceIn(20f, 100f), (l * 0.75f).coerceIn(28f, 50f)),
                base,
                HslColor(base.hue, (s * 0.80f).coerceIn(15f, 100f), (l + (100f - l) * 0.55f).coerceIn(65f, 88f)),
                HslColor(compHue, (s * 0.9f).coerceIn(20f, 100f), (l * 0.65f).coerceIn(25f, 50f)),
                HslColor(compHue, s, l),
                HslColor(compHue, (s * 0.80f).coerceIn(15f, 100f), (l + (100f - l) * 0.55f).coerceIn(65f, 88f))
            )
            else -> listOf( // 5 colors default
                HslColor(base.hue, (s * 0.9f).coerceIn(15f, 100f), (l * 0.6f).coerceIn(18f, 45f)),
                base,
                HslColor(base.hue, (s * 0.8f).coerceIn(15f, 100f), (l + (100f - l) * 0.5f).coerceIn(60f, 85f)),
                HslColor(compHue, s, l),
                HslColor(compHue, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.5f).coerceIn(60f, 85f))
            )
        }
        return hslList.map { ColorUtils.hslToRgb(it) }
    }

    fun generateAnalogousPalette(base: HslColor, count: Int): List<RgbColor> {
        val offsets = when (count) {
            3 -> listOf(-30f, 0f, 30f)
            7 -> listOf(-45f, -30f, -15f, 0f, 15f, 30f, 45f)
            else -> listOf(-30f, -15f, 0f, 15f, 30f)
        }
        return offsets.map { offset ->
            val h = ColorUtils.normalizeHue(base.hue + offset)
            ColorUtils.hslToRgb(HslColor(h, base.saturation, base.lightness))
        }
    }

    fun generateTriadicPalette(base: HslColor, count: Int): List<RgbColor> {
        val h1 = ColorUtils.normalizeHue(base.hue + 120f)
        val h2 = ColorUtils.normalizeHue(base.hue + 240f)
        val s = base.saturation
        val l = base.lightness

        val hslList = when (count) {
            3 -> listOf(
                base,
                HslColor(h1, s, l),
                HslColor(h2, s, l)
            )
            7 -> listOf(
                HslColor(base.hue, (s * 0.9f).coerceIn(15f, 100f), (l * 0.65f).coerceIn(20f, 50f)),
                base,
                HslColor(base.hue, (s * 0.8f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h1, s, l),
                HslColor(h1, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h2, s, l),
                HslColor(h2, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f))
            )
            else -> listOf( // 5 colors default
                base,
                HslColor(base.hue, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h1, s, l),
                HslColor(h1, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h2, s, l)
            )
        }
        return hslList.map { ColorUtils.hslToRgb(it) }
    }

    fun generateSplitComplementaryPalette(base: HslColor, count: Int): List<RgbColor> {
        val h1 = ColorUtils.normalizeHue(base.hue + 150f)
        val h2 = ColorUtils.normalizeHue(base.hue + 210f)
        val s = base.saturation
        val l = base.lightness

        val hslList = when (count) {
            3 -> listOf(
                base,
                HslColor(h1, s, l),
                HslColor(h2, s, l)
            )
            7 -> listOf(
                HslColor(base.hue, (s * 0.9f).coerceIn(15f, 100f), (l * 0.65f).coerceIn(20f, 50f)),
                base,
                HslColor(base.hue, (s * 0.8f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h1, s, l),
                HslColor(h1, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h2, s, l),
                HslColor(h2, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f))
            )
            else -> listOf( // 5 colors default
                base,
                HslColor(base.hue, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h1, s, l),
                HslColor(h1, (s * 0.85f).coerceIn(15f, 100f), (l + (100f - l) * 0.45f).coerceIn(60f, 85f)),
                HslColor(h2, s, l)
            )
        }
        return hslList.map { ColorUtils.hslToRgb(it) }
    }

    fun generateRandomBaseColor(random: Random = Random()): RgbColor {
        val h = random.nextFloat() * 360f
        val s = 45f + random.nextFloat() * 45f // 45% - 90%
        val l = 35f + random.nextFloat() * 35f // 35% - 70%
        return ColorUtils.hslToRgb(HslColor(h, s, l))
    }
}
