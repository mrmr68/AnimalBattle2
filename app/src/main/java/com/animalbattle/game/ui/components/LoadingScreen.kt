package com.animalbattle.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animalbattle.game.ui.theme.*
import kotlinx.coroutines.delay

// ── Loading Screen ─────────────────────────────────────────

@Composable
fun LoadingScreen(
    isLoading: Boolean,
    onLoadingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var tipIndex by remember { mutableIntStateOf(0) }

    val tips = listOf(
        "💡 Use combo bonuses to earn extra XP!",
        "💡 Perfect answers give bonus rewards!",
        "💡 Upgrade your animals for stronger attacks!",
        "💡 Complete stages to unlock new regions!",
        "💡 Chests contain rare skins and boosts!",
        "💡 Different animals have different strengths!",
        "💡 Save your progress regularly!",
        "💡 Use boost items for double rewards!",
        "💡 Watch out for boss stages!",
        "💡 Explore all 6 regions of the world map!"
    )

    // Animate progress
    LaunchedEffect(isLoading) {
        if (isLoading) {
            progress = 0f
            while (progress < 1f) {
                delay(50)
                progress = (progress + 0.02f).coerceAtMost(1f)
            }
            onLoadingComplete()
        }
    }

    // Cycle tips
    LaunchedEffect(isLoading) {
        while (isLoading) {
            delay(3000)
            tipIndex = (tipIndex + 1) % tips.size
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A237E),
                            Color(0xFF283593),
                            Color(0xFF3949AB)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Game logo area
                Text(
                    text = "⚔️",
                    fontSize = 72.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ANIMAL BATTLE",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    ),
                    color = Gold
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Animated loading dots
                LoadingDots()

                Spacer(modifier = Modifier.height(24.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Gold, GoldLight, Gold)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Tip
                Text(
                    text = tips[tipIndex],
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
    }
}

// ── Loading Dots Animation ─────────────────────────────────

@Composable
private fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$i"
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = alpha))
            )
        }
    }
}

// ── Mini Loading Indicator ─────────────────────────────────

@Composable
fun MiniLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Text(
        text = "⏳",
        fontSize = 24.sp,
        modifier = modifier
    )
}
