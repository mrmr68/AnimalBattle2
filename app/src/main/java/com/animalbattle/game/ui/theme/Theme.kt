package com.animalbattle.game.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Gold,
    onPrimary = TextOnGold,
    primaryContainer = GoldLight,
    onPrimaryContainer = TextPrimary,
    secondary = Cream,
    onSecondary = TextPrimary,
    secondaryContainer = CreamLight,
    onSecondaryContainer = TextPrimary,
    tertiary = DarkGreenPrimary,
    onTertiary = White,
    background = Cream,
    onBackground = TextPrimary,
    surface = PanelBackground,
    onSurface = TextPrimary,
    surfaceVariant = PanelBorder,
    onSurfaceVariant = TextSecondary,
    error = DefeatRed,
    onError = White
)

@Composable
fun AnimalBattleTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Gold.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GameTypography,
        content = content
    )
}
