package com.animalbattle.game.domain.model

import kotlin.random.Random

/**
 * Opponent AI with different strategies based on battle state.
 */
object OpponentAI {

    enum class Strategy {
        ATTACK,          // Direct attack
        USE_ABILITY,     // Use strongest available ability
        INCREASE_POWER,  // Answer question to gain power
        DEFENSIVE        // Low HP - play safe
    }

    /**
     * Decide opponent's next action based on current battle state.
     * Returns: Pair of strategy and ability index (if using ability).
     */
    fun decide(battle: BattleState): Pair<Strategy, Int?> {
        val opponent = battle.opponentAnimal
        val hpRatio = battle.opponentHp.toFloat() / battle.opponentMaxHp
        val xp = battle.opponentXp
        val power = battle.opponentPower

        // Low HP - defensive (still attack, but prioritize strongest ability)
        if (hpRatio < 0.25f) {
            return if (xp >= 3) {
                Pair(Strategy.USE_ABILITY, 2) // Use strongest ability
            } else if (xp >= 2) {
                Pair(Strategy.USE_ABILITY, 1) // Use medium ability
            } else {
                Pair(Strategy.ATTACK, null)
            }
        }

        // High HP - aggressive
        if (hpRatio > 0.6f) {
            // If has enough XP for strong ability, use it
            if (xp >= 3 && power >= 2) {
                return Pair(Strategy.USE_ABILITY, 2)
            }
            // If low power, increase it
            if (power < 3) {
                return Pair(Strategy.INCREASE_POWER, null)
            }
            // Otherwise attack
            return Pair(Strategy.ATTACK, null)
        }

        // Medium HP - balanced strategy
        val random = Random.nextFloat()

        return when {
            // 30% chance to use ability if available
            xp >= 2 && random < 0.3f -> {
                val abilityIndex = if (xp >= 3 && random < 0.15f) 2 else 1
                Pair(Strategy.USE_ABILITY, abilityIndex)
            }
            // 25% chance to increase power
            power < 4 && random < 0.55f -> {
                Pair(Strategy.INCREASE_POWER, null)
            }
            // 45% chance to attack
            else -> {
                Pair(Strategy.ATTACK, null)
            }
        }
    }

    /**
     * Get the damage for an opponent's attack.
     */
    fun getAttackDamage(battle: BattleState, strategy: Strategy, abilityIndex: Int?): Int {
        return when (strategy) {
            Strategy.ATTACK -> battle.opponentPower
            Strategy.USE_ABILITY -> {
                battle.opponentAnimal.abilities.getOrNull(abilityIndex ?: 0)?.damage ?: battle.opponentPower
            }
            Strategy.INCREASE_POWER -> 0 // No damage on power increase
            Strategy.DEFENSIVE -> battle.opponentPower
        }
    }
}
