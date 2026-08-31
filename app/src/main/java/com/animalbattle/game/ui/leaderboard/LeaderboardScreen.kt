package com.animalbattle.game.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.LeaderboardEntry
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.viewmodel.GameViewModel

@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val player by viewModel.player.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()

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
                text = stringResource(R.string.leaderboard_title),
                style = MaterialTheme.typography.headlineMedium,
                color = GoldDark
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(leaderboard) { entry ->
                LeaderboardItem(entry = entry)
            }
        }
    }
}

@Composable
private fun LeaderboardItem(entry: LeaderboardEntry) {
    GamePanel(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (entry.isPlayer) Gold.copy(alpha = 0.2f) else PanelBackground,
        borderColor = if (entry.isPlayer) Gold else Gold.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (entry.rank) {
                            1 -> Gold
                            2 -> Gold.copy(alpha = 0.7f)
                            3 -> Gold.copy(alpha = 0.5f)
                            else -> PanelBackground
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (entry.rank <= 3) TextOnGold else TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextOnGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.playerName,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (entry.isPlayer) GoldDark else TextPrimary
                )
            }

            // Trophies
            Text(
                text = "🏆 ${entry.trophies}",
                style = MaterialTheme.typography.titleSmall,
                color = GoldDark
            )
        }
    }
}
