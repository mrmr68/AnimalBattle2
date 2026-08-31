package com.animalbattle.game.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.AnimalData
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.XpBlue
import com.animalbattle.game.viewmodel.GameViewModel

@Composable
fun ProfileScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val player by viewModel.player.collectAsState()
    val selectedAnimal = AnimalData.getAnimalById(player.selectedAnimalId)

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
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                color = GoldDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Card
        GamePanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Gold)
                        .border(4.dp, GoldDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextOnGold,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = player.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                Text(
                    text = stringResource(R.string.player_level, player.calculateLevel()),
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // XP Progress
                LinearProgressIndicator(
                    progress = { player.xpToNextLevel().toFloat().let { if (it > 0) player.xpForNextLevel().toFloat() / it else 0f } },
                    modifier = Modifier
                        .width(200.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = XpBlue,
                    trackColor = Color.LightGray
                )
                Text(
                    text = stringResource(R.string.xp_progress, player.xpForNextLevel(), player.xpToNextLevel()),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Coins
            GamePanel(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🪙", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        text = player.coins.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldDark
                    )
                    Text(
                        text = stringResource(R.string.total_coins, player.coins),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            // Trophies
            GamePanel(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🏆", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        text = player.trophies.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldDark
                    )
                    Text(
                        text = stringResource(R.string.total_trophies, player.trophies),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Animal
        GamePanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.selected_animal),
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedAnimal?.let { stringResource(it.nameResId) } ?: "None",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    text = "🐾",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}
