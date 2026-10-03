package com.example.chromamix.data.model

data class Palette(
    val id: Long,
    val name: String,
    val baseColorHex: String,
    val type: PaletteType,
    val colors: List<String>,
    val size: Int,
    val timestamp: Long
)
