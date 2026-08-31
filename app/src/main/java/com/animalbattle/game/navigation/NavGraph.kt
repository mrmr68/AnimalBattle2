package com.animalbattle.game.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.animalbattle.game.ui.animals.AnimalsScreen
import com.animalbattle.game.ui.battle.BattleScreen
import com.animalbattle.game.ui.home.HomeScreen
import com.animalbattle.game.ui.luckywheel.LuckyWheelScreen
import com.animalbattle.game.ui.map.MapScreen
import com.animalbattle.game.ui.profile.ProfileScreen
import com.animalbattle.game.ui.recentbattles.RecentBattlesScreen
import com.animalbattle.game.ui.settings.SettingsScreen
import com.animalbattle.game.ui.shop.ShopScreen
import com.animalbattle.game.viewmodel.GameViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: GameViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToOnlineBattle = { navController.navigate(Screen.OnlineBattle.route) },
                onNavigateToAnimals = { navController.navigate(Screen.Animals.route) },
                onNavigateToShop = { navController.navigate(Screen.Shop.route) },
                onNavigateToLuckyWheel = { navController.navigate(Screen.LuckyWheel.route) },
                onNavigateToRecentBattles = { navController.navigate(Screen.RecentBattles.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.OnlineBattle.route) {
            BattleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Battle.route) {
            BattleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Animals.route) {
            AnimalsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Shop.route) {
            ShopScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LuckyWheel.route) {
            LuckyWheelScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RecentBattles.route) {
            RecentBattlesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartBattle = { navController.navigate(Screen.Battle.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Leaderboard.route) {
            com.animalbattle.game.ui.leaderboard.LeaderboardScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
