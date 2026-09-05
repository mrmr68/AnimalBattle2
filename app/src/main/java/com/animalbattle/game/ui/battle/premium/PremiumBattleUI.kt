package com.animalbattle.game.ui.battle.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animalbattle.game.ui.theme.*
import kotlinx.coroutines.delay

// ── Premium Timer ──────────────────────────────────────────

@Composable
fun PremiumTimer(
    secondsRemaining: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = secondsRemaining.toFloat() / totalSeconds
    val timerColor = when {
        progress > 0.5f -> VictoryGreen
        progress > 0.25f -> WarningOrange
        else -> DefeatRed
    }

    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (secondsRemaining <= 5) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer_pulse_scale"
    )

    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Timer ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background ring
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            // Progress ring
            drawArc(
                color = timerColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
        }

        // Timer text
        Text(
            text = secondsRemaining.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = (28 * pulseScale).sp
            ),
            color = timerColor
        )
    }
}

// ── Premium Question Panel ─────────────────────────────────

@Composable
fun PremiumQuestionPanel(
    questionText: String,
    options: List<String>,
    onAnswer: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PanelBackground, Gold.copy(alpha = 0.05f))
                )
            )
            .border(2.dp, Gold, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question text
        Text(
            text = questionText,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Options grid (2x2)
        for (rowIndex in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (colIndex in 0..1) {
                    val optionIndex = rowIndex * 2 + colIndex
                    if (optionIndex < options.size) {
                        val isSelected = selectedOption == optionIndex
                        val optionColor = when {
                            isSelected -> GradientGoldStart
                            else -> PanelBackground
                        }
                        val borderColor = when {
                            isSelected -> GoldDark
                            else -> PanelBorder
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(optionColor)
                                .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = enabled) {
                                    selectedOption = optionIndex
                                    onAnswer(optionIndex)
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = options[optionIndex],
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (isSelected) TextOnGold else TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            if (rowIndex == 0) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Combo Counter ──────────────────────────────────────────

@Composable
fun ComboCounter(
    combo: Int,
    modifier: Modifier = Modifier
) {
    if (combo <= 0) return

    val comboColor = when {
        combo >= 5 -> Color(0xFFFFD700)
        combo >= 3 -> Color(0xFFFF6D00)
        else -> Color(0xFF4CAF50)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "combo_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (combo >= 3) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "combo_scale"
    )

    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(comboColor, comboColor.copy(alpha = 0.7f))
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🔥",
                fontSize = (20 * scale).sp
            )
            Text(
                text = "COMBO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Text(
                text = "${combo}x",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (24 * scale).sp
                ),
                color = Color.White
            )
        }
    }
}

// ── Boost Meter ────────────────────────────────────────────

@Composable
fun BoostMeter(
    boostLevel: Float, // 0f to 1f
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = boostLevel,
        animationSpec = tween(300),
        label = "boost_level"
    )

    val glowColor = when {
        boostLevel >= 1f -> Color(0xFFFFD700)
        boostLevel >= 0.7f -> Color(0xFF00E5FF)
        else -> Color(0xFF42A5F5)
    }

    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(PanelBackground.copy(alpha = 0.9f))
            .border(1.5.dp, glowColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚡ BOOST",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = glowColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Boost bar
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .border(1.dp, glowColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        ) {
            // Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((120 * animatedLevel).dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(glowColor, glowColor.copy(alpha = 0.6f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(boostLevel * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary
        )
    }
}

// ── Race Progress Bar ──────────────────────────────────────

@Composable
fun RaceProgressBar(
    playerProgress: Float, // 0f to 1f
    opponentProgress: Float, // 0f to 1f
    modifier: Modifier = Modifier
) {
    val animatedPlayer by animateFloatAsState(
        targetValue = playerProgress,
        animationSpec = tween(500),
        label = "player_progress"
    )
    val animatedOpponent by animateFloatAsState(
        targetValue = opponentProgress,
        animationSpec = tween(500),
        label = "opponent_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(PanelBackground.copy(alpha = 0.95f))
            .border(2.dp, Gold, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Player progress
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🦁",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPlayer)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(VictoryGreen, VictoryGreenDark)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(animatedPlayer * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = VictoryGreen
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Opponent progress
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🐯",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedOpponent)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DefeatRed, DefeatRedDark)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(animatedOpponent * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DefeatRed
            )
        }
    }
}

// ── Finish Overlay ─────────────────────────────────────────

@Composable
fun FinishOverlay(
    isVictory: Boolean,
    coinsEarned: Int,
    trophiesEarned: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 },
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onContinue() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (isVictory) GradientGoldStart else DefeatRed.copy(alpha = 0.9f),
                                if (isVictory) GradientGoldEnd else DefeatRedDark
                            )
                        )
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(32.dp)
            ) {
                // Victory/Defeat icon
                Text(
                    text = if (isVictory) "🏆" else "💔",
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = if (isVictory) "VICTORY!" else "DEFEAT",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Rewards
                if (isVictory) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        RewardBadge(icon = "🪙", amount = coinsEarned, label = "Coins")
                        RewardBadge(icon = "🏆", amount = trophiesEarned, label = "Trophies")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Continue button
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onContinue() }
                        .padding(horizontal = 40.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONTINUE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardBadge(
    icon: String,
    amount: Int,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "+$amount",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

// ── Premium HP Bar ─────────────────────────────────────────

@Composable
fun PremiumHealthBar(
    currentHp: Int,
    maxHp: Int,
    isPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    val hpRatio = currentHp.toFloat() / maxHp
    val barColor = when {
        hpRatio > 0.6f -> if (isPlayer) VictoryGreen else DefeatRed
        hpRatio > 0.3f -> WarningOrange
        else -> DefeatRed
    }

    val animatedRatio by animateFloatAsState(
        targetValue = hpRatio,
        animationSpec = tween(300),
        label = "hp_ratio"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isPlayer) "❤️ HP" else "💔 HP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = barColor
            )
            Text(
                text = "$currentHp / $maxHp",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .border(1.dp, barColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor, barColor.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}
