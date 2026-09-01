package com.animalbattle.game.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.CreamLight
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.OverlayDark
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.components.DailyLoginDialog
import com.animalbattle.game.ui.components.LevelUpDialog
import com.animalbattle.game.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    onNavigateToOnlineBattle: () -> Unit,
    onNavigateToAnimals: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToLuckyWheel: () -> Unit,
    onNavigateToRecentBattles: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    val player by viewModel.player.collectAsState()
    val showDailyLogin by viewModel.showDailyLogin.collectAsState()
    val dailyRewards by viewModel.dailyLoginRewards.collectAsState()
    val showLevelUp by viewModel.showLevelUp.collectAsState()
    val newLevel by viewModel.newLevel.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            TopBar(
                playerName = player.name,
                level = player.calculateLevel(),
                coins = player.coins,
                trophies = player.trophies,
                xpProgress = player.xpToNextLevel().toFloat().let { if (it > 0) player.xpForNextLevel().toFloat() / it else 0f }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Title
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                color = GoldDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Buttons Grid
            val gameButtons = listOf(
                GameButtonData(
                    title = stringResource(R.string.nav_online_battle),
                    emoji = "⚔️",
                    onClick = onNavigateToOnlineBattle
                ),
                GameButtonData(
                    title = stringResource(R.string.nav_animals),
                    emoji = "🦁",
                    onClick = onNavigateToAnimals
                ),
                GameButtonData(
                    title = stringResource(R.string.nav_shop),
                    emoji = "🛒",
                    onClick = onNavigateToShop
                ),
                GameButtonData(
                    title = stringResource(R.string.nav_lucky_wheel),
                    emoji = "🎡",
                    onClick = onNavigateToLuckyWheel
                ),
                GameButtonData(
                    title = stringResource(R.string.nav_recent_battles),
                    emoji = "📋",
                    onClick = onNavigateToRecentBattles
                ),
                GameButtonData(
                    title = stringResource(R.string.nav_map),
                    emoji = "🗺️",
                    onClick = onNavigateToMap
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(gameButtons) { buttonData ->
                    HomeGameButton(
                        title = buttonData.title,
                        emoji = buttonData.emoji,
                        onClick = buttonData.onClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SmallGameButton(
                    title = stringResource(R.string.nav_settings),
                    emoji = "⚙️",
                    onClick = onNavigateToSettings
                )
                SmallGameButton(
                    title = stringResource(R.string.nav_profile),
                    emoji = "👤",
                    onClick = onNavigateToProfile
                )
                SmallGameButton(
                    title = stringResource(R.string.nav_leaderboard),
                    emoji = "🏆",
                    onClick = onNavigateToLeaderboard
                )
            }
    }

    // Daily Login Dialog
    if (showDailyLogin) {
        Box(
            modifier = Modifier.fillMaxSize().background(OverlayDark),
            contentAlignment = Alignment.Center
        ) {
            DailyLoginDialog(
                rewards = dailyRewards,
                onClaim = { viewModel.claimDailyReward(it) },
                onDismiss = { viewModel.dismissDailyLogin() }
            )
        }
    }

    // Level Up Dialog
    if (showLevelUp) {
        Box(
            modifier = Modifier.fillMaxSize().background(OverlayDark),
            contentAlignment = Alignment.Center
        ) {
            LevelUpDialog(
                newLevel = newLevel,
                onDismiss = { viewModel.dismissLevelUp() }
            )
        }
    }
}
}

@Composable
private fun HomeGameButton(
    title: String,
    emoji: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "home_button_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(PanelBackground)
            .border(3.dp, Gold, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun SmallGameButton(
    title: String,
    emoji: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Gold.copy(alpha = 0.3f))
            .border(2.dp, Gold, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
    }
}

private data class GameButtonData(
    val title: String,
    val emoji: String,
    val onClick: () -> Unit
)
