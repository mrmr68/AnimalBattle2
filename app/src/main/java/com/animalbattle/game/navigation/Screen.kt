package com.animalbattle.game.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object OnlineBattle : Screen("online_battle")
    data object Animals : Screen("animals")
    data object Shop : Screen("shop")
    data object LuckyWheel : Screen("lucky_wheel")
    data object RecentBattles : Screen("recent_battles")
    data object Map : Screen("map")
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object Battle : Screen("battle")
    data object Leaderboard : Screen("leaderboard")
    data object Achievements : Screen("achievements")
    data object DailyChallenges : Screen("daily_challenges")
}
