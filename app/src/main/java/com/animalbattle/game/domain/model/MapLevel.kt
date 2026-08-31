package com.animalbattle.game.domain.model

data class MapLevel(
    val level: Int,
    val nameResId: Int,
    val status: LevelStatus,
    val requiredTrophies: Int = 0,
    val reward: Reward? = null
)

enum class LevelStatus {
    LOCKED,
    AVAILABLE,
    COMPLETED
}
