package com.animalbattle.game.ui.battle

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.animalbattle.game.domain.model.GameConfig
import com.animalbattle.game.domain.model.Question
import com.animalbattle.game.ui.components.GameButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.PanelBackground
import com.animalbattle.game.ui.theme.TextOnGold
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.ui.theme.VictoryGreen
import com.animalbattle.game.ui.theme.WarningOrange
import com.animalbattle.game.ui.theme.XpBlue
import kotlinx.coroutines.delay

@Composable
fun QuestionScreen(
    question: Question?,
    playerUsedFiftyFifty: Boolean,
    playerUsedSkip: Boolean,
    coins: Int,
    onAnswer: (Int) -> Unit,
    onTimeUp: () -> Unit,
    onFiftyFifty: () -> Unit,
    onSkip: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(GameConfig.QUESTION_TIME_SECONDS) }
    var showResult by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft <= 0) {
            onTimeUp()
        }
    }

    val timerProgress by animateFloatAsState(
        targetValue = timeLeft.toFloat() / GameConfig.QUESTION_TIME_SECONDS,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "timer_progress"
    )

    question?.let { q ->
        val visibleOptions = if (playerUsedFiftyFifty) {
            val correct = q.correctOptionIndex
            val wrongOptions = (0 until q.options.size).filter { it != correct }
            val removedWrong = wrongOptions.shuffled().take(2)
            (0 until q.options.size).filter { it !in removedWrong }
        } else {
            (0 until q.options.size).toList()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream.copy(alpha = 0.95f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelBackground, RoundedCornerShape(16.dp))
                    .border(2.dp, Gold, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.question_time, timeLeft),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (timeLeft <= 5) WarningOrange else TextPrimary
                )

                LinearProgressIndicator(
                    progress = { timerProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (timeLeft <= 5) WarningOrange else VictoryGreen,
                    trackColor = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question
            GamePanel(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = q.questionText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options
            q.options.forEachIndexed { index, option ->
                if (index in visibleOptions) {
                    OptionButton(
                        text = option,
                        index = index,
                        onClick = {
                            lastAnswerCorrect = index == q.correctOptionIndex
                            showResult = true
                            onAnswer(index)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lifelines
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                GameButton(
                    text = "${stringResource(R.string.lifeline_50_50)} (${GameConfig.FIFTY_FIFTY_COST} 🪙)",
                    onClick = onFiftyFifty,
                    enabled = !playerUsedFiftyFifty && coins >= GameConfig.FIFTY_FIFTY_COST,
                    backgroundColor = DarkGreenPrimary,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                GameButton(
                    text = "${stringResource(R.string.lifeline_skip)} (${GameConfig.SKIP_COST} 🪙)",
                    onClick = onSkip,
                    enabled = !playerUsedSkip && coins >= GameConfig.SKIP_COST,
                    backgroundColor = DarkGreenPrimary,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun OptionButton(
    text: String,
    index: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Gold.copy(alpha = 0.3f))
            .border(2.dp, Gold, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${('A' + index)}",
                style = MaterialTheme.typography.titleMedium,
                color = GoldDark
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}
