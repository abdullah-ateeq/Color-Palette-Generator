package com.example.chromamix.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.chromamix.data.model.AppSettings
import com.example.chromamix.data.model.AppThemeMode
import com.example.chromamix.data.model.Palette
import com.example.chromamix.data.model.PaletteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chromamix_settings")

class PalettePreferencesRepository(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_DEFAULT_PALETTE_TYPE = stringPreferencesKey("default_palette_type")
        val KEY_DEFAULT_PALETTE_SIZE = intPreferencesKey("default_palette_size")
        val KEY_DEFAULT_BASE_COLOR = stringPreferencesKey("default_base_color")
        val KEY_SHOW_RGB = booleanPreferencesKey("show_rgb")
        val KEY_SHOW_HSL = booleanPreferencesKey("show_hsl")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_SAVED_PALETTES = stringPreferencesKey("saved_palettes_json")

        const val MAX_PALETTES = 50
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[KEY_THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }

        val dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true

        val paletteTypeStr = prefs[KEY_DEFAULT_PALETTE_TYPE] ?: PaletteType.MONOCHROMATIC.name
        val paletteType = try {
            PaletteType.valueOf(paletteTypeStr)
        } catch (e: Exception) {
            PaletteType.MONOCHROMATIC
        }

        val paletteSize = prefs[KEY_DEFAULT_PALETTE_SIZE] ?: 5
        val baseColor = prefs[KEY_DEFAULT_BASE_COLOR] ?: "#3F51B5"
        val showRgb = prefs[KEY_SHOW_RGB] ?: true
        val showHsl = prefs[KEY_SHOW_HSL] ?: true
        val haptic = prefs[KEY_HAPTIC_FEEDBACK] ?: true

        AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            defaultPaletteType = paletteType,
            defaultPaletteSize = paletteSize,
            defaultBaseColorHex = baseColor,
            showRgb = showRgb,
            showHsl = showHsl,
            hapticFeedback = haptic
        )
    }

    val savedPalettesFlow: Flow<List<Palette>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_SAVED_PALETTES]
        PaletteJsonSerializer.deserialize(json)
    }

    suspend fun savePalette(palette: Palette) {
        context.dataStore.edit { prefs ->
            val existing = PaletteJsonSerializer.deserialize(prefs[KEY_SAVED_PALETTES]).toMutableList()
            // Add new palette at beginning (most recent first)
            existing.add(0, palette)
            // Enforce maximum 50: remove oldest if exceeding
            if (existing.size > MAX_PALETTES) {
                while (existing.size > MAX_PALETTES) {
                    existing.removeAt(existing.lastIndex)
                }
            }
            prefs[KEY_SAVED_PALETTES] = PaletteJsonSerializer.serialize(existing)
        }
    }

    suspend fun deletePalette(paletteId: Long) {
        context.dataStore.edit { prefs ->
            val existing = PaletteJsonSerializer.deserialize(prefs[KEY_SAVED_PALETTES]).toMutableList()
            existing.removeAll { it.id == paletteId }
            prefs[KEY_SAVED_PALETTES] = PaletteJsonSerializer.serialize(existing)
        }
    }

    suspend fun renamePalette(paletteId: Long, newName: String) {
        context.dataStore.edit { prefs ->
            val existing = PaletteJsonSerializer.deserialize(prefs[KEY_SAVED_PALETTES]).map {
                if (it.id == paletteId) it.copy(name = newName) else it
            }
            prefs[KEY_SAVED_PALETTES] = PaletteJsonSerializer.serialize(existing)
        }
    }

    suspend fun clearAllPalettes() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_SAVED_PALETTES)
        }
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateDefaultPaletteType(type: PaletteType) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_PALETTE_TYPE] = type.name
        }
    }

    suspend fun updateDefaultPaletteSize(size: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_PALETTE_SIZE] = size
        }
    }

    suspend fun updateDefaultBaseColor(hex: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_BASE_COLOR] = hex
        }
    }

    suspend fun updateShowRgb(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_RGB] = show
        }
    }

    suspend fun updateShowHsl(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_HSL] = show
        }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_THEME_MODE)
            prefs.remove(KEY_DYNAMIC_COLOR)
            prefs.remove(KEY_DEFAULT_PALETTE_TYPE)
            prefs.remove(KEY_DEFAULT_PALETTE_SIZE)
            prefs.remove(KEY_DEFAULT_BASE_COLOR)
            prefs.remove(KEY_SHOW_RGB)
            prefs.remove(KEY_SHOW_HSL)
            prefs.remove(KEY_HAPTIC_FEEDBACK)
        }
    }
}
