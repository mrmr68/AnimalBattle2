package com.animalbattle.game.ui.settings

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.animalbattle.game.domain.model.GameSettings
import com.animalbattle.game.ui.components.BackButton
import com.animalbattle.game.ui.components.GamePanel
import com.animalbattle.game.ui.components.TopBar
import com.animalbattle.game.ui.theme.Cream
import com.animalbattle.game.ui.theme.DarkGreenPrimary
import com.animalbattle.game.ui.theme.Gold
import com.animalbattle.game.ui.theme.GoldDark
import com.animalbattle.game.ui.theme.TextPrimary
import com.animalbattle.game.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val player by viewModel.player.collectAsState()
    val settings = player.settings

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
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = GoldDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Language Setting
        GamePanel(modifier = Modifier.fillMaxWidth()) {
            SettingItem(
                title = stringResource(R.string.language),
                content = {
                    LanguageSelector(
                        selectedLanguage = settings.language,
                        onLanguageSelected = { lang ->
                            viewModel.updateSettings(settings.copy(language = lang))
                        }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sound Effects
        GamePanel(modifier = Modifier.fillMaxWidth()) {
            SettingToggle(
                title = stringResource(R.string.sound_effects),
                checked = settings.soundEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(soundEnabled = it)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Music
        GamePanel(modifier = Modifier.fillMaxWidth()) {
            SettingToggle(
                title = stringResource(R.string.music),
                checked = settings.musicEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(musicEnabled = it)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notifications
        GamePanel(modifier = Modifier.fillMaxWidth()) {
            SettingToggle(
                title = stringResource(R.string.notifications),
                checked = settings.notificationsEnabled,
                onCheckedChange = { viewModel.updateSettings(settings.copy(notificationsEnabled = it)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About
        GamePanel(modifier = Modifier.fillMaxWidth()) {
            SettingItem(
                title = stringResource(R.string.about),
                content = {
                    Text(
                        text = stringResource(R.string.version, "1.0.0"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Gold,
                checkedThumbColor = DarkGreenPrimary
            )
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "en" to stringResource(R.string.language_english),
        "fa" to stringResource(R.string.language_persian),
        "ar" to stringResource(R.string.language_arabic)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        languages.forEach { (code, name) ->
            val isSelected = code == selectedLanguage
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Gold else Gold.copy(alpha = 0.2f))
                    .border(2.dp, if (isSelected) GoldDark else Gold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { onLanguageSelected(code) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) DarkGreenPrimary else TextPrimary
                )
            }
        }
    }
}
