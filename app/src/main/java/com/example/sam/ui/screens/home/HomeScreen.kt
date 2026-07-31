package com.example.sam.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.sam.ui.screens.home.components.BottomControls
import com.example.sam.ui.screens.home.components.InteractiveSpraywall
import com.example.sam.ui.screens.home.components.SaveRouteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    val baseBitmap by homeViewModel.baseBitmap.collectAsState()
    val holds by homeViewModel.holds.collectAsState()

    val isImageReady by homeViewModel.isImageReady.collectAsState()

    val isRouteFinished by homeViewModel.isRouteFinished.collectAsState()

    var isProcessing by remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }

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
            .statusBarsPadding()
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

        InteractiveSpraywall(
            homeViewModel = homeViewModel,
            isProcessing = isProcessing,
            scale = scale,
            onScaleChange = { scale = it },
            offsetX = offsetX,
            onOffsetChangeX = { offsetX = it },
            offsetY = offsetY,
            onOffsetChangeY = { offsetY = it },
            size = size,
            onSizeChange = { size = it }
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomControls(
            isRouteFinished = isRouteFinished,
            canUpload = baseBitmap != null && holds.isNotEmpty(),
            onToggleRoute = { homeViewModel.toggleRouteFinished(!isRouteFinished) },
            onShowSaveDialog = { showSaveDialog = true },
            onSelectPhoto = { galleryLauncher.launch("image/*") }
        )

        if (showSaveDialog) {
            SaveRouteDialog(
                onDismiss = { showSaveDialog = false },
                onSave = { name, grade ->
                    homeViewModel.uploadBoulder(
                       name,
                       grade,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Route erfolgreich gespeichert!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onError = {
                            Toast.makeText(
                                context,
                                "Fehler: Server nicht erreichbar!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                    showSaveDialog = false
                }
            )
        }

    }
}