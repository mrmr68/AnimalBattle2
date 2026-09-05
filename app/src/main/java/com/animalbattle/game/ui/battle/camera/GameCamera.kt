package com.animalbattle.game.ui.battle.camera

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs

// ── Camera Target ──────────────────────────────────────────

data class CameraTarget(
    val x: Float,
    val y: Float,
    val zoom: Float = 1f,
    val rotation: Float = 0f
)

// ── Camera State ───────────────────────────────────────────

class GameCameraState {
    private var _x by mutableFloatStateOf(0f)
    private var _y by mutableFloatStateOf(0f)
    private var _zoom by mutableFloatStateOf(1f)
    private var _rotation by mutableFloatStateOf(0f)
    private var _shakeX by mutableFloatStateOf(0f)
    private var _shakeY by mutableFloatStateOf(0f)
    private var _isShaking by mutableStateOf(false)

    val x: Float get() = _x + _shakeX
    val y: Float get() = _y + _shakeY
    val zoom: Float get() = _zoom
    val rotation: Float get() = _rotation
    val isShaking: Boolean get() = _isShaking

    fun updateTo(target: CameraTarget) {
        _x = target.x
        _y = target.y
        _zoom = target.zoom
        _rotation = target.rotation
    }

    fun shake(intensity: Float = 10f) {
        _isShaking = true
        _shakeX = (Math.random() - 0.5).toFloat() * intensity
        _shakeY = (Math.random() - 0.5).toFloat() * intensity
    }

    fun stopShake() {
        _isShaking = false
        _shakeX = 0f
        _shakeY = 0f
    }

    fun getParallaxOffset(layer: Int): Float {
        // Higher layer = slower movement (farther away)
        return _x * (1f / (layer + 1))
    }
}

// ── Camera Presets ─────────────────────────────────────────

object CameraPresets {
    // Default battle view: both animals visible
    val BATTLE_DEFAULT = CameraTarget(x = 0f, y = 0f, zoom = 1f)

    // Zoom on player
    val FOCUS_PLAYER = CameraTarget(x = -80f, y = 0f, zoom = 1.2f)

    // Zoom on opponent
    val FOCUS_OPPONENT = CameraTarget(x = 80f, y = 0f, zoom = 1.2f)

    // Wide view for abilities
    val WIDE_VIEW = CameraTarget(x = 0f, y = -20f, zoom = 0.9f)

    // Close up for attacks
    val ATTACK_CLOSE = CameraTarget(x = 0f, y = 0f, zoom = 1.4f, rotation = 5f)

    // Victory celebration
    val VICTORY = CameraTarget(x = 0f, y = -30f, zoom = 1.1f, rotation = 0f)

    // Defeat
    val DEFEAT = CameraTarget(x = 0f, y = 20f, zoom = 0.8f, rotation = -3f)
}

// ── Camera Controller ──────────────────────────────────────

class CameraController(private val state: GameCameraState) {
    private var _currentTarget: CameraTarget = CameraPresets.BATTLE_DEFAULT
    private var _targetTarget: CameraTarget = CameraPresets.BATTLE_DEFAULT

    val currentState: GameCameraState get() = state

    fun setTarget(target: CameraTarget) {
        _targetTarget = target
    }

    fun update(progress: Float) {
        // Smooth interpolation between current and target
        val lerpFactor = 0.1f
        val newX = lerp(state.x, _targetTarget.x, lerpFactor)
        val newY = lerp(state.y, _targetTarget.y, lerpFactor)
        val newZoom = lerp(state.zoom, _targetTarget.zoom, lerpFactor)
        val newRotation = lerp(state.rotation, _targetTarget.rotation, lerpFactor)

        state.updateTo(CameraTarget(newX, newY, newZoom, newRotation))
    }

    fun snapTo(target: CameraTarget) {
        state.updateTo(target)
    }

    fun shake(intensity: Float = 10f) {
        state.shake(intensity)
    }

    fun stopShake() {
        state.stopShake()
    }

    private fun lerp(a: Float, b: Float, factor: Float): Float {
        return a + (b - a) * factor
    }
}

// ── Camera Modifier ────────────────────────────────────────

fun Modifier.cameraTransform(
    cameraState: GameCameraState
): Modifier = this.graphicsLayer {
    scaleX = cameraState.zoom
    scaleY = cameraState.zoom
    translationX = -cameraState.x * cameraState.zoom
    translationY = -cameraState.y * cameraState.zoom
    rotationZ = cameraState.rotation
}

// ── Smooth Camera Composable ───────────────────────────────

@Composable
fun rememberGameCameraState(): GameCameraState {
    return remember { GameCameraState() }
}

@Composable
fun rememberCameraController(state: GameCameraState): CameraController {
    return remember(state) { CameraController(state) }
}

// ── Auto Camera Logic ──────────────────────────────────────

object AutoCamera {
    /**
     * Determine the best camera target based on battle state.
     * Shows both animals when possible, zooms in during attacks.
     */
    fun forBattlePhase(
        isPlayerTurn: Boolean,
        isAttacking: Boolean,
        isTakingDamage: Boolean,
        screenWidth: Float
    ): CameraTarget {
        return when {
            isAttacking -> CameraPresets.ATTACK_CLOSE
            isPlayerTurn && !isAttacking -> CameraPresets.BATTLE_DEFAULT
            !isPlayerTurn -> CameraPresets.FOCUS_OPPONENT
            else -> CameraPresets.BATTLE_DEFAULT
        }
    }

    /**
     * Calculate smooth camera movement that keeps both
     * player and opponent visible.
     */
    fun calculateBalancedView(
        playerX: Float,
        opponentX: Float,
        screenWidth: Float
    ): CameraTarget {
        val centerX = (playerX + opponentX) / 2f
        val distance = abs(opponentX - playerX)
        val zoom = (screenWidth / (distance + 200f)).coerceIn(0.7f, 1.3f)

        return CameraTarget(x = centerX, y = 0f, zoom = zoom)
    }
}
