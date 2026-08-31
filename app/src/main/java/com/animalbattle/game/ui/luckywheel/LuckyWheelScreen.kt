package com.animalbattle.game.ui.luckywheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.GameConfig
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GameButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.WheelBlue
import com.animalbattle.game.ui.theme.WheelGreen
import com.animalbattle.game.ui.theme.WheelOrange
import com.animalbattle.game.ui.theme.WheelPink
import com.animalbattle.game.ui.theme.WheelPurple
import com.animalbattle.game.ui.theme.WheelRed
import com.animalbattle.game.ui.theme.WheelTeal
import com.animalbattle.game.ui.theme.WheelYellow
import com.animalbattle.game.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LuckyWheelScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val player by viewModel.player.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val wheelResult by viewModel.wheelResult.collectAsState()
    val segments = GameConfig.WHEEL_SEGMENTS

    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            val targetAngle = 360f * 5 + (0..360).random().toFloat()
            rotation.animateTo(
                targetValue = rotation.value + targetAngle,
                animationSpec = tween(
                    durationMillis = 3000,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopBar(
                playerName = player.name,
                level = player.calculateLevel(),
                coins = player.coins,
                trophies = player.trophies,
                xpProgress = player.xpToNextLevel().toFloat().let { if (it > 0) player.xpForNextLevel().toFloat() / it else 0f }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = onNavigateBack)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.lucky_wheel_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wheel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wheel Canvas
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(DarkGreenPrimary)
                        .border(4.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(CircleShape)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2
                        val segmentAngle = 360f / segments.size

                        segments.forEachIndexed { index, segment ->
                            val startAngle = index * segmentAngle - 90f

                            // Draw segment
                            drawArc(
                                color = Color(segment.color),
                                startAngle = startAngle,
                                sweepAngle = segmentAngle,
                                useCenter = true,
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height)
                            )

                            // Draw border
                            drawArc(
                                color = Color.White.copy(alpha = 0.3f),
                                startAngle = startAngle,
                                sweepAngle = segmentAngle,
                                useCenter = true,
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height),
                                style = Stroke(width = 2f)
                            )

                            // Draw text
                            rotate(
                                degrees = startAngle + segmentAngle / 2,
                                pivot = center
                            ) {
                                drawContext.canvas.nativeCanvas.apply {
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 28f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isFakeBoldText = true
                                    }
                                    drawText(
                                        segment.label,
                                        center.x,
                                        radius * 0.65f,
                                        paint
                                    )
                                }
                            }
                        }
                    }

                    // Center circle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Gold)
                            .border(3.dp, GoldDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎰",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Info Panel
                GamePanel(
                    modifier = Modifier.width(200.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.spin_cost, GameConfig.LUCKY_WHEEL_SPIN_COST),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val spinsLeft = GameConfig.MAX_DAILY_SPINS - player.luckyWheelSpinsToday
                        Text(
                            text = "Spins left: $spinsLeft",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (spinsLeft > 0) GoldDark else TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GameButton(
                            text = stringResource(R.string.spin_wheel),
                            onClick = { viewModel.spinWheel() },
                            enabled = !isSpinning &&
                                    player.coins >= GameConfig.LUCKY_WHEEL_SPIN_COST &&
                                    spinsLeft > 0,
                            modifier = Modifier.fillMaxWidth()
                        )

                        wheelResult?.let { reward ->
                            Spacer(modifier = Modifier.height(16.dp))
                            GamePanel(
                                backgroundColor = Gold.copy(alpha = 0.2f)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🎉",
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                    Text(
                                        text = stringResource(R.string.wheel_result, reward.description),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GoldDark,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    GameButton(
                                        text = stringResource(R.string.ok),
                                        onClick = { viewModel.clearWheelResult() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
