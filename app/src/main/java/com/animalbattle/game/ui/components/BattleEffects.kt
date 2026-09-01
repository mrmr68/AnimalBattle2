package com.animalbattle.game.ui.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animalbattle.game.ui.theme.DefeatRed
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.HpRed
import com.animalbattle.game.ui.theme.VictoryGreen
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * A floating damage number that rises up and fades out.
 */
@Composable
fun FloatingDamageNumber(
    damage: Int,
    isCritical: Boolean = false,
    isHeal: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = when {
        isHeal -> VictoryGreen
        isCritical -> Color(0xFFFF1744)
        else -> HpRed
    }

    val text = if (isHeal) "+$damage" else "-$damage"
    val fontSize = if (isCritical) 36.sp else 28.sp

    // Animate upward movement
    val infiniteTransition = rememberInfiniteTransition(label = "float_up")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    // Animate fade out
    val fadeAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade_alpha"
    )

    // Pulse effect
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCritical) 1.3f else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        modifier = modifier
            .alpha(fadeAlpha)
            .graphicsLayer {
                translationY = floatY
                scaleX = pulse
                scaleY = pulse
            }
    )
}

/**
 * Screen flash effect when a big hit lands.
 */
@Composable
fun ScreenFlash(
    trigger: Boolean,
    color: Color = Color.White,
    intensity: Float = 0.3f
) {
    val alpha by animateFloatAsState(
        targetValue = if (trigger) intensity else 0f,
        animationSpec = tween(80),
        label = "flash_alpha"
    )

    if (alpha > 0f) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawRect(
                color = color.copy(alpha = alpha)
            )
        }
    }
}

/**
 * Manages a list of floating damage numbers at specific positions.
 */
class DamageNumberManager {
    data class DamageEntry(
        val id: Long,
        val damage: Int,
        val x: Float,
        val y: Float,
        val isCritical: Boolean = false,
        val isHeal: Boolean = false
    )

    private val _entries = mutableStateListOf<DamageEntry>()
    val entries: List<DamageEntry> get() = _entries

    fun addDamage(damage: Int, x: Float, y: Float, isCritical: Boolean = false) {
        val entry = DamageEntry(
            id = System.nanoTime(),
            damage = damage,
            x = x,
            y = y,
            isCritical = isCritical
        )
        _entries.add(entry)
    }

    fun addHeal(amount: Int, x: Float, y: Float) {
        val entry = DamageEntry(
            id = System.nanoTime(),
            damage = amount,
            x = x,
            y = y,
            isHeal = true
        )
        _entries.add(entry)
    }

    fun remove(id: Long) {
        _entries.removeAll { it.id == id }
    }
}

/**
 * Renders all active floating damage numbers with auto-removal.
 */
@Composable
fun FloatingDamageOverlay(
    manager: DamageNumberManager,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        manager.entries.forEach { entry ->
            LaunchedEffect(entry.id) {
                delay(1200)
                manager.remove(entry.id)
            }

            FloatingDamageNumber(
                damage = entry.damage,
                isCritical = entry.isCritical,
                isHeal = entry.isHeal,
                modifier = Modifier.offset {
                    IntOffset(entry.x.roundToInt(), entry.y.roundToInt())
                }
            )
        }
    }
}

/**
 * Attack trail / slash effect that appears briefly during attacks.
 */
@Composable
fun AttackSlashEffect(
    trigger: Boolean,
    isPlayerAttacking: Boolean,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val alpha by animateFloatAsState(
        targetValue = if (trigger) 0.8f else 0f,
        animationSpec = tween(300, easing = LinearEasing),
        label = "slash_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (trigger) 1.5f else 0.5f,
        animationSpec = tween(250, easing = EaseOut),
        label = "slash_scale"
    )

    Canvas(
        modifier = modifier
            .alpha(alpha)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Slash lines
        drawLine(
            color = Color(0xFFFFD700),
            start = Offset(centerX - 40, centerY - 40),
            end = Offset(centerX + 40, centerY + 40),
            strokeWidth = 6f
        )
        drawLine(
            color = Color(0xFFFF6B35),
            start = Offset(centerX + 40, centerY - 40),
            end = Offset(centerX - 40, centerY + 40),
            strokeWidth = 6f
        )
        drawLine(
            color = Color.White,
            start = Offset(centerX, centerY - 50),
            end = Offset(centerX, centerY + 50),
            strokeWidth = 4f
        )
    }
}
