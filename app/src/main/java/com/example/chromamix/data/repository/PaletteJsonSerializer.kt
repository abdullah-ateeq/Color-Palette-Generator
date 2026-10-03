package com.example.chromamix.data.repository

import com.example.chromamix.data.model.Palette
import com.example.chromamix.data.model.PaletteType

object PaletteJsonSerializer {

    fun serialize(palettes: List<Palette>): String {
        val sb = StringBuilder()
        sb.append("[")
        palettes.forEachIndexed { index, palette ->
            if (index > 0) sb.append(",")
            sb.append("{")
            sb.append("\"id\":").append(palette.id).append(",")
            sb.append("\"name\":").append(escapeJson(palette.name)).append(",")
            sb.append("\"baseColorHex\":").append(escapeJson(palette.baseColorHex)).append(",")
            sb.append("\"type\":").append(escapeJson(palette.type.name)).append(",")
            sb.append("\"colors\":[")
            palette.colors.forEachIndexed { cIndex, color ->
                if (cIndex > 0) sb.append(",")
                sb.append(escapeJson(color))
            }
            sb.append("],")
            sb.append("\"size\":").append(palette.size).append(",")
            sb.append("\"timestamp\":").append(palette.timestamp)
            sb.append("}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun deserialize(json: String?): List<Palette> {
        if (json.isNullOrBlank()) return emptyList()
        val trimmed = json.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()

        val results = mutableListOf<Palette>()
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        val objectTokens = splitJsonObjects(content)
        for (token in objectTokens) {
            parsePalette(token)?.let { results.add(it) }
        }
        return results
    }

    private fun parsePalette(objJson: String): Palette? {
        val trimmed = objJson.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null
        val inner = trimmed.substring(1, trimmed.length - 1)

        var id: Long? = null
        var name: String? = null
        var baseColorHex: String? = null
        var type: PaletteType? = null
        val colors = mutableListOf<String>()
        var size: Int? = null
        var timestamp: Long? = null

        val pairs = splitKeyValues(inner)
        for ((key, value) in pairs) {
            when (key) {
                "id" -> id = value.trim().toLongOrNull()
                "name" -> name = unescapeJson(value)
                "baseColorHex" -> baseColorHex = unescapeJson(value)
                "type" -> {
                    val typeStr = unescapeJson(value)
                    type = try {
                        PaletteType.valueOf(typeStr)
                    } catch (e: Exception) {
                        PaletteType.MONOCHROMATIC
                    }
                }
                "colors" -> {
                    colors.clear()
                    colors.addAll(parseStringArray(value))
                }
                "size" -> size = value.trim().toIntOrNull()
                "timestamp" -> timestamp = value.trim().toLongOrNull()
            }
        }

        if (id != null && name != null && baseColorHex != null && type != null && size != null && timestamp != null) {
            return Palette(
                id = id,
                name = name,
                baseColorHex = baseColorHex,
                type = type,
                colors = colors.ifEmpty { listOf(baseColorHex) },
                size = size,
                timestamp = timestamp
            )
        }
        return null
    }

    private fun parseStringArray(arrayJson: String): List<String> {
        val trimmed = arrayJson.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()
        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) return emptyList()

        val items = mutableListOf<String>()
        var inQuotes = false
        val current = StringBuilder()
        for (i in inner.indices) {
            val c = inner[i]
            if (c == '"' && !isEscaped(inner, i)) {
                inQuotes = !inQuotes
                current.append(c)
            } else if (c == ',' && !inQuotes) {
                val str = unescapeJson(current.toString().trim())
                if (str.isNotEmpty()) items.add(str)
                current.clear()
            } else {
                current.append(c)
            }
        }
        if (current.isNotEmpty()) {
            val str = unescapeJson(current.toString().trim())
            if (str.isNotEmpty()) items.add(str)
        }
        return items
    }

    private fun splitKeyValues(content: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        var inQuotes = false
        var braceDepth = 0
        var bracketDepth = 0
        val token = StringBuilder()

        for (i in content.indices) {
            val c = content[i]
            if (c == '"' && !isEscaped(content, i)) {
                inQuotes = !inQuotes
            }
            if (!inQuotes) {
                if (c == '{') braceDepth++
                else if (c == '}') braceDepth--
                else if (c == '[') bracketDepth++
                else if (c == ']') bracketDepth--
            }

            if (c == ',' && !inQuotes && braceDepth == 0 && bracketDepth == 0) {
                val pair = parseKeyValue(token.toString().trim())
                if (pair != null) result.add(pair)
                token.clear()
            } else {
                token.append(c)
            }
        }
        if (token.isNotEmpty()) {
            val pair = parseKeyValue(token.toString().trim())
            if (pair != null) result.add(pair)
        }
        return result
    }

    private fun parseKeyValue(kv: String): Pair<String, String>? {
        val colonIdx = kv.indexOf(':')
        if (colonIdx == -1) return null
        val rawKey = kv.substring(0, colonIdx).trim()
        val rawValue = kv.substring(colonIdx + 1).trim()
        val key = unescapeJson(rawKey)
        return Pair(key, rawValue)
    }

    private fun splitJsonObjects(content: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var inQuotes = false
        val current = StringBuilder()

        for (i in content.indices) {
            val c = content[i]
            if (c == '"' && !isEscaped(content, i)) {
                inQuotes = !inQuotes
            }
            if (!inQuotes) {
                if (c == '{') depth++
                else if (c == '}') depth--
            }

            if (depth == 0 && !inQuotes && (c == ',' || c.isWhitespace())) {
                continue
            }

            current.append(c)
            if (depth == 0 && current.toString().trim().startsWith("{") && !inQuotes) {
                objects.add(current.toString().trim())
                current.clear()
            }
        }
        return objects
    }

    private fun isEscaped(s: String, index: Int): Boolean {
        var count = 0
        var k = index - 1
        while (k >= 0 && s[k] == '\\') {
            count++
            k--
        }
        return count % 2 != 0
    }

    private fun escapeJson(value: String): String {
        val sb = StringBuilder("\"")
        for (c in value) {
            when (c) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    private fun unescapeJson(value: String): String {
        val trimmed = value.trim()
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            val unquoted = trimmed.substring(1, trimmed.length - 1)
            val sb = StringBuilder()
            var i = 0
            while (i < unquoted.length) {
                if (unquoted[i] == '\\' && i + 1 < unquoted.length) {
                    when (unquoted[i + 1]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'b' -> sb.append('\b')
                        else -> sb.append(unquoted[i + 1])
                    }
                    i += 2
                } else {
                    sb.append(unquoted[i])
                    i++
                }
            }
            return sb.toString()
        }
        return trimmed
    }
}
