package com.example.sam.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomControls(
    isRouteFinished: Boolean,
    canUpload: Boolean,
    onToggleRoute: () -> Unit,
    onShowSaveDialog: () -> Unit,
    onSelectPhoto: () -> Unit
) {
    val appTurquoise = Color(0xFF374151)

    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onToggleRoute,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRouteFinished) Color(0xFF4B5563) else appTurquoise
            )
        ) {
            Text(
                text = if (isRouteFinished) "Route bearbeiten" else "Route fertigstellen",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isRouteFinished && canUpload) {
            Button(
                onClick = onShowSaveDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text(
                    text = "Route hochladen",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onSelectPhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = appTurquoise)
        ) {
            Text(text = "Foto auswählen", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}