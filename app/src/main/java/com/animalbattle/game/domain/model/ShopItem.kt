package com.animalbattle.game.domain.model

data class ShopItem(
    val id: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val price: Int,
    val category: ShopCategory,
    val iconEmoji: String = "📦",
    val isActive: Boolean = true
)

enum class ShopCategory {
    COIN_PACK,
    ANIMAL_UPGRADE,
    SKIN,
    ITEM
}

data class Chest(
    val id: String,
    val nameResId: Int,
    val reward: Reward,
    val isOpened: Boolean = false,
    val openTime: Long = 0L,
    val unlockDuration: Long = 0L
)
