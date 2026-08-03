package com.instasave.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.instasave.app.ui.downloads.DownloadsScreen
import com.instasave.app.ui.home.HomeScreen
import com.instasave.app.ui.placeholder.HistoryScreen
import com.instasave.app.ui.placeholder.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Home.route,
        modifier = modifier
    ) {
        composable(ScreenRoute.Home.route) {
            HomeScreen()
        }
        composable(ScreenRoute.Downloads.route) {
            DownloadsScreen()
        }
        composable(ScreenRoute.History.route) {
            HistoryScreen()
        }
        composable(ScreenRoute.Settings.route) {
            SettingsScreen()
        }
    }
}
