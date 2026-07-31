package com.example.sam.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sam.navigation.Screen

@Composable
fun AppBottomBar(navController: NavController) {
    val appTurquoise = Color(0xFF374151)

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Neu") },
            label = { Text("Neue Route") },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = appTurquoise,
                selectedTextColor = appTurquoise,
                indicatorColor = appTurquoise.copy(alpha = 0.1f),
                unselectedIconColor = appTurquoise.copy(alpha = 0.5f),
                unselectedTextColor = appTurquoise.copy(alpha = 0.5f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Liste") },
            label = { Text("Routen") },
            selected = currentRoute == Screen.List.route,
            onClick = {
                navController.navigate("list") {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = appTurquoise,
                selectedTextColor = appTurquoise,
                indicatorColor = appTurquoise.copy(alpha = 0.1f),
                unselectedIconColor = appTurquoise.copy(alpha = 0.5f),
                unselectedTextColor = appTurquoise.copy(alpha = 0.5f)
            )
        )
    }
}