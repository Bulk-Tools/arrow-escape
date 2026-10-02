package com.bulktools.arrowescape

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bulktools.arrowescape.ui.screens.DailyScreen
import com.bulktools.arrowescape.ui.screens.GameScreen
import com.bulktools.arrowescape.ui.screens.HomeScreen
import com.bulktools.arrowescape.ui.screens.HowToPlayScreen
import com.bulktools.arrowescape.ui.screens.LevelMapScreen
import com.bulktools.arrowescape.ui.screens.ModeSelectScreen
import com.bulktools.arrowescape.ui.screens.SettingsScreen
import com.bulktools.arrowescape.ui.screens.StatsScreen
import com.bulktools.arrowescape.ui.screens.WorldSelectScreen

object Routes {
    const val HOME = "home"
    const val MODES = "modes"
    const val WORLDS = "worlds"
    const val LEVEL_MAP = "levelmap/{world}"
    fun levelMap(world: Int) = "levelmap/$world"
    const val GAME = "game?mode={mode}&world={w}&level={l}"
    fun game(mode: String, world: Int, level: Int) = "game?mode=$mode&world=$world&level=$level"
    const val DAILY = "daily"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val HOWTO = "howto"
}

@Composable
fun GameApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.MODES) { ModeSelectScreen(navController) }
        composable(Routes.WORLDS) { WorldSelectScreen(navController) }
        composable(
            Routes.LEVEL_MAP,
            arguments = listOf(navArgument("world") { type = NavType.IntType })
        ) { entry ->
            LevelMapScreen(navController, entry.arguments!!.getInt("world"))
        }
        composable(
            Routes.GAME,
            arguments = listOf(
                navArgument("mode") { defaultValue = "classic" },
                navArgument("w") { type = NavType.IntType; defaultValue = 0 },
                navArgument("l") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            GameScreen(
                navController,
                entry.arguments!!.getString("mode") ?: "classic",
                entry.arguments!!.getInt("w"),
                entry.arguments!!.getInt("l")
            )
        }
        composable(Routes.DAILY) { DailyScreen(navController) }
        composable(Routes.STATS) { StatsScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(Routes.HOWTO) { HowToPlayScreen(navController) }
    }
}
