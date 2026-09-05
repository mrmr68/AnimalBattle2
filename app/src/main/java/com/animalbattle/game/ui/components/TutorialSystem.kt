package com.animalbattle.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animalbattle.game.ui.theme.*

// ── Tutorial Step ──────────────────────────────────────────

data class TutorialStep(
    val title: String,
    val message: String,
    val icon: String,
    val highlightX: Float = 0.5f, // 0-1 horizontal position
    val highlightY: Float = 0.5f, // 0-1 vertical position
    val highlightRadius: Float = 100f
)

// ── Tutorial Manager ───────────────────────────────────────

class TutorialManager {
    private val _steps = mutableListOf<TutorialStep>()
    private var _currentStep = mutableIntStateOf(0)
    private var _isActive = mutableStateOf(false)

    val currentStep: Int get() = _currentStep.intValue
    val isActive: Boolean get() = _isActive.value
    val totalSteps: Int get() = _steps.size
    val currentTutorialStep: TutorialStep?
        get() = if (_currentStep.intValue < _steps.size) _steps[_currentStep.intValue] else null

    fun addStep(step: TutorialStep) { _steps.add(step) }

    fun start() {
        if (_steps.isNotEmpty()) {
            _currentStep.intValue = 0
            _isActive.value = true
        }
    }

    fun next(): Boolean {
        return if (_currentStep.intValue < _steps.size - 1) {
            _currentStep.intValue++
            true
        } else {
            _isActive.value = false
            false
        }
    }

    fun skip() {
        _isActive.value = false
        _currentStep.intValue = 0
    }

    fun isComplete(): Boolean = _currentStep.intValue >= _steps.size - 1
}

// ── Tutorial Composable ────────────────────────────────────

@Composable
fun TutorialOverlay(
    manager: TutorialManager,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!manager.isActive) return

    val step = manager.currentTutorialStep ?: return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Prevent click-through */ }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(PanelBackground, Gold.copy(alpha = 0.05f))
                    )
                )
                .border(3.dp, Gold, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .width(320.dp)
        ) {
            // Step indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                for (i in 0 until manager.totalSteps) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= manager.currentStep) Gold
                                else Color.LightGray
                            )
                    )
                }
            }

            // Icon
            Text(
                text = step.icon,
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = GoldDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Message
            Text(
                text = step.message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skip
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                manager.skip()
                                onSkip()
                            }
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SKIP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextSecondary
                    )
                }

                // Next / Finish
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Gold, GoldDark)
                            )
                        )
                        .border(2.dp, GoldDark, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (manager.isComplete()) {
                                    manager.skip()
                                    onFinish()
                                } else {
                                    manager.next()
                                    onNext()
                                }
                            }
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (manager.isComplete()) "GOT IT!" else "NEXT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextOnGold
                    )
                }
            }
        }
    }
}

// ── Default Tutorial Steps ─────────────────────────────────

object DefaultTutorial {
    fun createHomeTutorial(): TutorialManager {
        val manager = TutorialManager()
        manager.addStep(TutorialStep(
            title = "Welcome!",
            message = "Welcome to Animal Battle! Choose your animal and battle your way through 6 regions!",
            icon = "🎮"
        ))
        manager.addStep(TutorialStep(
            title = "Battle",
            message = "Tap BATTLE to start a fight! Answer questions to power up your attacks!",
            icon = "⚔️"
        ))
        manager.addStep(TutorialStep(
            title = "Animals",
            message = "Collect and upgrade 12 unique animals, each with 3 special abilities!",
            icon = "🦁"
        ))
        manager.addStep(TutorialStep(
            title = "Shop",
            message = "Buy coin packs, upgrades, and exclusive skins in the shop!",
            icon = "🛒"
        ))
        manager.addStep(TutorialStep(
            title = "Map",
            message = "Progress through 30 stages across 6 regions to become the champion!",
            icon = "🗺️"
        ))
        return manager
    }

    fun createBattleTutorial(): TutorialManager {
        val manager = TutorialManager()
        manager.addStep(TutorialStep(
            title = "Your Turn",
            message = "When it's your turn, choose to ATTACK, INCREASE POWER, or use an ABILITY!",
            icon = "⚔️"
        ))
        manager.addStep(TutorialStep(
            title = "Questions",
            message = "Answer questions correctly to gain +2 Power! Wrong answers end your turn.",
            icon = "❓"
        ))
        manager.addStep(TutorialStep(
            title = "Abilities",
            message = "Build up XP to unlock powerful abilities! Each animal has 3 unique moves.",
            icon = "✨"
        ))
        manager.addStep(TutorialStep(
            title = "Win!",
            message = "Reduce your opponent's HP to 0 to win! Earn coins, XP, and trophies!",
            icon = "🏆"
        ))
        return manager
    }
}
