package com.animalbattle.game.ui.battle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.BattlePhase
import com.animalbattle.game.domain.model.BattleResult
import com.animalbattle.game.domain.model.GameConfig
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GameButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.DefeatRed
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.HpGreen
import com.animalbattle.game.ui.theme.HpRed
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.VictoryGreen
import com.animalbattle.game.ui.theme.XpBlue
import com.animalbattle.game.ui.theme.XpRed
import com.animalbattle.game.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun BattleScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val battleState by viewModel.battleState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startBattle()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        battleState?.let { state ->
            when (state.battlePhase) {
                BattlePhase.BATTLE_OVER -> {
                    BattleResultScreen(
                        result = state.battleResult,
                        onContinue = {
                            viewModel.endBattleAndReturn()
                            onNavigateBack()
                        }
                    )
                }
                BattlePhase.QUESTION -> {
                    QuestionScreen(
                        question = state.currentQuestion,
                        playerUsedFiftyFifty = state.playerUsedFiftyFifty,
                        playerUsedSkip = state.playerUsedSkip,
                        coins = viewModel.player.value.coins,
                        onAnswer = { viewModel.answerQuestion(it) },
                        onTimeUp = { viewModel.timeUp() },
                        onFiftyFifty = { viewModel.useFiftyFifty() },
                        onSkip = { viewModel.useSkip() }
                    )
                }
                else -> {
                    BattleArena(
                        state = state,
                        onIncreasePower = { viewModel.increasePower() },
                        onAttack = { viewModel.attack() },
                        onUseAbility = { viewModel.useAbility(it) },
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.battle_starting),
                    style = MaterialTheme.typography.headlineLarge,
                    color = GoldDark
                )
            }
        }
    }
}

@Composable
private fun BattleArena(
    state: com.animalbattle.game.domain.model.BattleState,
    onIncreasePower: () -> Unit,
    onAttack: () -> Unit,
    onUseAbility: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Turn indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state.isPlayerTurn) stringResource(R.string.your_turn) else stringResource(R.string.opponent_turn),
                style = MaterialTheme.typography.headlineMedium,
                color = if (state.isPlayerTurn) GoldDark else DefeatRed
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Battle Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Player XP Bar (Vertical)
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpBlue
                )
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.LightGray)
                ) {
                    val xpProgress = if (state.playerXp > 0) {
                        (state.playerXp.toFloat() / 5f).coerceIn(0f, 1f)
                    } else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxSize(fraction = xpProgress)
                            .clip(RoundedCornerShape(10.dp))
                            .background(XpBlue)
                    )
                }
            }

            // Player Section
            GamePanel(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animal Display
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold.copy(alpha = 0.3f))
                            .border(2.dp, Gold, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐾", style = MaterialTheme.typography.headlineLarge)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(state.playerAnimal.nameResId),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    // HP Bar
                    HPBar(
                        currentHp = state.playerHp,
                        maxHp = state.playerMaxHp,
                        color = HpGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Power
                    Text(
                        text = stringResource(R.string.power_label, state.playerPower),
                        style = MaterialTheme.typography.titleSmall,
                        color = GoldDark
                    )

                    // Level
                    Text(
                        text = "Lv.${state.playerLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            // VS
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(DarkGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextOnGold
                )
            }

            // Opponent Section
            GamePanel(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DefeatRed.copy(alpha = 0.3f))
                            .border(2.dp, DefeatRed, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐾", style = MaterialTheme.typography.headlineLarge)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(state.opponentAnimal.nameResId),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    HPBar(
                        currentHp = state.opponentHp,
                        maxHp = state.opponentMaxHp,
                        color = HpRed
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.power_label, state.opponentPower),
                        style = MaterialTheme.typography.titleSmall,
                        color = DefeatRed
                    )

                    Text(
                        text = "Lv.${state.opponentLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            // Opponent XP Bar (Vertical)
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpRed
                )
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.LightGray)
                ) {
                    val opponentXpProgress = if (state.opponentXp > 0) {
                        (state.opponentXp.toFloat() / 5f).coerceIn(0f, 1f)
                    } else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxSize(fraction = opponentXpProgress)
                            .clip(RoundedCornerShape(10.dp))
                            .background(XpRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons
        if (state.isPlayerTurn) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GameButton(
                    text = stringResource(R.string.increase_power),
                    onClick = onIncreasePower,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                GameButton(
                    text = stringResource(R.string.attack),
                    onClick = onAttack,
                    enabled = state.playerPower >= GameConfig.POWER_ATTACK_COST,
                    backgroundColor = DefeatRed,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Abilities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.playerAnimal.abilities.forEachIndexed { index, ability ->
                    val canUse = state.playerXp >= ability.xpCost && state.isPlayerTurn
                    GameButton(
                        text = "${stringResource(ability.nameResId)} (${ability.xpCost} XP)",
                        onClick = { onUseAbility(index) },
                        enabled = canUse,
                        backgroundColor = if (canUse) DarkGreenPrimary else Color.Gray,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.opponent_turn),
                    style = MaterialTheme.typography.headlineMedium,
                    color = DefeatRed
                )
            }
        }
    }
}

@Composable
private fun HPBar(
    currentHp: Int,
    maxHp: Int,
    color: Color
) {
    val progress = if (maxHp > 0) currentHp.toFloat() / maxHp else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = LinearEasing),
        label = "hp_progress"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.hp_label, currentHp),
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = color,
            trackColor = Color.LightGray,
            strokeHintSize = 12.dp
        )
    }
}

@Composable
private fun BattleResultScreen(
    result: BattleResult?,
    onContinue: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (result) {
            BattleResult.VICTORY -> VictoryGreen
            BattleResult.DEFEAT -> DefeatRed
            null -> Cream
        },
        animationSpec = tween(500),
        label = "result_bg"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = 0.2f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (result) {
                BattleResult.VICTORY -> stringResource(R.string.victory)
                BattleResult.DEFEAT -> stringResource(R.string.defeat)
                null -> ""
            },
            style = MaterialTheme.typography.displayLarge,
            color = when (result) {
                BattleResult.VICTORY -> VictoryGreen
                BattleResult.DEFEAT -> DefeatRed
                null -> TextPrimary
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (result == BattleResult.VICTORY) {
            GamePanel(
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎁",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.reward_coins, GameConfig.BATTLE_WIN_COINS),
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldDark
                    )
                    Text(
                        text = stringResource(R.string.reward_trophies, GameConfig.BATTLE_WIN_TROPHIES),
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        GameButton(
            text = stringResource(R.string.continue_button),
            onClick = onContinue,
            modifier = Modifier.width(200.dp)
        )
    }
}
