package com.example.chromamix.data.model

enum class PaletteType(val displayName: String, val shortName: String) {
    MONOCHROMATIC("Monochromatic", "MONO"),
    COMPLEMENTARY("Complementary", "COMPLEMENTARY"),
    ANALOGOUS("Analogous", "ANALOGOUS"),
    TRIADIC("Triadic", "TRIADIC"),
    SPLIT_COMPLEMENTARY("Split Complementary", "SPLIT")
}
