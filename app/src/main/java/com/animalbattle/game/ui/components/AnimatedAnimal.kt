package com.animalbattle.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.Animal
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.HpRed
import com.animalbattle.game.ui.theme.XpBlue

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
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Attack animation
    val attackOffset by animateFloatAsState(
        targetValue = if (isAttacking) 20f else 0f,
        animationSpec = tween(200),
        label = "attack_offset"
    )

    // Damage flash
    val damageAlpha by animateFloatAsState(
        targetValue = if (isTakingDamage) 0.5f else 1f,
        animationSpec = tween(100, repeatMode = RepeatMode.Reverse),
        label = "damage_flash"
    )

    // Power glow intensity
    val glowIntensity = (powerLevel / 10f).coerceIn(0.1f, 1f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Power glow effect
        if (powerLevel > 3) {
            Canvas(
                modifier = Modifier
                    .size(sizeDp + 20.dp)
                    .alpha(glowIntensity * 0.6f)
            ) {
                drawCircle(
                    color = if (powerLevel > 7) HpRed else Gold,
                    radius = size.width / 2 + 10f,
                    style = Stroke(width = 4f)
                )
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
                    translationX = if (isAttacking) attackOffset else 0f
                }
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 3.dp,
                    color = when {
                        isAttacking -> HpRed
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
