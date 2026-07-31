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

val dummyBoulders = listOf(
    BoulderListDto("1", "Boulder 1", "6b"),
    BoulderListDto("2", "Boulder 2", "7a"),
    BoulderListDto("3", "Boulder 3", "4c"),
    BoulderListDto("4", "Boulder 4", "6c+"),
    BoulderListDto("5", "Boulder 5", "8a"),
    BoulderListDto("6", "Boulder 6", "5b")
)

@Composable
fun BoulderListScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                )
            )
            .padding(20.dp)
    ) {
        AppTopBar()

        //Todo: Boulder aus DB laden
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(dummyBoulders) { boulder ->
                BoulderCard(boulder = boulder)
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