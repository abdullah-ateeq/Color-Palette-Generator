package com.example.chromamix.data

import com.example.chromamix.data.model.Palette
import com.example.chromamix.data.model.PaletteType
import com.example.chromamix.data.repository.PaletteJsonSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteSerializationTest {

    // 20. Palette serialization
    @Test
    fun testPaletteSerializationAndDeserialization() {
        val originalList = listOf(
            Palette(
                id = 101L,
                name = "Ocean Balance \"Quote\" & Special",
                baseColorHex = "#3F51B5",
                type = PaletteType.ANALOGOUS,
                colors = listOf("#3F51B5", "#5C6BC0", "#7986CB", "#9FA8DA", "#C5CAE9"),
                size = 5,
                timestamp = 1790900000000L
            ),
            Palette(
                id = 102L,
                name = "Sunset Glow",
                baseColorHex = "#FF5722",
                type = PaletteType.COMPLEMENTARY,
                colors = listOf("#FF5722", "#FF8A65", "#00BCD4"),
                size = 3,
                timestamp = 1790900100000L
            )
        )

        val json = PaletteJsonSerializer.serialize(originalList)
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))

        val deserialized = PaletteJsonSerializer.deserialize(json)
        assertEquals(2, deserialized.size)

        val first = deserialized[0]
        assertEquals(101L, first.id)
        assertEquals("Ocean Balance \"Quote\" & Special", first.name)
        assertEquals("#3F51B5", first.baseColorHex)
        assertEquals(PaletteType.ANALOGOUS, first.type)
        assertEquals(5, first.colors.size)
        assertEquals(listOf("#3F51B5", "#5C6BC0", "#7986CB", "#9FA8DA", "#C5CAE9"), first.colors)
        assertEquals(5, first.size)
        assertEquals(1790900000000L, first.timestamp)

        val second = deserialized[1]
        assertEquals(102L, second.id)
        assertEquals("Sunset Glow", second.name)
        assertEquals("#FF5722", second.baseColorHex)
        assertEquals(PaletteType.COMPLEMENTARY, second.type)
        assertEquals(3, second.colors.size)
        assertEquals(3, second.size)
        assertEquals(1790900100000L, second.timestamp)
    }

    @Test
    fun testEmptyAndMalformedJson() {
        assertEquals(0, PaletteJsonSerializer.deserialize("").size)
        assertEquals(0, PaletteJsonSerializer.deserialize(null).size)
        assertEquals(0, PaletteJsonSerializer.deserialize("[]").size)
        assertEquals(0, PaletteJsonSerializer.deserialize("not json").size)
    }
}
