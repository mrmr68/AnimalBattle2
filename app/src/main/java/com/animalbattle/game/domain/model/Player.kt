package com.animalbattle.game.domain.model

data class Player(
    val name: String = "Player",
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 100,
    val trophies: Int = 0,
    val selectedAnimalId: String = "lion",
    val unlockedAnimals: List<String> = listOf("lion"),
    val animalUpgrades: Map<String, Int> = emptyMap(),
    val recentBattles: List<BattleRecord> = emptyList(),
    val dailyLoginStreak: Int = 0,
    val lastLoginDate: Long = 0L,
    val luckyWheelSpinsToday: Int = 0,
    val lastSpinDate: Long = 0L,
    val currentMapLevel: Int = 1,
    val completedLevels: List<Int> = emptyList(),
    val settings: GameSettings = GameSettings()
) {
    companion object {
        const val XP_PER_LEVEL = 10
        const val INITIAL_COINS = 100
        const val BATTLE_WIN_COINS = 25
        const val BATTLE_WIN_TROPHIES = 1
    }

    fun calculateLevel(): Int {
        return (trophies / XP_PER_LEVEL) + 1
    }

    fun xpForNextLevel(): Int {
        val currentLevelTrophies = (calculateLevel() - 1) * XP_PER_LEVEL
        val nextLevelTrophies = calculateLevel() * XP_PER_LEVEL
        return trophies - currentLevelTrophies
    }

    fun xpToNextLevel(): Int {
        return XP_PER_LEVEL
    }
}

data class BattleRecord(
    val id: String,
    val opponentName: String,
    val opponentAnimalId: String,
    val playerAnimalId: String,
    val won: Boolean,
    val rewardCoins: Int,
    val rewardTrophies: Int,
    val timestamp: Long
)

data class GameSettings(
    val language: String = "en",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)
