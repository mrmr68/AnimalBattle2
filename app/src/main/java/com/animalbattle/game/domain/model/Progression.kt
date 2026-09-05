package com.animalbattle.game.domain.model

import com.animalbattle.game.R

// ── XP & Level System ──────────────────────────────────────

data class XPConfig(
    val xpPerWin: Int = 25,
    val xpPerLoss: Int = 5,
    val xpPerPerfect: Int = 10,
    val xpPerCombo: Int = 5,
    val xpPerStageComplete: Int = 50,
    val baseXpPerLevel: Int = 100,
    val xpMultiplierPerLevel: Float = 1.2f
) {
    fun xpRequiredForLevel(level: Int): Int {
        return (baseXpPerLevel * (xpMultiplierPerLevel.pow(level - 1))).toInt()
    }

    private fun Float.pow(n: Int): Float {
        var result = 1f
        for (i in 0 until n) result *= this
        return result
    }
}

data class LevelProgress(
    val currentLevel: Int,
    val currentXP: Int,
    val xpToNextLevel: Int,
    val progress: Float // 0f to 1f
)

// ── Coins & Economy ────────────────────────────────────────

data class CoinReward(
    val baseAmount: Int,
    val multiplier: Float = 1f,
    val bonusReason: String? = null
) {
    val totalAmount: Int get() = (baseAmount * multiplier).toInt()
}

data class EconomyConfig(
    val battleWinCoins: Int = 25,
    val battleLossCoins: Int = 5,
    val perfectAnswerBonus: Int = 10,
    val comboBonusPerLevel: Int = 5,
    val stageCompletionBonus: Int = 100,
    val dailyLoginBonus: Int = 50,
    val luckyWheelMinReward: Int = 10,
    val luckyWheelMaxReward: Int = 500,
    val chestCommonCoins: Int = 50,
    val chestRareCoins: Int = 150,
    val chestEpicCoins: Int = 500,
    val chestLegendaryCoins: Int = 1000
)

// ── Animal Skins ───────────────────────────────────────────

data class AnimalSkin(
    val id: String,
    val animalId: String,
    val nameResId: Int,
    val name: String,
    val description: String,
    val rarity: SkinRarity,
    val price: Int,
    val isUnlocked: Boolean = false,
    val isSelected: Boolean = false
)

enum class SkinRarity(val displayName: String, val colorHex: Long) {
    COMMON("Common", 0xFF9E9E9E),
    RARE("Rare", 0xFF2196F3),
    EPIC("Epic", 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFFFFD700),
    MYTHIC("Mythic", 0xFFFF6D00)
}

object SkinCatalog {
    private val allSkins = listOf(
        // Lion Skins
        AnimalSkin("lion_default", "lion", R.string.animal_lion, "Classic Lion", "The original king of the jungle", SkinRarity.COMMON, 0, true, true),
        AnimalSkin("lion_golden", "lion", R.string.animal_lion, "Golden Lion", "A majestic golden variant", SkinRarity.RARE, 500),
        AnimalSkin("lion_shadow", "lion", R.string.animal_lion, "Shadow Lion", "Darkness incarnate", SkinRarity.EPIC, 1500),
        AnimalSkin("lion_mystic", "lion", R.string.animal_lion, "Mystic Lion", "Ancient magical powers", SkinRarity.LEGENDARY, 3000),
        AnimalSkin("lion_flame", "lion", R.string.animal_lion, "Flame Lion", "Burns with eternal fire", SkinRarity.MYTHIC, 5000),

        // Tiger Skins
        AnimalSkin("tiger_default", "tiger", R.string.animal_tiger, "Classic Tiger", "The original stripes", SkinRarity.COMMON, 0, true, true),
        AnimalSkin("tiger_frost", "tiger", R.string.animal_tiger, "Frost Tiger", "Cold as ice", SkinRarity.RARE, 500),
        AnimalSkin("tiger_thunder", "tiger", R.string.animal_tiger, "Thunder Tiger", "Electric fury", SkinRarity.EPIC, 1500),

        // Wolf Skins
        AnimalSkin("wolf_default", "wolf", R.string.animal_wolf, "Classic Wolf", "The original pack leader", SkinRarity.COMMON, 0, true, true),
        AnimalSkin("wolf_moonlight", "wolf", R.string.animal_wolf, "Moonlight Wolf", "Glows under moonlight", SkinRarity.RARE, 500),
        AnimalSkin("wolf_void", "wolf", R.string.animal_wolf, "Void Wolf", "From the shadows", SkinRarity.EPIC, 1500),

        // Eagle Skins
        AnimalSkin("eagle_default", "eagle", R.string.animal_eagle, "Classic Eagle", "The original sky ruler", SkinRarity.COMMON, 0, true, true),
        AnimalSkin("eagle_storm", "eagle", R.string.animal_eagle, "Storm Eagle", "Commands the winds", SkinRarity.RARE, 500),

        // Bear Skins
        AnimalSkin("bear_default", "bear", R.string.animal_bear, "Classic Bear", "The original powerhouse", SkinRarity.COMMON, 0, true, true),
        AnimalSkin("bear_arctic", "bear", R.string.animal_bear, "Arctic Bear", "Frozen strength", SkinRarity.RARE, 500),

        // Cobra Skins
        AnimalSkin("cobra_default", "cobra", R.string.animal_cobra, "Classic Cobra", "The original venom", SkinRarity.COMMON, 0, true, true),
        AnimalSkin("cobra_diamond", "cobra", R.string.animal_cobra, "Diamond Cobra", "Scales like diamonds", SkinRarity.EPIC, 1500)
    )

    fun getSkinsForAnimal(animalId: String): List<AnimalSkin> =
        allSkins.filter { it.animalId == animalId }

    fun getAllSkins(): List<AnimalSkin> = allSkins

    fun getSkinById(id: String): AnimalSkin? =
        allSkins.find { it.id == id }
}

// ── Chests ─────────────────────────────────────────────────

data class Chest(
    val id: String,
    val rarity: ChestRarity,
    val isLocked: Boolean = true,
    val unlockTimeMs: Long = 0L,
    val contents: ChestContents = ChestContents()
)

enum class ChestRarity(val displayName: String, val colorHex: Long, val unlockTimeMinutes: Int) {
    COMMON("Common", 0xFF9E9E9E, 30),
    RARE("Rare", 0xFF2196F3, 60),
    EPIC("Epic", 0xFF9C27B0, 180),
    LEGENDARY("Legendary", 0xFFFFD700, 480)
}

data class ChestContents(
    val coins: Int = 0,
    val xp: Int = 0,
    val trophies: Int = 0,
    val skinId: String? = null,
    val boostType: BoostType? = null,
    val boostDuration: Int = 0
)

// ── Boosts ─────────────────────────────────────────────────

enum class BoostType(val displayName: String, val description: String) {
    XP_BOOST("XP Boost", "Double XP for a limited time"),
    COIN_BOOST("Coin Boost", "Double coins for a limited time"),
    POWER_BOOST("Power Boost", "Increased attack power"),
    SHIELD("Shield", "Protected from one defeat"),
    LUCKY("Lucky", "Better rewards from chests")
}

data class ActiveBoost(
    val type: BoostType,
    val expiresAt: Long,
    val remainingMs: Long
) {
    val isActive: Boolean get() = remainingMs > 0
    val progress: Float get() {
        val total = expiresAt - (expiresAt - remainingMs)
        return if (total > 0) remainingMs.toFloat() / total else 0f
    }
}

// ── Statistics ─────────────────────────────────────────────

data class PlayerStatistics(
    val totalBattles: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalXPEarned: Int = 0,
    val totalTrophiesEarned: Int = 0,
    val perfectAnswers: Int = 0,
    val maxCombo: Int = 0,
    val stagesCompleted: Int = 0,
    val chestsOpened: Int = 0,
    val skinsUnlocked: Int = 0,
    val animalsUnlocked: Int = 0,
    val playTimeMinutes: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val winRate: Float get() = if (totalBattles > 0) wins.toFloat() / totalBattles else 0f
    val averageBattlesPerDay: Float get() {
        val daysSinceFirst = ((System.currentTimeMillis() - lastUpdated) / (24 * 60 * 60 * 1000)).coerceAtLeast(1)
        return totalBattles.toFloat() / daysSinceFirst
    }
}

// ── Stage Difficulty ───────────────────────────────────────

enum class StageDifficulty(val displayName: String, val colorHex: Long, val rewardMultiplier: Float) {
    EASY("Easy", 0xFF4CAF50, 1.0f),
    MEDIUM("Medium", 0xFFFFC107, 1.5f),
    HARD("Hard", 0xFFFF9800, 2.0f),
    BOSS("Boss", 0xFFF44336, 3.0f)
}

data class StageReward(
    val coins: Int,
    val xp: Int,
    val trophies: Int,
    val chestChance: Float = 0.3f,
    val difficulty: StageDifficulty = StageDifficulty.EASY
)
