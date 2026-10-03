package com.example.chromamix.ui.screens.generator

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chromamix.data.model.RgbColor
import com.example.chromamix.domain.ColorUtils
import com.example.chromamix.ui.components.ColorPickerCanvas
import com.example.chromamix.ui.components.ColorPreviewCard
import com.example.chromamix.ui.components.ColorSwatchItem
import com.example.chromamix.ui.components.HslInputSection
import com.example.chromamix.ui.components.PaletteSizeSelector
import com.example.chromamix.ui.components.PaletteTypeSelector
import com.example.chromamix.ui.components.RgbInputSection
import com.example.chromamix.ui.viewmodel.ChromaMixViewModel

@Composable
fun GeneratorScreen(
    viewModel: ChromaMixViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val savedPalettes by viewModel.savedPalettes.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    val triggerHaptic: () -> Unit = {
        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var savePaletteName by remember { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth > 680.dp || isLandscape

        if (isWide) {
            // Tablet / Landscape Two-Column Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Column: Color Controls (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    GeneratorHeader()
                    Spacer(modifier = Modifier.height(14.dp))

                    ColorPreviewCard(
                        baseColor = uiState.baseColor,
                        hexInputText = uiState.hexInputText,
                        hexInputError = uiState.hexInputError,
                        showRgb = settings.showRgb,
                        showHsl = settings.showHsl,
                        onHexChanged = {
                            viewModel.onHexInputChanged(it)
                        },
                        onCopyHex = { color ->
                            triggerHaptic()
                            val hex = viewModel.copySingleColor(color)
                            clipboardManager.setText(AnnotatedString(hex))
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ColorPickerCanvas(
                        hue = uiState.hue,
                        saturation = uiState.pickerSaturation,
                        value = uiState.pickerValue,
                        onColorChanged = { h, s, v ->
                            viewModel.onPickerColorChanged(h, s, v)
                        }
                    )

                    if (settings.showRgb) {
                        Spacer(modifier = Modifier.height(12.dp))
                        RgbInputSection(
                            rgb = uiState.baseColor,
                            onRgbChanged = { r, g, b ->
                                viewModel.onRgbChannelsChanged(r, g, b)
                            }
                        )
                    }

                    if (settings.showHsl) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HslInputSection(
                            hsl = ColorUtils.rgbToHsl(uiState.baseColor),
                            onHslChanged = { h, s, l ->
                                viewModel.onHslChannelsChanged(h, s, l)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Right Column: Palette & Actions (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    PaletteTypeSelector(
                        selectedType = uiState.paletteType,
                        onTypeSelected = {
                            triggerHaptic()
                            viewModel.setPaletteType(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PaletteSizeSelector(
                        selectedSize = uiState.paletteSize,
                        onSizeSelected = {
                            triggerHaptic()
                            viewModel.setPaletteSize(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ActionButtonsRow(
                        onCopyPalette = {
                            triggerHaptic()
                            val text = viewModel.copyPaletteHexList()
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        onSavePalette = {
                            triggerHaptic()
                            savePaletteName = "${uiState.paletteType.displayName} ${savedPalettes.size + 1}"
                            showSaveDialog = true
                        },
                        onRandomize = {
                            triggerHaptic()
                            viewModel.randomize()
                        },
                        onReset = {
                            triggerHaptic()
                            viewModel.reset()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "GENERATED PALETTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    uiState.generatedPalette.forEachIndexed { index, color ->
                        ColorSwatchItem(
                            color = color,
                            index = index,
                            isLocked = index in uiState.lockedIndices,
                            onCopy = {
                                triggerHaptic()
                                val hex = viewModel.copySingleColor(it)
                                clipboardManager.setText(AnnotatedString(hex))
                            },
                            onSetAsBase = {
                                triggerHaptic()
                                viewModel.setSwatchAsBase(it)
                            },
                            onToggleLock = {
                                triggerHaptic()
                                viewModel.toggleLockColor(it)
                            },
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }
            }
        } else {
            // Single-Column Layout for Phone Portrait
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    GeneratorHeader()
                }

                item {
                    ColorPreviewCard(
                        baseColor = uiState.baseColor,
                        hexInputText = uiState.hexInputText,
                        hexInputError = uiState.hexInputError,
                        showRgb = settings.showRgb,
                        showHsl = settings.showHsl,
                        onHexChanged = {
                            viewModel.onHexInputChanged(it)
                        },
                        onCopyHex = { color ->
                            triggerHaptic()
                            val hex = viewModel.copySingleColor(color)
                            clipboardManager.setText(AnnotatedString(hex))
                        }
                    )
                }

                item {
                    ColorPickerCanvas(
                        hue = uiState.hue,
                        saturation = uiState.pickerSaturation,
                        value = uiState.pickerValue,
                        onColorChanged = { h, s, v ->
                            viewModel.onPickerColorChanged(h, s, v)
                        }
                    )
                }

                if (settings.showRgb) {
                    item {
                        RgbInputSection(
                            rgb = uiState.baseColor,
                            onRgbChanged = { r, g, b ->
                                viewModel.onRgbChannelsChanged(r, g, b)
                            }
                        )
                    }
                }

                if (settings.showHsl) {
                    item {
                        HslInputSection(
                            hsl = ColorUtils.rgbToHsl(uiState.baseColor),
                            onHslChanged = { h, s, l ->
                                viewModel.onHslChannelsChanged(h, s, l)
                            }
                        )
                    }
                }

                item {
                    PaletteTypeSelector(
                        selectedType = uiState.paletteType,
                        onTypeSelected = {
                            triggerHaptic()
                            viewModel.setPaletteType(it)
                        }
                    )
                }

                item {
                    PaletteSizeSelector(
                        selectedSize = uiState.paletteSize,
                        onSizeSelected = {
                            triggerHaptic()
                            viewModel.setPaletteSize(it)
                        }
                    )
                }

                item {
                    ActionButtonsRow(
                        onCopyPalette = {
                            triggerHaptic()
                            val text = viewModel.copyPaletteHexList()
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        onSavePalette = {
                            triggerHaptic()
                            savePaletteName = "${uiState.paletteType.displayName} ${savedPalettes.size + 1}"
                            showSaveDialog = true
                        },
                        onRandomize = {
                            triggerHaptic()
                            viewModel.randomize()
                        },
                        onReset = {
                            triggerHaptic()
                            viewModel.reset()
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GENERATED PALETTE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.generatedPalette.size} colors",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(uiState.generatedPalette) { index, color ->
                    ColorSwatchItem(
                        color = color,
                        index = index,
                        isLocked = index in uiState.lockedIndices,
                        onCopy = {
                            triggerHaptic()
                            val hex = viewModel.copySingleColor(it)
                            clipboardManager.setText(AnnotatedString(hex))
                        },
                        onSetAsBase = {
                            triggerHaptic()
                            viewModel.setSwatchAsBase(it)
                        },
                        onToggleLock = {
                            triggerHaptic()
                            viewModel.toggleLockColor(it)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Save Palette Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Palette",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Name this palette to save it offline in your collection:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = savePaletteName,
                        onValueChange = { savePaletteName = it },
                        label = { Text("Palette Name") },
                        placeholder = { Text("Ocean Harmony") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentPalette(savePaletteName)
                        showSaveDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GeneratorHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CHROMAMIX",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Create beautiful color palettes offline.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtonsRow(
    onCopyPalette: () -> Unit,
    onSavePalette: () -> Unit,
    onRandomize: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Primary Row: Save Palette & Copy Palette
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSavePalette,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onCopyPalette,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("COPY ALL", fontWeight = FontWeight.SemiBold)
            }
        }

        // Secondary Row: Randomize & Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRandomize,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("RANDOMIZE")
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("RESET")
            }
        }
    }
}
