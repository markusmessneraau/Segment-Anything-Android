package com.example.sam.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    val selectedImageUri by homeViewModel.selectedImageUri.collectAsState()
    val resultBitmap by homeViewModel.resultBitmap.collectAsState()
    val isImageReady by homeViewModel.isImageReady.collectAsState()

    var isProcessing by remember { mutableStateOf(false) }
    var componentWidth by remember { mutableStateOf(1f) }
    var componentHeight by remember { mutableStateOf(1f) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isProcessing = true
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "SAM 2: Tippe auf einen Klettergriff!", modifier = Modifier.padding(bottom = 16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // Speichert die echte Größe der Display-Box ab
                    componentWidth = coordinates.size.width.toFloat()
                    componentHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(isImageReady) {
                    if (isImageReady) {
                        detectTapGestures { offset ->
                            // Berechnet relative Klickposition (0.0 bis 1.0)
                            val normX = offset.x / componentWidth
                            val normY = offset.y / componentHeight
                            homeViewModel.onTrackTapped(normX, normY)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (resultBitmap != null) {
                Image(
                    bitmap = resultBitmap!!.asImageBitmap(),
                    contentDescription = "Kletterwand",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = "Wähle ein Bild aus, um zu starten.")
            }

            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { galleryLauncher.launch("image/*") }) {
            Text(text = "Bild auswählen")
        }
    }
}