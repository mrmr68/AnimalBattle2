package com.animalbattle.game

import com.animalbattle.game.domain.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerTest {

    @Test
    fun `default player has correct initial values`() {
        val player = Player()
        assertEquals("Player", player.name)
        assertEquals(1, player.level)
        assertEquals(0, player.xp)
        assertEquals(100, player.coins)
        assertEquals(0, player.trophies)
        assertEquals("lion", player.selectedAnimalId)
        assertEquals(listOf("lion"), player.unlockedAnimals)
        assertTrue(player.animalUpgrades.isEmpty())
        assertTrue(player.recentBattles.isEmpty())
        assertEquals(0, player.dailyLoginStreak)
        assertEquals(1, player.currentMapLevel)
        assertTrue(player.completedLevels.isEmpty())
    }

    @Test
    fun `level calculation with 0 trophies is level 1`() {
        val player = Player(trophies = 0)
        assertEquals(1, player.calculateLevel())
    }

    @Test
    fun `level calculation with 9 trophies is level 1`() {
        val player = Player(trophies = 9)
        assertEquals(1, player.calculateLevel())
    }

    @Test
    fun `level calculation with 10 trophies is level 2`() {
        val player = Player(trophies = 10)
        assertEquals(2, player.calculateLevel())
    }

    @Test
    fun `level calculation with 25 trophies is level 3`() {
        val player = Player(trophies = 25)
        assertEquals(3, player.calculateLevel())
    }

    @Test
    fun `level calculation with 100 trophies is level 11`() {
        val player = Player(trophies = 100)
        assertEquals(11, player.calculateLevel())
    }

    @Test
    fun `xpForNextLevel at 0 trophies is 0`() {
        val player = Player(trophies = 0)
        assertEquals(0, player.xpForNextLevel())
    }

    @Test
    fun `xpForNextLevel at 5 trophies is 5`() {
        val player = Player(trophies = 5)
        assertEquals(5, player.xpForNextLevel())
    }

    @Test
    fun `xpForNextLevel at 10 trophies is 0 (new level)`() {
        val player = Player(trophies = 10)
        assertEquals(0, player.xpForNextLevel())
    }

    @Test
    fun `xpForNextLevel at 15 trophies is 5`() {
        val player = Player(trophies = 15)
        assertEquals(5, player.xpForNextLevel())
    }

    @Test
    fun `xpToNextLevel is always 10`() {
        val player = Player(trophies = 0)
        assertEquals(10, player.xpToNextLevel())
        
        val player2 = Player(trophies = 50)
        assertEquals(10, player2.xpToNextLevel())
    }

    @Test
    fun `player constants are correct`() {
        assertEquals(10, Player.XP_PER_LEVEL)
        assertEquals(100, Player.INITIAL_COINS)
        assertEquals(25, Player.BATTLE_WIN_COINS)
        assertEquals(1, Player.BATTLE_WIN_TROPHIES)
    }

    @Test
    fun `player copy preserves fields`() {
        val player = Player(name = "Test", trophies = 15, coins = 500)
        val copy = player.copy(name = "New")
        assertEquals("New", copy.name)
        assertEquals(15, copy.trophies)
        assertEquals(500, copy.coins)
    }
}
