package com.example.chromamix.data.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultPaletteType: PaletteType = PaletteType.MONOCHROMATIC,
    val defaultPaletteSize: Int = 5,
    val defaultBaseColorHex: String = "#3F51B5",
    val showRgb: Boolean = true,
    val showHsl: Boolean = true,
    val hapticFeedback: Boolean = true
)
