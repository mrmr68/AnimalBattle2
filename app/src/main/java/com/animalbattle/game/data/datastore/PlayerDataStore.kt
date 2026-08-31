package com.animalbattle.game.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.animalbattle.game.domain.model.BattleRecord
import com.animalbattle.game.domain.model.GameSettings
import com.animalbattle.game.domain.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "player_data")

class PlayerDataStore(private val context: Context) {

    private object Keys {
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val LEVEL = intPreferencesKey("level")
        val XP = intPreferencesKey("xp")
        val COINS = intPreferencesKey("coins")
        val TROPHIES = intPreferencesKey("trophies")
        val SELECTED_ANIMAL_ID = stringPreferencesKey("selected_animal_id")
        val UNLOCKED_ANIMALS = stringSetPreferencesKey("unlocked_animals")
        val ANIMAL_UPGRADES = stringPreferencesKey("animal_upgrades")
        val RECENT_BATTLES = stringPreferencesKey("recent_battles")
        val DAILY_LOGIN_STREAK = intPreferencesKey("daily_login_streak")
        val LAST_LOGIN_DATE = longPreferencesKey("last_login_date")
        val LUCKY_WHEEL_SPINS_TODAY = intPreferencesKey("lucky_wheel_spins_today")
        val LAST_SPIN_DATE = longPreferencesKey("last_spin_date")
        val CURRENT_MAP_LEVEL = intPreferencesKey("current_map_level")
        val COMPLETED_LEVELS = stringPreferencesKey("completed_levels")
        val LANGUAGE = stringPreferencesKey("language")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val playerFlow: Flow<Player> = context.dataStore.data.map { prefs ->
        Player(
            name = prefs[Keys.PLAYER_NAME] ?: "Player",
            level = prefs[Keys.LEVEL] ?: 1,
            xp = prefs[Keys.XP] ?: 0,
            coins = prefs[Keys.COINS] ?: Player.INITIAL_COINS,
            trophies = prefs[Keys.TROPHIES] ?: 0,
            selectedAnimalId = prefs[Keys.SELECTED_ANIMAL_ID] ?: "lion",
            unlockedAnimals = (prefs[Keys.UNLOCKED_ANIMALS] ?: setOf("lion")).toList(),
            animalUpgrades = parseAnimalUpgrades(prefs[Keys.ANIMAL_UPGRADES] ?: ""),
            recentBattles = parseRecentBattles(prefs[Keys.RECENT_BATTLES] ?: ""),
            dailyLoginStreak = prefs[Keys.DAILY_LOGIN_STREAK] ?: 0,
            lastLoginDate = prefs[Keys.LAST_LOGIN_DATE] ?: 0L,
            luckyWheelSpinsToday = prefs[Keys.LUCKY_WHEEL_SPINS_TODAY] ?: 0,
            lastSpinDate = prefs[Keys.LAST_SPIN_DATE] ?: 0L,
            currentMapLevel = prefs[Keys.CURRENT_MAP_LEVEL] ?: 1,
            completedLevels = parseCompletedLevels(prefs[Keys.COMPLETED_LEVELS] ?: ""),
            settings = GameSettings(
                language = prefs[Keys.LANGUAGE] ?: "en",
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
            )
        )
    }

    suspend fun updatePlayer(player: Player) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PLAYER_NAME] = player.name
            prefs[Keys.LEVEL] = player.calculateLevel()
            prefs[Keys.XP] = player.xp
            prefs[Keys.COINS] = player.coins
            prefs[Keys.TROPHIES] = player.trophies
            prefs[Keys.SELECTED_ANIMAL_ID] = player.selectedAnimalId
            prefs[Keys.UNLOCKED_ANIMALS] = player.unlockedAnimals.toSet()
            prefs[Keys.ANIMAL_UPGRADES] = serializeAnimalUpgrades(player.animalUpgrades)
            prefs[Keys.RECENT_BATTLES] = serializeRecentBattles(player.recentBattles)
            prefs[Keys.DAILY_LOGIN_STREAK] = player.dailyLoginStreak
            prefs[Keys.LAST_LOGIN_DATE] = player.lastLoginDate
            prefs[Keys.LUCKY_WHEEL_SPINS_TODAY] = player.luckyWheelSpinsToday
            prefs[Keys.LAST_SPIN_DATE] = player.lastSpinDate
            prefs[Keys.CURRENT_MAP_LEVEL] = player.currentMapLevel
            prefs[Keys.COMPLETED_LEVELS] = serializeCompletedLevels(player.completedLevels)
            prefs[Keys.LANGUAGE] = player.settings.language
            prefs[Keys.SOUND_ENABLED] = player.settings.soundEnabled
            prefs[Keys.MUSIC_ENABLED] = player.settings.musicEnabled
            prefs[Keys.NOTIFICATIONS_ENABLED] = player.settings.notificationsEnabled
        }
    }

    suspend fun updateSettings(settings: GameSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = settings.language
            prefs[Keys.SOUND_ENABLED] = settings.soundEnabled
            prefs[Keys.MUSIC_ENABLED] = settings.musicEnabled
            prefs[Keys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
        }
    }

    private fun parseAnimalUpgrades(data: String): Map<String, Int> {
        if (data.isEmpty()) return emptyMap()
        return data.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }.toMap()
    }

    private fun serializeAnimalUpgrades(upgrades: Map<String, Int>): String {
        return upgrades.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    private fun parseRecentBattles(data: String): List<BattleRecord> {
        if (data.isEmpty()) return emptyList()
        return try {
            data.split(";").mapNotNull {
                val parts = it.split(",")
                if (parts.size >= 8) BattleRecord(
                    id = parts[0],
                    opponentName = parts[1],
                    opponentAnimalId = parts[2],
                    playerAnimalId = parts[3],
                    won = parts[4].toBoolean(),
                    rewardCoins = parts[5].toIntOrNull() ?: 0,
                    rewardTrophies = parts[6].toIntOrNull() ?: 0,
                    timestamp = parts[7].toLongOrNull() ?: 0L
                ) else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeRecentBattles(battles: List<BattleRecord>): String {
        return battles.takeLast(20).joinToString(";") {
            "${it.id},${it.opponentName},${it.opponentAnimalId},${it.playerAnimalId},${it.won},${it.rewardCoins},${it.rewardTrophies},${it.timestamp}"
        }
    }

    private fun parseCompletedLevels(data: String): List<Int> {
        if (data.isEmpty()) return emptyList()
        return data.split(",").mapNotNull { it.toIntOrNull() }
    }

    private fun serializeCompletedLevels(levels: List<Int>): String {
        return levels.joinToString(",")
    }
}
