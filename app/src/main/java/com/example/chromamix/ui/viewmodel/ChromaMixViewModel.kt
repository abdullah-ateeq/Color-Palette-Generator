package com.example.chromamix.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chromamix.data.model.AppSettings
import com.example.chromamix.data.model.AppThemeMode
import com.example.chromamix.data.model.HslColor
import com.example.chromamix.data.model.Palette
import com.example.chromamix.data.model.PaletteType
import com.example.chromamix.data.model.RgbColor
import com.example.chromamix.data.repository.PalettePreferencesRepository
import com.example.chromamix.domain.ColorUtils
import com.example.chromamix.domain.PaletteGenerator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GeneratorUiState(
    val baseColor: RgbColor = RgbColor(63, 81, 181),
    val hexInputText: String = "#3F51B5",
    val hexInputError: String? = null,
    val paletteType: PaletteType = PaletteType.MONOCHROMATIC,
    val paletteSize: Int = 5,
    val generatedPalette: List<RgbColor> = emptyList(),
    val lockedIndices: Set<Int> = emptySet(),
    val hue: Float = 231f,
    val pickerSaturation: Float = 0.65f,
    val pickerValue: Float = 0.71f
)

class ChromaMixViewModel(
    private val repository: PalettePreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    val savedPalettes: StateFlow<List<Palette>> = repository.savedPalettesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _snackbarChannel = Channel<String>(Channel.BUFFERED)
    val snackbarEvents = _snackbarChannel.receiveAsFlow()

    init {
        // Initialize base color and palette
        updateBaseColor(RgbColor(63, 81, 181), shouldRegenerate = true)
        // Load default preferences once available
        viewModelScope.launch {
            repository.settingsFlow.collect { appSettings ->
                // Apply defaults if uiState is at initial state
                if (_uiState.value.generatedPalette.isEmpty()) {
                    val defaultRgb = ColorUtils.hexToRgb(appSettings.defaultBaseColorHex)
                        ?: RgbColor(63, 81, 181)
                    _uiState.value = _uiState.value.copy(
                        paletteType = appSettings.defaultPaletteType,
                        paletteSize = appSettings.defaultPaletteSize
                    )
                    updateBaseColor(defaultRgb, shouldRegenerate = true)
                }
            }
        }
    }

    fun updateBaseColor(rgb: RgbColor, shouldRegenerate: Boolean = true) {
        val hsv = ColorUtils.rgbToHsv(rgb)
        val hex = ColorUtils.colorToHex(rgb)
        val currentLocked = _uiState.value.lockedIndices
        val currentPalette = _uiState.value.generatedPalette

        val newPalette = if (shouldRegenerate) {
            val generated = PaletteGenerator.generatePalette(
                baseColor = rgb,
                type = _uiState.value.paletteType,
                count = _uiState.value.paletteSize
            )
            // Preserve locked colors if sizes match
            if (currentLocked.isNotEmpty() && currentPalette.size == generated.size) {
                generated.mapIndexed { index, color ->
                    if (index in currentLocked) currentPalette[index] else color
                }
            } else {
                generated
            }
        } else {
            currentPalette
        }

        _uiState.value = _uiState.value.copy(
            baseColor = rgb,
            hexInputText = hex,
            hexInputError = null,
            hue = hsv[0],
            pickerSaturation = hsv[1],
            pickerValue = hsv[2],
            generatedPalette = newPalette
        )
    }

    fun onHexInputChanged(input: String) {
        val trimmed = input.trim()
        val formattedForChecking = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
        _uiState.value = _uiState.value.copy(hexInputText = trimmed)

        if (ColorUtils.isValidHex(trimmed)) {
            val rgb = ColorUtils.hexToRgb(trimmed)
            if (rgb != null) {
                updateBaseColor(rgb, shouldRegenerate = true)
            }
        } else {
            if (trimmed.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    hexInputError = "Enter a valid 6-digit HEX color."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    hexInputError = "HEX color cannot be empty."
                )
            }
        }
    }

    fun onPickerColorChanged(hue: Float, saturation: Float, value: Float) {
        val normalizedHue = ColorUtils.normalizeHue(hue)
        val clampedSat = saturation.coerceIn(0f, 1f)
        val clampedVal = value.coerceIn(0f, 1f)
        val rgb = ColorUtils.hsvToRgb(normalizedHue, clampedSat, clampedVal)
        val hex = ColorUtils.colorToHex(rgb)

        val generated = PaletteGenerator.generatePalette(
            baseColor = rgb,
            type = _uiState.value.paletteType,
            count = _uiState.value.paletteSize
        )

        val currentLocked = _uiState.value.lockedIndices
        val currentPalette = _uiState.value.generatedPalette
        val preservedPalette = if (currentLocked.isNotEmpty() && currentPalette.size == generated.size) {
            generated.mapIndexed { index, color ->
                if (index in currentLocked) currentPalette[index] else color
            }
        } else {
            generated
        }

        _uiState.value = _uiState.value.copy(
            baseColor = rgb,
            hexInputText = hex,
            hexInputError = null,
            hue = normalizedHue,
            pickerSaturation = clampedSat,
            pickerValue = clampedVal,
            generatedPalette = preservedPalette
        )
    }

    fun onRgbChannelsChanged(r: Int, g: Int, b: Int) {
        val clamped = ColorUtils.clampRgb(r, g, b)
        updateBaseColor(clamped, shouldRegenerate = true)
    }

    fun onHslChannelsChanged(h: Float, s: Float, l: Float) {
        val clamped = ColorUtils.clampHsl(h, s, l)
        val rgb = ColorUtils.hslToRgb(clamped)
        updateBaseColor(rgb, shouldRegenerate = true)
    }

    fun setPaletteType(type: PaletteType) {
        if (_uiState.value.paletteType != type) {
            val generated = PaletteGenerator.generatePalette(
                baseColor = _uiState.value.baseColor,
                type = type,
                count = _uiState.value.paletteSize
            )
            _uiState.value = _uiState.value.copy(
                paletteType = type,
                generatedPalette = generated,
                lockedIndices = emptySet() // Reset locked indices on type change
            )
        }
    }

    fun setPaletteSize(size: Int) {
        val safeSize = when (size) {
            3 -> 3
            7 -> 7
            else -> 5
        }
        if (_uiState.value.paletteSize != safeSize) {
            val generated = PaletteGenerator.generatePalette(
                baseColor = _uiState.value.baseColor,
                type = _uiState.value.paletteType,
                count = safeSize
            )
            _uiState.value = _uiState.value.copy(
                paletteSize = safeSize,
                generatedPalette = generated,
                lockedIndices = emptySet()
            )
        }
    }

    fun toggleLockColor(index: Int) {
        val current = _uiState.value.lockedIndices
        val updated = if (index in current) current - index else current + index
        _uiState.value = _uiState.value.copy(lockedIndices = updated)
    }

    fun setSwatchAsBase(color: RgbColor) {
        updateBaseColor(color, shouldRegenerate = true)
        sendSnackbar("Base color set to ${ColorUtils.colorToHex(color)}")
    }

    fun randomize() {
        val randomBase = PaletteGenerator.generateRandomBaseColor()
        updateBaseColor(randomBase, shouldRegenerate = true)
    }

    fun reset() {
        val defaultRgb = RgbColor(63, 81, 181) // #3F51B5
        val hsv = ColorUtils.rgbToHsv(defaultRgb)
        val generated = PaletteGenerator.generatePalette(
            baseColor = defaultRgb,
            type = PaletteType.MONOCHROMATIC,
            count = 5
        )
        _uiState.value = _uiState.value.copy(
            baseColor = defaultRgb,
            hexInputText = "#3F51B5",
            hexInputError = null,
            paletteType = PaletteType.MONOCHROMATIC,
            paletteSize = 5,
            generatedPalette = generated,
            lockedIndices = emptySet(),
            hue = hsv[0],
            pickerSaturation = hsv[1],
            pickerValue = hsv[2]
        )
        sendSnackbar("Reset to default")
    }

    fun copyPaletteHexList(): String {
        val hexList = _uiState.value.generatedPalette.joinToString("\n") { ColorUtils.colorToHex(it) }
        sendSnackbar("Palette copied")
        return hexList
    }

    fun copySingleColor(rgb: RgbColor): String {
        val hex = ColorUtils.colorToHex(rgb)
        sendSnackbar("Copied $hex")
        return hex
    }

    fun saveCurrentPalette(customName: String? = null) {
        viewModelScope.launch {
            val state = _uiState.value
            val nextNumber = (savedPalettes.value.size + 1)
            val name = if (!customName.isNullOrBlank()) {
                customName.trim()
            } else {
                "${state.paletteType.displayName} $nextNumber"
            }
            val palette = Palette(
                id = System.currentTimeMillis(),
                name = name,
                baseColorHex = ColorUtils.colorToHex(state.baseColor),
                type = state.paletteType,
                colors = state.generatedPalette.map { ColorUtils.colorToHex(it) },
                size = state.paletteSize,
                timestamp = System.currentTimeMillis()
            )
            repository.savePalette(palette)
            sendSnackbar("Saved \"$name\"")
        }
    }

    fun restorePalette(palette: Palette) {
        val baseRgb = ColorUtils.hexToRgb(palette.baseColorHex) ?: RgbColor(63, 81, 181)
        val hsv = ColorUtils.rgbToHsv(baseRgb)
        val restoredColors = palette.colors.mapNotNull { ColorUtils.hexToRgb(it) }
        val finalColors = if (restoredColors.isNotEmpty()) {
            restoredColors
        } else {
            PaletteGenerator.generatePalette(baseRgb, palette.type, palette.size)
        }

        _uiState.value = _uiState.value.copy(
            baseColor = baseRgb,
            hexInputText = palette.baseColorHex,
            hexInputError = null,
            paletteType = palette.type,
            paletteSize = palette.size,
            generatedPalette = finalColors,
            lockedIndices = emptySet(),
            hue = hsv[0],
            pickerSaturation = hsv[1],
            pickerValue = hsv[2]
        )
        sendSnackbar("Restored \"${palette.name}\"")
    }

    fun deletePalette(paletteId: Long) {
        viewModelScope.launch {
            repository.deletePalette(paletteId)
            sendSnackbar("Palette deleted")
        }
    }

    fun renamePalette(paletteId: Long, newName: String) {
        viewModelScope.launch {
            repository.renamePalette(paletteId, newName)
            sendSnackbar("Palette renamed")
        }
    }

    fun clearAllSavedPalettes() {
        viewModelScope.launch {
            repository.clearAllPalettes()
            sendSnackbar("All saved palettes cleared")
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDynamicColor(enabled)
        }
    }

    fun updateDefaultPaletteType(type: PaletteType) {
        viewModelScope.launch {
            repository.updateDefaultPaletteType(type)
        }
    }

    fun updateDefaultPaletteSize(size: Int) {
        viewModelScope.launch {
            repository.updateDefaultPaletteSize(size)
        }
    }

    fun updateDefaultBaseColor(hex: String) {
        if (ColorUtils.isValidHex(hex)) {
            viewModelScope.launch {
                val formatted = if (hex.startsWith("#")) hex.uppercase() else "#${hex.uppercase()}"
                repository.updateDefaultBaseColor(formatted)
                sendSnackbar("Default base color updated")
            }
        }
    }

    fun updateShowRgb(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowRgb(show)
        }
    }

    fun updateShowHsl(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowHsl(show)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHapticFeedback(enabled)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            repository.resetSettings()
            sendSnackbar("Settings reset to defaults")
        }
    }

    fun sendSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarChannel.send(message)
        }
    }
}

class ChromaMixViewModelFactory(
    private val repository: PalettePreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChromaMixViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChromaMixViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
