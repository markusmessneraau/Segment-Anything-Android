package com.example.sam.ui.screens.boulderList

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sam.network.dto.BoulderListDto
import com.example.sam.ui.AppTopBar
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun BoulderListScreen(
    navController: NavController,
    viewModel: BoulderListViewModel
) {

    val boulders by viewModel.boulders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                )
            )
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        AppTopBar()

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF374151))
            }
        } else if (boulders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Noch keine Boulder vorhanden.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(boulders) { boulder ->
                    BoulderCard(boulder = boulder)
                }
            }
        }
    }
}

@Composable
fun BoulderCard(boulder: BoulderListDto) {
    val appTurquoise = Color(0xFF374151)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                // TODO ViewBoulderscreen anzeigen mit angeklicktem Boulder
                println("User hat auf Route ${boulder.name} (ID: ${boulder.id}) geklickt!")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = boulder.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(appTurquoise, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = boulder.grade,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}