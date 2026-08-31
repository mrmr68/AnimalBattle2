package com.animalbattle.game.domain.model

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val trophies: Int,
    val avatarResId: Int,
    val isPlayer: Boolean = false
)
