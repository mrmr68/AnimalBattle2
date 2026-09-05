package com.animalbattle.game.ui.battle.particles

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ── Particle Types ─────────────────────────────────────────

enum class ParticleType {
    DUST,
    JUMP,
    LANDING,
    SPEED_LINES,
    HIT,
    CORRECT_ANSWER,
    PERFECT_ANSWER,
    COMBO,
    BOOST,
    FINISH
}

// ── Particle Data ──────────────────────────────────────────

data class Particle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var size: Float = 4f,
    var alpha: Float = 1f,
    var life: Float = 1f,
    var maxLife: Float = 1f,
    var color: Color = Color.White,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var type: ParticleType = ParticleType.DUST,
    var isAlive: Boolean = false
) {
    fun reset() {
        x = 0f; y = 0f; vx = 0f; vy = 0f
        size = 4f; alpha = 1f; life = 1f; maxLife = 1f
        color = Color.White; rotation = 0f; rotationSpeed = 0f
        isAlive = false
    }

    fun update(dt: Float) {
        if (!isAlive) return
        x += vx * dt
        y += vy * dt
        vy += 200f * dt // gravity
        rotation += rotationSpeed * dt
        life -= dt / maxLife
        alpha = (life).coerceIn(0f, 1f)
        if (life <= 0f) isAlive = false
    }
}

// ── Particle Emitter Config ────────────────────────────────

data class EmitterConfig(
    val count: Int = 10,
    val spread: Float = 360f,
    val speed: Float = 100f,
    val speedVariance: Float = 50f,
    val size: Float = 6f,
    val sizeVariance: Float = 3f,
    val life: Float = 1f,
    val lifeVariance: Float = 0.3f,
    val gravity: Float = 200f,
    val colors: List<Color> = listOf(Color.White),
    val startAngle: Float = 0f,
    val fadeOut: Boolean = true,
    val shrink: Boolean = true
)

object EmitterConfigs {

    val DUST = EmitterConfig(
        count = 8,
        spread = 180f,
        speed = 60f,
        speedVariance = 30f,
        size = 5f,
        sizeVariance = 2f,
        life = 0.6f,
        lifeVariance = 0.2f,
        gravity = 50f,
        colors = listOf(Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFFA1887F)),
        startAngle = 270f
    )

    val JUMP = EmitterConfig(
        count = 6,
        spread = 90f,
        speed = 80f,
        speedVariance = 40f,
        size = 4f,
        sizeVariance = 2f,
        life = 0.5f,
        lifeVariance = 0.2f,
        gravity = 100f,
        colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
        startAngle = 270f
    )

    val LANDING = EmitterConfig(
        count = 12,
        spread = 160f,
        speed = 120f,
        speedVariance = 60f,
        size = 6f,
        sizeVariance = 3f,
        life = 0.5f,
        lifeVariance = 0.2f,
        gravity = 300f,
        colors = listOf(Color(0xFFD7CCC8), Color(0xFFBCAAA4)),
        startAngle = 270f
    )

    val SPEED_LINES = EmitterConfig(
        count = 5,
        spread = 20f,
        speed = 400f,
        speedVariance = 100f,
        size = 2f,
        sizeVariance = 1f,
        life = 0.3f,
        lifeVariance = 0.1f,
        gravity = 0f,
        colors = listOf(Color.White.copy(alpha = 0.8f), Color(0xFFE3F2FD).copy(alpha = 0.6f)),
        startAngle = 180f,
        fadeOut = true,
        shrink = false
    )

    val HIT = EmitterConfig(
        count = 15,
        spread = 360f,
        speed = 150f,
        speedVariance = 80f,
        size = 5f,
        sizeVariance = 3f,
        life = 0.4f,
        lifeVariance = 0.2f,
        gravity = 200f,
        colors = listOf(Color(0xFFFF5252), Color(0xFFFF8A80), Color(0xFFFFAB91)),
        startAngle = 0f
    )

    val CORRECT_ANSWER = EmitterConfig(
        count = 20,
        spread = 360f,
        speed = 200f,
        speedVariance = 100f,
        size = 8f,
        sizeVariance = 4f,
        life = 1f,
        lifeVariance = 0.3f,
        gravity = 100f,
        colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784), Color(0xFFA5D6A7)),
        startAngle = 0f
    )

    val PERFECT_ANSWER = EmitterConfig(
        count = 30,
        spread = 360f,
        speed = 250f,
        speedVariance = 120f,
        size = 10f,
        sizeVariance = 5f,
        life = 1.2f,
        lifeVariance = 0.4f,
        gravity = 80f,
        colors = listOf(Color(0xFFFFD700), Color(0xFFFFE082), Color(0xFFFFF176), Color(0xFFFFAB40)),
        startAngle = 0f
    )

    val COMBO = EmitterConfig(
        count = 25,
        spread = 270f,
        speed = 180f,
        speedVariance = 90f,
        size = 7f,
        sizeVariance = 4f,
        life = 0.8f,
        lifeVariance = 0.3f,
        gravity = 150f,
        colors = listOf(Color(0xFFFF6D00), Color(0xFFFFAB40), Color(0xFFFFD54F)),
        startAngle = 270f
    )

    val BOOST = EmitterConfig(
        count = 20,
        spread = 60f,
        speed = 300f,
        speedVariance = 150f,
        size = 6f,
        sizeVariance = 3f,
        life = 0.6f,
        lifeVariance = 0.2f,
        gravity = 50f,
        colors = listOf(Color(0xFF00E5FF), Color(0xFF18FFFF), Color(0xFF84FFFF)),
        startAngle = 180f
    )

    val FINISH = EmitterConfig(
        count = 40,
        spread = 360f,
        speed = 300f,
        speedVariance = 150f,
        size = 10f,
        sizeVariance = 5f,
        life = 1.5f,
        lifeVariance = 0.5f,
        gravity = 120f,
        colors = listOf(
            Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFF4CAF50),
            Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF4081)
        ),
        startAngle = 0f
    )

    fun forType(type: ParticleType): EmitterConfig = when (type) {
        ParticleType.DUST -> DUST
        ParticleType.JUMP -> JUMP
        ParticleType.LANDING -> LANDING
        ParticleType.SPEED_LINES -> SPEED_LINES
        ParticleType.HIT -> HIT
        ParticleType.CORRECT_ANSWER -> CORRECT_ANSWER
        ParticleType.PERFECT_ANSWER -> PERFECT_ANSWER
        ParticleType.COMBO -> COMBO
        ParticleType.BOOST -> BOOST
        ParticleType.FINISH -> FINISH
    }
}

// ── Object Pool ────────────────────────────────────────────

class ParticlePool(private val maxSize: Int = 200) {
    private val pool = Array(maxSize) { Particle() }
    private var nextIndex = 0

    fun acquire(): Particle? {
        // Try to find a dead particle
        for (i in 0 until maxSize) {
            val idx = (nextIndex + i) % maxSize
            if (!pool[idx].isAlive) {
                nextIndex = (idx + 1) % maxSize
                pool[idx].reset()
                return pool[idx]
            }
        }
        return null // Pool exhausted
    }

    fun getAliveParticles(): List<Particle> = pool.filter { it.isAlive }

    fun updateAll(dt: Float) {
        pool.forEach { it.update(dt) }
    }

    fun emit(
        type: ParticleType,
        x: Float,
        y: Float,
        config: EmitterConfig = EmitterConfigs.forType(type)
    ) {
        val count = minOf(config.count, maxSize / 2)
        for (i in 0 until count) {
            val particle = acquire() ?: break
            val angle = Math.toRadians(
                (config.startAngle + (Random.nextFloat() - 0.5f) * config.spread).toDouble()
            )
            val speed = config.speed + (Random.nextFloat() - 0.5f) * config.speedVariance

            particle.x = x + (Random.nextFloat() - 0.5f) * 20f
            particle.y = y + (Random.nextFloat() - 0.5f) * 20f
            particle.vx = (cos(angle) * speed).toFloat()
            particle.vy = (sin(angle) * speed).toFloat()
            particle.size = config.size + (Random.nextFloat() - 0.5f) * config.sizeVariance
            particle.maxLife = config.life + (Random.nextFloat() - 0.5f) * config.lifeVariance
            particle.life = particle.maxLife
            particle.color = config.colors.random()
            particle.rotation = Random.nextFloat() * 360f
            particle.rotationSpeed = (Random.nextFloat() - 0.5f) * 360f
            particle.type = type
            particle.isAlive = true
        }
    }
}

// ── Particle Renderer ──────────────────────────────────────

@Composable
fun ParticleEffectLayer(
    particlePool: ParticlePool,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_anim")
    val frame by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing) // ~60fps
        ),
        label = "particle_frame"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val particles = particlePool.getAliveParticles()
        particles.forEach { p ->
            if (!p.isAlive) return@forEach

            val drawAlpha = p.alpha.coerceIn(0f, 1f)
            val drawSize = if (p.type == ParticleType.SPEED_LINES) {
                p.size
            } else {
                p.size * (if (p.life / p.maxLife < 0.3f) p.life / p.maxLife / 0.3f else 1f)
            }

            when (p.type) {
                ParticleType.SPEED_LINES -> {
                    drawLine(
                        color = p.color.copy(alpha = drawAlpha),
                        start = Offset(p.x, p.y),
                        end = Offset(p.x + 30f, p.y),
                        strokeWidth = drawSize
                    )
                }
                ParticleType.HIT, ParticleType.CORRECT_ANSWER, ParticleType.PERFECT_ANSWER -> {
                    // Star burst
                    drawCircle(
                        color = p.color.copy(alpha = drawAlpha),
                        radius = drawSize,
                        center = Offset(p.x, p.y)
                    )
                    // Inner glow
                    drawCircle(
                        color = Color.White.copy(alpha = drawAlpha * 0.5f),
                        radius = drawSize * 0.5f,
                        center = Offset(p.x, p.y)
                    )
                }
                else -> {
                    // Default circle particle
                    drawCircle(
                        color = p.color.copy(alpha = drawAlpha),
                        radius = drawSize,
                        center = Offset(p.x, p.y)
                    )
                }
            }
        }
    }
}

// ── Screen Flash Effect ────────────────────────────────────

@Composable
fun ScreenFlash(
    trigger: Boolean,
    color: Color,
    intensity: Float = 0.3f,
    durationMs: Int = 200
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) intensity else 0f,
        animationSpec = tween(durationMs),
        label = "flash_alpha"
    )

    LaunchedEffect(trigger) {
        if (trigger) {
            visible = true
            delay(durationMs.toLong())
            visible = false
        }
    }

    if (alpha > 0.01f) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = color.copy(alpha = alpha)
            )
        }
    }
}
