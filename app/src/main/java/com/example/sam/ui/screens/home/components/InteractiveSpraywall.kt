package com.example.sam.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.sam.ui.screens.home.HomeViewModel

@Composable
fun InteractiveSpraywall(
    homeViewModel: HomeViewModel,
    isProcessing: Boolean,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    offsetX: Float,
    onOffsetChangeX: (Float) -> Unit,
    offsetY: Float,
    onOffsetChangeY: (Float) -> Unit,
    size: IntSize,
    onSizeChange: (IntSize) -> Unit
) {
    // states holen aus ViewModel
    val baseBitmap by homeViewModel.baseBitmap.collectAsState()
    val holds by homeViewModel.holds.collectAsState()
    val activeHoldId by homeViewModel.activeHoldId.collectAsState()
    val isImageReady by homeViewModel.isImageReady.collectAsState()
    val isRouteFinished by homeViewModel.isRouteFinished.collectAsState()

    val appTurquoise = Color(0xFF374151)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp))
            .background(Color.White)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray.copy(alpha = 0.2f))
            .onSizeChanged { onSizeChange(it) } // Update nach oben
            .pointerInput(isImageReady, isRouteFinished) {
                if (isImageReady && !isRouteFinished) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                        onScaleChange(newScale)

                        val maxOffset = (newScale - 1) * size.width / 2
                        onOffsetChangeX((offsetX + pan.x * newScale).coerceIn(-maxOffset, maxOffset))
                        onOffsetChangeY((offsetY + pan.y * newScale).coerceIn(-maxOffset, maxOffset))
                    }
                }
            }
            .pointerInput(isImageReady, isRouteFinished) {
                if (isImageReady && !isRouteFinished) {
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
            },
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(color = appTurquoise, modifier = Modifier.size(50.dp))
        } else if (baseBitmap != null) {
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
                Image(
                    bitmap = baseBitmap!!.asImageBitmap(),
                    contentDescription = "Spraywall",
                    modifier = Modifier.fillMaxSize()
                )

                if (isRouteFinished) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                    )
                }

                val density = LocalDensity.current

                holds.forEach { hold ->
                    val data = hold.holdData
                    if (data != null) {
                        val isActive = hold.id == activeHoldId

                        val colorFilter = if (isRouteFinished) {
                            null
                        } else {
                            val tintColor = if (isActive) Color(0xCC00E6E6) else Color(0x6600E6E6)
                            androidx.compose.ui.graphics.ColorFilter.tint(
                                color = tintColor,
                                blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn
                            )
                        }

                        val decodedBitmap = remember(data) {
                            android.graphics.BitmapFactory.decodeByteArray(
                                data.imageBlob, 0, data.imageBlob.size
                            ).asImageBitmap()
                        }

                        val ratioX = size.width / 1024f
                        val ratioY = size.height / 1024f

                        Image(
                            bitmap = decodedBitmap,
                            contentDescription = "Maske",
                            colorFilter = colorFilter,
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            modifier = Modifier
                                .offset {
                                    androidx.compose.ui.unit.IntOffset(
                                        x = (data.xOffset * ratioX).toInt(),
                                        y = (data.yOffset * ratioY).toInt()
                                    )
                                }
                                .size(
                                    width = with(density) { (decodedBitmap.width * ratioX).toDp() },
                                    height = with(density) { (decodedBitmap.height * ratioY).toDp() }
                                )
                        )
                    }
                }
                if (activeHoldId != null && !isRouteFinished) {
                    IconButton(
                        onClick = { homeViewModel.deleteActiveHold() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Red.copy(alpha = 0.8f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Griff löschen",
                            tint = Color.White
                        )
                    }
                }
            }
        } else {
            Text(text = "Kein Bild ausgewählt", color = Color.Gray)
        }
    }
}
