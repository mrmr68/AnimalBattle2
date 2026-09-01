package com.animalbattle.game.ui.components

import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.Animal
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.HpRed
import kotlin.math.sin

enum class AnimalSize {
    SMALL,   // For thumbnails
    MEDIUM,  // For selection
    LARGE    // For battle
}

@Composable
fun AnimatedAnimal(
    animal: Animal,
    size: AnimalSize = AnimalSize.MEDIUM,
    isAttacking: Boolean = false,
    isTakingDamage: Boolean = false,
    powerLevel: Int = 1,
    modifier: Modifier = Modifier
) {
    val sizeDp = when (size) {
        AnimalSize.SMALL -> 64.dp
        AnimalSize.MEDIUM -> 120.dp
        AnimalSize.LARGE -> 150.dp
    }

    val drawableRes = getAnimalDrawable(animal.id)

    // Breathe animation
    val infiniteTransition = rememberInfiniteTransition(label = "animal_breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Attack lunge — aggressive forward motion then spring back
    val attackOffset by animateFloatAsState(
        targetValue = if (isAttacking) 50f else 0f,
        animationSpec = if (isAttacking) {
            tween(120, easing = EaseInBack)
        } else {
            spring(dampingRatio = 0.4f)
        },
        label = "attack_lunge"
    )

    // Attack scale — punch effect on hit
    val attackScale by animateFloatAsState(
        targetValue = if (isAttacking) 1.2f else 1f,
        animationSpec = if (isAttacking) {
            tween(120)
        } else {
            spring(dampingRatio = 0.5f)
        },
        label = "attack_scale"
    )

    // Damage shake — multi-frame horizontal jitter
    val shakeOffset by animateFloatAsState(
        targetValue = if (isTakingDamage) 15f else 0f,
        animationSpec = if (isTakingDamage) {
            tween(50, repeatMode = RepeatMode.Reverse)
        } else {
            tween(150)
        },
        label = "damage_shake"
    )

    // Damage flash — white flash on hit
    val damageAlpha by animateFloatAsState(
        targetValue = if (isTakingDamage) 0.3f else 1f,
        animationSpec = if (isTakingDamage) {
            tween(60, repeatMode = RepeatMode.Reverse)
        } else {
            tween(200)
        },
        label = "damage_flash"
    )

    // Recoil — pushes back hard when hit
    val recoilOffset by animateFloatAsState(
        targetValue = if (isTakingDamage) -25f else 0f,
        animationSpec = if (isTakingDamage) {
            tween(100, easing = EaseOutBack)
        } else {
            spring(dampingRatio = 0.5f)
        },
        label = "recoil"
    )

    // Idle bob — gentle floating motion
    val idleOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_bob"
    )

    // Power glow pulsing
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val glowIntensity = (powerLevel / 10f).coerceIn(0.1f, 1f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Power glow effect with pulsing
        if (powerLevel > 3) {
            Canvas(
                modifier = Modifier
                    .size(sizeDp + 24.dp)
                    .alpha(glowIntensity * glowPulse * 0.5f)
            ) {
                val radius = size.width / 2 + 12f
                drawCircle(
                    color = if (powerLevel > 7) HpRed else Gold,
                    radius = radius,
                    style = Stroke(width = 3f)
                )
                // Inner glow ring
                drawCircle(
                    color = if (powerLevel > 7) HpRed.copy(alpha = 0.3f) else Gold.copy(alpha = 0.3f),
                    radius = radius * 0.85f,
                    style = Stroke(width = 2f)
                )
            }
        }

        // Particle sparks during attack
        if (isAttacking && powerLevel > 2) {
            val particleTransition = rememberInfiniteTransition(label = "particles")
            val particlePhase by particleTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing)
                ),
                label = "particle_phase"
            )
            Canvas(
                modifier = Modifier.size(sizeDp + 30.dp)
            ) {
                val cx = size.width / 2
                val cy = size.height / 2
                val r = size.width / 2 - 5f
                for (i in 0..5) {
                    val angle = Math.toRadians((particlePhase + i * 60.0))
                    val px = cx + r * sin(angle).toFloat()
                    val py = cy + r * kotlin.math.cos(angle).toFloat()
                    drawCircle(
                        color = if (powerLevel > 7) HpRed else Gold,
                        radius = 4f,
                        center = Offset(px, py)
                    )
                }
            }
        }

        // Main animal image
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = animal.nameResId.toString(),
            modifier = Modifier
                .size(sizeDp)
                .alpha(damageAlpha)
                .graphicsLayer {
                    translationX = attackOffset + shakeOffset + recoilOffset
                    translationY = idleOffset
                    scaleX = attackScale * breatheScale
                    scaleY = attackScale * breatheScale
                }
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 3.dp,
                    color = when {
                        isAttacking -> HpRed
                        isTakingDamage -> Color(0xFFFF0000)
                        powerLevel > 7 -> Color(0xFFFF6B35)
                        powerLevel > 4 -> Gold
                        else -> Gold.copy(alpha = 0.5f)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
        )
    }
}

fun getAnimalDrawable(animalId: String): Int {
    return when (animalId) {
        "lion" -> R.drawable.animal_lion
        "tiger" -> R.drawable.animal_tiger
        "bear" -> R.drawable.animal_bear
        "wolf" -> R.drawable.animal_wolf
        "eagle" -> R.drawable.animal_eagle
        "cobra" -> R.drawable.animal_cobra
        "elephant" -> R.drawable.animal_elephant
        "leopard" -> R.drawable.animal_leopard
        "cheetah" -> R.drawable.animal_cheetah
        "gorilla" -> R.drawable.animal_bear
        "rhino" -> R.drawable.animal_elephant
        "crocodile" -> R.drawable.animal_wolf
        else -> R.drawable.animal_lion
    }
}

@Composable
fun PowerIndicator(
    power: Int,
    maxPower: Int = 10,
    color: Color = Gold,
    modifier: Modifier = Modifier
) {
    val animatedPower by animateFloatAsState(
        targetValue = power.toFloat() / maxPower,
        animationSpec = tween(500),
        label = "power_indicator"
    )

    Canvas(modifier = modifier.size(40.dp, 100.dp)) {
        // Background
        drawRoundRect(
            color = Color.LightGray,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        // Fill
        drawRoundRect(
            color = color,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            size = size.copy(height = size.height * animatedPower)
        )
        // Border
        drawRoundRect(
            color = color.copy(alpha = 0.7f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            style = Stroke(width = 2f)
        )
    }
}

@Composable
fun HealthBar(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    val hpRatio = currentHp.toFloat() / maxHp
    val animatedHp by animateFloatAsState(
        targetValue = hpRatio,
        animationSpec = tween(500),
        label = "hp_bar"
    )

    val barColor = when {
        hpRatio > 0.6f -> Color(0xFF4CAF50)
        hpRatio > 0.3f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Canvas(modifier = modifier.size(120.dp, 16.dp)) {
        // Background
        drawRoundRect(
            color = Color.LightGray,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        // Fill
        drawRoundRect(
            color = barColor,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            size = size.copy(width = size.width * animatedHp)
        )
        // Border
        drawRoundRect(
            color = barColor.copy(alpha = 0.7f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            style = Stroke(width = 2f)
        )
    }
}
