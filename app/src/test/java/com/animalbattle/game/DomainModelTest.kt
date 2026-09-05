package com.animalbattle.game

import com.animalbattle.game.domain.model.BattleRecord
import com.animalbattle.game.domain.model.LeaderboardEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for domain model data classes used across the app.
 */
class DomainModelTest {

    // ── BattleRecord ────────────────────────────────────────

    @Test
    fun `battle record stores all fields correctly`() {
        val record = BattleRecord(
            id = "12345",
            opponentName = "Shadow",
            opponentAnimalId = "tiger",
            playerAnimalId = "lion",
            won = true,
            rewardCoins = 25,
            rewardTrophies = 1,
            timestamp = 1700000000000L
        )
        assertEquals("12345", record.id)
        assertEquals("Shadow", record.opponentName)
        assertEquals("tiger", record.opponentAnimalId)
        assertEquals("lion", record.playerAnimalId)
        assertTrue(record.won)
        assertEquals(25, record.rewardCoins)
        assertEquals(1, record.rewardTrophies)
        assertEquals(1700000000000L, record.timestamp)
    }

    @Test
    fun `battle record defeat has zero rewards`() {
        val record = BattleRecord(
            id = "99",
            opponentName = "Blaze",
            opponentAnimalId = "bear",
            playerAnimalId = "wolf",
            won = false,
            rewardCoins = 0,
            rewardTrophies = 0,
            timestamp = System.currentTimeMillis()
        )
        assertFalse(record.won)
        assertEquals(0, record.rewardCoins)
        assertEquals(0, record.rewardTrophies)
    }

    @Test
    fun `battle record id can be timestamp string`() {
        val id = System.currentTimeMillis().toString()
        assertTrue(id.toLongOrNull() != null)
        assertTrue(id.length >= 10)
    }

    // ── LeaderboardEntry ────────────────────────────────────

    @Test
    fun `leaderboard entry stores rank correctly`() {
        val entry = LeaderboardEntry(
            rank = 1,
            playerName = "Shadow",
            trophies = 150,
            avatarEmoji = "🦁"
        )
        assertEquals(1, entry.rank)
        assertEquals("Shadow", entry.playerName)
        assertEquals(150, entry.trophies)
        assertEquals("🦁", entry.avatarEmoji)
        assertFalse(entry.isPlayer)
    }

    @Test
    fun `leaderboard entry isPlayer flag works`() {
        val playerEntry = LeaderboardEntry(
            rank = 5,
            playerName = "TestPlayer",
            trophies = 50,
            avatarEmoji = "⭐",
            isPlayer = true
        )
        assertTrue(playerEntry.isPlayer)
    }

    @Test
    fun `leaderboard entries can be sorted by trophies`() {
        val entries = listOf(
            LeaderboardEntry(1, "Low", 10, "🦁"),
            LeaderboardEntry(2, "High", 200, "🐯"),
            LeaderboardEntry(3, "Mid", 100, "🦅")
        )
        val sorted = entries.sortedByDescending { it.trophies }
        assertEquals("High", sorted[0].playerName)
        assertEquals("Mid", sorted[1].playerName)
        assertEquals("Low", sorted[2].playerName)
    }

    // ── String serialization round-trip (mimics DataStore) ───

    @Test
    fun `animal upgrades serialize and deserialize correctly`() {
        val upgrades = mapOf("lion" to 3, "tiger" to 1, "eagle" to 5)

        // Serialize
        val serialized = upgrades.entries.joinToString(",") { "${it.key}:${it.value}" }
        assertEquals("lion:3,tiger:1,eagle:5", serialized)

        // Deserialize
        val deserialized = serialized.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }.toMap()

        assertEquals(3, deserialized.size)
        assertEquals(3, deserialized["lion"])
        assertEquals(1, deserialized["tiger"])
        assertEquals(5, deserialized["eagle"])
    }

    @Test
    fun `empty upgrades serialize to empty string`() {
        val upgrades = emptyMap<String, Int>()
        val serialized = upgrades.entries.joinToString(",") { "${it.key}:${it.value}" }
        assertEquals("", serialized)
    }

    @Test
    fun `recent battles serialize and deserialize round-trip`() {
        val battles = listOf(
            BattleRecord("1", "Shadow", "tiger", "lion", true, 25, 1, 1700000000000L),
            BattleRecord("2", "Blaze", "bear", "wolf", false, 0, 0, 1700001000000L)
        )

        // Serialize (matches PlayerDataStore format)
        val serialized = battles.joinToString(";") {
            "${it.id},${it.opponentName},${it.opponentAnimalId},${it.playerAnimalId},${it.won},${it.rewardCoins},${it.rewardTrophies},${it.timestamp}"
        }
        assertEquals("1,Shadow,tiger,lion,true,25,1,1700000000000;2,Blaze,bear,wolf,false,0,0,1700001000000", serialized)

        // Deserialize
        val deserialized = serialized.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size >= 8) BattleRecord(
                id = parts[0],
                opponentName = parts[1],
                opponentAnimalId = parts[2],
                playerAnimalId = parts[3],
                won = parts[4].toBoolean(),
                rewardCoins = parts[5].toIntOrNull() ?: 0,
                rewardTrophies = parts[6].toIntOrNull() ?: 0,
                timestamp = parts[7].toLongOrNull() ?: 0L
            ) else null
        }

        assertEquals(2, deserialized.size)
        assertEquals("1", deserialized[0].id)
        assertTrue(deserialized[0].won)
        assertEquals(25, deserialized[0].rewardCoins)
        assertFalse(deserialized[1].won)
        assertEquals(0, deserialized[1].rewardTrophies)
    }

    @Test
    fun `completed levels serialize and deserialize correctly`() {
        val levels = listOf(1, 3, 5, 7)
        val serialized = levels.joinToString(",") { it.toString() }
        assertEquals("1,3,5,7", serialized)

        val deserialized = serialized.split(",").mapNotNull { it.toIntOrNull() }
        assertEquals(levels, deserialized)
    }

    @Test
    fun `empty completed levels serialize to empty string`() {
        val levels = emptyList<Int>()
        val serialized = levels.joinToString(",") { it.toString() }
        assertEquals("", serialized)
    }
}
