package com.animalbattle.game.ui.battle.animation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset

// ── Animation States ───────────────────────────────────────

enum class AnimationState {
    IDLE,           // Standing still, breathing
    RUN,            // Running forward
    SHORT_JUMP,     // Small hop
    LONG_JUMP,      // Big leap
    LANDING,        // Landing impact
    HAPPY,          // Victory celebration
    SAD,            // Defeat reaction
    HIT,            // Taking damage
    BOOST,          // Speed boost
    VICTORY         // Victory pose
}

// ── Animation Config ───────────────────────────────────────

data class AnimationConfig(
    val durationMs: Int = 300,
    val translateY: Float = 0f,
    val translateX: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val alpha: Float = 1f,
    val bounceCount: Int = 0,
    val shakeIntensity: Float = 0f,
    val glowColor: Long = 0L,
    val particleCount: Int = 0,
    val spec: AnimationSpec<Float> = tween(durationMs, easing = LinearEasing)
)

// ── Default Animation Configs ──────────────────────────────

object AnimationConfigs {

    val IDLE = AnimationConfig(
        durationMs = 1200,
        translateY = -4f,
        spec = tween(1200, easing = LinearEasing)
    )

    val RUN = AnimationConfig(
        durationMs = 400,
        translateY = -8f,
        bounceCount = 2,
        spec = tween(400, easing = LinearEasing)
    )

    val SHORT_JUMP = AnimationConfig(
        durationMs = 400,
        translateY = -60f,
        scale = 1.1f,
        spec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
    )

    val LONG_JUMP = AnimationConfig(
        durationMs = 600,
        translateY = -100f,
        translateX = 40f,
        scale = 1.15f,
        rotation = 15f,
        spec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
    )

    val LANDING = AnimationConfig(
        durationMs = 300,
        translateY = 10f,
        scale = 0.9f,
        particleCount = 6,
        spec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessHigh)
    )

    val HAPPY = AnimationConfig(
        durationMs = 600,
        translateY = -20f,
        scale = 1.1f,
        bounceCount = 3,
        particleCount = 8,
        spec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow)
    )

    val SAD = AnimationConfig(
        durationMs = 500,
        translateY = 15f,
        scale = 0.9f,
        alpha = 0.7f,
        spec = tween(500, easing = LinearEasing)
    )

    val HIT = AnimationConfig(
        durationMs = 200,
        translateX = -15f,
        shakeIntensity = 8f,
        scale = 0.95f,
        particleCount = 4,
        spec = tween(200, easing = LinearEasing)
    )

    val BOOST = AnimationConfig(
        durationMs = 500,
        translateY = -30f,
        scale = 1.2f,
        rotation = -10f,
        particleCount = 10,
        spec = spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessHigh)
    )

    val VICTORY = AnimationConfig(
        durationMs = 800,
        translateY = -40f,
        scale = 1.3f,
        rotation = 360f,
        particleCount = 15,
        spec = spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMediumLow)
    )

    fun forState(state: AnimationState): AnimationConfig = when (state) {
        AnimationState.IDLE -> IDLE
        AnimationState.RUN -> RUN
        AnimationState.SHORT_JUMP -> SHORT_JUMP
        AnimationState.LONG_JUMP -> LONG_JUMP
        AnimationState.LANDING -> LANDING
        AnimationState.HAPPY -> HAPPY
        AnimationState.SAD -> SAD
        AnimationState.HIT -> HIT
        AnimationState.BOOST -> BOOST
        AnimationState.VICTORY -> VICTORY
    }
}

// ── Animation State Manager ────────────────────────────────

class AnimationStateManager {
    private var _currentState: AnimationState = AnimationState.IDLE
    val currentState: AnimationState get() = _currentState

    private var _stateStartTime: Long = System.currentTimeMillis()
    val stateStartTime: Long get() = _stateStartTime

    fun transitionTo(newState: AnimationState) {
        if (_currentState != newState) {
            _currentState = newState
            _stateStartTime = System.currentTimeMillis()
        }
    }

    fun getConfig(): AnimationConfig = AnimationConfigs.forState(_currentState)

    fun getProgress(): Float {
        val config = getConfig()
        val elapsed = System.currentTimeMillis() - _stateStartTime
        return (elapsed.toFloat() / config.durationMs).coerceIn(0f, 1f)
    }

    fun isComplete(): Boolean {
        return getProgress() >= 1f
    }
}

// ── Animal Visual Config ───────────────────────────────────

data class AnimalVisualConfig(
    val bodyColor: Long,
    val accentColor: Long,
    val eyeColor: Long = 0xFF000000,
    val bodyScale: Float = 1f,
    val hasWings: Boolean = false,
    val hasHorn: Boolean = false,
    val hasTusks: Boolean = false,
    val tailLength: Float = 0.3f,
    val earSize: Float = 0.15f
)

object AnimalVisualConfigs {
    private val configs = mapOf(
        "lion" to AnimalVisualConfig(
            bodyColor = 0xFFD4A017,
            accentColor = 0xFF8B6914,
            hasHorn = false,
            tailLength = 0.4f,
            earSize = 0.12f
        ),
        "tiger" to AnimalVisualConfig(
            bodyColor = 0xFFFF8C00,
            accentColor = 0xFF000000,
            tailLength = 0.35f
        ),
        "leopard" to AnimalVisualConfig(
            bodyColor = 0xFFDAA520,
            accentColor = 0xFF2D2D2D,
            tailLength = 0.4f
        ),
        "cheetah" to AnimalVisualConfig(
            bodyColor = 0xFFE8B830,
            accentColor = 0xFF2D2D2D,
            bodyScale = 0.9f,
            tailLength = 0.45f
        ),
        "bear" to AnimalVisualConfig(
            bodyColor = 0xFF5D4037,
            accentColor = 0xFF3E2723,
            bodyScale = 1.2f,
            earSize = 0.1f
        ),
        "wolf" to AnimalVisualConfig(
            bodyColor = 0xFF757575,
            accentColor = 0xFF424242,
            tailLength = 0.3f
        ),
        "gorilla" to AnimalVisualConfig(
            bodyColor = 0xFF212121,
            accentColor = 0xFF424242,
            bodyScale = 1.3f,
            earSize = 0.08f
        ),
        "rhino" to AnimalVisualConfig(
            bodyColor = 0xFF9E9E9E,
            accentColor = 0xFF616161,
            bodyScale = 1.25f,
            hasHorn = true,
            tailLength = 0.15f
        ),
        "elephant" to AnimalVisualConfig(
            bodyColor = 0xFFBDBDBD,
            accentColor = 0xFF757575,
            bodyScale = 1.4f,
            hasTusks = true,
            earSize = 0.25f,
            tailLength = 0.1f
        ),
        "crocodile" to AnimalVisualConfig(
            bodyColor = 0xFF558B2F,
            accentColor = 0xFF33691E,
            bodyScale = 1.1f,
            tailLength = 0.5f
        ),
        "eagle" to AnimalVisualConfig(
            bodyColor = 0xFF5D4037,
            accentColor = 0xFFFFD700,
            hasWings = true,
            bodyScale = 0.8f,
            tailLength = 0.2f
        ),
        "cobra" to AnimalVisualConfig(
            bodyColor = 0xFF4A148C,
            accentColor = 0xFFFFD700,
            bodyScale = 0.7f,
            tailLength = 0.6f
        )
    )

    fun forAnimal(animalId: String): AnimalVisualConfig =
        configs[animalId] ?: AnimalVisualConfig(
            bodyColor = 0xFF9E9E9E,
            accentColor = 0xFF616161
        )
}
