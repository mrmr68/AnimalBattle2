package com.animalbattle.game.domain.model

import kotlin.random.Random

// ── Economy System ─────────────────────────────────────────

class EconomySystem(private val config: EconomyConfig = EconomyConfig()) {

    /**
     * Calculate battle rewards based on outcome and modifiers.
     */
    fun calculateBattleReward(
        won: Boolean,
        comboLevel: Int = 0,
        perfectAnswers: Int = 0,
        hasXpBoost: Boolean = false,
        hasCoinBoost: Boolean = false,
        stageDifficulty: StageDifficulty = StageDifficulty.EASY
    ): BattleRewardResult {
        // Base coins
        var coins = if (won) config.battleWinCoins else config.battleLossCoins
        var xp = if (won) config.battleWinCoins else 5 // XP matches coins for simplicity

        // Perfect answer bonus
        if (perfectAnswers > 0) {
            coins += config.perfectAnswerBonus * perfectAnswers
            xp += config.perfectAnswerBonus * perfectAnswers
        }

        // Combo bonus
        if (comboLevel > 0) {
            coins += config.comboBonusPerLevel * comboLevel
            xp += config.comboBonusPerLevel * comboLevel
        }

        // Difficulty multiplier
        coins = (coins * stageDifficulty.rewardMultiplier).toInt()
        xp = (xp * stageDifficulty.rewardMultiplier).toInt()

        // Boost multipliers
        if (hasCoinBoost) coins *= 2
        if (hasXpBoost) xp *= 2

        return BattleRewardResult(
            coins = coins,
            xp = xp,
            trophies = if (won) 1 else 0,
            perfectAnswers = perfectAnswers,
            comboLevel = comboLevel
        )
    }

    /**
     * Calculate stage completion rewards.
     */
    fun calculateStageReward(
        stageIndex: Int,
        difficulty: StageDifficulty,
        isPerfect: Boolean = false
    ): StageReward {
        val baseCoins = config.stageCompletionBonus + (stageIndex * 10)
        val baseXp = 50 + (stageIndex * 5)
        val baseTrophies = 1 + (stageIndex / 3)

        return StageReward(
            coins = (baseCoins * difficulty.rewardMultiplier).toInt(),
            xp = (baseXp * difficulty.rewardMultiplier).toInt(),
            trophies = (baseTrophies * difficulty.rewardMultiplier).toInt(),
            chestChance = when (difficulty) {
                StageDifficulty.EASY -> 0.2f
                StageDifficulty.MEDIUM -> 0.35f
                StageDifficulty.HARD -> 0.5f
                StageDifficulty.BOSS -> 1.0f
            },
            difficulty = difficulty
        )
    }

    /**
     * Generate chest contents based on rarity.
     */
    fun generateChestContents(rarity: ChestRarity): ChestContents {
        return when (rarity) {
            ChestRarity.COMMON -> ChestContents(
                coins = config.chestCommonCoins + Random.nextInt(-10, 20),
                xp = 25 + Random.nextInt(0, 25),
                trophies = 0
            )
            ChestRarity.RARE -> ChestContents(
                coins = config.chestRareCoins + Random.nextInt(-20, 50),
                xp = 75 + Random.nextInt(0, 50),
                trophies = 1,
                boostType = if (Random.nextFloat() < 0.3f) BoostType.values().random() else null,
                boostDuration = 30 * 60 * 1000 // 30 minutes
            )
            ChestRarity.EPIC -> ChestContents(
                coins = config.chestEpicCoins + Random.nextInt(-50, 100),
                xp = 200 + Random.nextInt(0, 100),
                trophies = 3,
                skinId = if (Random.nextFloat() < 0.2f) "skin_epic_${Random.nextInt(1, 5)}" else null,
                boostType = if (Random.nextFloat() < 0.5f) BoostType.values().random() else null,
                boostDuration = 60 * 60 * 1000 // 1 hour
            )
            ChestRarity.LEGENDARY -> ChestContents(
                coins = config.chestLegendaryCoins + Random.nextInt(-100, 200),
                xp = 500 + Random.nextInt(0, 200),
                trophies = 5,
                skinId = if (Random.nextFloat() < 0.4f) "skin_legendary_${Random.nextInt(1, 3)}" else null,
                boostType = BoostType.values().random(),
                boostDuration = 120 * 60 * 1000 // 2 hours
            )
        }
    }

    /**
     * Calculate daily login reward.
     */
    fun calculateDailyLoginReward(streak: Int): DailyLoginResult {
        val baseReward = config.dailyLoginBonus
        val streakMultiplier = 1f + (streak.coerceAtMost(7) - 1) * 0.1f
        val coins = (baseReward * streakMultiplier).toInt()
        val xp = (baseReward * streakMultiplier * 0.5f).toInt()

        return DailyLoginResult(
            day = streak,
            coins = coins,
            xp = xp,
            streakMultiplier = streakMultiplier
        )
    }

    /**
     * Check if player can afford an item.
     */
    fun canAfford(playerCoins: Int, cost: Int): Boolean = playerCoins >= cost

    /**
     * Calculate animal upgrade cost.
     */
    fun calculateUpgradeCost(animalId: String, currentLevel: Int): Int {
        val baseCost = 100
        val costMultiplier = 1.5f
        return (baseCost * (costMultiplier.pow(currentLevel))).toInt()
    }

    private fun Float.pow(n: Int): Float {
        var result = 1f
        for (i in 0 until n) result *= this
        return result
    }
}

// ── Battle Reward Result ───────────────────────────────────

data class BattleRewardResult(
    val coins: Int,
    val xp: Int,
    val trophies: Int,
    val perfectAnswers: Int,
    val comboLevel: Int
)

// ── Daily Login Result ─────────────────────────────────────

data class DailyLoginResult(
    val day: Int,
    val coins: Int,
    val xp: Int,
    val streakMultiplier: Float
)

// ── Level Progress Calculator ──────────────────────────────

class ProgressionSystem(private val xpConfig: XPConfig = XPConfig()) {

    /**
     * Calculate current level progress.
     */
    fun calculateLevelProgress(currentXP: Int): LevelProgress {
        var level = 1
        var xpAccumulated = 0

        while (true) {
            val xpRequired = xpConfig.xpRequiredForLevel(level)
            if (xpAccumulated + xpRequired > currentXP) {
                val progress = (currentXP - xpAccumulated).toFloat() / xpRequired
                return LevelProgress(
                    currentLevel = level,
                    currentXP = currentXP - xpAccumulated,
                    xpToNextLevel = xpRequired,
                    progress = progress.coerceIn(0f, 1f)
                )
            }
            xpAccumulated += xpRequired
            level++
        }
    }

    /**
     * Calculate XP needed to reach a specific level.
     */
    fun xpToReachLevel(targetLevel: Int): Int {
        var totalXp = 0
        for (level in 1 until targetLevel) {
            totalXp += xpConfig.xpRequiredForLevel(level)
        }
        return totalXp
    }

    /**
     * Calculate level from total XP.
     */
    fun levelFromXP(totalXP: Int): Int {
        return calculateLevelProgress(totalXP).currentLevel
    }

    /**
     * Get XP breakdown for a battle.
     */
    fun getXPBreakdown(
        won: Boolean,
        perfectAnswers: Int = 0,
        comboLevel: Int = 0,
        hasXpBoost: Boolean = false
    ): XPBreakdown {
        var baseXP = if (won) xpConfig.xpPerWin else xpConfig.xpPerLoss
        var perfectXP = 0
        var comboXP = 0
        var boostMultiplier = 1f

        if (perfectAnswers > 0) {
            perfectXP = xpConfig.xpPerPerfect * perfectAnswers
        }
        if (comboLevel > 0) {
            comboXP = xpConfig.xpPerCombo * comboLevel
        }
        if (hasXpBoost) {
            boostMultiplier = 2f
        }

        val totalXP = ((baseXP + perfectXP + comboXP) * boostMultiplier).toInt()

        return XPBreakdown(
            baseXP = baseXP,
            perfectXP = perfectXP,
            comboXP = comboXP,
            boostMultiplier = boostMultiplier,
            totalXP = totalXP
        )
    }
}

data class XPBreakdown(
    val baseXP: Int,
    val perfectXP: Int,
    val comboXP: Int,
    val boostMultiplier: Float,
    val totalXP: Int
)
