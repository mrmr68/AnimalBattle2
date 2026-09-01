package com.animalbattle.game.ui.animals

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.animalbattle.game.R
import com.animalbattle.game.domain.model.AnimalData
import com.animalbattle.game.ui.components.AnimatedAnimal
import com.animalbattle.game.ui.components.AnimalSize
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GameButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DefeatRed
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.TextSecondary
import com.animalbattle.game.ui.theme.VictoryGreen
import com.animalbattle.game.ui.theme.XpBlue
import com.animalbattle.game.viewmodel.GameViewModel

@Composable
fun AnimalsScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val player by viewModel.player.collectAsState()
    val allAnimals = AnimalData.getAllAnimals()
    val selectedAnimal = allAnimals.find { it.id == player.selectedAnimalId } ?: allAnimals.first()
    val selectedIndex = allAnimals.indexOf(selectedAnimal).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onNavigateBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.animals_title),
                style = MaterialTheme.typography.headlineMedium,
                color = GoldDark
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Selected Animal Display with Animation
            GamePanel(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animated Animal
                    AnimatedAnimal(
                        animal = selectedAnimal,
                        size = AnimalSize.LARGE,
                        powerLevel = player.animalUpgrades[selectedAnimal.id] ?: 1
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(selectedAnimal.nameResId),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    if (player.animalUpgrades.containsKey(selectedAnimal.id)) {
                        Text(
                            text = stringResource(R.string.animal_level, player.animalUpgrades[selectedAnimal.id] ?: 1),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldDark
                        )
                    }

                    // Power Level Indicator
                    val currentLevel = player.animalUpgrades[selectedAnimal.id] ?: 1
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(1.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < currentLevel) Gold else Color.LightGray
                                    )
                            )
                        }
                        Text(
                            text = " Power: $currentLevel",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldDark
                        )
                    }
                }
            }

            // Animal Info Panel
            GamePanel(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "❤️ ${stringResource(R.string.animal_hp, selectedAnimal.baseHp)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = DefeatRed
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "⚔️ Base Power: ${selectedAnimal.basePower}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.animal_abilities),
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Abilities with power visualization
                    selectedAnimal.abilities.forEachIndexed { index, ability ->
                        val powerColor = when (index) {
                            0 -> VictoryGreen  // Low power
                            1 -> Color(0xFFFFC107)  // Medium power
                            2 -> DefeatRed  // High power
                            else -> Color.Gray
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(powerColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .border(1.dp, powerColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Power indicator
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(powerColor)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(ability.nameResId),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Damage: ${ability.damage} | XP: ${ability.xpCost}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            // Power bar
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = ability.damage / 50f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(powerColor)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upgrade Button with cost
                    val currentLevel = player.animalUpgrades[selectedAnimal.id] ?: 0
                    val upgradeCost = selectedAnimal.upgradeCost * (currentLevel + 1)

                    GameButton(
                        text = "⬆️ ${stringResource(R.string.upgrade_cost, upgradeCost)}",
                        onClick = { viewModel.upgradeAnimal(selectedAnimal.id) },
                        enabled = player.coins >= upgradeCost,
                        backgroundColor = if (player.coins >= upgradeCost) Gold else Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Animal Thumbnails with selection indicator
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allAnimals) { animal ->
                val isSelected = animal.id == player.selectedAnimalId
                val isUnlocked = animal.id in player.unlockedAnimals
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) Gold else Color.Transparent,
                    animationSpec = tween(200),
                    label = "animal_border"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Gold.copy(alpha = 0.3f) else PanelBackground)
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable {
                            if (isUnlocked) {
                                viewModel.selectAnimal(animal.id)
                            } else {
                                viewModel.unlockAnimal(animal.id)
                            }
                        }
                        .padding(8.dp)
                ) {
                    if (isUnlocked) {
                        // Mini animal icon
                        AnimatedAnimal(
                            animal = animal,
                            size = AnimalSize.SMALL,
                            powerLevel = player.animalUpgrades[animal.id] ?: 1
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.animal_locked),
                                tint = GoldDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(animal.nameResId),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUnlocked) TextPrimary else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
