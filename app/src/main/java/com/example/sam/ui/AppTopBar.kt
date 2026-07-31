package com.example.sam.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar() {
    val appTurquoise = Color(0xFF374151)

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Alle Boulder",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = appTurquoise
            )
        },
        actions = {
            IconButton(onClick = { println("Filter geklickt!") }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtern",
                    tint = appTurquoise
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
