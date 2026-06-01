package com.example.sam.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sam.ui.screens.home.HomeScreen
import com.example.sam.ui.screens.home.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
}

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(homeViewModel = homeViewModel)
        }
    }
}