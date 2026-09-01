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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.BattlePhase
import com.animalbattle.game.domain.model.BattleResult
import com.animalbattle.game.domain.model.BattleState
import com.animalbattle.game.domain.model.GameConfig
import com.animalbattle.game.ui.components.AnimatedAnimal
import com.animalbattle.game.ui.components.AnimalSize
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GameButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.HealthBar
import com.animalbattle.game.ui.components.PowerIndicator
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
    state: BattleState,
    onIncreasePower: () -> Unit,
    onAttack: () -> Unit,
    onUseAbility: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    var isPlayerAttacking by remember { mutableStateOf(false) }
    var isOpponentTakingDamage by remember { mutableStateOf(false) }

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
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player XP Bar (Vertical)
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpBlue
                )
                PowerIndicator(
                    power = state.playerXp,
                    maxPower = 5,
                    color = XpBlue,
                    modifier = Modifier
                        .width(20.dp)
                        .height(120.dp)
                )
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
                    // Animated Animal
                    AnimatedAnimal(
                        animal = state.playerAnimal,
                        size = AnimalSize.LARGE,
                        isAttacking = isPlayerAttacking,
                        powerLevel = state.playerPower
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(state.playerAnimal.nameResId),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    // HP Bar
                    HealthBar(
                        currentHp = state.playerHp,
                        maxHp = state.playerMaxHp
                    )
                    Text(
                        text = "HP: ${state.playerHp}/${state.playerMaxHp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Power with visual indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚡ ${state.playerPower}",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldDark
                        )
                    }

                    Text(
                        text = "Lv.${state.playerLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            // VS Badge with animation
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(DarkGreenPrimary)
                    .border(3.dp, Gold, RoundedCornerShape(32.dp)),
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
                    // Animated Animal with damage effect
                    AnimatedAnimal(
                        animal = state.opponentAnimal,
                        size = AnimalSize.LARGE,
                        isTakingDamage = isOpponentTakingDamage,
                        powerLevel = state.opponentPower
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(state.opponentAnimal.nameResId),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    // HP Bar
                    HealthBar(
                        currentHp = state.opponentHp,
                        maxHp = state.opponentMaxHp
                    )
                    Text(
                        text = "HP: ${state.opponentHp}/${state.opponentMaxHp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚡ ${state.opponentPower}",
                            style = MaterialTheme.typography.titleSmall,
                            color = DefeatRed
                        )
                    }

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
                    .width(50.dp)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = XpRed
                )
                PowerIndicator(
                    power = state.opponentXp,
                    maxPower = 5,
                    color = XpRed,
                    modifier = Modifier
                        .width(20.dp)
                        .height(120.dp)
                )
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
                    text = "⚡ ${stringResource(R.string.increase_power)}",
                    onClick = onIncreasePower,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                GameButton(
                    text = "⚔️ ${stringResource(R.string.attack)}",
                    onClick = {
                        isPlayerAttacking = true
                        isOpponentTakingDamage = true
                        onAttack()
                    },
                    enabled = state.playerPower >= GameConfig.POWER_ATTACK_COST,
                    backgroundColor = DefeatRed,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Abilities with power visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.playerAnimal.abilities.forEachIndexed { index, ability ->
                    val canUse = state.playerXp >= ability.xpCost && state.isPlayerTurn
                    val powerColor = when (index) {
                        0 -> Color(0xFF4CAF50) // Green for low power
                        1 -> Color(0xFFFFC107) // Yellow for medium power
                        2 -> Color(0xFFF44336) // Red for high power
                        else -> Color.Gray
                    }

                    GameButton(
                        text = "${stringResource(ability.nameResId)}\n${ability.damage} DMG",
                        onClick = {
                            isPlayerAttacking = true
                            isOpponentTakingDamage = true
                            onUseAbility(index)
                        },
                        enabled = canUse,
                        backgroundColor = if (canUse) powerColor else Color.Gray,
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
                BattleResult.VICTORY -> "🏆 ${stringResource(R.string.victory)}"
                BattleResult.DEFEAT -> "💔 ${stringResource(R.string.defeat)}"
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
