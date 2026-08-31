package com.animalbattle.game.ui.recentbattles

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.BattleRecord
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DefeatRed
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.VictoryGreen
import com.animalbattle.game.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentBattlesScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val player by viewModel.player.collectAsState()

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
                text = stringResource(R.string.recent_battles_title),
                style = MaterialTheme.typography.headlineMedium,
                color = GoldDark
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (player.recentBattles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_battles),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(player.recentBattles.reversed().take(20)) { battle ->
                    BattleRecordItem(battle = battle)
                }
            }
        }
    }
}

@Composable
private fun BattleRecordItem(battle: BattleRecord) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(battle.timestamp))

    GamePanel(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (battle.won) VictoryGreen.copy(alpha = 0.1f) else DefeatRed.copy(alpha = 0.1f),
        borderColor = if (battle.won) VictoryGreen else DefeatRed
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Result Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (battle.won) VictoryGreen else DefeatRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (battle.won) "🏆" else "💔",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.vs_opponent, battle.opponentName),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (battle.won) stringResource(R.string.battle_win) else stringResource(R.string.battle_loss),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (battle.won) VictoryGreen else DefeatRed
                )
                if (battle.won) {
                    Text(
                        text = "+${battle.rewardCoins} 🪙 +${battle.rewardTrophies} 🏆",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldDark
                    )
                }
            }
        }
    }
}
