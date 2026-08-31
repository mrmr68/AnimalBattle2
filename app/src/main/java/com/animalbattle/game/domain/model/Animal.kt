package com.animalbattle.game.domain.model

data class Animal(
    val id: String,
    val nameResId: Int,
    val baseHp: Int,
    val basePower: Int,
    val abilities: List<Ability>,
    val unlockCost: Int = 0,
    val isUnlockedByDefault: Boolean = false,
    val upgradeCost: Int = 100
)

data class Ability(
    val id: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val damage: Int,
    val xpCost: Int,
    val levelRequired: Int = 1
)
