package com.animalbattle.game.ui.battle.environment

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.animalbattle.game.ui.theme.*

// ── Environment Types ──────────────────────────────────────

enum class EnvironmentType {
    FOREST,
    DESERT,
    ARCTIC,
    VOLCANO,
    ISLAND,
    CASTLE
}

data class EnvironmentColors(
    val skyTop: Color,
    val skyBottom: Color,
    val mountainFar: Color,
    val mountainMid: Color,
    val mountainNear: Color,
    val ground: Color,
    val groundAccent: Color,
    val accent: Color,
    val particleTint: Color
)

// ── Environment Color Palettes ─────────────────────────────

object EnvironmentPalettes {
    val FOREST = EnvironmentColors(
        skyTop = Color(0xFF87CEEB),
        skyBottom = Color(0xFFB8E6B8),
        mountainFar = Color(0xFF2E7D32),
        mountainMid = Color(0xFF388E3C),
        mountainNear = Color(0xFF4CAF50),
        ground = Color(0xFF33691E),
        groundAccent = Color(0xFF558B2F),
        accent = Color(0xFFFFEB3B),
        particleTint = Color(0xFF8BC34A)
    )

    val DESERT = EnvironmentColors(
        skyTop = Color(0xFFFF8F00),
        skyBottom = Color(0xFFFFE082),
        mountainFar = Color(0xFFBF360C),
        mountainMid = Color(0xFFD84315),
        mountainNear = Color(0xFFE65100),
        ground = Color(0xFFEF6C00),
        groundAccent = Color(0xFFFFA726),
        accent = Color(0xFFFFC107),
        particleTint = Color(0xFFFFE082)
    )

    val ARCTIC = EnvironmentColors(
        skyTop = Color(0xFF1565C0),
        skyBottom = Color(0xFF90CAF9),
        mountainFar = Color(0xFFB3E5FC),
        mountainMid = Color(0xFFE1F5FE),
        mountainNear = Color(0xFFE8F5E9),
        ground = Color(0xFFFAFAFA),
        groundAccent = Color(0xFFE0E0E0),
        accent = Color(0xFF00BCD4),
        particleTint = Color(0xFFFFFFFF)
    )

    val VOLCANO = EnvironmentColors(
        skyTop = Color(0xFF1A0000),
        skyBottom = Color(0xFF4A0000),
        mountainFar = Color(0xFF3E0000),
        mountainMid = Color(0xFF5D0000),
        mountainNear = Color(0xFF7F0000),
        ground = Color(0xFF212121),
        groundAccent = Color(0xFF424242),
        accent = Color(0xFFFF3D00),
        particleTint = Color(0xFFFF6D00)
    )

    val ISLAND = EnvironmentColors(
        skyTop = Color(0xFF039BE5),
        skyBottom = Color(0xFF4FC3F7),
        mountainFar = Color(0xFF00695C),
        mountainMid = Color(0xFF00897B),
        mountainNear = Color(0xFF26A69A),
        ground = Color(0xFF8D6E63),
        groundAccent = Color(0xFFA1887F),
        accent = Color(0xFF00E5FF),
        particleTint = Color(0xFF80DEEA)
    )

    val CASTLE = EnvironmentColors(
        skyTop = Color(0xFF1A237E),
        skyBottom = Color(0xFF3F51B5),
        mountainFar = Color(0xFF283593),
        mountainMid = Color(0xFF3949AB),
        mountainNear = Color(0xFF5C6BC0),
        ground = Color(0xFF455A64),
        groundAccent = Color(0xFF607D8B),
        accent = Color(0xFFFFD700),
        particleTint = Color(0xFFCE93D8)
    )

    fun forType(type: EnvironmentType): EnvironmentColors = when (type) {
        EnvironmentType.FOREST -> FOREST
        EnvironmentType.DESERT -> DESERT
        EnvironmentType.ARCTIC -> ARCTIC
        EnvironmentType.VOLCANO -> VOLCANO
        EnvironmentType.ISLAND -> ISLAND
        EnvironmentType.CASTLE -> CASTLE
    }
}

// ── Environment Composable ─────────────────────────────────

@Composable
fun BattleEnvironment(
    type: EnvironmentType,
    parallaxOffset: Float = 0f,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = remember(type) { EnvironmentPalettes.forType(type) }

    // Infinite parallax animation
    val infiniteTransition = rememberInfiniteTransition(label = "env_parallax")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(30_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "parallax_scroll"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Sky gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colors.skyTop, colors.skyBottom)
                    )
                )
        )

        // Far mountains (slow parallax)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMountainLayer(
                color = colors.mountainFar,
                yOffset = size.height * 0.55f,
                amplitude = size.height * 0.15f,
                frequency = 0.003f,
                offset = animatedOffset * 0.2f + parallaxOffset * 0.2f
            )
        }

        // Mid mountains (medium parallax)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMountainLayer(
                color = colors.mountainMid,
                yOffset = size.height * 0.6f,
                amplitude = size.height * 0.12f,
                frequency = 0.005f,
                offset = animatedOffset * 0.5f + parallaxOffset * 0.5f
            )
        }

        // Near mountains (fast parallax)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMountainLayer(
                color = colors.mountainNear,
                yOffset = size.height * 0.68f,
                amplitude = size.height * 0.08f,
                frequency = 0.008f,
                offset = animatedOffset * 0.8f + parallaxOffset * 0.8f
            )
        }

        // Ground
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Main ground
            drawRect(
                color = colors.ground,
                topLeft = Offset(0f, size.height * 0.75f),
                size = size.copy(height = size.height * 0.25f)
            )
            // Ground accent line
            drawLine(
                color = colors.groundAccent,
                start = Offset(0f, size.height * 0.75f),
                end = Offset(size.width, size.height * 0.75f),
                strokeWidth = 4f
            )
            // Ground texture dots
            for (i in 0..20) {
                val x = (i * size.width / 20f + animatedOffset * 0.3f) % size.width
                drawCircle(
                    color = colors.groundAccent.copy(alpha = 0.4f),
                    radius = 3f,
                    center = Offset(x, size.height * 0.82f)
                )
            }
        }

        // Environment-specific decorations
        EnvironmentDecorations(type, colors, animatedOffset)

        // Content overlay
        content()
    }
}

// ── Mountain Layer Drawing ─────────────────────────────────

private fun DrawScope.drawMountainLayer(
    color: Color,
    yOffset: Float,
    amplitude: Float,
    frequency: Float,
    offset: Float
) {
    val path = Path()
    path.moveTo(0f, size.height)

    var x = 0f
    path.moveTo(x, yOffset)
    while (x <= size.width) {
        val y = yOffset - amplitude * kotlin.math.sin((x + offset) * frequency).toFloat()
            .coerceIn(-amplitude, amplitude)
        path.lineTo(x, y)
        x += 4f
    }
    path.lineTo(size.width, size.height)
    path.lineTo(0f, size.height)
    path.close()

    drawPath(path, color)
}

// ── Environment Decorations ────────────────────────────────

@Composable
private fun EnvironmentDecorations(
    type: EnvironmentType,
    colors: EnvironmentColors,
    animatedOffset: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        when (type) {
            EnvironmentType.FOREST -> drawForestDecorations(colors, animatedOffset)
            EnvironmentType.DESERT -> drawDesertDecorations(colors, animatedOffset)
            EnvironmentType.ARCTIC -> drawArcticDecorations(colors, animatedOffset)
            EnvironmentType.VOLCANO -> drawVolcanoDecorations(colors, animatedOffset)
            EnvironmentType.ISLAND -> drawIslandDecorations(colors, animatedOffset)
            EnvironmentType.CASTLE -> drawCastleDecorations(colors, animatedOffset)
        }
    }
}

private fun DrawScope.drawForestDecorations(colors: EnvironmentColors, offset: Float) {
    // Trees
    for (i in 0..5) {
        val x = (i * size.width / 5f + offset * 0.1f) % size.width
        val treeHeight = size.height * 0.15f
        val trunkWidth = size.width * 0.015f

        // Trunk
        drawRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(x - trunkWidth / 2, size.height * 0.7f - treeHeight * 0.3f),
            size = androidx.compose.ui.geometry.Size(trunkWidth, treeHeight * 0.3f)
        )
        // Canopy
        drawCircle(
            color = colors.accent.copy(alpha = 0.6f),
            radius = treeHeight * 0.35f,
            center = Offset(x, size.height * 0.7f - treeHeight * 0.5f)
        )
    }
}

private fun DrawScope.drawDesertDecorations(colors: EnvironmentColors, offset: Float) {
    // Cacti
    for (i in 0..3) {
        val x = (i * size.width / 3f + 50f + offset * 0.05f) % size.width
        drawRect(
            color = Color(0xFF558B2F),
            topLeft = Offset(x - 4f, size.height * 0.68f - 40f),
            size = androidx.compose.ui.geometry.Size(8f, 40f)
        )
        // Arms
        drawRect(
            color = Color(0xFF558B2F),
            topLeft = Offset(x + 4f, size.height * 0.68f - 30f),
            size = androidx.compose.ui.geometry.Size(15f, 6f)
        )
        drawRect(
            color = Color(0xFF558B2F),
            topLeft = Offset(x - 19f, size.height * 0.68f - 20f),
            size = androidx.compose.ui.geometry.Size(15f, 6f)
        )
    }
    // Sand dunes
    val path = Path()
    path.moveTo(0f, size.height * 0.8f)
    for (x in 0..size.width.toInt() step 4) {
        val y = size.height * 0.8f + 20f * kotlin.math.sin((x + offset * 0.2f) * 0.01f).toFloat()
        path.lineTo(x.toFloat(), y)
    }
    path.lineTo(size.width, size.height)
    path.lineTo(0f, size.height)
    path.close()
    drawPath(path, colors.groundAccent.copy(alpha = 0.3f))
}

private fun DrawScope.drawArcticDecorations(colors: EnvironmentColors, offset: Float) {
    // Snowflakes
    for (i in 0..15) {
        val x = (i * size.width / 15f + offset * 0.3f + i * 37f) % size.width
        val y = (i * size.height / 8f + offset * 0.5f + i * 23f) % size.height
        drawCircle(
            color = colors.particleTint.copy(alpha = 0.6f),
            radius = 2f + (i % 3).toFloat(),
            center = Offset(x, y)
        )
    }
    // Ice crystals
    for (i in 0..2) {
        val x = (i * size.width / 2f + 100f) % size.width
        drawLine(
            color = colors.accent.copy(alpha = 0.3f),
            start = Offset(x, size.height * 0.72f),
            end = Offset(x + 20f, size.height * 0.68f),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawVolcanoDecorations(colors: EnvironmentColors, offset: Float) {
    // Lava particles
    for (i in 0..8) {
        val x = (size.width * 0.4f + i * 30f + offset * 0.1f) % size.width
        val y = size.height * 0.55f - (offset * 0.5f + i * 15f) % (size.height * 0.3f)
        drawCircle(
            color = colors.accent.copy(alpha = 0.7f),
            radius = 3f + (i % 3).toFloat() * 2f,
            center = Offset(x, y)
        )
    }
    // Smoke
    for (i in 0..4) {
        val x = size.width * 0.5f + i * 25f - 50f
        val y = size.height * 0.4f - i * 30f
        drawCircle(
            color = Color(0xFF616161).copy(alpha = 0.3f - i * 0.05f),
            radius = 15f + i * 8f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawIslandDecorations(colors: EnvironmentColors, offset: Float) {
    // Palm trees
    for (i in 0..2) {
        val x = (i * size.width / 2f + 80f) % size.width
        // Trunk (curved)
        drawLine(
            color = Color(0xFF5D4037),
            start = Offset(x, size.height * 0.75f),
            end = Offset(x + 10f, size.height * 0.55f),
            strokeWidth = 6f
        )
        // Leaves
        drawCircle(
            color = Color(0xFF2E7D32).copy(alpha = 0.7f),
            radius = 25f,
            center = Offset(x + 10f, size.height * 0.53f)
        )
    }
    // Waves
    val wavePath = Path()
    wavePath.moveTo(0f, size.height * 0.76f)
    for (x in 0..size.width.toInt() step 4) {
        val y = size.height * 0.76f + 5f * kotlin.math.sin((x + offset * 0.8f) * 0.03f).toFloat()
        wavePath.lineTo(x.toFloat(), y)
    }
    drawPath(wavePath, colors.accent.copy(alpha = 0.3f), style = Stroke(width = 2f))
}

private fun DrawScope.drawCastleDecorations(colors: EnvironmentColors, offset: Float) {
    // Castle towers
    for (i in 0..1) {
        val x = size.width * 0.15f + i * size.width * 0.7f
        // Tower body
        drawRect(
            color = colors.groundAccent.copy(alpha = 0.8f),
            topLeft = Offset(x - 15f, size.height * 0.5f),
            size = androidx.compose.ui.geometry.Size(30f, size.height * 0.25f)
        )
        // Tower top (triangle)
        val topPath = Path()
        topPath.moveTo(x - 20f, size.height * 0.5f)
        topPath.lineTo(x, size.height * 0.42f)
        topPath.lineTo(x + 20f, size.height * 0.5f)
        topPath.close()
        drawPath(topPath, colors.accent.copy(alpha = 0.8f))
    }
    // Floating particles (magic)
    for (i in 0..6) {
        val x = (i * size.width / 6f + offset * 0.2f) % size.width
        val y = size.height * 0.3f + (i * 47f) % (size.height * 0.3f)
        drawCircle(
            color = colors.accent.copy(alpha = 0.5f),
            radius = 2f + (i % 3).toFloat(),
            center = Offset(x, y)
        )
    }
}
