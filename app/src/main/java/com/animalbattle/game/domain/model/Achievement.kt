package com.animalbattle.game.domain.model

/**
 * Achievement system for player motivation.
 */
data class Achievement(
    val id: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val iconEmoji: String,
    val requirement: AchievementRequirement,
    val reward: Reward,
    val isUnlocked: Boolean = false
)

sealed class AchievementRequirement {
    data class WinBattles(val count: Int) : AchievementRequirement()
    data class CollectTrophies(val count: Int) : AchievementRequirement()
    data class CollectCoins(val count: Int) : AchievementRequirement()
    data class UnlockAnimals(val count: Int) : AchievementRequirement()
    data class UpgradeAnimal(val animalId: String, val level: Int) : AchievementRequirement()
    data class WinStreak(val count: Int) : AchievementRequirement()
    data class DailyLogin(val days: Int) : AchievementRequirement()
    data class UseAbility(val count: Int) : AchievementRequirement()
}

/**
 * Manages achievement progress and unlocking.
 */
object AchievementManager {

    fun checkAchievements(
        player: Player,
        achievements: List<Achievement>
    ): List<Achievement> {
        return achievements.map { achievement ->
            if (achievement.isUnlocked) return@map achievement

            val isUnlocked = when (achievement.requirement) {
                is AchievementRequirement.WinBattles -> {
                    player.recentBattles.count { it.won } >= achievement.requirement.count
                }
                is AchievementRequirement.CollectTrophies -> {
                    player.trophies >= achievement.requirement.count
                }
                is AchievementRequirement.CollectCoins -> {
                    player.coins >= achievement.requirement.count
                }
                is AchievementRequirement.UnlockAnimals -> {
                    player.unlockedAnimals.size >= achievement.requirement.count
                }
                is AchievementRequirement.UpgradeAnimal -> {
                    val level = player.animalUpgrades[achievement.requirement.animalId] ?: 0
                    level >= achievement.requirement.level
                }
                is AchievementRequirement.WinStreak -> {
                    // Check last N battles are all wins
                    val recentWins = player.recentBattles.take(achievement.requirement.count)
                    recentWins.size == achievement.requirement.count && recentWins.all { it.won }
                }
                is AchievementRequirement.DailyLogin -> {
                    player.dailyLoginStreak >= achievement.requirement.days
                }
                is AchievementRequirement.UseAbility -> {
                    // Track in separate counter (simplified)
                    false
                }
            }

            achievement.copy(isUnlocked = isUnlocked)
        }
    }

    fun getAchievementProgress(
        player: Player,
        achievement: Achievement
    ): Pair<Int, Int> {
        return when (achievement.requirement) {
            is AchievementRequirement.WinBattles -> {
                val wins = player.recentBattles.count { it.won }
                Pair(wins, achievement.requirement.count)
            }
            is AchievementRequirement.CollectTrophies -> {
                Pair(player.trophies, achievement.requirement.count)
            }
            is AchievementRequirement.CollectCoins -> {
                Pair(player.coins, achievement.requirement.count)
            }
            is AchievementRequirement.UnlockAnimals -> {
                Pair(player.unlockedAnimals.size, achievement.requirement.count)
            }
            is AchievementRequirement.UpgradeAnimal -> {
                val level = player.animalUpgrades[achievement.requirement.animalId] ?: 0
                Pair(level, achievement.requirement.level)
            }
            is AchievementRequirement.WinStreak -> {
                val streak = player.recentBattles.take(achievement.requirement.count).count { it.won }
                Pair(streak, achievement.requirement.count)
            }
            is AchievementRequirement.DailyLogin -> {
                Pair(player.dailyLoginStreak, achievement.requirement.days)
            }
            is AchievementRequirement.UseAbility -> {
                Pair(0, achievement.requirement.count) // Track separately
            }
        }
    }
}
