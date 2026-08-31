package com.animalbattle.game.domain.model

object GameConfig {
    // Battle Configuration
    const val INITIAL_PLAYER_POWER = 3
    const val INITIAL_OPPONENT_POWER = 4
    const val CORRECT_ANSWER_POWER_BONUS = 2
    const val POWER_ATTACK_COST = 1
    const val QUESTION_TIME_SECONDS = 15
    const val BATTLE_WIN_COINS = 25
    const val BATTLE_WIN_TROPHIES = 1

    // Level Configuration
    const val TROPHIES_PER_LEVEL = 10

    // Lifeline Configuration
    const val FIFTY_FIFTY_COST = 50
    const val SKIP_COST = 30

    // Lucky Wheel Configuration
    const val LUCKY_WHEEL_SPIN_COST = 100
    const val MAX_DAILY_SPINS = 3

    // Daily Login Configuration
    const val DAILY_LOGIN_DAYS = 7
    val DAILY_LOGIN_REWARDS = listOf(
        Reward("daily_1", RewardType.COINS, 50),
        Reward("daily_2", RewardType.COINS, 100),
        Reward("daily_3", RewardType.TROPHIES, 2),
        Reward("daily_4", RewardType.COINS, 200),
        Reward("daily_5", RewardType.XP, 5),
        Reward("daily_6", RewardType.COINS, 300),
        Reward("daily_7", RewardType.TROPHIES, 5)
    )

    // Shop Configuration
    val SHOP_ITEMS = listOf(
        ShopItem("coin_pack_small", R.string.coin_packs, R.string.coin_packs, 50, ShopCategory.COIN_PACK, R.drawable.ic_coin),
        ShopItem("coin_pack_medium", R.string.coin_packs, R.string.coin_packs, 200, ShopCategory.COIN_PACK, R.drawable.ic_coin),
        ShopItem("coin_pack_large", R.string.coin_packs, R.string.coin_packs, 500, ShopCategory.COIN_PACK, R.drawable.ic_coin),
        ShopItem("skin_gold", R.string.skins, R.string.skins, 300, ShopCategory.SKIN, R.drawable.ic_trophy),
        ShopItem("item_lucky_charm", R.string.items, R.string.items, 150, ShopCategory.ITEM, R.drawable.ic_trophy)
    )

    // Lucky Wheel Configuration
    val WHEEL_SEGMENTS = listOf(
        WheelSegment("s1", "100 Coins", Reward("r1", RewardType.COINS, 100), 0xFFFFD700, 0.2f),
        WheelSegment("s2", "200 Coins", Reward("r2", RewardType.COINS, 200), 0xFFFF6B35, 0.15f),
        WheelSegment("s3", "1 Trophy", Reward("r3", RewardType.TROPHIES, 1), 0xFF4CAF50, 0.15f),
        WheelSegment("s4", "50 Coins", Reward("r4", RewardType.COINS, 50), 0xFF2196F3, 0.2f),
        WheelSegment("s5", "500 Coins", Reward("r5", RewardType.COINS, 500), 0xFF9C27B0, 0.05f),
        WheelSegment("s6", "3 Trophies", Reward("r6", RewardType.TROPHIES, 3), 0xFFFF5722, 0.1f),
        WheelSegment("s7", "25 Coins", Reward("r7", RewardType.COINS, 25), 0xFF00BCD4, 0.1f),
        WheelSegment("s8", "2 Trophies", Reward("r8", RewardType.TROPHIES, 2), 0xFFE91E63, 0.05f)
    )

    // Map Configuration
    fun getMapLevels(): List<MapLevel> = listOf(
        MapLevel(1, R.string.level_node, LevelStatus.COMPLETED, 0, Reward("map_1", RewardType.COINS, 50)),
        MapLevel(2, R.string.level_node, LevelStatus.COMPLETED, 5, Reward("map_2", RewardType.COINS, 75)),
        MapLevel(3, R.string.level_node, LevelStatus.AVAILABLE, 10, Reward("map_3", RewardType.COINS, 100)),
        MapLevel(4, R.string.level_node, LevelStatus.LOCKED, 20, Reward("map_4", RewardType.TROPHIES, 2)),
        MapLevel(5, R.string.level_node, LevelStatus.LOCKED, 35, Reward("map_5", RewardType.COINS, 200)),
        MapLevel(6, R.string.level_node, LevelStatus.LOCKED, 50, Reward("map_6", RewardType.TROPHIES, 3)),
        MapLevel(7, R.string.level_node, LevelStatus.LOCKED, 70, Reward("map_7", RewardType.COINS, 300)),
        MapLevel(8, R.string.level_node, LevelStatus.LOCKED, 100, Reward("map_8", RewardType.TROPHIES, 5)),
        MapLevel(9, R.string.level_node, LevelStatus.LOCKED, 130, Reward("map_9", RewardType.COINS, 500)),
        MapLevel(10, R.string.level_node, LevelStatus.LOCKED, 160, Reward("map_10", RewardType.TROPHIES, 10))
    )

    // Opponent Names
    val OPPONENT_NAMES = listOf(
        "Shadow", "Blaze", "Storm", "Thunder",
        "Frost", "Phoenix", "Dragon", "Titan",
        "Viper", "Spike", "Razor", "Blade"
    )
}
