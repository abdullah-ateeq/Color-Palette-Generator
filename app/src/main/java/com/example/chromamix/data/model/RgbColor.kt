package com.example.chromamix.data.model

data class RgbColor(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    init {
        require(red in 0..255) { "Red must be between 0 and 255, got: $red" }
        require(green in 0..255) { "Green must be between 0 and 255, got: $green" }
        require(blue in 0..255) { "Blue must be between 0 and 255, got: $blue" }
    }
}
