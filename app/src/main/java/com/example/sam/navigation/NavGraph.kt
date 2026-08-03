package com.example.sam.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sam.ui.AppBottomBar
import com.example.sam.ui.screens.boulderList.BoulderListScreen
import com.example.sam.ui.screens.boulderDetail.BoulderDetailScreen
import com.example.sam.ui.screens.boulderDetail.BoulderDetailViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sam.ui.screens.home.HomeScreen
import com.example.sam.ui.screens.home.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object List : Screen(route = "list")
    object BoulderDetail : Screen(route = "boulderDetail/{boulderId}") {
        fun createRoute(boulderId: String) = "boulderDetail/$boulderId"
    }
}

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel
) {
    Scaffold(
        bottomBar = { AppBottomBar(navController = navController) }
    ){ innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(homeViewModel = homeViewModel)
            }
            composable(route = Screen.List.route) {
                BoulderListScreen(
                    navController = navController,
                    viewModel = viewModel ()
                )
            }
            composable(
                route = Screen.BoulderDetail.route,
                arguments = listOf(navArgument("boulderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val boulderId = backStackEntry.arguments?.getString("boulderId") ?: ""
                BoulderDetailScreen(
                    boulderId = boulderId,
                    navController = navController,
                    viewModel = viewModel()
                )
            }
        }
    }
}