package com.animalbattle.game.ui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R

private val ConnectedGreen = Color(0xFF4CAF50)
private val DisconnectedRed = Color(0xFFF44336)

/**
 * A small pulsing dot + label showing backend connection status.
 *
 * @param connected true = green pulse, false = red pulse
 * @param modifier  optional modifier (e.g. to align inside a Row)
 * @param textColor override label color (useful for BattleScreen dark overlays)
 */
@Composable
fun ConnectionStatusIndicator(
    connected: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
) {
    val dotColor = if (connected) ConnectedGreen else DisconnectedRed
    val label = if (connected) {
        "Server Online"
    } else {
        "Offline"
    }

    // Infinite pulse: scale 1.0 → 1.4 → 1.0
    val infiniteTransition = rememberInfiniteTransition(label = "conn_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    // Fade alpha: 1.0 → 0.4 → 1.0
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Outer pulse ring (faded)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size((10 * pulseScale).dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = pulseAlpha * 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            // Inner solid dot
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
