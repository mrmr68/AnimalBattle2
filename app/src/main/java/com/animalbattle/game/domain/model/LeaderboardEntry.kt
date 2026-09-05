package com.animalbattle.game.domain.model

/**
 * Leaderboard entry. All stats are exposed so UI and the backend
 * API layer can share one shape. `wins`, `losses`, `winRate`, `xp`
 * and `level` are ready for server polling — the existing backend
 * already returns `wins`, `weekly_trophies` and `total_battles`.
 */
data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val trophies: Int,
    val avatarEmoji: String = "👤",
    val isPlayer: Boolean = false,
    // ── Extended stats (Part 6) ──
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Float = 0f,
    val xp: Int = 0,
    val level: Int = 1
) {
    companion object {
        /** Build a rank list from backend rows (aliased snake_case). */
        fun fromRows(rows: List<Map<String, Any?>>): List<LeaderboardEntry> {
            return rows.mapIndexed { index, row ->
                val wins = (row["wins"] as? Number)?.toInt() ?: 0
                val total = ((row["total_battles"] as? Number)?.toInt() ?: wins)
                LeaderboardEntry(
                    rank = (row["rank"] as? Number)?.toInt() ?: (index + 1),
                    playerName = (row["player_name"] as? String) ?: "Player",
                    trophies = (row["weekly_trophies"] as? Number)?.toInt()
                        ?: (row["trophies"] as? Number)?.toInt() ?: 0,
                    avatarEmoji = "⭐",
                    wins = wins,
                    losses = ((row["total_battles"] as? Number)?.toInt() ?: wins) - wins,
                    winRate = if (total > 0) wins.toFloat() / total else 0f,
                    xp = (row["xp"] as? Number)?.toInt() ?: 0,
                    level = (row["level"] as? Number)?.toInt() ?: 1
                )
            }
        }
    }
}
