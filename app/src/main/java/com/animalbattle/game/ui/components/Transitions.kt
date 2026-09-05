package com.animalbattle.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// ── Transition Types ───────────────────────────────────────

enum class TransitionType {
    FADE,
    SLIDE_LEFT,
    SLIDE_RIGHT,
    SLIDE_UP,
    SLIDE_DOWN,
    ZOOM_IN,
    ZOOM_OUT,
    BOUNCE
}

// ── Screen Transition Composable ───────────────────────────

@Composable
fun ScreenTransition(
    targetState: Any,
    transitionType: TransitionType = TransitionType.FADE,
    modifier: Modifier = Modifier,
    content: @Composable (Any) -> Unit
) {
    val enterTransition = when (transitionType) {
        TransitionType.FADE -> fadeIn(tween(300))
        TransitionType.SLIDE_LEFT -> slideInHorizontally(tween(400)) { -it }
        TransitionType.SLIDE_RIGHT -> slideInHorizontally(tween(400)) { it }
        TransitionType.SLIDE_UP -> slideInVertically(tween(400)) { it }
        TransitionType.SLIDE_DOWN -> slideInVertically(tween(400)) { -it }
        TransitionType.ZOOM_IN -> fadeIn(tween(300)) + androidx.compose.animation.core.scaleIn(
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            initialScale = 0.8f
        )
        TransitionType.ZOOM_OUT -> fadeIn(tween(300)) + androidx.compose.animation.core.scaleIn(
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            initialScale = 1.2f
        )
        TransitionType.BOUNCE -> fadeIn(tween(300)) + androidx.compose.animation.core.scaleIn(
            spring(dampingRatio = Spring.DampingRatioLowBouncy),
            initialScale = 0.5f
        )
    }

    val exitTransition = when (transitionType) {
        TransitionType.FADE -> fadeOut(tween(200))
        TransitionType.SLIDE_LEFT -> slideOutHorizontally(tween(300)) { it }
        TransitionType.SLIDE_RIGHT -> slideOutHorizontally(tween(300)) { -it }
        TransitionType.SLIDE_UP -> slideOutVertically(tween(300)) { -it }
        TransitionType.SLIDE_DOWN -> slideOutVertically(tween(300)) { it }
        TransitionType.ZOOM_IN -> fadeOut(tween(200)) + androidx.compose.animation.core.scaleOut(
            tween(200),
            targetScale = 1.2f
        )
        TransitionType.ZOOM_OUT -> fadeOut(tween(200)) + androidx.compose.animation.core.scaleOut(
            tween(200),
            targetScale = 0.8f
        )
        TransitionType.BOUNCE -> fadeOut(tween(200)) + androidx.compose.animation.core.scaleOut(
            tween(200),
            targetScale = 0.5f
        )
    }

    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            enterTransition togetherWith exitTransition
        },
        label = "screen_transition"
    ) { state ->
        content(state)
    }
}

// ── Battle Transition Effects ──────────────────────────────

@Composable
fun BattleStartTransition(
    isActive: Boolean,
    onTransitionComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(300)) + slideInVertically(tween(500)) { -it },
        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        )
    }
}

@Composable
fun VictoryTransition(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(500)) + androidx.compose.animation.core.scaleIn(
            spring(dampingRatio = Spring.DampingRatioLowBouncy),
            initialScale = 0.3f
        ),
        exit = fadeOut(tween(300)),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun DamageFlash(
    trigger: Boolean,
    color: Color = Color.Red,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = trigger,
        enter = fadeIn(tween(100)),
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.3f))
        )
    }
}

// ── Page Turn Transition ───────────────────────────────────

@Composable
fun PageTransition(
    currentPage: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = currentPage,
        modifier = modifier,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(tween(400)) { it } togetherWith
                    slideOutHorizontally(tween(400)) { -it }
            } else {
                slideInHorizontally(tween(400)) { -it } togetherWith
                    slideOutHorizontally(tween(400)) { it }
            }
        },
        label = "page_transition"
    ) { page ->
        content(page)
    }
}

// ── Shimmer Effect ─────────────────────────────────────────

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1500),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    Box(
        modifier = modifier.background(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    Color.LightGray.copy(alpha = 0.3f),
                    Color.LightGray.copy(alpha = 0.6f),
                    Color.LightGray.copy(alpha = 0.3f)
                ),
                startX = shimmerOffset - 100f,
                endX = shimmerOffset + 100f
            )
        )
    )
}
