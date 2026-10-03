package com.example.chromamix.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.chromamix.domain.ColorUtils

@Composable
fun ColorPickerCanvas(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChanged: (hue: Float, saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pureHueRgb = remember(hue) {
        ColorUtils.hsvToRgb(hue, 1f, 1f)
    }
    val pureHueColor = remember(pureHueRgb) {
        Color(pureHueRgb.red, pureHueRgb.green, pureHueRgb.blue)
    }

    Column(modifier = modifier) {
        // Saturation / Value 2D Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .semantics {
                    contentDescription = "Saturation and brightness color picker area"
                }
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(hue) {
                        detectTapGestures { offset ->
                            val newSat = (offset.x / size.width).coerceIn(0f, 1f)
                            val newVal = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                            onColorChanged(hue, newSat, newVal)
                        }
                    }
                    .pointerInput(hue) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newSat = (change.position.x / size.width).coerceIn(0f, 1f)
                            val newVal = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                            onColorChanged(hue, newSat, newVal)
                        }
                    }
            ) {
                // Layer 1: Horizontal gradient from White to Pure Hue
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, pureHueColor),
                        startX = 0f,
                        endX = size.width
                    ),
                    size = size
                )

                // Layer 2: Vertical gradient from Transparent to Black
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 0f,
                        endY = size.height
                    ),
                    size = size
                )

                // Selector thumb indicator
                val thumbX = (saturation * size.width).coerceIn(8f, size.width - 8f)
                val thumbY = ((1f - value) * size.height).coerceIn(8f, size.height - 8f)
                val thumbCenter = Offset(thumbX, thumbY)

                // Outer shadow/border
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = 11f,
                    center = thumbCenter,
                    style = Stroke(width = 3.5f)
                )
                // Inner white ring
                drawCircle(
                    color = Color.White,
                    radius = 9f,
                    center = thumbCenter,
                    style = Stroke(width = 2.5f)
                )
                // Center dot
                val currentRgb = ColorUtils.hsvToRgb(hue, saturation, value)
                drawCircle(
                    color = Color(currentRgb.red, currentRgb.green, currentRgb.blue),
                    radius = 6.5f,
                    center = thumbCenter
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hue Slider Canvas Bar
        Text(
            text = "Hue",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(15.dp))
                .semantics {
                    contentDescription = "Hue slider, currently ${hue.toInt()} degrees"
                }
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(saturation, value) {
                        detectTapGestures { offset ->
                            val newHue = ((offset.x / size.width).coerceIn(0f, 1f) * 360f) % 360f
                            onColorChanged(newHue, saturation, value)
                        }
                    }
                    .pointerInput(saturation, value) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newHue = ((change.position.x / size.width).coerceIn(0f, 1f) * 360f) % 360f
                            onColorChanged(newHue, saturation, value)
                        }
                    }
            ) {
                // Rainbow Hue Spectrum
                val rainbowColors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = rainbowColors,
                        startX = 0f,
                        endX = size.width
                    ),
                    size = size
                )

                // Hue Thumb Indicator
                val normalizedHue = ColorUtils.normalizeHue(hue)
                val thumbX = ((normalizedHue / 360f) * size.width).coerceIn(10f, size.width - 10f)

                // Thumb vertical pill indicator
                drawLine(
                    color = Color.Black.copy(alpha = 0.6f),
                    start = Offset(thumbX, 2f),
                    end = Offset(thumbX, size.height - 2f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(thumbX, 3f),
                    end = Offset(thumbX, size.height - 3f),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
