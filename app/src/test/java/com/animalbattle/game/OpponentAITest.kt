package com.animalbattle.game

import com.animalbattle.game.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpponentAITest {

    private fun createTestBattle(
        opponentHp: Int = 100,
        opponentMaxHp: Int = 100,
        opponentXp: Int = 0,
        opponentPower: Int = 4
    ): BattleState {
        val animal = AnimalData.getAnimalById("lion")!!
        return BattleState(
            playerAnimal = animal,
            opponentAnimal = animal,
            playerHp = 100,
            playerMaxHp = 100,
            playerPower = 3,
            playerXp = 0,
            playerLevel = 1,
            opponentHp = opponentHp,
            opponentMaxHp = opponentMaxHp,
            opponentPower = opponentPower,
            opponentXp = opponentXp,
            opponentLevel = 1,
            isPlayerTurn = false,
            battlePhase = BattlePhase.OPPONENT_TURN
        )
    }

    @Test
    fun `opponent uses strongest ability when low HP and high XP`() {
        val battle = createTestBattle(opponentHp = 20, opponentMaxHp = 100, opponentXp = 3)
        val (strategy, abilityIndex) = OpponentAI.decide(battle)
        assertEquals(OpponentAI.Strategy.USE_ABILITY, strategy)
        assertEquals(2, abilityIndex)
    }

    @Test
    fun `opponent uses medium ability when low HP and medium XP`() {
        val battle = createTestBattle(opponentHp = 20, opponentMaxHp = 100, opponentXp = 2)
        val (strategy, abilityIndex) = OpponentAI.decide(battle)
        assertEquals(OpponentAI.Strategy.USE_ABILITY, strategy)
        assertEquals(1, abilityIndex)
    }

    @Test
    fun `opponent attacks when low HP and no XP`() {
        val battle = createTestBattle(opponentHp = 20, opponentMaxHp = 100, opponentXp = 0)
        val (strategy, _) = OpponentAI.decide(battle)
        assertEquals(OpponentAI.Strategy.ATTACK, strategy)
    }

    @Test
    fun `opponent uses strong ability when high HP and enough XP`() {
        val battle = createTestBattle(opponentHp = 80, opponentMaxHp = 100, opponentXp = 3, opponentPower = 3)
        val (strategy, abilityIndex) = OpponentAI.decide(battle)
        assertEquals(OpponentAI.Strategy.USE_ABILITY, strategy)
        assertEquals(2, abilityIndex)
    }

    @Test
    fun `opponent increases power when high HP and low power`() {
        val battle = createTestBattle(opponentHp = 80, opponentMaxHp = 100, opponentPower = 2)
        val (strategy, _) = OpponentAI.decide(battle)
        assertEquals(OpponentAI.Strategy.INCREASE_POWER, strategy)
    }

    @Test
    fun `opponent attacks when high HP and enough power`() {
        val battle = createTestBattle(opponentHp = 80, opponentMaxHp = 100, opponentPower = 5)
        val (strategy, _) = OpponentAI.decide(battle)
        assertEquals(OpponentAI.Strategy.ATTACK, strategy)
    }

    @Test
    fun `attack damage equals opponent power`() {
        val battle = createTestBattle(opponentPower = 5)
        val damage = OpponentAI.getAttackDamage(battle, OpponentAI.Strategy.ATTACK, null)
        assertEquals(5, damage)
    }

    @Test
    fun `ability damage equals ability damage value`() {
        val battle = createTestBattle()
        val damage = OpponentAI.getAttackDamage(battle, OpponentAI.Strategy.USE_ABILITY, 0)
        assertTrue(damage > 0)
    }

    @Test
    fun `increase power deals no damage`() {
        val battle = createTestBattle()
        val damage = OpponentAI.getAttackDamage(battle, OpponentAI.Strategy.INCREASE_POWER, null)
        assertEquals(0, damage)
    }
}
