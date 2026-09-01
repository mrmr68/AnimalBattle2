package com.animalbattle.game.domain.model

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val trophies: Int,
    val avatarEmoji: String = "👤",
    val isPlayer: Boolean = false
)
