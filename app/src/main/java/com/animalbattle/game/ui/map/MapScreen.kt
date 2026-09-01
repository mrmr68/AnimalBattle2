package com.animalbattle.game.ui.map

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.LevelStatus
import com.animalbattle.game.domain.model.MapLevel
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GameButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.DefeatRed
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.VictoryGreen
import com.animalbattle.game.viewmodel.GameViewModel

@Composable
fun MapScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onStartBattle: () -> Unit
) {
    val player by viewModel.player.collectAsState()
    val mapLevels by viewModel.mapLevels.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp)
    ) {
        TopBar(
            playerName = player.name,
            level = player.calculateLevel(),
            coins = player.coins,
            trophies = player.trophies,
            xpProgress = player.xpToNextLevel().toFloat().let { if (it > 0) player.xpForNextLevel().toFloat() / it else 0f }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onNavigateBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.map_title),
                style = MaterialTheme.typography.headlineMedium,
                color = GoldDark
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mapLevels) { level ->
                MapLevelNode(
                    level = level,
                    onLevelClick = {
                        if (level.status == LevelStatus.AVAILABLE) {
                            viewModel.completeMapLevel(level.level)
                            onStartBattle()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MapLevelNode(
    level: MapLevel,
    onLevelClick: () -> Unit
) {
    GamePanel(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = when (level.status) {
            LevelStatus.COMPLETED -> VictoryGreen.copy(alpha = 0.15f)
            LevelStatus.AVAILABLE -> Gold.copy(alpha = 0.2f)
            LevelStatus.LOCKED -> PanelBackground.copy(alpha = 0.5f)
        },
        borderColor = when (level.status) {
            LevelStatus.COMPLETED -> VictoryGreen
            LevelStatus.AVAILABLE -> Gold
            LevelStatus.LOCKED -> Color.LightGray
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Number Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (level.status) {
                            LevelStatus.COMPLETED -> VictoryGreen
                            LevelStatus.AVAILABLE -> Gold
                            LevelStatus.LOCKED -> Color.LightGray
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (level.status) {
                    LevelStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TextOnGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    LevelStatus.AVAILABLE -> {
                        Text(
                            text = level.level.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextOnGold
                        )
                    }
                    LevelStatus.LOCKED -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.stage_label, level.level),
                    style = MaterialTheme.typography.titleMedium,
                    color = when (level.status) {
                        LevelStatus.COMPLETED -> VictoryGreen
                        LevelStatus.AVAILABLE -> GoldDark
                        LevelStatus.LOCKED -> Color.Gray
                    }
                )
                Text(
                    text = when (level.status) {
                        LevelStatus.COMPLETED -> stringResource(R.string.level_completed)
                        LevelStatus.AVAILABLE -> stringResource(R.string.level_available)
                        LevelStatus.LOCKED -> "${stringResource(R.string.level_locked)} (${level.requiredTrophies} 🏆)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }

            if (level.status == LevelStatus.AVAILABLE) {
                GameButton(
                    text = "⚔️",
                    onClick = onLevelClick,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
