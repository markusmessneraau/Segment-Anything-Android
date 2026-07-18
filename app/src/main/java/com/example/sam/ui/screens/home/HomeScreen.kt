package com.example.sam.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    val selectedImageUri by homeViewModel.selectedImageUri.collectAsState()

    val baseBitmap by homeViewModel.baseBitmap.collectAsState()
    val holds by homeViewModel.holds.collectAsState()
    val activeHoldId by homeViewModel.activeHoldId.collectAsState()

    val isImageReady by homeViewModel.isImageReady.collectAsState()

    var isProcessing by remember { mutableStateOf(false) }
    var componentWidth by remember { mutableStateOf(1f) }
    var componentHeight by remember { mutableStateOf(1f) }

    // aktuellen Zoom merknen
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) } // Größe des Bildschirms

    val appTurquoise = Color(0xFF374151)

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isProcessing = true
            // wenn neues Bild geladen wird -> Zoom zurücksetzen
            scale = 1f
            offsetX = 0f
            offsetY = 0f
            homeViewModel.onImageSelected(context, uri)
        }
    }

    LaunchedEffect(isImageReady) {
        if (isImageReady) {
            isProcessing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8F9FA), Color(0xFFE2E8F0))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SAM 2 Erkennung",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = appTurquoise,
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )
        Text(
            text = "Tippe auf einen Griff zum Segmentieren",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Spacer(modifier = Modifier.weight(0.3f))
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp))
                .background(Color.White)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
                .onSizeChanged{ size = it} // speichert Box Größe

                // Zoom
                .pointerInput(isImageReady) {
                    if (isImageReady) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)

                            val maxOffset = (scale - 1) * size.width / 2
                            offsetX = (offsetX + pan.x * scale).coerceIn(-maxOffset, maxOffset)
                            offsetY = (offsetY + pan.y * scale).coerceIn(-maxOffset, maxOffset)
                        }
                    }
                }
                .pointerInput(isImageReady) {
                    if (isImageReady) {
                        detectTapGestures(
                            onTap = { tapOffset ->
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val originalX = centerX + (tapOffset.x - offsetX - centerX) / scale
                                val originalY = centerY + (tapOffset.y - offsetY - centerY) / scale
                                val normX = originalX / size.width.toFloat()
                                val normY = originalY / size.height.toFloat()

                                if (normX in 0f..1f && normY in 0f..1f) {
                                    homeViewModel.onTrackTapped(normX, normY)
                                }
                            },
                            // Fehler wegschneiden
                            onLongPress = { tapOffset ->
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val originalX = centerX + (tapOffset.x - offsetX - centerX) / scale
                                val originalY = centerY + (tapOffset.y - offsetY - centerY) / scale
                                val normX = originalX / size.width.toFloat()
                                val normY = originalY / size.height.toFloat()

                                if (normX in 0f..1f && normY in 0f..1f) {
                                    homeViewModel.onTrackLongPressed(normX, normY)
                                }
                            }
                        )

                    }
                }
            ,
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = appTurquoise, modifier = Modifier.size(50.dp))
            }
            else if (baseBitmap != null) {
               // Bilder werden übereinander gelegt
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                ) {
                    // unterstes Foto
                    Image(
                        bitmap = baseBitmap!!.asImageBitmap(),
                        contentDescription = "Spraywall",
                        modifier = Modifier.fillMaxSize()
                    )

                    // darüber alle Griffmasken
                    holds.forEach { hold ->
                        if (hold.maskBitmap != null) {

                            val isActive = hold.id == activeHoldId

                            val alphaValue = if (isActive) 1.0f else 0.4f

                            Image(
                                bitmap = hold.maskBitmap.asImageBitmap(),
                                contentDescription = "Maske",
                                modifier = Modifier.fillMaxSize(),
                                alpha = alphaValue
                            )
                        }
                    }
                }
            } else {
                Text(text = "Kein Bild ausgewählt", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { galleryLauncher.launch("image/*") },
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