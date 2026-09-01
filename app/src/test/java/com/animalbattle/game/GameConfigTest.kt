package com.animalbattle.game

import com.animalbattle.game.domain.model.GameConfig
import com.animalbattle.game.domain.model.RewardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameConfigTest {

    @Test
    fun `battle config values are correct`() {
        assertEquals(3, GameConfig.INITIAL_PLAYER_POWER)
        assertEquals(4, GameConfig.INITIAL_OPPONENT_POWER)
        assertEquals(2, GameConfig.CORRECT_ANSWER_POWER_BONUS)
        assertEquals(1, GameConfig.POWER_ATTACK_COST)
        assertEquals(15, GameConfig.QUESTION_TIME_SECONDS)
        assertEquals(25, GameConfig.BATTLE_WIN_COINS)
        assertEquals(1, GameConfig.BATTLE_WIN_TROPHIES)
    }

    @Test
    fun `lifeline costs are positive`() {
        assertTrue(GameConfig.FIFTY_FIFTY_COST > 0)
        assertTrue(GameConfig.SKIP_COST > 0)
        assertTrue(GameConfig.FIFTY_FIFTY_COST > GameConfig.SKIP_COST)
    }

    @Test
    fun `lucky wheel config is valid`() {
        assertTrue(GameConfig.LUCKY_WHEEL_SPIN_COST > 0)
        assertTrue(GameConfig.MAX_DAILY_SPINS > 0)
        assertTrue(GameConfig.MAX_DAILY_SPINS <= 10)
    }

    @Test
    fun `wheel segments probabilities sum to 1`() {
        val totalProbability = GameConfig.WHEEL_SEGMENTS.sumOf { it.probability.toDouble() }
        assertEquals(1.0, totalProbability, 0.01)
    }

    @Test
    fun `wheel segments have valid rewards`() {
        GameConfig.WHEEL_SEGMENTS.forEach { segment ->
            assertTrue(segment.reward.amount > 0)
            assertTrue(
                segment.reward.type == RewardType.COINS || 
                segment.reward.type == RewardType.TROPHIES ||
                segment.reward.type == RewardType.XP
            )
        }
    }

    @Test
    fun `daily login has 7 rewards`() {
        assertEquals(7, GameConfig.DAILY_LOGIN_REWARDS.size)
        assertEquals(7, GameConfig.DAILY_LOGIN_DAYS)
    }

    @Test
    fun `daily login rewards are positive`() {
        GameConfig.DAILY_LOGIN_REWARDS.forEach { reward ->
            assertTrue(reward.amount > 0)
        }
    }

    @Test
    fun `shop items have positive prices`() {
        GameConfig.SHOP_ITEMS.forEach { item ->
            assertTrue(item.price > 0)
        }
    }

    @Test
    fun `map has 10 levels`() {
        val levels = GameConfig.getMapLevels()
        assertEquals(10, levels.size)
    }

    @Test
    fun `map levels have increasing trophy requirements`() {
        val levels = GameConfig.getMapLevels()
        for (i in 1 until levels.size) {
            assertTrue(levels[i].requiredTrophies >= levels[i - 1].requiredTrophies)
        }
    }

    @Test
    fun `opponent names list is not empty`() {
        assertTrue(GameConfig.OPPONENT_NAMES.isNotEmpty())
        assertTrue(GameConfig.OPPONENT_NAMES.size >= 10)
    }

    @Test
    fun `trophies per level matches Player constant`() {
        assertEquals(10, GameConfig.TROPHIES_PER_LEVEL)
        assertEquals(10, com.animalbattle.game.domain.model.Player.XP_PER_LEVEL)
    }
}
