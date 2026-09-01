package com.animalbattle.game.ui.components

import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
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
import androidx.compose.runtime.remember
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
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

/** Unique attack animation style per animal */
enum class AttackStyle {
    ROAR,        // Lion: shake + scale up
    CLAW,        // Tiger: horizontal swipe
    DASH,        // Leopard: quick forward dash
    SPRINT,      // Cheetah: lightning fast
    SLAM,        // Bear: heavy downward slam
    HOWL,        // Wolf: upward howl
    POUND,       // Gorilla: chest pound shake
    CHARGE,      // Rhino: straight heavy charge
    STOMP,       // Elephant: downward heavy stomp
    ROLL,        // Crocodile: spinning death roll
    DIVE,        // Eagle: dive bomb from above
    VENOM        // Cobra: quick venom jab
}

fun getAttackStyle(animalId: String): AttackStyle {
    return when (animalId) {
        "lion" -> AttackStyle.ROAR
        "tiger" -> AttackStyle.CLAW
        "leopard" -> AttackStyle.DASH
        "cheetah" -> AttackStyle.SPRINT
        "bear" -> AttackStyle.SLAM
        "wolf" -> AttackStyle.HOWL
        "gorilla" -> AttackStyle.POUND
        "rhino" -> AttackStyle.CHARGE
        "elephant" -> AttackStyle.STOMP
        "crocodile" -> AttackStyle.ROLL
        "eagle" -> AttackStyle.DIVE
        "cobra" -> AttackStyle.VENOM
        else -> AttackStyle.ROAR
    }
}

@Composable
fun AnimatedAnimal(
    animal: Animal,
    size: AnimalSize = AnimalSize.MEDIUM,
    isAttacking: Boolean = false,
    isTakingDamage: Boolean = false,
    powerLevel: Int = 1,
    abilityIndex: Int = 0,
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

    // Get animal-specific attack style
    val attackStyle = remember(animal.id) { getAttackStyle(animal.id) }

    // Attack lunge — animal-specific motion
    val attackOffsetX by animateFloatAsState(
        targetValue = when {
            !isAttacking -> 0f
            attackStyle == AttackStyle.DIVE -> 0f
            attackStyle == AttackStyle.STOMP -> 0f
            attackStyle == AttackStyle.POUND -> 0f
            attackStyle == AttackStyle.SLAM -> 0f
            attackStyle == AttackStyle.SPRINT -> 70f
            attackStyle == AttackStyle.DASH -> 55f
            attackStyle == AttackStyle.CHARGE -> 65f
            attackStyle == AttackStyle.CLAW -> 40f
            attackStyle == AttackStyle.VENOM -> 30f
            else -> 50f // ROAR, HOWL, ROLL
        },
        animationSpec = when {
            !isAttacking -> spring(dampingRatio = 0.4f)
            attackStyle == AttackStyle.SPRINT -> tween(80, easing = EaseInBack)
            attackStyle == AttackStyle.DASH -> tween(100, easing = EaseInBack)
            attackStyle == AttackStyle.CLAW -> tween(150, easing = EaseInBack)
            attackStyle == AttackStyle.VENOM -> tween(100, easing = EaseInBack)
            else -> tween(140, easing = EaseInBack)
        },
        label = "attack_lunge_x"
    )

    // Vertical attack offset for dive/stomp/slam
    val attackOffsetY by animateFloatAsState(
        targetValue = when {
            !isAttacking -> 0f
            attackStyle == AttackStyle.DIVE -> 60f
            attackStyle == AttackStyle.STOMP -> -40f
            attackStyle == AttackStyle.SLAM -> -30f
            attackStyle == AttackStyle.HOWL -> -20f
            attackStyle == AttackStyle.POUND -> 15f
            else -> 0f
        },
        animationSpec = when {
            !isAttacking -> spring(dampingRatio = 0.4f)
            attackStyle == AttackStyle.DIVE -> tween(200, easing = EaseInBack)
            attackStyle == AttackStyle.STOMP -> tween(150)
            attackStyle == AttackStyle.SLAM -> tween(150)
            else -> spring()
        },
        label = "attack_lunge_y"
    )

    // Attack rotation for ROLL
    val attackRotation by animateFloatAsState(
        targetValue = when {
            !isAttacking -> 0f
            attackStyle == AttackStyle.ROLL -> 360f
            attackStyle == AttackStyle.POUND -> -8f
            attackStyle == AttackStyle.ROAR -> 5f
            else -> 0f
        },
        animationSpec = when {
            !isAttacking -> spring()
            attackStyle == AttackStyle.ROLL -> tween(400)
            attackStyle == AttackStyle.POUND -> tween(100, repeatMode = RepeatMode.Reverse)
            attackStyle == AttackStyle.ROAR -> tween(200, repeatMode = RepeatMode.Reverse)
            else -> spring()
        },
        label = "attack_rotation"
    )

    // Attack scale — animal-specific
    val attackScale by animateFloatAsState(
        targetValue = when {
            !isAttacking -> 1f
            attackStyle == AttackStyle.SLAM -> 1.1f
            attackStyle == AttackStyle.STOMP -> 1.15f
            attackStyle == AttackStyle.POUND -> 1.12f
            attackStyle == AttackStyle.CHARGE -> 1.2f
            attackStyle == AttackStyle.ROAR -> 1.18f
            attackStyle == AttackStyle.HOWL -> 1.1f
            else -> 1.15f
        },
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

        // Check for Lottie animation
        val lottieRes = remember(animal.id, abilityIndex) { getAnimalAttackAnimation(animal.id, abilityIndex) }
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(lottieRes ?: R.raw.lion_claw)
        )
        val lottieProgress by animateLottieCompositionAsState(
            composition,
            iterations = if (isAttacking) 1 else 0,
            speed = 1.5f
        )

        // Show Lottie when attacking, otherwise show static image
        if (isAttacking && lottieRes != null && composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { lottieProgress },
                modifier = Modifier
                    .size(sizeDp)
                    .graphicsLayer {
                        translationX = attackOffsetX + shakeOffset + recoilOffset
                        translationY = attackOffsetY + idleOffset
                        scaleX = attackScale
                        scaleY = attackScale
                    }
            )
        } else {
            // Main animal image
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = animal.nameResId.toString(),
                modifier = Modifier
                    .size(sizeDp)
                    .alpha(damageAlpha)
                    .graphicsLayer {
                        translationX = attackOffsetX + shakeOffset + recoilOffset
                        translationY = attackOffsetY + idleOffset
                        rotationZ = attackRotation
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

/**
 * Get Lottie animation resource for animal attack.
 * Returns null if no Lottie animation is available.
 */
fun getAnimalAttackAnimation(animalId: String, abilityIndex: Int = 0): Int? {
    val abilityName = when (abilityIndex) {
        0 -> when (animalId) {
            "lion" -> "claw"
            "tiger" -> "strike"
            "leopard" -> "swipe"
            "cheetah" -> "scratch"
            "bear" -> "swipe"
            "wolf" -> "bite"
            "gorilla" -> "punch"
            "rhino" -> "gore"
            "elephant" -> "trunk"
            "crocodile" -> "bite"
            "eagle" -> "talon"
            "cobra" -> "spit"
            else -> "claw"
        }
        1 -> when (animalId) {
            "lion" -> "roar"
            "tiger" -> "pounce"
            "leopard" -> "dash"
            "cheetah" -> "sprint"
            "bear" -> "maul"
            "wolf" -> "howl"
            "gorilla" -> "slam"
            "rhino" -> "charge"
            "elephant" -> "stomp"
            "crocodile" -> "roll"
            "eagle" -> "dive"
            "cobra" -> "constrict"
            else -> "roar"
        }
        2 -> when (animalId) {
            "lion" -> "fury"
            "tiger" -> "fang"
            "leopard" -> "ambush"
            "cheetah" -> "blitz"
            "bear" -> "crush"
            "wolf" -> "pack"
            "gorilla" -> "rage"
            "rhino" -> "rampage"
            "elephant" -> "tusks"
            "crocodile" -> "lurk"
            "eagle" -> "storm"
            "cobra" -> "venom"
            else -> "fury"
        }
        else -> "claw"
    }
    
    val resId = when (animalId) {
        "lion" -> when (abilityName) {
            "claw" -> R.raw.lion_claw
            "roar" -> R.raw.lion_roar
            "fury" -> R.raw.lion_fury
            else -> null
        }
        "tiger" -> when (abilityName) {
            "strike" -> R.raw.tiger_strike
            "pounce" -> R.raw.tiger_pounce
            "fang" -> R.raw.tiger_fang
            else -> null
        }
        "leopard" -> when (abilityName) {
            "swipe" -> R.raw.leopard_swipe
            "dash" -> R.raw.leopard_dash
            "ambush" -> R.raw.leopard_ambush
            else -> null
        }
        "cheetah" -> when (abilityName) {
            "scratch" -> R.raw.cheetah_scratch
            "sprint" -> R.raw.cheetah_sprint
            "blitz" -> R.raw.cheetah_blitz
            else -> null
        }
        "bear" -> when (abilityName) {
            "swipe" -> R.raw.bear_swipe
            "maul" -> R.raw.bear_maul
            "crush" -> R.raw.bear_crush
            else -> null
        }
        "wolf" -> when (abilityName) {
            "bite" -> R.raw.wolf_bite
            "howl" -> R.raw.wolf_howl
            "pack" -> R.raw.wolf_pack
            else -> null
        }
        "gorilla" -> when (abilityName) {
            "punch" -> R.raw.gorilla_punch
            "slam" -> R.raw.gorilla_slam
            "rage" -> R.raw.gorilla_rage
            else -> null
        }
        "rhino" -> when (abilityName) {
            "gore" -> R.raw.rhino_gore
            "charge" -> R.raw.rhino_charge
            "rampage" -> R.raw.rhino_rampage
            else -> null
        }
        "elephant" -> when (abilityName) {
            "trunk" -> R.raw.elephant_trunk
            "stomp" -> R.raw.elephant_stomp
            "tusks" -> R.raw.elephant_tusks
            else -> null
        }
        "crocodile" -> when (abilityName) {
            "bite" -> R.raw.crocodile_bite
            "roll" -> R.raw.crocodile_roll
            "lurk" -> R.raw.crocodile_lurk
            else -> null
        }
        "eagle" -> when (abilityName) {
            "talon" -> R.raw.eagle_talon
            "dive" -> R.raw.eagle_dive
            "storm" -> R.raw.eagle_storm
            else -> null
        }
        "cobra" -> when (abilityName) {
            "spit" -> R.raw.cobra_spit
            "constrict" -> R.raw.cobra_constrict
            "venom" -> R.raw.cobra_venom
            else -> null
        }
        else -> null
    }
    
    return resId
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
