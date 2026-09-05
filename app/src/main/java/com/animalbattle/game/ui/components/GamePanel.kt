package com.animalbattle.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.PanelBackgroundDark
import com.animalbattle.game.ui.theme.GradientGoldEnd
import com.animalbattle.game.ui.theme.GradientGoldStart

@Composable
fun GamePanel(
    modifier: Modifier = Modifier,
    backgroundColor: Brush = Brush.verticalGradient(
        listOf(PanelBackground, PanelBackgroundDark)
    ),
    borderColor: Color = Gold,
    cornerRadius: Int = 16,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(cornerRadius.dp))
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(cornerRadius.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GoldBorderPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PanelBackground, Gold.copy(alpha = 0.08f))
                )
            )
            .border(3.dp, Gold, RoundedCornerShape(20.dp))
            .padding(12.dp),
        content = content
    )
}
