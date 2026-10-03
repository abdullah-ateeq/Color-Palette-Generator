package com.example.chromamix.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val animationProgress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }

    val splashColors = remember {
        listOf(
            Color(0xFF283593),
            Color(0xFF3F51B5),
            Color(0xFF5C6BC0),
            Color(0xFF7986CB),
            Color(0xFF9FA8DA)
        )
    }

    LaunchedEffect(Unit) {
        // Step 1: Animate swatches expanding
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        // Step 2: Title fades in
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )
        // Wait total 1.3 - 1.4s
        delay(250)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Expanding Swatches Row
            Row(
                horizontalArrangement = Arrangement.spacedBy((6 * animationProgress.value).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                splashColors.forEachIndexed { index, color ->
                    val factor = ((animationProgress.value * (splashColors.size + 1)) - index)
                        .coerceIn(0f, 1f)

                    val swatchWidth = (22f * factor).dp
                    val swatchHeight = (48f * factor).dp

                    Box(
                        modifier = Modifier
                            .width(swatchWidth)
                            .height(swatchHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Name & Subtitle Fade-in
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha.value)
            ) {
                Text(
                    text = "CHROMAMIX",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Harmonious offline color palettes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
