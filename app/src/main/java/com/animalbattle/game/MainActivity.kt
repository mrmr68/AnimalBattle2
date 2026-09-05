package com.animalbattle.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.animalbattle.game.navigation.NavGraph
import com.animalbattle.game.ui.theme.AnimalBattleTheme
import com.animalbattle.game.ui.theme.Cream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Swap the branded splash theme for the game theme once the
        // window is ready (keep the splash visible during onCreate).
        setTheme(R.style.Theme_AnimalBattle)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimalBattleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Cream
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
