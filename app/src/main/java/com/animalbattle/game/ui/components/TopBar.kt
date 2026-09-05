package com.animalbattle.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.Player
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.GoldDeep
import com.animalbattle.game.ui.theme.GradientGoldEnd
import com.animalbattle.game.ui.theme.GradientGoldStart
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.XpBlue
import com.animalbattle.game.ui.theme.XpBlueDark

@Composable
fun TopBar(
    playerName: String,
    level: Int,
    coins: Int,
    trophies: Int,
    xpProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(GradientGoldStart, GradientGoldEnd, GoldDeep)
                )
            )
            .border(2.dp, GoldDark, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with glow
                Box(
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Gold, GoldDark)
                            )
                        )
                        .border(2.dp, PanelBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextOnGold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = playerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = PanelBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.level_label, level),
                        style = MaterialTheme.typography.bodySmall,
                        color = PanelBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .width(110.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = XpBlue,
                        trackColor = PanelBackground.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Coins
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBackground.copy(alpha = 0.9f))
                    .border(1.5.dp, GoldDark, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "🪙", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = coins.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoldDark
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trophies
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelBackground.copy(alpha = 0.9f))
                    .border(1.5.dp, GoldDark, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "🏆", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trophies.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoldDark
                )
            }
        }
    }
}
