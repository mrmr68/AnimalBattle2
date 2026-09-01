package com.animalbattle.game.domain.model

import kotlin.random.Random

/**
 * Random events that can occur during battle for variety.
 */
sealed class RandomEvent {
    data class CriticalHit(val damageMultiplier: Float) : RandomEvent()
    data class Dodge(val chance: Float) : RandomEvent()
    data class PowerSurge(val extraPower: Int) : RandomEvent()
    data class Healing(val healAmount: Int) : RandomEvent()
    data class CoinBonus(val bonusCoins: Int) : RandomEvent()
    data class XPBonus(val bonusXp: Int) : RandomEvent()
    object DoubleDamage : RandomEvent()
    object Stun : RandomEvent()
    object Nothing : RandomEvent()
}

/**
 * Manages random events during battle.
 */
object RandomEventManager {

    // Base chance for each event (out of 100)
    private const val CRITICAL_HIT_CHANCE = 10
    private const val DODGE_CHANCE = 5
    private const val POWER_SURGE_CHANCE = 8
    private const val HEALING_CHANCE = 7
    private const val COIN_BONUS_CHANCE = 6
    private const val XP_BONUS_CHANCE = 5
    private const val DOUBLE_DAMAGE_CHANCE = 3
    private const val STUN_CHANCE = 4

    /**
     * Check if a random event occurs.
     */
    fun checkEvent(battlePhase: BattlePhase): RandomEvent {
        // Random events only occur during attacks
        if (battlePhase != BattlePhase.ATTACKING && battlePhase != BattlePhase.USING_ABILITY) {
            return RandomEvent.Nothing
        }

        val roll = Random.nextInt(100)

        return when {
            roll < CRITICAL_HIT_CHANCE -> {
                val multiplier = when {
                    Random.nextFloat() < 0.3f -> 2.0f  // 30% chance for 2x
                    else -> 1.5f  // 70% chance for 1.5x
                }
                RandomEvent.CriticalHit(multiplier)
            }
            roll < CRITICAL_HIT_CHANCE + DODGE_CHANCE -> {
                RandomEvent.Dodge(0.5f)
            }
            roll < CRITICAL_HIT_CHANCE + DODGE_CHANCE + POWER_SURGE_CHANCE -> {
                RandomEvent.PowerSurge(Random.nextInt(2, 4))
            }
            roll < CRITICAL_HIT_CHANCE + DODGE_CHANCE + POWER_SURGE_CHANCE + HEALING_CHANCE -> {
                RandomEvent.Healing(Random.nextInt(10, 25))
            }
            roll < CRITICAL_HIT_CHANCE + DODGE_CHANCE + POWER_SURGE_CHANCE + HEALING_CHANCE + COIN_BONUS_CHANCE -> {
                RandomEvent.CoinBonus(Random.nextInt(10, 30))
            }
            roll < CRITICAL_HIT_CHANCE + DODGE_CHANCE + POWER_SURGE_CHANCE + HEALING_CHANCE + COIN_BONUS_CHANCE + XP_BONUS_CHANCE -> {
                RandomEvent.XPBonus(Random.nextInt(1, 3))
            }
            roll < CRITICAL_HIT_CHANCE + DODGE_CHANCE + POWER_SURGE_CHANCE + HEALING_CHANCE + COIN_BONUS_CHANCE + XP_BONUS_CHANCE + DOUBLE_DAMAGE_CHANCE -> {
                RandomEvent.DoubleDamage
            }
            roll < CRITICAL_HIT_CHANCE + DODGE_CHANCE + POWER_SURGE_CHANCE + HEALING_CHANCE + COIN_BONUS_CHANCE + XP_BONUS_CHANCE + DOUBLE_DAMAGE_CHANCE + STUN_CHANCE -> {
                RandomEvent.Stun
            }
            else -> RandomEvent.Nothing
        }
    }

    /**
     * Get event description for display.
     */
    fun getEventDescription(event: RandomEvent): String {
        return when (event) {
            is RandomEvent.CriticalHit -> "Critical Hit! ${event.damageMultiplier}x damage!"
            is RandomEvent.Dodge -> "Dodged the attack!"
            is RandomEvent.PowerSurge -> "Power Surge! +${event.extraPower} Power!"
            is RandomEvent.Healing -> "Healing! +${event.healAmount} HP!"
            is RandomEvent.CoinBonus -> "Coin Bonus! +${event.bonusCoins} Coins!"
            is RandomEvent.XPBonus -> "XP Bonus! +${event.bonusXp} XP!"
            is RandomEvent.DoubleDamage -> "Double Damage!"
            is RandomEvent.Stun -> "Stunned! Opponent skips turn!"
            is RandomEvent.Nothing -> ""
        }
    }
}
