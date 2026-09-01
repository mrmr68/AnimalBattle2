package com.animalbattle.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.DailyLoginReward
import com.animalbattle.game.domain.model.RewardType
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.VictoryGreen

@Composable
fun DailyLoginDialog(
    rewards: List<DailyLoginReward>,
    onClaim: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Cream)
            .border(3.dp, Gold, RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🎁 ${stringResource(R.string.daily_login_title)}",
                style = MaterialTheme.typography.headlineMedium,
                color = GoldDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 7 Day Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rewards.forEach { reward ->
                    DailyRewardCard(
                        reward = reward,
                        onClaim = { onClaim(reward.day) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GameButton(
                text = stringResource(R.string.close),
                onClick = onDismiss,
                modifier = Modifier.width(150.dp)
            )
        }
    }
}

@Composable
private fun DailyRewardCard(
    reward: DailyLoginReward,
    onClaim: () -> Unit
) {
    val isClaimed = reward.isClaimed
    val isAvailable = !isClaimed

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isClaimed -> VictoryGreen.copy(alpha = 0.2f)
                    isAvailable -> Gold.copy(alpha = 0.3f)
                    else -> Cream
                }
            )
            .border(
                2.dp,
                when {
                    isClaimed -> VictoryGreen
                    isAvailable -> Gold
                    else -> Gold.copy(alpha = 0.3f)
                },
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isAvailable) { onClaim() }
            .padding(4.dp)
    ) {
        Text(
            text = stringResource(R.string.day_label, reward.day),
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = when (reward.reward.type) {
                RewardType.COINS -> "🪙${reward.reward.amount}"
                RewardType.TROPHIES -> "🏆${reward.reward.amount}"
                RewardType.XP -> "⭐${reward.reward.amount}"
                else -> "🎁"
            },
            style = MaterialTheme.typography.labelSmall,
            color = GoldDark,
            textAlign = TextAlign.Center
        )

        if (isClaimed) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelSmall,
                color = VictoryGreen,
                textAlign = TextAlign.Center
            )
        }
    }
}
