package com.example.chromamix.data.model

data class HslColor(
    val hue: Float,
    val saturation: Float,
    val lightness: Float
) {
    init {
        require(hue in 0f..360f) { "Hue must be between 0 and 360, got: $hue" }
        require(saturation in 0f..100f) { "Saturation must be between 0 and 100, got: $saturation" }
        require(lightness in 0f..100f) { "Lightness must be between 0 and 100, got: $lightness" }
    }
}
