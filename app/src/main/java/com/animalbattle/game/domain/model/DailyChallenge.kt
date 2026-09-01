package com.animalbattle.game.domain.model

/**
 * Daily challenge system for player retention.
 */
data class DailyChallenge(
    val id: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val iconEmoji: String,
    val type: ChallengeType,
    val target: Int,
    val progress: Int = 0,
    val reward: Reward,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    val progressPercentage: Float
        get() = (progress.toFloat() / target).coerceIn(0f, 1f)
}

enum class ChallengeType {
    WIN_BATTLES,        // Win X battles
    USE_ABILITIES,      // Use X abilities
    COLLECT_COINS,      // Collect X coins
    SPIN_WHEEL,         // Spin wheel X times
    LOGIN_DAYS,         // Login X days in a row
    DEFEAT_ANIMALS,     // Defeat X different animals
    PERFECT_BATTLES     // Win X battles without losing HP
}

/**
 * Manages daily challenges.
 */
object DailyChallengeManager {

    /**
     * Generate today's challenges.
     */
    fun generateDailyChallenges(): List<DailyChallenge> {
        return listOf(
            DailyChallenge(
                id = "daily_win_3",
                nameResId = 0, // Will use string resource
                descriptionResId = 0,
                iconEmoji = "⚔️",
                type = ChallengeType.WIN_BATTLES,
                target = 3,
                reward = Reward("daily_win_3_reward", RewardType.COINS, 100)
            ),
            DailyChallenge(
                id = "daily_ability_5",
                nameResId = 0,
                descriptionResId = 0,
                iconEmoji = "💪",
                type = ChallengeType.USE_ABILITIES,
                target = 5,
                reward = Reward("daily_ability_5_reward", RewardType.TROPHIES, 2)
            ),
            DailyChallenge(
                id = "daily_coins_200",
                nameResId = 0,
                descriptionResId = 0,
                iconEmoji = "🪙",
                type = ChallengeType.COLLECT_COINS,
                target = 200,
                reward = Reward("daily_coins_200_reward", RewardType.XP, 3)
            )
        )
    }

    /**
     * Update challenge progress based on player action.
     */
    fun updateProgress(
        challenges: List<DailyChallenge>,
        type: ChallengeType,
        amount: Int = 1
    ): List<DailyChallenge> {
        return challenges.map { challenge ->
            if (challenge.type == type && !challenge.isCompleted) {
                val newProgress = (challenge.progress + amount).coerceAtMost(challenge.target)
                challenge.copy(
                    progress = newProgress,
                    isCompleted = newProgress >= challenge.target
                )
            } else {
                challenge
            }
        }
    }

    /**
     * Claim reward for completed challenge.
     */
    fun claimReward(challenge: DailyChallenge): Pair<DailyChallenge, Reward?> {
        return if (challenge.isCompleted && !challenge.isClaimed) {
            Pair(challenge.copy(isClaimed = true), challenge.reward)
        } else {
            Pair(challenge, null)
        }
    }
}
