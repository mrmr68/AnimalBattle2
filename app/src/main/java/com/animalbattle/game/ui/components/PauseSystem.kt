package com.animalbattle.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animalbattle.game.ui.theme.*

// ── Pause State ────────────────────────────────────────────

class PauseState {
    var isPaused by mutableStateOf(false)
        private set

    var showSettings by mutableStateOf(false)
        private set

    var showQuitConfirm by mutableStateOf(false)
        private set

    fun pause() { isPaused = true }
    fun resume() { isPaused = false; showSettings = false; showQuitConfirm = false }
    fun openSettings() { showSettings = true }
    fun closeSettings() { showSettings = false }
    fun openQuitConfirm() { showQuitConfirm = true }
    fun closeQuitConfirm() { showQuitConfirm = false }
}

@Composable
fun rememberPauseState(): PauseState = remember { PauseState() }

// ── Pause Button ───────────────────────────────────────────

@Composable
fun PauseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(PanelBackground, Gold.copy(alpha = 0.2f))
                )
            )
            .border(2.dp, Gold, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⏸",
            fontSize = 20.sp
        )
    }
}

// ── Pause Overlay ──────────────────────────────────────────

@Composable
fun PauseOverlay(
    pauseState: PauseState,
    onResume: () -> Unit,
    onSettings: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = pauseState.isPaused,
        enter = fadeIn(tween(300)) + scaleIn(tween(300, animationSpec = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Prevent click-through */ }
                ),
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
                            listOf(PanelBackground, Gold.copy(alpha = 0.1f))
                        )
                    )
                    .border(3.dp, Gold, RoundedCornerShape(24.dp))
                    .padding(32.dp)
            ) {
                // Title
                Text(
                    text = "⏸ PAUSED",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp
                    ),
                    color = GoldDark
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Resume Button
                PauseMenuButton(
                    text = "▶ RESUME",
                    color = VictoryGreen,
                    onClick = {
                        pauseState.resume()
                        onResume()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Settings Button
                PauseMenuButton(
                    text = "⚙ SETTINGS",
                    color = InfoBlue,
                    onClick = {
                        pauseState.openSettings()
                        onSettings()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quit Button
                PauseMenuButton(
                    text = "🚪 QUIT",
                    color = DefeatRed,
                    onClick = {
                        pauseState.openQuitConfirm()
                    }
                )
            }
        }
    }

    // Quit Confirmation Dialog
    if (pauseState.showQuitConfirm) {
        QuitConfirmDialog(
            onConfirm = {
                pauseState.resume()
                onQuit()
            },
            onDismiss = { pauseState.closeQuitConfirm() }
        )
    }
}

@Composable
private fun PauseMenuButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(color, color.copy(alpha = 0.7f))
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 40.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = Color.White
        )
    }
}

// ── Quit Confirmation Dialog ───────────────────────────────

@Composable
fun QuitConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(PanelBackground)
                .border(2.dp, Gold, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "🚪",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Quit Battle?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your progress in this battle will be lost.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBackground)
                        .border(2.dp, Gold, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CANCEL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                }

                // Quit
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(DefeatRed)
                        .border(2.dp, DefeatRedDark, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onConfirm
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "QUIT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
