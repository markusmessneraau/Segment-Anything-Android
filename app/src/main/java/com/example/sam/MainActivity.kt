package com.example.sam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.sam.data.analyzer.SamLocalAnalyzer
import com.example.sam.data.repository.SamRepository
import com.example.sam.navigation.SetupNavGraph // Import für den NavGraph
import com.example.sam.ui.screens.home.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val samAnalyzer = SamLocalAnalyzer(applicationContext)
        val samRepository = SamRepository(samAnalyzer)
        val homeViewModel = HomeViewModel(samRepository)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()


                SetupNavGraph(
                    navController = navController,
                    homeViewModel = homeViewModel
                )
            }
        }
    }
}