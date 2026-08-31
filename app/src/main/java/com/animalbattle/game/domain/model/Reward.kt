package com.animalbattle.game.domain.model

data class Reward(
    val id: String,
    val type: RewardType,
    val amount: Int,
    val description: String = ""
)

enum class RewardType {
    COINS,
    TROPHIES,
    XP,
    ITEM,
    BONUS
}

data class WheelSegment(
    val id: String,
    val label: String,
    val reward: Reward,
    val color: Long,
    val probability: Float
)

data class DailyLoginReward(
    val day: Int,
    val reward: Reward,
    val isClaimed: Boolean = false
)
